package fastcampus.spring.batch.part3;

import fastcampus.spring.batch.TestConfiguration;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SavePersonConfigurationTest.class, TestConfiguration.class})
public class SavePersonConfigurationTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Test
  @Disabled
  public void test_allow_duplicate() throws Exception {
    //given
    JobParameters jobParameters = new JobParametersBuilder().addString("allow_duplicate", "false").toJobParameters();

    //when
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    //then
    assertThat(jobExecution
        .getStepExecutions()
        .stream()
        .mapToInt(StepExecution::getWriteCount)
        .sum())
        .isEqualTo(3);

  }
}
