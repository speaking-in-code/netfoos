package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class KToolFile {
    private static final String ONE_GAME_K_VALUE = "16";
    private static final String FULL_MATCH_K_VALUE = "32";

    public static MonsterResult load(KToolFileConfig config) throws IOException {
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
            for (KToolResults.Level level : results.ko().levels()) {
                addPlays(level.plays(), matches);
                ++i;
            }
            addPlays(results.ko().third().plays(), matches);
            return matches.build();
        }

        private ImmutableList<TournamentResults.Finish> makeFinishes() {
            List<TournamentResults.Finish> finishes = Lists.newArrayList();
            while (finishes.size() < 4) {
                finishes.add(null);
            }
            for (KToolResults.Level level : results.ko().levels()) {
                if (level.name().equals("1/1")) {
                    KToolPlay play = level.plays().get(0);
                    if (play.valid()) {
                        finishes.set(0, makeFinish(0, play.team1()));
                        finishes.set(1, makeFinish(1, play.team2()));
                    }
                }
            }
            KToolPlay third = results.ko().third().plays().get(0);
            if (third.valid()) {
                finishes.set(2, makeFinish(2, third.team1()));
                finishes.set(3, makeFinish(3, third.team2()));
            }
            ImmutableList.Builder<TournamentResults.Finish> noNulls = ImmutableList.builder();
            for (int i = 0; i < finishes.size(); ++i) {
                if (finishes.get(i) != null) {
                    noNulls.add(finishes.get(i));
                }
            }
            return noNulls.build();
        }

        private TournamentResults.Finish makeFinish(int place, KToolPlay.Team team) {
            TournamentResults.Finish.Builder finish = TournamentResults.Finish.builder();
            finish.finish(place);
            KToolTeam t = idToTeam.get(team.id());
            finish.playerOne(idToPlayer.get(t.players().get(0).id()));
            if (t.players().size() > 1) {
                finish.playerTwo(idToPlayer.get(t.players().get(1).id()));
            }
            return finish.build();
        }

        private void addPlays(List<KToolPlay> plays, ImmutableList.Builder<SingleMatchEvent> matches) {
            for (KToolPlay play : plays) {
                if (!play.valid()) {
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
            b.winnerPlayerOne(playerName(winner.players().get(0)));
            if (winner.players().size() > 1) {
                b.winnerPlayerTwo(playerName(winner.players().get(1)));
            }
            b.loserPlayerOne(playerName(loser.players().get(0)));
            if (loser.players().size() > 1) {
                b.loserPlayerTwo(playerName(loser.players().get(1)));
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
                players.add(player.name());
            }
            return players.build();
        }

        private Tournament makeTournament() {
            Tournament.Builder writable = tournament.toBuilder();
            writable.setDate(results.createdDate());
            return writable.build();
        }
    }
}
