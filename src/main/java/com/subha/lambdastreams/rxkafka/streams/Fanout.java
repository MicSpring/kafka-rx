package com.subha.lambdastreams.rxkafka.streams;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

/**
 * Created by user on 5/9/2017.
 */
public class Fanout {
    public static void main(String[] args){

       EmitterProcessor<String> emitterProcessor = EmitterProcessor.create();
        BlockingSink<String> blockingSink = emitterProcessor.connectSink();

        emitterProcessor.onNext("i AM fINEEE");
        emitterProcessor.subscribe(s -> System.out.println("Fine for:"+s));
       // emitterProcessor.onNext("i AM fINEEE");

        emitterProcessor.subscribe(s -> System.out.println("Hmmm:"+s));

       Flux<BlockingSink.Emission> blockingSinkFlux =
               Flux.<Integer>range(1,3).map(integer -> blockingSink.emit(String.valueOf(integer)));

      /* Flux<String> stringFlux = emitterProcessor.publishOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
               .doOnNext(s -> System.out.println("Data is:"+s))
               .map(s -> {
           System.out.println("The Data is:"+s);
           return s+"Processed";});*/

        blockingSinkFlux.subscribe(s -> System.out.println(s));

    }
}
