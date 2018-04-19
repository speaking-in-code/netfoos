package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KToolResultsTest {
    private static final ZoneId LA_TIMEZONE = ZoneId.of("America/Los_Angeles");
    private static final ZoneId ABIDJAN_TIMEZONE = ZoneId.of("Africa/Abidjan");

    @Test
    public void dateParseWorks() throws Exception {
        assertThat(KToolResults.getLocalDate("2018-04-11T03:07:59.255Z", LA_TIMEZONE),
            equalTo(LocalDate.of(2018, 4, 10)));
        assertThat(KToolResults.getLocalDate("2018-04-11T03:07:59.255Z", ABIDJAN_TIMEZONE),
            equalTo(LocalDate.of(2018, 4, 11)));
        assertThat(KToolResults.getLocalDate("2018-04-10T12:07:59.255Z", LA_TIMEZONE),
            equalTo(LocalDate.of(2018, 4, 10)));
        assertThat(KToolResults.getLocalDate("2016-05-11T12:07:59.255Z", LA_TIMEZONE),
            equalTo(LocalDate.of(2016, 5, 11)));
    }

    @Test
    public void parsesRealResults() throws Exception {
        try (InputStream testInput = getClass().getResourceAsStream("/example.ktool")) {
             String json = CharStreams.toString(new InputStreamReader(testInput, Charsets.UTF_8));
             KToolResults result = KToolResults.fromJson(json);
             assertThat(result.players().get(0).name(), equalTo("Vera"));
             assertThat(result.ko().third().plays().get(0).team1().id(),
                equalTo("c7bbd364-8db4-4238-fdc5-37935966493a"));
        }
    }
}
