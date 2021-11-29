package locker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * IdLock handles entity locking
 * reentrantLock - lock
 * waitingThread - threads waiting for the lock realising
 * currentThread - thread acquired lock
 * localGlobalId - global id for calculating local id
 * localId - id for LockId identification
 * deadlockFlag - true if deadlock happened
 */
public class IdLock {
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Set<Thread> waitingThreads = new HashSet<>();
    private Thread currentThread = null;
    private static int localGlobalId = 0;
    private int localId = 0;
    private boolean deadlockFlag = false;

    {
        localId = localGlobalId++;
    }

    /**
     * Thread tries to lock entity
     * Thread waits if the entity locked by another thread
     * While waiting thread tries to acquire lock if there was no deadlock
     * If deadlock was, it exits from waiting block
     */
    public void lock() {
        deadlockFlag = false;
        waitingThreads.add(Thread.currentThread());
        boolean locked = false;
        while (!locked) {
            locked = reentrantLock.tryLock();
            if (deadlockFlag) {
                return;
            }
        }
        currentThread = Thread.currentThread();
        waitingThreads.remove(Thread.currentThread());
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return reentrantLock.tryLock(timeout, unit);
    }

    /**
     * Thread releases lock
     * Throws IllegalMonitorException if another thread tries to unlock entity and there was no deadlock
     */
    public void unlock() {
        if (!reentrantLock.isHeldByCurrentThread() && !deadlockFlag) {
            throw new IllegalMonitorStateException();
        }
        if (reentrantLock.isHeldByCurrentThread()) {
            currentThread = null;
            reentrantLock.unlock();
        }
    }

    public int getLocalId() {
        return localId;
    }

    public Set<Thread> getWaitingThreads() {
        return waitingThreads;
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public void setDeadlockFlag(boolean flag) {
        deadlockFlag = flag;
    }
}
