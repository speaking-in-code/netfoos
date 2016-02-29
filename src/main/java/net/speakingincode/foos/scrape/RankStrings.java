package net.speakingincode.foos.scrape;

public class RankStrings {
  public static String toStringRank(int rank) {
    ++rank; // human readable, start index at 1.
    StringBuilder rankStr = new StringBuilder();
    rankStr.append(rank);
    if (rank >= 11 && rank <= 13) {
      // Special case for the teens.
      rankStr.append("th");
    } else {
      switch (rank % 10) {
      case 1:
        rankStr.append("st");
        break;
      case 2:
        rankStr.append("nd");
        break;
      case 3:
        rankStr.append("rd");
        break;
      default:
        rankStr.append("th");
        break;
      }
    }
    return rankStr.toString();
  }
}
