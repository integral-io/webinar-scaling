package io.integral.webinar.blocking.task;

import io.integral.webinar.blocking.NonBlockingTask;
import io.integral.webinar.blocking.observability.DemoMetrics;
import io.integral.webinar.blocking.runner.service.TicketedExecutor;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

import static io.integral.webinar.blocking.observability.DemoMetrics.getRandom;
import static io.integral.webinar.blocking.task.TaskState.*;
import static java.time.Duration.ofMillis;
import static java.time.Instant.now;

@Slf4j
public class NonBlockingIO implements NonBlockingTask {

  public final Timer timer;
  private final Integer waitAtMost;

  @Getter
  private TaskState taskState = CREATED;

  public NonBlockingIO(Integer maxBlock, Integer minBlock, TicketedExecutor executor) {
    setScheduler(executor);
    this.timer = DemoMetrics.getTimer("nonblock.io");
    waitAtMost = getRandom(minBlock, maxBlock);
  }

  @Override
  public String getName() {
    return "BlockingIO";
  }

  @Override
  public String getLogStatement() {
    return "simulating Non Blocking I/O with scheduled executor on same thread";
  }

  @Override
  public void run() {
    timer.wrap(this::simulateIOBlocking).run();
  }

  private void simulateIOBlocking() {
    taskState = RUNNING;
    Instant startTime = now();
    UUID executionId = UUID.randomUUID();
    log.debug("Started request of: " + executionId);

    Instant waitUntil = now().plusMillis(waitAtMost);
    taskState = WAITING;
    wait(
        ofMillis(waitAtMost), //Maximum amount of time to wait before giving up
        () -> {
          // Testable Condition indicating that the thing we are waiting for has happened.
          // This could be the goal state of a network call, Database statement, or other I/O
          return now().isAfter(waitUntil);
        },
        () -> {
          // This is what we want to do when we are done.
          // For example purpose I am trivially logging.
          taskState = COMPLETE;
          final long difference = now().toEpochMilli() - startTime.toEpochMilli();
          timer.record(ofMillis(difference));
          log.debug("Finished request of: " + executionId);
        },
        () -> {
          // This is what we want to happen if your condition is not met in the defined timeframe
          // Wait at most was left at race time conditions purely for the fun of seeing this hit
          taskState = FAILED;
          final long difference = now().toEpochMilli() - startTime.toEpochMilli();
          timer.record(ofMillis(difference));
          log.warn("Request Failed For: " + executionId);
        });
  }
}
