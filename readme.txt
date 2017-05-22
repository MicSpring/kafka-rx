Reactive Glimpse-Part 1

A peek into Observables. <br/>

<b>Reactive Approach/Principles</b> has unfurled new dimensions of programming to us. <b>Reactive Manifesto</b> being the key, paved the way to approach things with a different eye and with its multilingual dominion we have Rx's omnipresence.<br/><br/>

For me it all started with RxJava and RxGroovy and now with ProjectReactor providing Specification to build Reactive Systems, Spring too incorporating Reactive principles in its 5th edition and together with Java 9 flow, it is evident that Reactive is gaining both strength and momentum.<br/><br/>
Even different tools/api now extend their reactive support, be it JDBC, RabbitMQ, Kafka etc.<br/><br/>

My interaction with the Reactive Dimension has been limited to Observables, Observers/Subscribers accompanied by several other operators. Thats fairly little I can understand and the justification being I am JUST A BEGINNER HERE.<br/><br/>

From what I understood till now is Observables are the Data Sources and it is through Observers/Subscribers we fetch the data out of them. These Observers/Subscribers are much like several terminal operations we perform with Java 8 Streams. <br/><br/>
What makes me think is how the data flows from Observables to Observers/Subscribers once we subscribe.<br/>
Here we are going to take quick look at it.<br/><br/>

Lets take an Example first:<br/><br/>

<pre>
 Observable.fromIterable(Arrays.asList("11111", "222", "33"))
</pre>

and now when we subscribe to this Observable using some Observers/Subscribers we start getting values in the onNext() method. <br/>
But how does it happen?<br/><br/>

Under the hood there is a component known as <b>RxJavaPlugins</b>, which plays a vital role here.<br/>
This Component provides hooks to the various Lifecycle events of Observables, with the help of various functions like: <b>onObservableAssembly, onFlowableAssembly</b> etc.<br/><br/>
RxJavaPlugins provides a lot of ultility functionalities. We will discuss them in our upcoming posts.<br/><br/>
So when we try to create an Observable (here For example from an Iterable), we end up getting a specific Observable Type which here is: <b>ObservableFromIterable</b>. Similarly, when we use <b>just(T item)</b> we get <b>ObservableJust</b> and hence these follows whenever we create or apply operations on the Observable. <br/><br/>
So for every creator function of Observable we end up in getting a Specific type and this pattern even follows when we apply any operator on any ObservableSource.<br/><br/>
For Example when we apply <b>subscribeOn()</b> operator to any ObservableSource we get <b>ObservableSubscribeOn.</b><br/>
This pattern follows for others operator too. So there lies a generic uniformity across the API.<br/><br/>

<b>RxJavaPlugins</b> then acts on these specific Observable types and returns types specific to the corresponding Lifecycle hook function if present.<br/><br/>
Since the method used by RxJavaPlugins here is <b>onAssembly()</b> so the Lifecycle hook function used here is <b>onObservableAssembly</b>.<br/><br/>

I think the following Sequence diagram will help us to understand the situation better.<br/><br/>

Now when we subscribe for that Specific Observable type, then the <b>subscribeActual()</b> method gets called. The <b>Observers/Subscribers</b> that we use while subscribing gets passed to this method.<br/><br/>
This method does the real magic.<br/><br/>
Within this method we create an entity of type <b>Disposable</b> and we pass this disposable to the <b>onSubscribe()</b> method of the <b>Subscriber/Observer( passed as a parmeter)</b> to the subscribeActual() method and then finally we call some method specific to that disposable.<br/><br/>
This method (belonging to the Disposable type as created in the above step) iterates through the source and passes one value at a time to the onNext() method of the Subscriber/Observer( passed as a parmeter), if error arises then onError() method is invoked otherwise onComplete() method is called finally.<br/><br/>

The Sequence Diagram for this flow will be: <br/><br/>

This is the basic flow which describes how data flows from the ObservableSource to its corresponding Subscriber.<br/><br/>
