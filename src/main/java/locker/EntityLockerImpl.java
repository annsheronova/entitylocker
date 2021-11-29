package locker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class EntityLockerImpl<ID> implements EntityLocker<ID>{

    private final Map<ID, ReentrantLock> ids = new ConcurrentHashMap<>();

    @Override
    public void lock(ID id) {
        ids.putIfAbsent(id, new ReentrantLock());
        ids.get(id).lock();
    }

    @Override
    public boolean tryLock(ID id, long timeout, TimeUnit unit) throws InterruptedException {
        ids.putIfAbsent(id, new ReentrantLock());
        var lock = ids.get(id);
        return lock.tryLock(timeout, unit);
    }

    @Override
    public void unlock(ID id) {
        var lock = ids.get(id);
        if (lock == null || !lock.isHeldByCurrentThread()) {
            throw new UnsupportedOperationException();
        }
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
