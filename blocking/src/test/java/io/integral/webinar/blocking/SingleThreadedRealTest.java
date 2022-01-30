package io.integral.webinar.blocking;

import io.integral.webinar.blocking.runners.Runner;
import io.integral.webinar.blocking.runners.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"real", "single"})
@TestPropertySource(properties = {"THREADING = single", "BLOCKING = real"})
class SingleThreadedRealTest {

  @Autowired
  public Runner runner;

  @Test
  void contextLoads() throws Exception {
    Assertions.assertNotNull(runner);
    assertThat(runner.getState()).isEqualTo(State.ON);
    runner.close();
    assertThat(runner.getState()).isEqualTo(State.OFF);
  }

}
