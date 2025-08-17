package fastcampus.spring.batch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class ItemWriterConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  public ItemWriterConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
  }

  @Bean
  public Job itemWriterJob() throws Exception {
    return this.jobBuilderFactory
        .get("itemWriterJob")
        .incrementer(new RunIdIncrementer())
        .start(this.csvItemWriterStep())
        .build();
  }

  @Bean
  public Step csvItemWriterStep() throws Exception {
    return this.stepBuilderFactory
        .get("csvItemWriterStep")
        .<PersonEntity, PersonEntity>chunk(10)
        .reader(itemReader())
        .writer(csvFileItemWriter())
        .build();
  }

  private ItemWriter<PersonEntity> csvFileItemWriter() throws Exception {
    BeanWrapperFieldExtractor<PersonEntity> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[]{"id", "name", "age", "address"});

    DelimitedLineAggregator<PersonEntity> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(",");
    lineAggregator.setFieldExtractor(fieldExtractor);

    FlatFileItemWriter<PersonEntity> itemWriter = new FlatFileItemWriterBuilder<PersonEntity>()
        .name("csvFileItemWriter")
        .encoding("UTF-8")
        .resource(new FileSystemResource("output/test-output.csv"))
        .lineAggregator(lineAggregator)
        .headerCallback(writer -> writer.write("id,이름,나이,거주지"))
        .footerCallback(writer -> writer.write("---------------------\n"))
        .append(true)
        .build();

    itemWriter.afterPropertiesSet();

    return itemWriter;
  }

  private ItemReader<PersonEntity> itemReader() {

    return new CustomItemReader<>(getItems());
  }

  private List<PersonEntity> getItems() {
    List<PersonEntity> items = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      items.add(new PersonEntity(i + 1, "test name" + i, "test age", "test address"));
    }
    return items;
  }
}
