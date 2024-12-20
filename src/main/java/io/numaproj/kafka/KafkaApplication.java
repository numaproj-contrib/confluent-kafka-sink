package io.numaproj.kafka;

import io.numaproj.kafka.consumer.Admin;
import io.numaproj.kafka.consumer.KafkaSourcer;
import io.numaproj.kafka.consumer.Worker;
import io.numaproj.kafka.producer.KafkaSinker;
import io.numaproj.kafka.schema.ConfluentRegistry;
import io.numaproj.kafka.schema.Registry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {
            KafkaSinker.class,
            KafkaSourcer.class,
            Admin.class,
            Worker.class,
            ConfluentRegistry.class,
            Registry.class
          })
    })
public class KafkaApplication {

  public static void main(String[] args) {
    log.info("Supplied arguments: {}", (Object) args);
    new SpringApplicationBuilder(KafkaApplication.class).run(args);
  }
}
