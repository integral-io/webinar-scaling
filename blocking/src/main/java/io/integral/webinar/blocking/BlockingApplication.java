package io.integral.webinar.blocking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@Slf4j
public class BlockingApplication implements CommandLineRunner {

  private Runnable runnableRunner;

  @Bean
  public Runnable getRunnableRunner(Runner taskRunner) {
    runnableRunner = () -> {
      try(Runner runner = taskRunner) {
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
//    CompletableFuture.runAsync(runnableRunner);
    runnableRunner.run();
  }
}
