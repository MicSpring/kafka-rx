package com.subha.reactivestreams

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.time.Duration
import java.util.concurrent.TimeUnit
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

    @Test
    public void parallelismTest(){
        Flowable.range(1,10)
                .subscribeOn(Schedulers.computation())
                //
        .map({x ->
            println Thread.currentThread()
            x*x})
                .observeOn(Schedulers.io())
             //   .subscribeOn(Schedulers.computation())
        .subscribe({x -> println "${Thread.currentThread()} For $x"})

        /**
         * The Sleep can be replaced with blockingSubscribe
         */
        TimeUnit.SECONDS.sleep(5l)
    }

    @Test
    public void subscriberTest(){
        Flowable.range(1,10)
        .subscribeOn(Schedulers.computation())
        .subscribe(new ResourceSubscriber<Integer>() {

            @Override
            protected void onStart(){
                request(4)
            }

            @Override
            void onNext(Integer integer) {
                println "$integer in ${Thread.currentThread()}"
               // request(4)
            }

            @Override
            void onError(Throwable t) {
                t.printStackTrace()
            }

            @Override
            void onComplete() {
                println "Completed"
            }
        })

        TimeUnit.SECONDS.sleep(5l)
    }

    @Test
    public void parallelFlowable() {
        Flowable.range(1,10)
        .parallel()
        .flatMap({x->
                    println "Mappping $x for ${Thread.currentThread()}"
            Flowable.just(x*x)})
        .sequential()
        .subscribe({x-> println "Mappping $x for ${Thread.currentThread()}"})

        TimeUnit.SECONDS.sleep(5l)

    }
}
