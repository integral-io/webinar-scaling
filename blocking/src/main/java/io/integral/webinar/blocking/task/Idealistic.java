package io.integral.webinar.blocking.task;

import io.integral.webinar.blocking.NonBlockingTask;
import io.integral.webinar.blocking.runner.service.TicketedExecutor;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

import static io.integral.webinar.blocking.observability.DemoMetrics.getTimer;
import static io.integral.webinar.blocking.task.TaskState.*;
import static java.time.Duration.ofMillis;
import static java.time.Instant.now;

@Slf4j
public class Idealistic implements NonBlockingTask {
  private final CPU cpuTask;
  private final NonBlockingIO ioTask;
  private final Timer timer;

  @Getter
  private TaskState taskState = CREATED;

  public Idealistic(TaskFactory taskFactory, TicketedExecutor executor) {
    this.cpuTask = taskFactory.getCPUBlockingTask();
    this.ioTask = taskFactory.getNonBlockingIOTask(executor);
    this.setScheduler(executor);
    timer = getTimer("ideal");
  }

  @Override
  public String getName() {
    return "Non Blocking Idealistic";
  }

  @Override
  public String getLogStatement() {
    return "simulating Mixed CPU Processing with Non Blocking I/O with scheduled executor on same thread";
  }

  @Override
  public void run() {
    taskState = RUNNING;
    Instant startTime = now();
    UUID computationId = UUID.randomUUID();
    log.debug("Started computation of: " + computationId);

    ioTask.run();
    wait(ofMillis(1000),
        () -> ioTask.getTaskState() == COMPLETE,
        () -> {
          taskState = COMPLETE;
          final long difference = now().toEpochMilli() - startTime.toEpochMilli();
          timer.record(ofMillis(difference));
          log.debug("Finished query: " + computationId);
        },() -> {
          taskState = FAILED;
          final long difference = now().toEpochMilli() - startTime.toEpochMilli();
          timer.record(ofMillis(difference));
          log.debug("Query Failed: " + computationId);
        });
    taskState = RUNNING;
    cpuTask.run(); // simulates some processing occurring while waiting for IO
    taskState = WAITING;
  }
}
