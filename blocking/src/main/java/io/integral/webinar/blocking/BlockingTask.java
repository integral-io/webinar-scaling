package io.integral.webinar.blocking;

import io.integral.webinar.blocking.task.TaskState;

public interface BlockingTask extends Runnable{
    String getName();
    String getLogStatement();
    TaskState getTaskState();
}
