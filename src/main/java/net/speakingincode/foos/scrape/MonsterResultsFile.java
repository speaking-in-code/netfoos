package net.speakingincode.foos.scrape;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonsterResultsFile {
    private static final Logger log = Logger.getLogger(MonsterResultsFile.class.getName());
    private static final Pattern infoPattern = Pattern.compile("(.*): (.*)");
    @VisibleForTesting
    static final Pattern resultPattern = Pattern.compile(
        "(?<w1>[^&]+)(?: & (?<w2>.*))? (?<result>defeat|tie) (?<l1>[^&;]+)(?: & (?<l2>[^;]*))?(?:; k=(?<kValue>\\d+))?");

    public static MonsterResult load(File file) throws IOException {
        List<String> lines = Files.readLines(file, Charsets.UTF_8);
        return load(lines);
    }

    public static MonsterResult load(List<String> lines) throws IOException {
        if (lines.size() < 6) {
            throw new IOException("Input file too short:\n" + Joiner.on('\n').join(lines));
        }
        Tournament.Builder tb = Tournament.builder();
        int lineNum = 0;
        for (String line : lines) {
            ++lineNum;
            line = line.trim();
            if (line.isEmpty()) {
                break;
            }
            parseLine(tb, line);
        }
        Tournament tournament = tb.build();
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
                kValue = tournament.defaultKValue();
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
        result.tournament(tournament);
        result.players(players.build());
        result.matches(matches.build());
        return result.build();
    }

    private interface FieldSetter {
       void setField(Tournament.Builder builder, String value);
    }

    private static Map<String, FieldSetter> fieldSetters;
    static {
        ImmutableMap.Builder<String, FieldSetter> b = ImmutableMap.builder();
        b.put("Name", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setName(v);
            }
        });
        b.put("Description", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setDescription(v);
            }
        });
        b.put("Date", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setDate(LocalDate.parse(v));
            }
        });
        b.put("Location", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setLocation(v);
            }
        });
        b.put("Address", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setAddress(v);
            }
        });
        b.put("City", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setCity(v);
            }
        });
        b.put("State", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setState(v);
            }
        });
        b.put("Zip", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setZip(v);
            }
        });
        b.put("KValue", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setDefaultKValue(v);
            }
        });
        b.put("Output Format", new FieldSetter() {
            public void setField(Tournament.Builder b, String v) {
                b.setOutputFormat(Tournament.OutputFormatType.valueOf(v.toUpperCase()));
            }
        });
        fieldSetters = b.build();
    }

    private static void parseLine(Tournament.Builder builder, String line) throws IOException {
        Matcher m = infoPattern.matcher(line);
        if (!m.matches()) {
            throw new IOException("Expected pattern '" + infoPattern.pattern() + "', got " +
                line);
        }
        String key = m.group(1);
        String value = m.group(2);
        FieldSetter setter = fieldSetters.get(key);
        if (setter == null) {
            throw new IOException("Unknown field: " + key);
        }
        setter.setField(builder, value);
    }
}
