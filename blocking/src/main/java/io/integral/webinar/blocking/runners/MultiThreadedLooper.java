package io.integral.webinar.blocking.runners;

import io.integral.webinar.blocking.types.BlockingTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.integral.webinar.blocking.runners.State.OFF;
import static io.integral.webinar.blocking.runners.State.ON;

@Slf4j
@Component
@Profile({"multi & real"})
public class MultiThreadedLooper implements Runner{

  @Getter
  @Setter
  private State state;

  private BlockingTask task;

  private final Executor executor;

  public MultiThreadedLooper(@Value("${THREADS:2}") Integer threadCount) {
    setState(ON);
    executor = Executors.newFixedThreadPool(threadCount);
  }

  @Override
  public void run() {
    log.info("starting to run " + task.getName());
    while (state.isRunning()) {
      executor.execute(task);
    }
  }

  @Override
  public void setTask(BlockingTask task) {
    this.task = task;
  }

  @Override
  public void close() throws Exception {
    setState(OFF);
  }
}
