package io.integral.webinar.blocking.task;

import io.integral.webinar.blocking.BlockingTask;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;

import static io.integral.webinar.blocking.observability.DemoMetrics.getTimer;
import static io.integral.webinar.blocking.task.TaskState.*;

public class Realistic implements BlockingTask {

  private final CPU cpuTask;
  private final IO ioTask;
  private final Timer timer;

  @Getter
  private TaskState taskState = CREATED;

  public Realistic(TaskFactory taskFactory) {
    this.cpuTask = taskFactory.getCPUBlockingTask();
    this.ioTask = taskFactory.getIOBlockingTask();
    this.timer =  getTimer("real");
  }

  @Override
  public String getName() {
    return "BlockingRealistic";
  }

  @Override
  public String getLogStatement() {
    return "Simulating realistic blocking with busy loop and thread sleep";
  }

  @Override
  public void run() {
    timer.wrap(() -> {
      taskState = RUNNING;
      ioTask.run();
      cpuTask.run();
      taskState = COMPLETE;
    }).run();
  }
}
