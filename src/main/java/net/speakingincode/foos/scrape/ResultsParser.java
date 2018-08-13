package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ResultsParser {
    private static final String ONE_GAME_K_VALUE = "16";
    private static final String FULL_MATCH_K_VALUE = "32";

    public static MonsterResult load(ResultsParserConfig config) throws IOException {
        if (config.matches() != null) {
            return MonsterResultsFile.load(config.matches());
        }
        Tournament tournament = Tournament.fromJson(
            CharStreams.toString(new InputStreamReader(config.metadata(), Charsets.UTF_8)));
        KToolResults results = KToolResults.fromJson(
            CharStreams.toString(new InputStreamReader(config.ktool(), Charsets.UTF_8)));
        Parser parser = new Parser(tournament, results);
        parser.parse();
        return parser.result();
    }

    private static class Parser {
        private final Tournament tournament;
        private final KToolResults results;
        private final MonsterResult.Builder builder = MonsterResult.builder();
        private final Map<String, String> idToPlayer;
        private final Map<String, KToolTeam> idToTeam;

        public Parser(Tournament tournament, KToolResults results) {
            this.tournament = tournament;
            this.results = results;
            idToPlayer = Maps.newHashMap();
            for (KToolPlayer player : results.players()) {
                idToPlayer.put(player.id(), player.name());
            }
            idToTeam = Maps.newHashMap();
            for (KToolTeam team : results.teams()) {
                idToTeam.put(team.id(), team);
            }
        }

        public void parse() {
            builder.tournament(makeTournament());
            builder.players(makePlayers());
            builder.matches(makeMatches());
            builder.finishes(makeFinishes());
        }

        public MonsterResult result() {
            return builder.build();
        }

        private ImmutableList<SingleMatchEvent> makeMatches() {
            ImmutableList.Builder<SingleMatchEvent> matches = ImmutableList.builder();
            SingleMatchEvent.Builder b = SingleMatchEvent.builder();
            addPlays(results.plays(), matches);
            int i = 0;
            if (results.ko() != null) {
                for (KToolResults.Level level : results.ko().levels()) {
                    addPlays(level.plays(), matches);
                    ++i;
                }
                addPlays(results.ko().third().plays(), matches);
            }
            return matches.build();
        }

        private ImmutableList<TournamentResults.Finish> makeFinishes() {
            List<TournamentResults.Finish> finishes = Lists.newArrayList();
            while (finishes.size() < 4) {
                finishes.add(null);
            }
            if (results.ko() != null) {
                    for (KToolResults.Level level : results.ko().levels()) {
                        if (level.name().equals("1/1")) {
                            KToolPlay play = level.plays().get(0);
                            if (play.matchWasPlayed()) {
                                SingleMatchEvent result = makeMatch(level.plays().get(0));
                                finishes.set(0, makeFinish(0, result.winnerPlayerOne(), result.winnerPlayerTwo()));
                                finishes.set(1, makeFinish(1, result.loserPlayerOne(), result.loserPlayerTwo()));
                            }
                        }
                    }
                KToolPlay third = results.ko().third().plays().get(0);
                if (third.valid()) {
                    SingleMatchEvent result = makeMatch(third);
                    finishes.set(2, makeFinish(2, result.winnerPlayerOne(), result.winnerPlayerTwo()));
                    finishes.set(3, makeFinish(3, result.loserPlayerOne(), result.loserPlayerTwo()));
                }
            }
            ImmutableList.Builder<TournamentResults.Finish> noNulls = ImmutableList.builder();
            for (int i = 0; i < finishes.size(); ++i) {
                if (finishes.get(i) != null) {
                    noNulls.add(finishes.get(i));
                }
            }
            return noNulls.build();
        }

        private TournamentResults.Finish makeFinish(int place, String playerOne, @Nullable String playerTwo) {
            TournamentResults.Finish.Builder finish = TournamentResults.Finish.builder();
            finish.finish(place);
            finish.playerOne(playerOne);
            finish.playerTwo(playerTwo);
            return finish.build();
        }

        private void addPlays(List<KToolPlay> plays, ImmutableList.Builder<SingleMatchEvent> matches) {
            for (KToolPlay play : plays) {
                if (!play.matchWasPlayed()) {
                    continue;
                }
                SingleMatchEvent match = makeMatch(play);
                matches.add(match);
            }
        }

        private SingleMatchEvent makeMatch(KToolPlay match) {
            SingleMatchEvent.Builder b = SingleMatchEvent.builder();
            KToolTeam team1 = idToTeam.get(match.team1().id());
            KToolTeam team2 = idToTeam.get(match.team2().id());
            List<KToolPlay.Set> sets = match.disciplines().get(0).sets();
            KToolTeam winner = null;
            KToolTeam loser = null;
            int team1win = 0;
            int team2win = 0;
            for (KToolPlay.Set set : sets) {
                if (set.team1() > set.team2()) {
                    ++team1win;
                } else if (set.team1() < set.team2()) {
                    ++team2win;
                }
            }
            if (team1win == team2win) {
                b.tie(true);
                winner = team1;
                loser = team2;
            } else if (team1win > team2win) {
                winner = team1;
                loser = team2;
            } else {
                winner = team2;
                loser = team1;
            }
            // If there is a players list, use that.
            // Otherwise fallback to assuming the team name is the player names
            // separated by slash, e.g. "Brian/Albert".
            if (!winner.players().isEmpty()) {
                b.winnerPlayerOne(playerName(winner.players().get(0)));
                if (winner.players().size() > 1) {
                    b.winnerPlayerTwo(playerName(winner.players().get(1)));
                }
                b.loserPlayerOne(playerName(loser.players().get(0)));
                if (loser.players().size() > 1) {
                    b.loserPlayerTwo(playerName(loser.players().get(1)));
                }
            } else {
                List<String> winners = winner.teamPlayerNames();
                List<String> losers = loser.teamPlayerNames();
                b.winnerPlayerOne(winners.get(0));
                if (winners.size() > 1) {
                    b.winnerPlayerTwo(winners.get(1));
                }
                b.loserPlayerOne(losers.get(0));
                if (losers.size() > 1) {
                    b.loserPlayerTwo(losers.get(1));
                }
            }
            String kValue = (sets.size() == 1) ? ONE_GAME_K_VALUE : FULL_MATCH_K_VALUE;
            b.kValue(kValue);
            return b.build();
        }

        private String playerName(KToolTeam.Player p) {
            return idToPlayer.get(p.id());
        }

        private ImmutableSet<String> makePlayers() {
            ImmutableSet.Builder<String> players = ImmutableSet.builder();
            for (KToolPlayer player : results.players()) {
                // Player name might be a team name. Team names are / separated
                // player names, e.g. "Albert/Brian".
                List<String> names = KToolTeam.teamToPlayers(player.name());
                players.addAll(names);
            }
            return players.build();
        }

        private Tournament makeTournament() {
            Tournament.Builder writable = tournament.toBuilder();
            // Horrible hack to deal with a scraping issue. If the event we're trying to
            // create isn't the first event in the tournament list page, it's quite difficult
            // to find it at all. One way to deal with that is to hardcode the event date so
            // that it's first in the page. /me shudders. There's got to be a better way.
            // writable.setDate(LocalDate.of(2018, 5, 6));
            writable.setDate(results.createdDate());
            return writable.build();
        }
    }
}
