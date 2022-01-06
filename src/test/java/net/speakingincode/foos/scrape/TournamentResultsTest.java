package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TournamentResultsTest {
  @Test
  public void deserializes() throws IOException {
    try (InputStream results = getClass().getResourceAsStream("/tournament-results.json")) {
      String json = CharStreams.toString(new InputStreamReader(results, Charsets.UTF_8));
      TournamentResults out = GsonUtil.gson().fromJson(json, TournamentResults.class);
      TournamentResults expected = TournamentResults.builder()
          .tournamentId("63735")
          .tournamentName("Monster DYP")
          .events(ImmutableList.of(event1(), event2()))
          .build();
      assertThat(out, equalTo(expected));
    }
  }

  @Test
  public void serializes() throws IOException {
    try (InputStream results = getClass().getResourceAsStream("/tournament-results.json")) {
      String expected = CharStreams.toString(new InputStreamReader(results, Charsets.UTF_8));
      TournamentResults actual = TournamentResults.builder()
          .tournamentId("63735")
          .tournamentName("Monster DYP")
          .events(ImmutableList.of(event1(), event2()))
          .build();
      String actualStr = GsonUtil.gson().toJson(actual);
      assertThat(actualStr.trim(), equalTo(expected.trim()));
    }
  }

  private TournamentResults.EventResults event1() {
    return TournamentResults.EventResults.builder()
        .tournamentName("Monster DYP")
        .date("2021-12-21")
        .eventName("Tournament Match 1")
        .chartUrl("http://www.netfoos.com/display/119/charts/1/63736.html")
        .finishesInternal(
            ImmutableList.of(TournamentResults.Finish.builder()
                    .finish(1)
                    .playerOne("Greg Mendel")
                    .playerTwo("Andrey Trok")
                    .build(),
                TournamentResults.Finish.builder()
                    .finish(2).playerOne("Brijesh Patel")
                    .playerTwo("Gemma Mio").build()
            ))
        .build();
  }

  private TournamentResults.EventResults event2() {
    return TournamentResults.EventResults.builder()
        .tournamentName("Monster DYP")
        .date("2021-12-21")
        .eventName("Tournament Match 2")
        .chartUrl("http://www.netfoos.com/display/119/charts/1/63737.html")
        .finishesInternal(ImmutableList.of(TournamentResults.Finish.builder()
                .finish(1)
                .playerOne("Brian Eaton")
                .playerTwo("Alex Hong")
                .build(),
            TournamentResults.Finish.builder()
                .finish(2)
                .playerOne("Bing Yep")
                .playerTwo("Mate Kovacs")
                .build()))
        .build();
  }
}
