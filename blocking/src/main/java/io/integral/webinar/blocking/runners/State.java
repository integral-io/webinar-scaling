package io.integral.webinar.blocking.runners;

import lombok.Getter;

public enum State {
    ON(true), OFF(false);

    @Getter
    private final boolean running;

    State(Boolean running) {
        this.running = running;
    }
}
