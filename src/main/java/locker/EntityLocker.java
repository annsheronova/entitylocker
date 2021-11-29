package locker;

import java.util.concurrent.TimeUnit;

public interface EntityLocker<ID> {

    /**
     * Locks an entity with given id
     * Waits if an entity is already locked by another thread
     *
     * @param id entity id
     */
    void lock(ID id);

    /**
     * Tries to acquire lock with given timeout
     *
     * @param id entity id
     * @param timeout time for waiting
     * @param unit time unit
     * @return
     * @throws InterruptedException
     */
    boolean tryLock(ID id, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Releases lock
     * @param id entity id
     */
    void unlock(ID id);

}
