package io.integral.webinar.blocking.task;

import io.integral.webinar.blocking.runner.service.TicketedExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TaskFactory {

  private Integer ioMaxBlock;
  private Integer ioMinBlock;
  private Integer busyMaxBlock;
  private Integer busyMinBlock;

  public TaskFactory(@Value("${IO_MAX_BLOCK:1000}") Integer ioMaxBlock,
                     @Value("${IO_MIN_BLOCK:100}") Integer ioMinBlock,
                     @Value("${CPU_MAX_BLOCK:1000}") Integer busyMaxBlock,
                     @Value("${CPU_MIN_BLOCK:100}") Integer busyMinBlock
                     ) {
    this.ioMaxBlock = ioMaxBlock;
    this.ioMinBlock = ioMinBlock;
    this.busyMaxBlock = busyMaxBlock;
    this.busyMinBlock = busyMinBlock;
  }

  public IO getIOBlockingTask() {
    return new IO(ioMaxBlock, ioMinBlock);
  }

  public NonBlockingIO getNonBlockingIOTask(TicketedExecutor executor) {
    return new NonBlockingIO(ioMaxBlock, ioMinBlock, executor);
  }

  public CPU getCPUBlockingTask() {
    return new CPU(busyMaxBlock, busyMinBlock);
  }

  public Realistic getRealisticTask() {
    return new Realistic(this);
  }

  public Idealistic getBetterTask(TicketedExecutor executor ) {
    return new Idealistic(this, executor);
  }

  public Idealistic getIdealisticTask(TicketedExecutor executor) {
    return new Idealistic(this, executor);
  }

}
