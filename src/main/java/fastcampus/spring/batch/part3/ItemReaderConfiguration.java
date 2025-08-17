package fastcampus.spring.batch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ItemReaderConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource;
  private final EntityManagerFactory entityManagerFactory;

  public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource, EntityManagerFactory entityManagerFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.dataSource = dataSource;
    this.entityManagerFactory = entityManagerFactory;
  }

  @Bean
  public Job itemReaderJob() throws Exception {

    return this.jobBuilderFactory
        .get("itemReaderJob")
        .incrementer(new RunIdIncrementer())
        .start(this.customItemReaderStep())
        .next(this.csvFileStep())
        .next(this.jdbcStep())
        .next(this.jpaStep())
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

  @Bean
  public Step csvFileStep() throws Exception {

    return stepBuilderFactory
        .get("csvFileStep")
        .<PersonEntity, PersonEntity>chunk(10)
        .reader(this.csvFileItemReader())
        .writer(itemWriter())
        .build();
  }

  @Bean
  public Step jdbcStep() throws Exception {

    return stepBuilderFactory
        .get("jdbcStep")
        .<PersonEntity, PersonEntity>chunk(10)
        .reader(jdbcCursorItemReader())
        .writer(itemWriter())
        .build();
  }

  @Bean
  public Step jpaStep() throws Exception {
    return stepBuilderFactory
        .get("jpaStep")
        .<PersonEntity, PersonEntity>chunk(10)
        .reader(this.jpaCursorItemReader())
        .writer(itemWriter())
        .build();
  }

  private JpaCursorItemReader<PersonEntity> jpaCursorItemReader() throws Exception {
    JpaCursorItemReader<PersonEntity> jpaCursorItemReader = new JpaCursorItemReaderBuilder<PersonEntity>()
        .name("jpaCursorItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("select p from PersonEntity p")
        .build();
    jpaCursorItemReader.afterPropertiesSet();

    return jpaCursorItemReader;
  }

  private JdbcCursorItemReader<PersonEntity> jdbcCursorItemReader() throws Exception {
    JdbcCursorItemReader<PersonEntity> jdbcCursorItemReader = new JdbcCursorItemReaderBuilder<PersonEntity>()
        .name("jdbcCursorItemReader")
        .dataSource(dataSource)
        .sql("SELECT id, name, age, address FROM person")
        .rowMapper((rs, rowNum)
            -> new PersonEntity(rs.getInt(1)
            , rs.getString(2)
            , rs.getString(3)
            , rs.getString(4)
        )).build();
    jdbcCursorItemReader.afterPropertiesSet();

    return jdbcCursorItemReader;
  }

  private FlatFileItemReader<PersonEntity> csvFileItemReader() throws Exception {
    DefaultLineMapper<PersonEntity> lineMapper = new DefaultLineMapper<>();

    DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
    delimitedLineTokenizer.setNames("id", "name", "age", "address");

    lineMapper.setLineTokenizer(delimitedLineTokenizer);
    lineMapper.setFieldSetMapper(fieldSet -> {
      int id = fieldSet.readInt("id");
      String name = fieldSet.readString("name");
      String age = fieldSet.readString("age");
      String address = fieldSet.readString("address");
      return new PersonEntity(id, name, age, address);
    });

    FlatFileItemReader<PersonEntity> itemReader = new FlatFileItemReaderBuilder<PersonEntity>()
        .name("csvFileItemReader")
        .encoding("UTF-8")
        .resource(new ClassPathResource("test.csv"))
        .linesToSkip(1)
        .lineMapper(lineMapper)
        .build();
    itemReader.afterPropertiesSet();

    return itemReader;
  }

  private ItemWriter<PersonEntity> itemWriter() {

    return items -> {
      log.info(
          items.stream()
              .map(PersonEntity::getName)
              .collect(Collectors.joining(", ")));
    };
  }

  private List<PersonEntity> getItems() {
    List<PersonEntity> items = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      items.add(new PersonEntity(i + 1, "test name" + i, "test age", "test address"));
    }

    return items;
  }

}
