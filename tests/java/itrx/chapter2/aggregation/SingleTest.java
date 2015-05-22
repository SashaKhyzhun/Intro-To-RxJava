package itrx.chapter2.aggregation;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public class SingleTest {
	
	private class PrintSubscriber extends Subscriber<Object>{
	    private final String name;
	    public PrintSubscriber(String name) {
	        this.name = name;
	    }
	    @Override
	    public void onCompleted() {
	        System.out.println(name + ": Completed");
	    }
	    @Override
	    public void onError(Throwable e) {
	        System.out.println(name + ": Error: " + e);
	    }
	    @Override
	    public void onNext(Object v) {
	        System.out.println(name + ": " + v);
	    }
	}

	public void exampleSingle() {
		Observable<Long> values = Observable.interval(100, TimeUnit.MILLISECONDS);

		values.take(10)
		    .single(v -> v == 5L) // Emits a result
		    .subscribe(new PrintSubscriber("Single1"));
		values
		    .single(v -> v == 5L) // Never emits
		    .subscribe(new PrintSubscriber("Single2"));
		
		// Single1: 5
		// Single1: Completed
	}
	
	public void exampleSingleOrDefault() {
		Observable<Integer> values = Observable.empty();
		
		values
			.singleOrDefault(-1)
			.subscribe(new PrintSubscriber("SingleOrDefault"));
		
		// SingleOrDefault: -1
		// SingleOrDefault: Completed
	}
	
	
	//
	// Tests
	//
	
	@Test
	public void testSingle() {
		TestSubscriber<Long> tester1 = new TestSubscriber<>();
		TestSubscriber<Long> tester2 = new TestSubscriber<>();
		TestScheduler scheduler = Schedulers.test();
		
		Observable<Long> values = Observable.interval(100, TimeUnit.MILLISECONDS, scheduler);

		Subscription s1 = values.take(10)
		    .single(v -> v == 5L) // Emits a result
		    .subscribe(tester1);
		Subscription s2 = values
		    .single(v -> v == 5L) // Never emits
		    .subscribe(tester2);
		
		scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
		
		tester1.assertReceivedOnNext(Arrays.asList(5L));
		tester1.assertTerminalEvent();
		tester1.assertNoErrors();
		tester2.assertReceivedOnNext(Arrays.asList());
		assertEquals(tester2.getOnCompletedEvents().size(), 0);
		tester2.assertNoErrors();
		
		s1.unsubscribe();
		s2.unsubscribe();
	}
	
	@Test
	public void testSingleOrDefault() {
		TestSubscriber<Integer> tester = new TestSubscriber<>();
		
		Observable<Integer> values = Observable.empty();
		
		values
			.singleOrDefault(-1)
			.subscribe(tester);
		
		tester.assertReceivedOnNext(Arrays.asList(-1));
		tester.assertTerminalEvent();
		tester.assertNoErrors();
	}
}

