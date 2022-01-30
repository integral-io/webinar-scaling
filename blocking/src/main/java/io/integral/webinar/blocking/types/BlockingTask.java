package io.integral.webinar.blocking.types;

public interface BlockingTask extends Runnable{
    void setResource(String resource);
    String getName();
    String getLogStatement();
}
