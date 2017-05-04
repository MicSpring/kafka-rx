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
import java.util.concurrent.CountDownLatch;

/**
 * Created by user on 5/3/2017.
 */
public class MessageSender {
    public static void main(String[] args) throws InterruptedException {

        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        SenderOptions<Integer, String> senderOptions =
                SenderOptions.<Integer, String>create(producerProps)
                .maxInFlight(1024);

        Sender<Integer, String> sender = Sender.create(senderOptions);

        Flux<SenderRecord<Integer, String, Integer>> outboundFlux =
                Flux.range(1, 9)
                        .map(i -> SenderRecord.<Integer, String, Integer>create(
                                new ProducerRecord<Integer, String>("demo-topic",i, "Message"+i), i));

        CountDownLatch countDownLatch = new CountDownLatch(9);

        sender.send(outboundFlux, false)
                .doOnError(Throwable::printStackTrace)
                .doOnNext(integerSenderResult -> System.out.println(
                        integerSenderResult.recordMetadata()+"---"+integerSenderResult.correlationMetadata()))
                .subscribe(integerSenderResult -> {
                    countDownLatch.countDown();
                });
               // .dispose();

        countDownLatch.await();

    }
}
