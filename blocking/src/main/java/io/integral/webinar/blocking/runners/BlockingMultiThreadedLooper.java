package io.integral.webinar.blocking.runners;

import io.integral.webinar.blocking.types.BlockingMetrics;
import io.integral.webinar.blocking.types.BlockingTask;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.integral.webinar.blocking.runners.State.OFF;
import static io.integral.webinar.blocking.runners.State.ON;

@Slf4j
@Component
@Profile({"queue & real"})
public class BlockingMultiThreadedLooper implements Runner{

  private final Timer timer;
  @Getter
  @Setter
  private State state;

  private BlockingTask templateTask;

  private final ExecutorService executor;

  private final ArrayBlockingQueue<BlockingTask> queue;
  private final Integer threadCount;

  public BlockingMultiThreadedLooper(@Value("${THREADS:2}") Integer threadCount) {
    this.threadCount = threadCount;
    setState(ON);
    queue = new ArrayBlockingQueue<>(threadCount * 10);
    executor = Executors.newFixedThreadPool(threadCount);
    timer = BlockingMetrics.getTimer("threaded");
  }

  @Override
  public void run() {
    startConsumer(threadCount);
    log.info("starting to run " + templateTask.getName());
    while (state.isRunning()) {
      timer.wrap(() -> {
        try {
          queue.put(templateTask);
        } catch (InterruptedException e) {
          log.warn(e.getMessage(), e);
        }
      }).run();
    }
  }

  private void startConsumer(Integer threadCount) {
    for (int i = 0; i < threadCount; i++) {
      executor.execute(this::consumeTasks);
    }
  }

  private void consumeTasks() {
    while (state.isRunning()) {
      try {
        consumeTask(queue.take());
      } catch (InterruptedException e) {
        log.warn(e.getMessage(), e);
      }
    }
  }

  private void consumeTask(BlockingTask taskToRun) {
    log.info(taskToRun.getLogStatement());
    taskToRun.run();
  }

  public void setTask(BlockingTask task) {
    this.templateTask = task;
  }

  @Override
  public void close() throws Exception {
    setState(OFF);
    queue.clear();
    executor.shutdownNow();
  }
}
