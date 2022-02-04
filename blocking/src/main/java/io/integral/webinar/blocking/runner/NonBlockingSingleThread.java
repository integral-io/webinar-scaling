package io.integral.webinar.blocking.runner;

import io.integral.webinar.blocking.NonBlockingTask;
import io.integral.webinar.blocking.Runner;
import io.integral.webinar.blocking.runner.service.Consumer;
import io.integral.webinar.blocking.runner.service.Producer;
import io.integral.webinar.blocking.runner.service.TicketedExecutor;
import io.integral.webinar.blocking.task.TaskFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static io.integral.webinar.blocking.runner.RunnerState.OFF;
import static io.integral.webinar.blocking.runner.RunnerState.ON;
import static java.util.concurrent.Executors.newScheduledThreadPool;

@Slf4j
@Component
@Profile({"io & nonblock", "better & nonblock", "ideal & nonblock"})
public class NonBlockingSingleThread implements Runner<NonBlockingTask> {

  private final TicketedExecutor<NonBlockingTask> executor = new TicketedExecutor<>(newScheduledThreadPool(1), 2);;
  private final Consumer<NonBlockingTask> consumer = new Consumer<>(executor);;
  private final Supplier<NonBlockingTask> generator;
  private final Producer<NonBlockingTask> producer;;
  @Getter
  @Setter
  private RunnerState runnerState = ON;


  public NonBlockingSingleThread(TaskFactory taskFactory, @Value("${BLOCKING}") String BlockingTaskType) {
    generator = () -> {
      if (BlockingTaskType.equals("io")) {
        return taskFactory.getNonBlockingIOTask(executor);
      } else {
        return taskFactory.getIdealisticTask(executor);
      }
    };
    producer = new Producer<>("single.nonblock.io", generator, executor);
  }

  @Override
  public void close() throws Exception {
    log.info("stopping " + generator.get().getName());
    setRunnerState(OFF);
    consumer.close();
    executor.close();
  }

  @Override
  public void run() {
    consumer.start();
    producer.run();
  }
}
