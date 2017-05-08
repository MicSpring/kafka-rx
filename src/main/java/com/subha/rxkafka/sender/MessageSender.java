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
import java.util.concurrent.ExecutorService;
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

        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        ExecutorService executorService2 = Executors.newCachedThreadPool();


      sender.send(outboundFlux, true)
              //.subscribeOn(Schedulers.fromExecutor(executorService2))
              //  .publishOn(Schedulers.fromExecutor(executorService2))

              .doOnNext(integerSenderResult -> System.out.println(" Do On Next:"+
                      integerSenderResult.recordMetadata()+"---"+integerSenderResult.correlationMetadata()+" For:"+Thread.currentThread()))
              .doOnError(Throwable::printStackTrace)
               .doOnComplete(() -> System.out.println("Done"+ " For:"+Thread.currentThread()))
             // .publishOn(Schedulers.fromExecutor(executorService2))
                 .doOnNext(integerSenderResult -> System.out.println("Logging:"+integerSenderResult+" For:"+Thread.currentThread()))
                .subscribeOn(Schedulers.fromExecutor(executorService2))
             // .publishOn(Schedulers.fromExecutor(executorService2))
                .subscribe(new Subscriber<SenderResult<Integer>>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("The Subscription is:"+s+" For:"+Thread.currentThread());
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(SenderResult<Integer> integerSenderResult) {
                        System.out.println("On NEXT: "+integerSenderResult+" For:"+Thread.currentThread());
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("All Records Sent"+" For:"+Thread.currentThread());
                        executorService2.shutdown();
                        //executorService1.shutdown();
                    }
                });


        System.out.println(" For:"+Thread.currentThread());
       // TimeUnit.SECONDS.sleep(3000L);
        //countDownLatch.await();

    }
}
