package io.integral.webinar.blocking.runner.service;

import io.integral.webinar.blocking.BlockingTask;
import io.integral.webinar.blocking.Runner;
import io.integral.webinar.blocking.runner.RunnerState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.integral.webinar.blocking.runner.RunnerState.OFF;
import static io.integral.webinar.blocking.runner.RunnerState.ON;

@Slf4j
public class Consumer<T extends BlockingTask> implements Runner<T> {

  @Getter
  @Setter
  private RunnerState runnerState;
  public ArrayBlockingQueue<T> queue;
  private final TicketedExecutor<T> workerExecutor;
  private final ScheduledExecutorService controlExecutor;

  public Consumer(TicketedExecutor<T> workerExecutor) {
    this.workerExecutor = workerExecutor;
    this.controlExecutor = Executors.newSingleThreadScheduledExecutor();
    setRunnerState(ON);
  }

  private Integer counter = 0;
  @Override
  public void run() {
    log.info("Consumer starting");
    while (runnerState.isRunning()) {
      try {
        T task = workerExecutor.take();
        workerExecutor.execute(() -> {
          consumeTask(task);
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    log.info("Post Consumer State is " + runnerState);
    log.info("Consumer Started");
  }

  private void consumeTask(T taskToRun) {
    log.debug(taskToRun.getLogStatement());
    taskToRun.run();
  }

  public void start() {
    controlExecutor.schedule(this, 1, TimeUnit.SECONDS);
  }

  @Override
  public void close() throws Exception {
    log.info("Consumer Shutting Down");
    setRunnerState(OFF);
    controlExecutor.shutdownNow();
    workerExecutor.close();
  }
}
