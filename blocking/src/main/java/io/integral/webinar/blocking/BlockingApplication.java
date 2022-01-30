package io.integral.webinar.blocking;

import io.integral.webinar.blocking.runners.Runner;
import io.integral.webinar.blocking.types.BlockingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@Configuration
@Slf4j
public class BlockingApplication implements CommandLineRunner {

  private Runnable runnableRunner;

  @Bean
  public Runnable getRunnableRunner(Runner taskRunner, BlockingTask task) {
    runnableRunner = () -> {
      try(Runner runner = taskRunner) {
        runner.setTask(task);
        task.setResource("running");
        log.info("runner state is " + runner.getState());
        runner.run();
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
      }
    };
    return runnableRunner;
  }

  public static void main(String[] args) {
    SpringApplication.run(BlockingApplication.class, args);
  }


  @Override
  public void run(String... args) throws Exception {
    CompletableFuture.runAsync(runnableRunner);
  }
}
