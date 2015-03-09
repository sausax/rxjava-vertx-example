package com.saurabhsaxena;

import io.vertx.rxcore.java.eventbus.RxEventBus;
import io.vertx.rxcore.java.eventbus.RxMessage;

import org.vertx.java.platform.Verticle;

import rx.Observable;
import rx.functions.Action1;

public class RxJavaTestVerticle extends Verticle{
	
	
	public void start(){
		//Wrapping Vertx event bus into Rx object
		RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());

		
		//Listener for 'foo' vertx topic, replys 'pong!' message 
		rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
		  public void call(RxMessage<String> message) {
		    // Send a single reply
		    message.reply("pong!");
		  }
		});

		rxEventBus.<String>registerHandler("poo").subscribe(new Action1<RxMessage<String>>() {
		  public void call(RxMessage<String> message) {
		    // Send a single reply
		    message.reply("fong!");
		  }
		});
		
		
		//Sending 'ping!' string to 'foo' vertx topic. Expecting a RxMessage
		//(Rx wrapper around Vertx's Message) with string content in it 
		Observable<RxMessage<String>> obs1 = rxEventBus.send("foo", "ping!");

		Observable<RxMessage<String>> obs2 = rxEventBus.send("poo", "fing!");

		
		
		//This line combines stream obs1 and obs2 using a zip function. First two argument of zip function
		// are the two Observable streams that need to combined. Third argument is a 
		// combining function (in Java 8 lambda format). This function takes the output of stream obs1 and obs2
		// as argument and populates ExampleClass object. This creates a new stream of ExampleClass object.
		// For more information http://reactivex.io/RxJava/javadoc/rx/Observable.html#zipWith(rx.Observable,%20rx.functions.Func2)
		Observable<ExampleClass> zippedObs = Observable.zip(obs1, obs2,
				                                                 (val1,val2) -> {
                                                                     ExampleClass ec = new ExampleClass();
                                                                     ec.field1 = val1.body();
                                                                     ec.field2 = val2.body();
                                                                     return ec;
																 }
																);
		// This line attaches a subscriber to ExampleClass stream that we have created in the previous step.
		zippedObs
		.subscribe(
		  new Action1<ExampleClass>() {
		    public void call(ExampleClass message) {
		      // Handle response 
		    	System.out.println("\n\nInside call: \n"+message);
		    }
		  },
		  new Action1<Throwable>() {
		    public void call(Throwable err) {
		     // Handle error
		    }
		  }
		);
	}

}


class ExampleClass {
	
	public String field1;
	public String field2;
	
	public String toString(){
		return "Field1: "+field1 + " Field2: " + field2;
	}
}