package net.speakingincode;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import net.speakingincode.Worker.Factory;

/**
 * Does work in parallel.
 */
public class WorkerPool<X, Y> {
  private final int threads;
  private final ExecutorService executor;
  private final Factory<X, Y> workerFactory;
  
  public static <X, Y>  WorkerPool<X, Y> create(int threads, Worker.Factory<X, Y> factory) {
    return new WorkerPool<X, Y>(threads, factory);
  }
  
  private WorkerPool(int threads, Worker.Factory<X, Y> workerFactory) {
    this.threads = threads;
    executor = Executors.newScheduledThreadPool(threads);
    this.workerFactory = workerFactory;
  }
  
  public ImmutableMap<X, Y> parallelDo(List<X> inputs) {
    Queue<X> workQueue = Queues.newConcurrentLinkedQueue();
    Map<X, Y> outputs = Maps.newConcurrentMap();
    workQueue.addAll(inputs);
    List<Future<Void>> pendingTasks = Lists.newArrayList();
    for (int i = 0; i < threads; ++i) {
      pendingTasks.add(executor.submit(new RunningWorker(workQueue, outputs)));
    }
    for (Future<Void> pendingTask : pendingTasks) {
      try {
        pendingTask.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    return ImmutableMap.copyOf(outputs);
  }
  
  private class RunningWorker implements Callable<Void> {
    private final Queue<X> workQueue;
    private final Map<X, Y> outputs;
    private final Worker<X, Y> worker;
    
    public RunningWorker(Queue<X> workQueue, Map<X, Y> outputs) {
      this.workQueue = workQueue;
      this.outputs = outputs;
      worker = workerFactory.newWorker();
    }
    
    public Void call() {
      for (X input = workQueue.poll(); input != null; input = workQueue.poll()) {
        Y output = worker.convert(input);
        outputs.put(input, output);
      }
      worker.shutdown();
      return null;
    }
  }
}
