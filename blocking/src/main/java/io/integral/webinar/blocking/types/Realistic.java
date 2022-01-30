package io.integral.webinar.blocking.types;

import io.micrometer.core.instrument.Timer;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static io.integral.webinar.blocking.types.BlockingMetrics.getTimer;

@Component
@Profile("real")
public class Realistic implements BlockingTask{

  public final Integer maxBlock;
  private final Integer minBlock;
  private final CPU cpuTask;
  private final IO ioTask;
  private final Timer timer;

  @Setter
  private String resource = "initialized";

  public Realistic(@Value("${MAX_BLOCK:20}") Integer maxBlock,
                   @Value("${MIN_BLOCK:1}") Integer minBlock) {
    this.maxBlock = maxBlock;
    this.minBlock = minBlock;
    this.cpuTask = new CPU(maxBlock, minBlock);
    this.ioTask = new IO(maxBlock*10, minBlock*10);
    this.timer =  getTimer("real");
  }

  @Override
  public String getName() {
    return "BlockingRealistic";
  }

  @Override
  public String getLogStatement() {
    return "resources for task " + resource + ": performing realistic blocking with busy loop and thread sleep";
  }

  @Override
  public void run() {
    timer.wrap(() -> {
      cpuTask.run();
      ioTask.run();
    }).run();
  }
}
