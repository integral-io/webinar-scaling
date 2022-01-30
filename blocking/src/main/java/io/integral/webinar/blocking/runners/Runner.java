package io.integral.webinar.blocking.runners;

import io.integral.webinar.blocking.types.BlockingTask;

public interface Runner extends Runnable, AutoCloseable{
    State getState();
    void setState(State state);
    void setTask(BlockingTask task);
}
