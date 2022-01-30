package io.integral.webinar.blocking.types;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.Random;

public class BlockingMetrics {

    private static final Random random = new Random();
    private static final Timer.Builder loopTimerBuilder = Timer.builder("loop.timer")
            .description("how long it takes to loop");

    public static Timer getTimer(String blockingType) {
        return loopTimerBuilder.tags("blocking", blockingType)
                .register(Metrics.globalRegistry);
    }

    public static int getRandom(int min, int max) {
        return random.nextInt(max - min) + min;
    }

}
