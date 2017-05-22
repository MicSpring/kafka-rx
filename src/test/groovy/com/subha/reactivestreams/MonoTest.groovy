package com.subha.reactivestreams

import com.subha.lambdastreams.LambdaTest
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiPredicate
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import org.junit.Assert
import org.junit.Test
import org.reactivestreams.Subscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
/**
 * Created by NM9 on 5/9/2017.
 */
class MonoTest {

    Closure<Integer> func1 = {String str -> str.length()}
    Closure<Integer> func2 = { str -> str.length()}

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

        ResourceSubscriber resourceSubscriber1 =[onNext: {Object o -> println "The data 1 is:$o"},
                                                 onError:{Throwable t -> t.printStackTrace()},
                                                 onComplete: { println "The data 1 Completed"}] as ResourceSubscriber


        ResourceSubscriber resourceSubscriber2 =[onNext: {Object o -> println "The data 2 is:$o"},
                                                 onError:{Throwable t -> t.printStackTrace()},
                                                 onComplete: { println "The data 2 Completed"}] as ResourceSubscriber

        Subscriber[] subsarr = [resourceSubscriber1,resourceSubscriber2] as Subscriber[]

        Flowable.range(1,10)
                .parallel()
                .flatMap({x->
            println "Mappping $x for ${Thread.currentThread()}"
            Flowable.just(x*x)})
                .subscribe(subsarr)

        // println val -- when called method parallelism()
        /*.sequential()
        .subscribe({x-> println "Mappping $x for ${Thread.currentThread()}"})*/

        TimeUnit.SECONDS.sleep(5l)

    }

    @Test
    public void retryWhenTest(){

        /* TestObserver testObserver = */Observable.fromIterable(Arrays.asList("Subha", "Puchu", "Mi"))
                .map({s -> s.substring(0,3)})
                .doOnNext({val -> println "The Value after retryWhennn is $val"})
                .doOnComplete({-> println "Its Completeddddd"})
                .doOnError({throwable -> println "The Exceptionnn is:$throwable"})
                .retryWhen({errors -> errors.zipWith(io.reactivex.Observable.range(1,3),
                {err, i -> i})
                .flatMap({interval ->
            println "The Interval is:$interval"
            interval > 3? io.reactivex.Observable.error(new Exception("Exiting")):io.reactivex.Observable.timer(interval, TimeUnit.SECONDS)})})
                .doOnError({throwable -> println "The Exception is:$throwable"})
                .doOnNext({val -> println "The Value after retryWhen is $val"})
                .doOnComplete({-> println "Its Completed"})
                .subscribe({val -> println "The Value is:$val"},
                {throwable -> throwable.printStackTrace()},
                {-> println "Completed"})

        //testObserver.assertError(AssertionError)

        TimeUnit.SECONDS.sleep(12l)
    }

    @Test
    public void testObserverTest(){
        TestObserver<String> observer = new TestObserver<String>();

        io.reactivex.functions.BiPredicate<? super Integer, ? super Throwable> pred = [test: { count, err -> println "count:$count Err:$err"
            if(count>3)
                return false
            else
                return true }] as io.reactivex.functions.BiPredicate<? super String, ? super Throwable>

        Observable.interval(3l, TimeUnit.SECONDS).flatMap({val ->Observable.fromIterable(["Subha", "Puchu", "Mi"]).take(val)})
                .map({s ->
            s.substring(0,2)})
        //.retry(pred)
                .retry({count, err -> println "count:$count Err:$err"
            if(count>3)
                return false
            else
                return true} as BiPredicate<Integer, Throwable>)
                .subscribe(/*{val -> println "The val is:$val"},
                {throwable -> throwable.printStackTrace()}*/observer)

        TimeUnit.SECONDS.sleep(12l)

        observer.assertValueCount(6)
    }

    @Test
    public void testClosure()
    {
        Assert.assertEquals  func1('Subha'), 5
        Assert.assertEquals func2('Subha'), 5
    }

    @Test
    public void testGetMeLength(){
        Assert.assertEquals  LambdaTest.getMeLength({str -> str.length()}, "Donald Duck"), 11
        Assert.assertEquals  LambdaTest.getMeLength(func1, "Donald Duck"), 11
        Assert.assertEquals  LambdaTest.getMeLength(func2, "Donald Duck"), 11
    }

    @Test
    public void mapFilterReduce(){
        def filter = {
            println "Filtering on ${Thread.currentThread()}"
            it%2 ==0}
        def map = { it * 2}
        def reduce = { sum, val -> sum + val}


        rx.Observable.from(1..5)
                .filter(filter)
                .map(map)
                .reduce(reduce).subscribe{ println it}

        rx.Observable.from(1..11)
                .groupBy(filter)
                .flatMap({numbers ->
            println "FlatMap on ${Thread.currentThread()}"
            numbers.reduce(0, {count, num -> count+1})
                    .map({cnt->
                //numbers.key? "Even:$cnt":"Odd:$cnt"
                [(numbers.key?"EVEN":"ODD"):cnt]
            })
        })
        /**
         * This reduce operation consolidates the data in a Single Observable<Map>, rather than
         * Observable<Map> for each GroupedObservable
         */
                .reduce([:],{Map oddsEvens, Map obs -> oddsEvens+=obs})
                .observeOn(rx.schedulers.Schedulers.computation())
        //.flatMap({str -> str})
                .subscribe({it ->
            println "Subscribe on ${Thread.currentThread()}"
            println it})
        //.subscribe  {val -> val.toList().subscribe({intList -> println intList})}

        TimeUnit.SECONDS.sleep(20l)

    }

    @Test
    public void backPressure(){

        rx.Observable.from(["One", "Two", "Three", "Four", "Five"])
        //.takeUntil({s -> s.equalsIgnoreCase("THreE")})
                .takeUntil(rx.Observable.from([]))
                .subscribe(System.out.&println,
                {throwable -> throwable.printStackTrace()},
                {-> println "Done"})

        TimeUnit.SECONDS.sleep(5l)

    }

    @Test
    public void testRxJavaHooksOnCreate(){

        /**
         * Needs to properly Implemented.
         */

        //RxJavaPlugins.onAssembly(Observable.fromArray([1,2,3] as Integer[]))

        Observable<String> stringObservable = Observable.<String>fromIterable(["Mic", "Subha", "Puchu"])//Observable.range(1,5)//

        Observer o = new Observer() {
            Disposable disposable

            @Override
            void onSubscribe(@NonNull Disposable d) {
                println "Setting disposable $d"
                this.disposable = d
            }

            @Override
            void onNext(@NonNull Object o) {
                println "The Data is:$o"
                if(((String)o).equals("Subha")) {
                    println "P Present"
                    disposable.dispose()
                }
            }

            @Override
            void onError(@NonNull Throwable e) {
                println "The error is:$e"
            }

            @Override
            void onComplete() {
                println "Hence Completed!"
            }
        }

        stringObservable.subscribe(o)

    }

    @Test
    public void testConcurrency(){

        rx.Observable.range(1,5)
                //.subscribeOn(rx.schedulers.Schedulers.from(Executors.newFixedThreadPool(3)))

        .observeOn(rx.schedulers.Schedulers.from(Executors.newFixedThreadPool(3)))
                .map({val ->
            println "Maps: ${ val +1} at ${Thread.currentThread()}"
            val +1})
                .flatMap({integer -> rx.Observable.just(integer)
                .subscribeOn(rx.schedulers.Schedulers.io())
        //.observeOn(rx.schedulers.Schedulers.computation())
         //.observeOn(rx.schedulers.Schedulers.io())
                .map({val->
            println "$val at ${Thread.currentThread()}"
            val*val})
        })
             //   .subscribeOn(rx.schedulers.Schedulers.io())
         .observeOn(rx.schedulers.Schedulers.computation())
        //.observeOn(rx.schedulers.Schedulers.from(Executors.newFixedThreadPool(3)))
                .subscribe({output -> println "Output: $output at ${Thread.currentThread()}\n\n"})


        TimeUnit.SECONDS.sleep(6l)
    }
}
