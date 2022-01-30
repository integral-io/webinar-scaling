package io.integral.webinar.blocking.types;

import io.micrometer.core.instrument.Timer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static io.integral.webinar.blocking.types.BlockingMetrics.*;
import static java.time.Duration.ofMillis;
import static java.time.Instant.*;

@Slf4j
@Component
@Profile("cpu")
public class CPU implements BlockingTask {

    public final Timer timer;

    public final Integer maxBlock;
    private final Integer minBlock;

    @Setter
    private String resource = "initialized";

    public CPU(@Value("${MAX_BLOCK:1000}") Integer maxBlock,
               @Value("${MIN_BLOCK:100}") Integer minBlock
    ) {
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
        return "resources for task " + resource + ": performing CPU blocking with busy loop";
    }

    @Override
    public void run() {
        timer.wrap(this::simulateIOBlocking).run();
    }

    private void simulateIOBlocking() {
        Instant future = now().plus(ofMillis(getRandom(minBlock, maxBlock)));
        while (future.isAfter(now())) {
            log.trace("A watched pot never boils");
        }
    }
}
