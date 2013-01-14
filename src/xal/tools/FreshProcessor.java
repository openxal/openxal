//
//  FreshProcessor.java
//  xal
//
//  Created by Tom Pelaia on 5/22/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.util.concurrent.*;


/** process on a separate thread pending requests dropping any previous ones */
public class FreshProcessor {
	/** pending requests waiting to be processed */
	final private ArrayBlockingQueue<Runnable> REQUEST_QUEUE;
	
	/** indicates whether the processor should keep running */
	private volatile boolean _keepRunning;
	
	
	/** Constructor */
	public FreshProcessor() {
		_keepRunning = true;
		
		REQUEST_QUEUE = new ArrayBlockingQueue<Runnable>( 1 );
		new Thread( new RequestProcessor() ).start();
	}
	
	
	/** Clear pending requests */
	synchronized public void clear() {
		REQUEST_QUEUE.clear();
	}
	
	
	/** Stop processing pending requests */
	synchronized public void terminate() {
		_keepRunning = false;
		post( new EmptyRequest() );
	}
	
	
	/**
	 * Post a new request to be processed replacing any pending request.
	 * @param request Runnable request to be processed
	 */
	synchronized public boolean post( final Runnable request ) {
		try {
			REQUEST_QUEUE.clear();
			REQUEST_QUEUE.put( request );
			return true;
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return false;
		}
	}
	
	
	/** Perform post processing */
	protected void postProcess() throws Exception {}
	
	
	
	/** Process runner task */
	private class RequestProcessor extends Thread {
		public void run() {
			while ( _keepRunning ) {
				try {
					final Runnable request = REQUEST_QUEUE.take();
					request.run();
					postProcess();
				}
				catch( Exception exception ) {
					exception.printStackTrace();
				}
			}
		}
	}	
}



/** Empty Request used during termination */
class EmptyRequest implements Runnable {
	public void run() {}
}

