package locker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EntityLockerImpl<ID> implements EntityLocker<ID>{

    private final Map<ID, IdLock> ids = new ConcurrentHashMap<>();

    private final ScheduledExecutorService checkDeadlockExecutor = Executors.newScheduledThreadPool(1);

    private final static long DELAY_SECONDS = 5;
    private final static long PERIOD_SECONDS = 5;

    {
        DeadlockChecker deadlockResolver = new DeadlockChecker();
        checkDeadlockExecutor.scheduleAtFixedRate(() -> deadlockResolver.check(ids), DELAY_SECONDS, PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void lock(ID id) {
        ids.putIfAbsent(id, new IdLock());
        ids.get(id).lock();
    }

    @Override
    public boolean tryLock(ID id, long timeout, TimeUnit unit) throws InterruptedException {
        ids.putIfAbsent(id, new IdLock());
        var lock = ids.get(id);
        return lock.tryLock(timeout, unit);
    }

    @Override
    public void unlock(ID id) {
        var lock = ids.get(id);
        if (lock == null) {
            throw new IllegalMonitorStateException();
        }
        lock.unlock();
    }
}
