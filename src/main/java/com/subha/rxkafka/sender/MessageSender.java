package com.subha.rxkafka.sender;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.Sender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

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
                Flux.range(1, 3)
                        .map(i -> SenderRecord.<Integer, String, Integer>create(
                                new ProducerRecord<Integer, String>("demo-topic",i, "Message"+i), i));

       // CountDownLatch countDownLatch = new CountDownLatch(9);

      sender.send(outboundFlux, true)
               // .publishOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
                .doOnError(Throwable::printStackTrace)
                .doOnNext(integerSenderResult -> System.out.println(
                        integerSenderResult.recordMetadata()+"---"+integerSenderResult.correlationMetadata()))
               .doOnComplete(() -> System.out.println("Done"))
              .subscribeOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
                .subscribe(new Subscriber<SenderResult<Integer>>() {

                    Subscription subscription = null;

                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("The Subscription is:"+s+" For:"+Thread.currentThread());
                        subscription = s;
                    }

                    @Override
                    public void onNext(SenderResult<Integer> integerSenderResult) {
                        System.out.println(integerSenderResult+" For:"+Thread.currentThread());
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("All Records Sent"+" For:"+Thread.currentThread());
                        //sender.close();
                        //subscription.cancel();

                    }
                });


        System.out.println(" For:"+Thread.currentThread());

        //countDownLatch.await();

    }
}
