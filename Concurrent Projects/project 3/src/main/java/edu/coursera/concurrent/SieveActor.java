package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.runtime.BaseTask.FinishTask;

import static edu.rice.pcdp.PCDP.finish;

import java.util.concurrent.*;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
    	CounterStruct counter = new CounterStruct();
        SieveActorActor sActor = new SieveActorActor(counter, 2);
        
        finish(() -> {
       	 for (int i=3 ; i<=limit; i+=2) {
	        	sActor.send(i);
	        }
	        sActor.send(0);
       });
        return counter.numberPrimes;
        
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        
    	SieveActorActor nextActor = null;
    	
    	CounterStruct counter;
    	
    	int primeNumber;
    	
        @Override //if there are data races, try synchronized lock. 
        public void process(final Object msg) {
            final int can = (Integer) msg;
            if (can <= 0 ) {
            	counter.numberPrimes++;
            	if (nextActor != null) nextActor.send(msg);
            	else counter.finished = true;
            } else {
            	if ((can % primeNumber) != 0) {
            		if (nextActor == null) {
            			nextActor = new SieveActorActor(counter, can);
            		} else {
            			nextActor.send(can);
            		}
            	}
            }
        }
        public SieveActorActor(CounterStruct counter, int primeNumber) { //By default start from 2
        	super();
        	this.counter = counter;
        	this.primeNumber = primeNumber;
        	
        }
    }
    public static class CounterStruct {
    	int numberPrimes;
    	boolean finished = false;
    }
}
