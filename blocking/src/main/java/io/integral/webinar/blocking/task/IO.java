package io.integral.webinar.blocking.task;

import io.integral.webinar.blocking.BlockingTask;
import io.integral.webinar.blocking.observability.DemoMetrics;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static io.integral.webinar.blocking.task.TaskState.*;

@Slf4j
public class IO implements BlockingTask {

    public final Timer timer;

    public final Integer maxBlock;
    private final Integer minBlock;
    @Getter
    private TaskState taskState = CREATED;

    @Setter
    private String resource = "initialized";

    public IO(Integer maxBlock, Integer minBlock) {
        this.maxBlock = maxBlock;
        this.minBlock = minBlock;
        this.timer =  DemoMetrics.getTimer("io");
    }

    @Override
    public String getName() {
        return "BlockingIO";
    }

    @Override
    public String getLogStatement() {
        return "resources for task " + resource + ": performing I/O blocking with thread sleep";
    }

    @Override
    public void run() {
        timer.wrap(this::simulateIOBlocking).run();
    }

    private void simulateIOBlocking() {
        taskState = RUNNING;
        try {
            taskState = WAITING;
            TimeUnit.MILLISECONDS.sleep(DemoMetrics.getRandom(minBlock, maxBlock));
            taskState = RUNNING;
        } catch (InterruptedException e) {
            taskState = RUNNING;
            log.warn(e.getMessage(), e);
        }
        taskState = COMPLETE;
    }
}
