package com.subha.reactivestreams.mono

import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.time.Duration
/**
 * Created by NM9 on 5/9/2017.
 */
class MonoTest {

    @Test
    public void delayedSubscription(){

        Flux<String> helloPauseWorld = Mono.just("Hello")
                .concatWith(Mono.just("world"))
                .delay(Duration.ofMillis(3000l))
                .flatMap({s -> Mono.just(s)})

        helloPauseWorld.toIterable().forEach({s -> println s})
       // helloPauseWorld.publishOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(3))).subscribe({ s -> println s})

    }

    @Test
    public void firstEmitting() {
        Mono<String> a = Mono.just("oops I'm late")
                .delaySubscriptionMillis(450);
        Flux<String> b = Flux.just("let's get", "the party", "started")
                .delayMillis(400);

        Flux.firstEmitting(a, b)
                .toIterable()
                .forEach(System.out.&println);
    }
}
