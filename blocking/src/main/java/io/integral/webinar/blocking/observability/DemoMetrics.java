package io.integral.webinar.blocking.observability;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoMetrics {

    private static final Map<String, CountGauge> counters = new ConcurrentHashMap<>();

    private static final Random random = new Random();
    private static final Timer.Builder loopTimerBuilder = Timer.builder("loop.timer")
            .description("how long it takes to loop");

    public static Timer getTimer(String blockingType) {
        return loopTimerBuilder.tags("blocking", blockingType)
                .register(Metrics.globalRegistry);
    }

    public static AtomicInteger getGauge(String gaugeName) {
        if (counters.containsKey(gaugeName)) {
            return counters.get(gaugeName).counter;
        }
        counters.put(gaugeName, new CountGauge(gaugeName));
        return getGauge(gaugeName);
    }


    public static int getRandom(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    private static class CountGauge {
        public String name;
        public Gauge gauge;
        public AtomicInteger counter;

        public CountGauge(String gaugeName) {
            name = gaugeName;
            counter = new AtomicInteger(0);
            gauge = Gauge.builder("counter.gauge", counter, AtomicInteger::doubleValue)
                .tag("tasks", name)
                .register(Metrics.globalRegistry);
        }
    }

}
