package io.integral.webinar.blocking.task;

import io.integral.webinar.blocking.BlockingTask;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import static io.integral.webinar.blocking.observability.DemoMetrics.getRandom;
import static io.integral.webinar.blocking.observability.DemoMetrics.getTimer;
import static io.integral.webinar.blocking.task.TaskState.*;
import static java.time.Duration.ofNanos;
import static java.time.Instant.now;

@Slf4j
public class CPU implements BlockingTask {

    public final Timer timer;

    public final Integer maxBlock;
    private final Integer minBlock;
    @Getter
    private TaskState taskState = CREATED;

    public CPU(Integer maxBlock, Integer minBlock) {
        this.maxBlock = maxBlock;
        this.minBlock = minBlock;
        this.timer =  getTimer("cpu");
    }

    @Override
    public String getName() {
        return "BlockingCPU";
    }

    @Override
    public String getLogStatement() {
        return "Simulating CPU blocking with busy loop";
    }

    @Override
    public void run() {
        timer.wrap(this::simulateIOBlocking).run();
    }

    private void simulateIOBlocking() {
        taskState = RUNNING;
        Instant future = now().plus(ofNanos(getRandom(minBlock, maxBlock) * 1000L));
        while (future.isAfter(now())) {
            log.trace("A watched pot never boils");
        }
        taskState = COMPLETE;
    }
}
