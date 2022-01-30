package io.integral.webinar.blocking.runners;

import io.integral.webinar.blocking.types.BlockingTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static io.integral.webinar.blocking.runners.State.OFF;
import static io.integral.webinar.blocking.runners.State.ON;

@Slf4j
@Component
@Profile({"cpu & single", "io & single", "real & single"})
public class SingleThreadLoop implements Runner{

    @Getter
    private State state = ON;

    private BlockingTask task;

    @Override
    public void run() {
        log.info("starting to run " + task.getName());
        while (state.isRunning()) {
            log.info(task.getLogStatement());
            task.run();
        }

    }

    @Override
    public void close() throws Exception {
        log.info("stopping " + task.getName());
        setState(OFF);
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setTask(BlockingTask task) {
        this.task = task;
    }
}
