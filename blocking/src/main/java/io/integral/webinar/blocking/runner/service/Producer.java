package io.integral.webinar.blocking.runner.service;

import io.integral.webinar.blocking.NonBlockingTask;
import io.integral.webinar.blocking.Runner;
import io.integral.webinar.blocking.observability.DemoMetrics;
import io.integral.webinar.blocking.runner.RunnerState;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static io.integral.webinar.blocking.runner.RunnerState.OFF;
import static io.integral.webinar.blocking.runner.RunnerState.ON;

@Slf4j
public class Producer<T extends NonBlockingTask> implements Runner<T> {

  private final Timer timer;
  private final TicketedExecutor<T> queue;

  @Setter
  @Getter
  private RunnerState runnerState;
  private final Supplier<T> generator;

  public Producer(String timerName, Supplier<T> generator, TicketedExecutor<T> queue) {
    setRunnerState(ON);
    timer = DemoMetrics.getTimer(timerName);

    this.generator = generator;
    this.queue = queue;
  }

  private Integer counter = 0;
  @Override
  public void run() {
    log.info("Producer Starting");
    T task = generator.get();
    log.info("starting to run " + task.getName());
    while (runnerState.isRunning()) {
      timer.wrap(() -> {
        try {
          queue.put(generator.get());
        } catch (InterruptedException e) {
          log.warn(e.getMessage(), e);
        }
      }).run();
    }
    log.info("Post Producer State is " + runnerState);
  }

  @Override
  public void close() throws Exception {
    setRunnerState(OFF);
    queue.clear();
  }
}
