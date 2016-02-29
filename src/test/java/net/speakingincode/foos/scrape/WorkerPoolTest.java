package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.speakingincode.foos.scrape.Worker;
import net.speakingincode.foos.scrape.WorkerPool;

public class WorkerPoolTest {
  @Test
  public void doesWork() {
    WorkerPool<Integer, String> pool = WorkerPool.create(4, new Doubler.Factory());
    ImmutableMap<Integer, String> out =
        pool.parallelDo(ImmutableList.of(1, 2, 3, 4, 5));
    assertEquals(ImmutableMap.of(
        1, "2", 2, "4", 3, "6", 4, "8", 5, "10"), out);
  }
  
  @Test
  public void callsShutdown() {
    Doubler.Factory factory = new Doubler.Factory();
    assertEquals(0, factory.shutdownCount.get());
    WorkerPool<Integer, String> pool = WorkerPool.create(4, factory);
    pool.parallelDo(ImmutableList.of(1));
    assertEquals(4, factory.shutdownCount.get());
  }
  
  private static class Doubler implements Worker<Integer, String> {
    private static class Factory implements Worker.Factory<Integer, String> {
      AtomicInteger shutdownCount = new AtomicInteger(0);
      
      @Override
      public Worker<Integer, String> newWorker() {
        return new Doubler(shutdownCount);
      }
    }
    
    private final AtomicInteger shutdownCount;
    
    public Doubler(AtomicInteger shutdownCount) {
      this.shutdownCount = shutdownCount;
    }
    
    @Override
    public String convert(Integer x) {
      return "" + 2 * x;
    }

    @Override
    public void shutdown() {
      shutdownCount.incrementAndGet();
    }
  }
}
