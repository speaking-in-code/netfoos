package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableList;

class AmbiguousPlayerNameException extends Exception {
  private final ImmutableList<String> possibleNames;
  
  public AmbiguousPlayerNameException(ImmutableList<String> possibleNames) {
    this.possibleNames = possibleNames();
  }
  
  public ImmutableList<String> possibleNames() {
    return possibleNames;
  }
}
