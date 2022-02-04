package io.integral.webinar.blocking;

import io.integral.webinar.blocking.observability.DemoMetrics;
import io.integral.webinar.blocking.runner.service.TicketedExecutor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.time.Duration.ofMillis;

/**
 * This Interface is inspired by Awaitility, Promises, Observer patterns, and Other Async paradigms
 */
public interface NonBlockingTask extends BlockingTask {

    AtomicReference<TicketedExecutor> scheduler = new AtomicReference<>();

    default void wait(Duration atMost, Supplier<Boolean> until, Runnable then) {
        wait(atMost, until, then, null);
    }

    default void wait(Duration atMost, Supplier<Boolean> until, Runnable then, Runnable onFail) {
        Instant future = Instant.now().plusMillis(atMost.toMillis());
        scheduler.get().schedule(()->{
            DemoMetrics.getGauge("waiting").decrementAndGet();
            if (until.get()) {
                DemoMetrics.getGauge("running").incrementAndGet();
                then.run();
                DemoMetrics.getGauge("running").decrementAndGet();
            } else {
                if (Instant.now().isBefore(future)) {
                    Duration waitRemaining = ofMillis(future.minusMillis(Instant.now().toEpochMilli()).toEpochMilli());
                    wait(waitRemaining, until, then, onFail);
                } else {
                    if (null != onFail) {
                        onFail.run();
                    }
                }
            }
        }, 10, TimeUnit.MILLISECONDS);
        DemoMetrics.getGauge("waiting").incrementAndGet();
    }

    default void setScheduler(TicketedExecutor executor) {
        scheduler.set(executor);
    };

}
