package net.speakingincode.foos.scrape;

import com.google.common.base.Joiner;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.InputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KToolFileTest {

    @Test
    public void readsFile() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/example.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.json")) {
            KToolFileConfig config = KToolFileConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = KToolFile.load(config);
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
            KToolFileConfig config = KToolFileConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = KToolFile.load(config);
            assertThat(result.finishes(), Matchers.contains(
                finish(2, "Lih Chen", "Sean"),
                finish(3, "Bharath", "Jorge")));
        }
    }
}
