package io.integral.webinar.blocking.runner.service;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TicketedExecutor<T extends Runnable> implements AutoCloseable {

  private final ScheduledExecutorService worker;
  private final ArrayBlockingQueue<T> taskQueue;
  private final ArrayBlockingQueue<UUID> tickets;

  public TicketedExecutor(ScheduledExecutorService worker, Integer capacity) {
    this.worker = worker;
    taskQueue = new ArrayBlockingQueue<>(capacity);
    tickets = new ArrayBlockingQueue<>(capacity);
  }

  public void put(T task) throws InterruptedException {
    log.debug("task added");
    tickets.put(UUID.randomUUID());
    taskQueue.put(task);
  }

  public T take() throws InterruptedException {
    log.debug("task accepted");
    return taskQueue.take();
  }


  public void clear() {
    tickets.clear();
    taskQueue.clear();
  }

  public void execute(Runnable currentTask) {
    worker.execute(() -> {
      currentTask.run();
      try {
        UUID ticket = tickets.take();
        log.debug("ticket received " + ticket);
      } catch (InterruptedException e) {
        log.warn(e.getMessage(), e);
      }
    });
  }

  public void schedule(Runnable runnable, long delay, TimeUnit unit) {
    worker.schedule(runnable, delay, unit);
  }

  @Override
  public void close() throws Exception {
    worker.shutdownNow();
    clear();
  }
}
