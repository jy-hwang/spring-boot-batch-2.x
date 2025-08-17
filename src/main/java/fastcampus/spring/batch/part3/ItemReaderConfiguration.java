package fastcampus.spring.batch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ItemReaderConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
  }

  @Bean
  public Job itemReaderJob() {
    return this.jobBuilderFactory
        .get("itemReaderJob")
        .incrementer(new RunIdIncrementer())
        .start(this.customItemReaderStep())
        .build();
  }

  @Bean
  public Step customItemReaderStep() {
    return this.stepBuilderFactory
        .get("customItemReaderStep")
        .<PersonEntity, PersonEntity>chunk(10)
        .reader(new CustomItemReader<PersonEntity>(getItems()))
        .writer(itemWriter())
        .build();
  }

  private ItemWriter<PersonEntity> itemWriter() {
    return items -> log.info(
        items.stream()
            .map(PersonEntity::getName)
            .collect(Collectors.joining(",")));
  }


  private List<PersonEntity> getItems() {
    List<PersonEntity> items = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      items.add(new PersonEntity(i + 1, "test name" + i, "test age", "test address"));
    }

    return items;
  }

}
