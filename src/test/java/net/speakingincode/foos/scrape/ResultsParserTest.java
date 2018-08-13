package net.speakingincode.foos.scrape;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.InputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ResultsParserTest {

    @Test
    public void readsFile() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/example.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            ResultsParserConfig config = ResultsParserConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = ResultsParser.load(config);
            assertThat(result.matches().size(), equalTo(28));
            // Single game, race to 7.
            assertThat(result.matches().get(0).kValue(), equalTo("16"));
            // Playoff game, best 2/3.
            assertThat(result.matches().get(26).kValue(), equalTo("32"));
            assertThat(result.finishes(), Matchers.contains(
                finish(0, "Xavi", "Albert C"),
                finish(1, "Kevin", "Haoran"),
                finish(2, "Brian", "Natalie"),
                finish(3, "Andrey", "Jorge")));
        }
    }

    public static TournamentResults.Finish finish(int place, String p1, String p2) {
        return TournamentResults.Finish.builder().finish(place).playerOne(p1).playerTwo(p2).build();
    }

    @Test
    public void parsesKennedys() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/kennedys.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            ResultsParserConfig config = ResultsParserConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = ResultsParser.load(config);
            assertThat(result.finishes(), Matchers.contains(
                finish(2, "Lih Chen", "Sean"),
                finish(3, "Bharath", "Jorge")));
        }
    }

    @Test
    public void noPlayoffs() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/no-playoffs.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            ResultsParserConfig config = ResultsParserConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = ResultsParser.load(config);
            assertThat(result.players(), Matchers.containsInAnyOrder(
                "Ray", "Mo", "Phil", "Rod R", "Naveen", "Filiep", "Min",
                "Xavi", "Alex C", "Natalie", "Haoran", "Brian", "Vera"));
            assertThat(result.finishes(), Matchers.empty());
        }
    }

    @Test
    public void noPlayerNames() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/team-names-instead-of-player-names.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            ResultsParserConfig config = ResultsParserConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = ResultsParser.load(config);
            assertThat(result.players(), Matchers.containsInAnyOrder(
                "Ray", "Allan", "Brian", "Albert", "Kin", "Mo", "John", "Min", "David", "Alex",
                "Xavi", "Natalie", "Hao", "Bing", "Haoran", "Jorge"));
            assertThat(result.finishes(), Matchers.contains(
                finish(0, "Mo", "Kin"),
                finish(1, "Brian", "Albert"),
                finish(2, "Ray", "Allan"),
                finish(3, "John", "Min")
            ));
        }
    }

    @Test
    public void team2WinsPlayoffs() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/team2-wins.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            ResultsParserConfig config = ResultsParserConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = ResultsParser.load(config);
            assertThat(result.finishes(), Matchers.contains(
                finish(0, "Nick Furci", "Hamdi"),
                finish(1, "Kevin", "Morgan"),
                finish(2, "Lih", "Eric"),
                finish(3, "Maverick", "Mike")
            ));
        }
    }

    @Test
    public void byeInPlayoffs() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/playoff-bye.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            ResultsParserConfig config = ResultsParserConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = ResultsParser.load(config);
            assertThat(result.finishes(), Matchers.contains(
                finish(0, "Lih", "Eric"),
                finish(1, "Kevin", "Haoran"),
                finish(2, "Naveen", "Bharath"),
                finish(3, "Min", "John")
            ));
        }
    }
}
