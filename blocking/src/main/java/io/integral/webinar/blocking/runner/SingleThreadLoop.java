package io.integral.webinar.blocking.runner;

import io.integral.webinar.blocking.BlockingTask;
import io.integral.webinar.blocking.Runner;
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

@Slf4j
@Component
@Profile({"cpu & single", "io & single", "real & single"})
public class SingleThreadLoop implements Runner<BlockingTask> {

    @Getter
    @Setter
    private RunnerState runnerState = ON;

    private Supplier<BlockingTask> generator;

    public SingleThreadLoop(TaskFactory taskFactory, @Value("${BLOCKING}") String BlockingTaskType) {
        generator = () -> {
          if (BlockingTaskType.equals("io")) {
              return taskFactory.getIOBlockingTask();
          } else if (BlockingTaskType.equals("cpu")) {
              return taskFactory.getCPUBlockingTask();
          } else {
              return taskFactory.getRealisticTask();
          }
        };
    }

    @Override
    public void run() {
        BlockingTask task = generator.get();
        log.info("starting to run " + task.getName());
        while (runnerState.isRunning()) {
            log.info(task.getLogStatement());
            task.run();
        }

    }

    @Override
    public void close() throws Exception {
        log.info("stopping " + generator.get().getName());
        setRunnerState(OFF);
    }

}
