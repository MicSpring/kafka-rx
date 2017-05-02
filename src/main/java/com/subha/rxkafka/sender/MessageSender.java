package com.subha.rxkafka.sender;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.Sender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 5/3/2017.
 */
public class MessageSender {
    public static void main(String[] args) {
        Map<String, Object> producerProps = new HashMap<String, Object>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:2181");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        SenderOptions<Integer, String> senderOptions =
                SenderOptions.<Integer, String>create(producerProps)
                .maxInFlight(1024);

        Sender<Integer, String> sender = Sender.create(senderOptions);

        Flux<SenderRecord<Integer, String, Integer>> outboundFlux =
                Flux.range(1, 10)
                        .map(i -> SenderRecord.create(new ProducerRecord<Object, Object>("demo-topic", i, "Message_" + i), i);
    }
}
