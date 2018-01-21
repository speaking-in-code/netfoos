package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;

public class MonsterResultsFileTest {
    private List<String> description() {
        return Lists.newArrayList(
        "Name: Tournament Name",
            "Description: Some Description",
            "Date: 2018-01-21",
            "Location: Dingy Pool Hall",
            "Address: 1001 Highway Blvd",
            "City: Smallville",
            "State: CA",
            "Zip: 94040",
            "KValue: 12",
            "Output Format: individual",
            "");
    }
    @Test
    public void readsTournament() throws IOException {
        MonsterResult result = MonsterResultsFile.load(description());
        assertThat(result.tournament().name(), is("Tournament Name"));
        assertThat(result.tournament().description(), is("Some Description"));
        assertThat(result.tournament().date(), is(LocalDate.of(2018, 1, 21)));
        assertThat(result.tournament().location(), is("Dingy Pool Hall"));
        assertThat(result.tournament().address(), is("1001 Highway Blvd"));
        assertThat(result.tournament().city(), is("Smallville"));
        assertThat(result.tournament().state(), is("CA"));
        assertThat(result.tournament().zip(), is("94040"));
        assertThat(result.players(), empty());
        assertThat(result.matches(), empty());
    }

    @Test
    public void readsDoubles() throws IOException {
        List<String> in = description();
        in.add("Alice & Bob defeat Claire & Doug");
        MonsterResult result = MonsterResultsFile.load(in);
        assertThat(result.players(), contains("Alice", "Bob", "Claire", "Doug"));
        assertThat(result.matches(), contains(SingleMatchEvent.builder()
            .winnerPlayerOne("Alice")
            .winnerPlayerTwo("Bob")
            .loserPlayerOne("Claire")
            .loserPlayerTwo("Doug")
            .kValue("12")
            .build()));
    }

    @Test
    public void overridesKValue() throws IOException {
        List<String> in = description();
        in.add("Alice & Bob defeat Claire & Doug; k=32");
        in.add("Alice & Bob defeat Ella & Fran");
        MonsterResult result = MonsterResultsFile.load(in);
        assertThat(result.players(), contains("Alice", "Bob", "Claire", "Doug", "Ella", "Fran"));
        assertThat(result.matches(), contains(
            SingleMatchEvent.builder()
                .winnerPlayerOne("Alice")
                .winnerPlayerTwo("Bob")
                .loserPlayerOne("Claire")
                .loserPlayerTwo("Doug")
                .kValue("32")
                .build(),
            SingleMatchEvent.builder()
                .winnerPlayerOne("Alice")
                .winnerPlayerTwo("Bob")
                .loserPlayerOne("Ella")
                .loserPlayerTwo("Fran")
                .kValue("12")
                .build()));
    }

    @Test
    public void parseDoublesLine() {
        Matcher m = MonsterResultsFile.resultPattern.matcher("Alice & Bob defeat Claire & Doug");
        assertThat(m.matches(), is(true));
        assertThat(m.group("w1"), is("Alice"));
        assertThat(m.group("w2"), is("Bob"));
        assertThat(m.group("l1"), is("Claire"));
        assertThat(m.group("l2"), is("Doug"));
        assertThat(m.group("kValue"), Matchers.nullValue());
    }

    @Test
    public void parseSinglesLine() {
        Matcher m = MonsterResultsFile.resultPattern.matcher("Alice defeat Bob");
        assertThat(m.matches(), is(true));
        assertThat(m.group("w1"), is("Alice"));
        assertThat(m.group("w2"), Matchers.nullValue());
        assertThat(m.group("l1"), is("Bob"));
        assertThat(m.group("l2"), Matchers.nullValue());
        assertThat(m.group("kValue"), Matchers.nullValue());
    }

    @Test
    public void readsSingles() throws IOException {
        List<String> in = description();
        in.add("Alice defeat Bob");
        MonsterResult result = MonsterResultsFile.load(in);
        assertThat(result.players(), contains("Alice", "Bob"));
        assertThat(result.matches(), contains(SingleMatchEvent.builder()
            .winnerPlayerOne("Alice")
            .winnerPlayerTwo(null)
            .loserPlayerOne("Bob")
            .loserPlayerTwo(null)
            .kValue("12")
            .build()));
    }
}
