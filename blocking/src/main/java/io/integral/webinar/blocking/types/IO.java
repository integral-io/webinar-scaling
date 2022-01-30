package io.integral.webinar.blocking.types;

import io.micrometer.core.instrument.Timer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("io")
public class IO implements BlockingTask {

    public final Timer timer;

    public final Integer maxBlock;
    private final Integer minBlock;

    @Setter
    private String resource = "initialized";

    public IO(@Value("${MAX_BLOCK:1000}") Integer maxBlock,
              @Value("${MIN_BLOCK:100}") Integer minBlock) {
        this.maxBlock = maxBlock;
        this.minBlock = minBlock;
        this.timer =  BlockingMetrics.getTimer("io");
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
        try {
            TimeUnit.MILLISECONDS.sleep(BlockingMetrics.getRandom(minBlock, maxBlock));
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
