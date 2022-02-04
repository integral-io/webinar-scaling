package io.integral.webinar.blocking.runner;

import lombok.Getter;

public enum RunnerState {
    ON(true), OFF(false);

    @Getter
    private final boolean running;

    RunnerState(Boolean running) {
        this.running = running;
    }
}
