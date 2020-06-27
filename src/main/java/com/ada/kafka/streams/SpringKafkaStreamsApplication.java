package com.ada.kafka.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafka
@EnableKafkaStreams
@SpringBootApplication
public class SpringKafkaStreamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringKafkaStreamsApplication.class, args);
    }

}
