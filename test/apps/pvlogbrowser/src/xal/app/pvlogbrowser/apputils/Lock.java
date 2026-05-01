/*
 * Lock.java
 *
 * Created on August 1, 2003, 1:21 PM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.apputils;


/**
 * Lock allows an alternative method to Java's <code>synchronize</code> feature
 * for locking resources from other threads.  The reason for this alternative is
 * to provide the ability to test for a lock without waiting.  Lock has methods
 * for both waiting for a lock and testing for a lock.  If a lock is held by 
 * the same thread requesting it, the lock is immediately granted.  Every lock
 * must be balanced by a call to <code>unlock()</code>.  The lock is freed only 
 * when all locks have been relinquished.
 *
 * @author  tap
 */
public class Lock {
    /** number of locks held by a single thread */
    private volatile int numLocks;
    
    /** thread holding the lock */
    private volatile Thread lockingThread;
    
    /** used to synchronize multiple blocking lock requests */
    private Object _lock;
    
    /** used to synchronize any lock request */
    private Object _trialLock;

    
    /** Creates a new instance of Lock */
    public Lock() {
        numLocks = 0;
        _lock = new Object();
        _trialLock = new Object();
    }
    
    
    /**
     * Request a lock and wait until the lock is granted.  If the request comes 
     * from the same thread holding the lock, this method simply increments the 
     * lock count and grants the lock immediately.  Every call to this
     * method must be balanced with a call to <code>unlock()</code> to 
     * relinquish the lock.  
     * @see #unlock
     * @return true if the lock has been granted and false if the lock failed
     */
    public boolean lock() {
        synchronized(_lock) {
            if ( tryLock() ) {
                return true;
            }
            else {
                try {
                    _lock.wait();
                    return lock();
                }
                catch(InterruptedException exception) {
                    System.err.println(exception);
                    return false;
                }
            }
        }
    }
    
    
    /**
     * Relinquish the lock.  This method must be called by the same thread that 
     * currently holds the lock.
     */
    public void unlock() {
        synchronized(_trialLock) {
            if ( canUnlock() ) {      // test to make sure we are allowed to unlock
                synchronized(_lock) {
                    --numLocks;
                    if ( numLocks == 0 ) {
                        _lock.notify();
                    }
                }
            }
        }
    }
    
    
    /**
     * Try to get a lock.  If the request is successful then hold the lock.
     * Whether or not the request is successful, this method will always 
     * return immediately.  Every call to this method that is successful (and
     * only those that are successful) should be balanced by a call to unlock.
     * A typical call looks like: <br>
     * <code>if( lock.tryLock() ) { <br>
     *  // some code here...  <br>
     *  lock.unlock(); <br>
     * } 
     * </code>
     * @return true if the lock was successful and false otherwise.
     */
    public boolean tryLock() {
        synchronized(_trialLock) {
            if ( canLock() ) {
                    holdLock();
                    return true;
            }
            else {
                return false;
            }
        }
    }
    
    
    /**
     * Establish and hold a lock.
     */
    private void holdLock() {
        ++numLocks;
        lockingThread = Thread.currentThread();
    }
    
    
    /**
     * Test whether a lock can be granted.  If there is currently no lock or
     * if the lock is requested from the same thread currently holding the lock
     * then the lock can be granted.
     * @return true if there is no lock or if the lock is from the same thread holding the lock and false otherwise.
     */
    private boolean canLock() {
        return numLocks == 0 || lockingThread == Thread.currentThread();
    }
    
    
    /**
     * Test whether the current thread can unlock the lock.  Checks the current
     * thread is the thread holding the lock and that there is at least one 
     * lock being held.
     * @return true if the current thread holds this lock and there is at least one lock being held and false otherwise.
     */
    private boolean canUnlock() {
        return numLocks > 0 && lockingThread == Thread.currentThread();
    }
}
