package locker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class EntityLockerImplTest {

    @Test
    public void testThreadLockUnlockEntity() {
        String entityId = "id";
        EntityLocker<String> locker = new EntityLockerImpl<>();
        locker.lock(entityId);
        locker.unlock(entityId);
    }

    @Test
    public void testThreadWaitingForEntityLock() throws InterruptedException {
        String entityId = "id";
        EntityLocker<String> locker = new EntityLockerImpl<>();

        Thread thread1 = new Thread(() -> {
            locker.lock(entityId);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            locker.unlock(entityId);
        });
        Thread thread2 = new Thread(() -> {
            locker.lock(entityId);
            locker.unlock(entityId);
        });

        thread1.start();
        Thread.sleep(1000);
        thread2.start();
        Thread.sleep(1000);
        Assertions.assertEquals(Thread.State.WAITING,thread2.getState());
    }

    @Test
    public void testReentrantLock() throws InterruptedException {
        String entityId = "id";
        EntityLocker<String> locker = new EntityLockerImpl<>();

        Thread thread1 = new Thread(() -> {
            locker.lock(entityId);
            locker.lock(entityId);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            locker.unlock(entityId);
        });
        thread1.start();
        Thread.sleep(1000);
        Assertions.assertEquals(Thread.State.TIMED_WAITING, thread1.getState());
    }

    @Test
    public void testLockWithTimeout() throws InterruptedException {
        String entityId = "id";
        EntityLocker<String> locker = new EntityLockerImpl<>();

        Thread thread1 = new Thread(() -> {
            try {
                locker.lock(entityId);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                locker.unlock(entityId);
            }
        });
        AtomicBoolean acquired = new AtomicBoolean(false);
        Thread thread2 = new Thread(() -> {
            boolean locked = false;
            try {
                locked = locker.tryLock(entityId, 7000, TimeUnit.MILLISECONDS);
                if (locked) {
                    acquired.set(true);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (locked) {
                    locker.unlock(entityId);
                }
            }
        });
        thread1.start();
        thread2.start();
        Thread.sleep(1000);
        Assertions.assertEquals(Thread.State.TIMED_WAITING, thread2.getState());
        Thread.sleep(5000);
        Assertions.assertTrue(acquired.get());
    }


}