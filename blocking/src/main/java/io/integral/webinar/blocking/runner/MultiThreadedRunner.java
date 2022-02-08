package io.integral.webinar.blocking.runner;

import io.integral.webinar.blocking.BlockingTask;
import io.integral.webinar.blocking.Runner;
import io.integral.webinar.blocking.observability.DemoMetrics;
import io.integral.webinar.blocking.runner.service.Consumer;
import io.integral.webinar.blocking.runner.service.Producer;
import io.integral.webinar.blocking.runner.service.TicketedExecutor;
import io.integral.webinar.blocking.task.TaskFactory;
import io.micrometer.core.instrument.Timer;
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
@Profile({"multi & ideal", "multi & real"})
public class MultiThreadedRunner implements Runner<BlockingTask> {


  private final TicketedExecutor<BlockingTask> executor;
  private final Consumer<BlockingTask> consumer;
  private final Producer<BlockingTask> producer;;
  private final Supplier<BlockingTask> generator;
  @Getter
  @Setter
  private RunnerState runnerState = ON;
  private final Timer timer;


  private final Integer threadCount;

  public MultiThreadedRunner(TaskFactory taskFactory, @Value("${THREADS}") Integer threadCount,  @Value("${BLOCKING}") String BlockingTaskType) {
    this.threadCount = threadCount;
    executor = new TicketedExecutor<>(newScheduledThreadPool(threadCount), threadCount * 10);
    timer = DemoMetrics.getTimer("threaded");
    consumer = new Consumer<>(executor);
    generator = () -> {
      if (BlockingTaskType.equals("ideal")) {
        return taskFactory.getIdealisticTask(executor);
      } else {
        return taskFactory.getRealisticTask();
      }
    };
    producer = new Producer<>("threaded", generator, executor);
  }

  @Override
  public void run() {
    consumer.start();
    producer.run();
  }

  @Override
  public void close() throws Exception {
    setRunnerState(OFF);
    consumer.close();
    producer.close();
  }
}
