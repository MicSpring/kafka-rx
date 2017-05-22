package com.subha.lambdastreams.rxkafka.receiver;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import reactor.kafka.receiver.Receiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 5/4/2017.
 */
public class MessgeReceiver {
    public static void main(String[] args) {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.<Integer, String>create(props)
                                                            .subscription(Collections.singleton("demo-topic"))
                                                            .assignment(Collections.singleton(new TopicPartition("demo-topic",0)))
                                                            .addAssignListener(receiverPartitions -> receiverPartitions.forEach(
                                                                    receiverPartition -> {
                                                                        System.out.println(receiverPartition);
                                                                        receiverPartition.seekToEnd();}
                                                            ))
                                                            .addRevokeListener(receiverPartitions ->
                                                            receiverPartitions.forEach(receiverPartition ->
                                                                System.out.println(receiverPartition+"--"+receiverPartition.topicPartition())));


        Receiver.<Integer,String>create(receiverOptions)
                .receive()
                .subscribe(record -> {
                    System.out.println(record.record()+"==="+record.offset());
                   record.offset().acknowledge();
                    record.offset().commit();
                });
    }
}
