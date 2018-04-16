package net.speakingincode.foos.scrape;

import com.google.common.base.Joiner;
import org.junit.Test;

import java.io.InputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KToolFileTest {

    @Test
    public void readsFile() throws Exception {
        try (InputStream ktool = getClass().getResourceAsStream("/example.ktool");
             InputStream metadata = getClass().getResourceAsStream("/location.txt")) {
            KToolFileConfig config = KToolFileConfig.builder()
                .ktool(ktool)
                .metadata(metadata)
                .build();
            MonsterResult result = KToolFile.load(config);
            assertThat(result.matches().size(), equalTo(27));
            // Single game, race to 7.
            assertThat(result.matches().get(0).kValue(), equalTo("16"));
            // Playoff game, best 2/3.
            assertThat(result.matches().get(26).kValue(), equalTo("32"));
        }
    }
}
