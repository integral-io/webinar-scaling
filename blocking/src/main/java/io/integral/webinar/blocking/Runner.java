package io.integral.webinar.blocking;

import io.integral.webinar.blocking.runner.RunnerState;

public interface Runner<T> extends Runnable, AutoCloseable{
    RunnerState getRunnerState();
    void setRunnerState(RunnerState runnerState);
}
