package net.speakingincode.foos.scrape;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.google.common.collect.ImmutableList;

/**
 * Additional selectors for WebDriver.
 */
public class MoreBy {
  /**
   * Matches submit buttons with the specified value.
   */
  public static By submitValue(String value) {
    return new BySubmitValue(value);
  }
  
  private static class BySubmitValue extends By {
    private final String value;
    
    public BySubmitValue(String value) {
      this.value = value;
    }
    
    @Override
    public List<WebElement> findElements(SearchContext context) {
      List<WebElement> list = context.findElements(By.name("Submit"));
      ImmutableList.Builder<WebElement> out = ImmutableList.builder();
      for (WebElement item : list) {
        if (value.equals(item.getAttribute("value"))) {
          out.add(item);
        }
      }
      return out.build();
    }
  }
  
  /**
   * Matches links with the specified text and CGI arg.
   */
  public static By linkTextAndArg(String text, String arg) {
    return new ByLinkTextAndArg(text, arg);
  }
  
  private static class ByLinkTextAndArg extends By {
    private final String text;
    private final String arg;
    
    public ByLinkTextAndArg(String text, String arg) {
      this.text = text;
      this.arg = arg;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
      List<WebElement> list = context.findElements(By.linkText(text));
      ImmutableList.Builder<WebElement> out = ImmutableList.builder();
      for (WebElement item : list) {
        if (item.getAttribute("href").contains(arg)) {
          out.add(item);
        }
      }
      return out.build();
    }
  }
  
  /**
   * Matches links with the specified text and CGI arg.
   */
  public static By linkPlayerName(String name) {
    return new ByLinkPlayerName(name);
  }
  
  private static class ByLinkPlayerName extends By {
    private final String toFind;
    public ByLinkPlayerName(String toFind) {
      this.toFind = toFind;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
      List<WebElement> list = context.findElements(By.partialLinkText(toFind));
      ImmutableList.Builder<WebElement> out = ImmutableList.builder();
      for (WebElement item : list) {
        String name = item.getText();
        // Name format is like this for people with nicknames:
        //    Schlaefer, Phil "Dr. Doom"
        // or like this for people without:
        //    Eaton, Brian
        String noNickName = removeNickName(name);
        if (toFind.equals(noNickName)) {
          out.add(item);
        }
      }
      return out.build();
    }
  }

  @VisibleForTesting
  static String removeNickName(String name) {
    return name.split("\"")[0].trim();
  }
}
