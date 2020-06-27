package com.ada.kafka.streams.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringKafkaStreamsConfig {

    private final JsonSerializer<JsonNode> jsonSerializer = new JsonSerializer();
    private final JsonDeserializer<JsonNode> jsonDeserializer = new JsonDeserializer(JsonNode.class);
    private final Serde<JsonNode> jsonSerde = new JsonSerde(jsonSerializer, jsonDeserializer);

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-transaction-stream");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.KEY_DEFAULT_TYPE, String.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JsonNode.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public KStream<String, JsonNode> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, JsonNode> bankTransactions =
                streamsBuilder.stream("bank-transaction",
                        Consumed.with(Serdes.String(), jsonSerde));
        bankTransactions.print(Printed.toSysOut());
        KStream<String, Integer> balanceStream = bankTransactions
                .map((k, v) -> KeyValue.pair(v.get("name").textValue(), v))
                .groupByKey()
                .aggregate(() -> {
                            ObjectNode newBalance = JsonNodeFactory.instance.objectNode();
                            newBalance.put("balance", 0);
                            newBalance.put("timestamp", 0);
                            return newBalance;
                        },
                        (key, transaction, balance) -> {
                            ObjectNode bal = JsonNodeFactory.instance.objectNode();
                            bal.put("balance", balance.get("balance").intValue() + transaction.get("amount").asInt());
                            bal.put("timestamp", System.currentTimeMillis());
                            return bal;
                        },
                        Materialized.as("bank-balance-store")
                ).toStream()
                .map((k, v) -> KeyValue.pair(k, v.get("balance").intValue()));
        balanceStream.print(Printed.toSysOut());
        balanceStream.to("bank-balance-exactly-once");
        return bankTransactions;
    }
}
