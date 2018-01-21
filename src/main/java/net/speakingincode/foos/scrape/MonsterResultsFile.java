package net.speakingincode.foos.scrape;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonsterResultsFile {
    private static final Logger log = Logger.getLogger(MonsterResultsFile.class.getName());
    private static final Pattern infoPattern = Pattern.compile("(.*): (.*)");
    @VisibleForTesting
    static final Pattern resultPattern = Pattern.compile(
        "(?<w1>[^&]+)(?: & (?<w2>.*))? (?<result>defeat|tie) (?<l1>[^&]+)(?: & (?<l2>[^;]*))?(?:; k=(?<kValue>\\d+))?");

    public static MonsterResult load(List<String> lines) throws IOException {
        if (lines.size() < 6) {
            throw new IOException("Input file too short:\n" + Joiner.on('\n').join(lines));
        }
        Tournament.Builder tournament = Tournament.builder();
        int lineNum = 0;
        tournament.setName(parse("Name", lines.get(lineNum++)));
        tournament.setDescription(parse("Description", lines.get(lineNum++)));
        tournament.setDate(LocalDate.parse(parse("Date", lines.get(lineNum++))));
        tournament.setLocation(parse("Location", lines.get(lineNum++)));
        tournament.setAddress(parse("Address", lines.get(lineNum++)));
        tournament.setCity(parse("City", lines.get(lineNum++)));
        tournament.setState(parse("State", lines.get(lineNum++)));
        tournament.setZip(parse("Zip", lines.get(lineNum++)));
        String defaultKValue = parse("KValue", lines.get(lineNum++));
        int failCount = 0;
        ImmutableSet.Builder<String> players = ImmutableSet.builder();
        ImmutableList.Builder<SingleMatchEvent> matches = ImmutableList.builder();
        MonsterResult.Builder result = MonsterResult.builder();
        for (;lineNum < lines.size(); ++lineNum) {
            String line = lines.get(lineNum);
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            Matcher m = resultPattern.matcher(line);
            if (!m.matches()) {
                log.warning("Bad result format. Expected " + resultPattern.pattern() + ", got " +
                    line);
                ++failCount;
            }
            SingleMatchEvent.Builder match = SingleMatchEvent.builder();
            match.winnerPlayerOne(m.group("w1"));
            match.winnerPlayerTwo(m.group("w2"));
            match.loserPlayerOne(m.group("l1"));
            match.loserPlayerTwo(m.group("l2"));
            if ("tie".equals(m.group("result"))) {
                match.tie(true);
            }
            String kValue = m.group("kValue");
            if (kValue == null) {
                kValue = defaultKValue;
            }
            match.kValue(kValue.trim());
            for (String player : new String[] { "w1", "w2", "l1", "l2" }) {
                String name = m.group(player);
                if (name != null) {
                    players.add(name);
                }
            }
            matches.add(match.build());
        }
        if (failCount > 0) {
            throw new IOException("Failed to parse some result lines.");
        }
        result.tournament(tournament.build());
        result.players(players.build());
        result.matches(matches.build());
        return result.build();
    }

    private static String parse(String field, String line) throws IOException {
        Matcher m = infoPattern.matcher(line);
        if (!m.matches()) {
            throw new IOException("Expected pattern '" + infoPattern.pattern() + "', got " +
                line);
        }
        if (!m.group(1).equals(field)) {
            throw new IOException("Expected leading '" + field + "': got " + m.group(1));
        }
        return m.group(2);
    }
}
