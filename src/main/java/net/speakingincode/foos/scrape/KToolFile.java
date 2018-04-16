package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
        MonsterResult location = MonsterResultsFile.load(CharStreams.readLines(new InputStreamReader(
            config.metadata(), Charsets.UTF_8)));
        KToolResults results = KToolResults.fromJson(
            CharStreams.toString(new InputStreamReader(config.ktool(), Charsets.UTF_8)));
        MonsterResult.Builder b = MonsterResult.builder();
        b.tournament(makeTournament(location, results));
        b.players(makePlayers(results));
        b.matches(makeMatches(results));
        return b.build();
    }

    private static ImmutableList<SingleMatchEvent> makeMatches(KToolResults results) {
        Map<String, String> idToPlayer = Maps.newHashMap();
        for (KToolPlayer player : results.players()) {
            idToPlayer.put(player.id(), player.name());
        }
        Map<String, KToolTeam> idToTeam = Maps.newHashMap();
        for (KToolTeam team : results.teams()) {
            idToTeam.put(team.id(), team);
        }
        ImmutableList.Builder<SingleMatchEvent> matches = ImmutableList.builder();
        SingleMatchEvent.Builder b = SingleMatchEvent.builder();
        for (KToolPlay match : results.plays()) {
            if (!match.valid()) {
                continue;
            }
            matches.add(makeMatch(match, idToPlayer, idToTeam));
        }
        for (KToolResults.Level level : results.ko().levels()) {
            for (KToolPlay match : level.plays()) {
                if (!match.valid()) {
                    continue;
                }
                matches.add(makeMatch(match, idToPlayer, idToTeam));
            }

        }
        return matches.build();
    }

    private static SingleMatchEvent makeMatch(KToolPlay match, Map<String, String> idToPlayer, Map<String, KToolTeam> idToTeam) {
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
        b.winnerPlayerOne(playerName(idToPlayer, winner.players().get(0)));
        if (winner.players().size() > 1) {
            b.winnerPlayerTwo(playerName(idToPlayer, winner.players().get(1)));
        }
        b.loserPlayerOne(playerName(idToPlayer, loser.players().get(0)));
        if (loser.players().size() > 1) {
            b.loserPlayerTwo(playerName(idToPlayer, loser.players().get(1)));
        }
        String kValue = (sets.size() == 1) ? ONE_GAME_K_VALUE : FULL_MATCH_K_VALUE;
        b.kValue(kValue);
        return b.build();
    }

    private static String playerName(Map<String, String> idToPlayer, KToolTeam.Player p) {
        return idToPlayer.get(p.id());
    }

    private static ImmutableSet<String> makePlayers(KToolResults results) {
        ImmutableSet.Builder<String> players = ImmutableSet.builder();
        for (KToolPlayer player : results.players()) {
            players.add(player.name());
        }
        return players.build();
    }

    private static Tournament makeTournament(MonsterResult location, KToolResults results) {
        Tournament.Builder tournament = location.tournament().toBuilder();
        tournament.setDate(results.createdDate());
        return tournament.build();
    }
}
