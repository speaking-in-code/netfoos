package net.speakingincode;

public interface Worker<X, Y> {
  public static interface Factory<X, Y> {
    public Worker<X, Y> newWorker();
  }
  
  Y convert(X x);
  
  void shutdown();
}
