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
        AtomicBoolean waiting = new AtomicBoolean(false);
        Thread thread2 = new Thread(() -> {
            waiting.set(true);
            locker.lock(entityId);
            waiting.set(false);
            locker.unlock(entityId);
        });

        thread1.start();
        Thread.sleep(1000);
        thread2.start();
        Thread.sleep(1000);
        Assertions.assertTrue(waiting.get());
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
    public void testTryLockAcquireSuccess() throws InterruptedException {
        String entityId = "id";
        EntityLocker<String> locker = new EntityLockerImpl<>();

        Thread thread1 = new Thread(() -> {
            try {
                locker.lock(entityId);
                Thread.sleep(3000);
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
                locked = locker.tryLock(entityId, 5000, TimeUnit.MILLISECONDS);
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
        Thread.sleep(1000);
        thread2.start();
        Thread.sleep(5000);
        Assertions.assertTrue(acquired.get());
    }


    @Test
    public void testTryLockAcquireFail() throws InterruptedException {
        String entityId = "id";
        EntityLocker<String> locker = new EntityLockerImpl<>();

        Thread thread1 = new Thread(() -> {
            try {
                locker.lock(entityId);
                Thread.sleep(6000);
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
                locked = locker.tryLock(entityId, 1000, TimeUnit.MILLISECONDS);
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
        Thread.sleep(1000);
        thread2.start();
        Thread.sleep(5000);
        Assertions.assertFalse(acquired.get());
    }

    @Test
    public void testDeadlockException() throws InterruptedException {
        String id1 = "id1";
        String id2 = "id2";
        String id3 = "id3";
        EntityLocker<String> locker = new EntityLockerImpl<>();
        Thread thread1 = new Thread(() -> {
            try {
                locker.lock(id1);
                Thread.sleep(2000);
                locker.lock(id2);
                locker.unlock(id2);
            } catch (InterruptedException | IllegalMonitorStateException e) {
                e.printStackTrace();
            } finally {
                locker.unlock(id1);

            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                locker.lock(id2);
                Thread.sleep(2000);
                locker.lock(id3);
                locker.unlock(id3);
            } catch (InterruptedException | IllegalMonitorStateException e) {
                e.printStackTrace();
            } finally {
                locker.unlock(id2);

            }
        });

        Thread thread3 = new Thread(() -> {
            try {
                locker.lock(id3);
                Thread.sleep(2000);
                locker.lock(id1);
                locker.unlock(id1);
            } catch (InterruptedException | IllegalMonitorStateException e) {
                e.printStackTrace();
            } finally {
                locker.unlock(id3);

            }
        });
        thread1.start();
        Thread.sleep(1000);
        thread2.start();
        Thread.sleep(1000);
        thread3.start();

    }

}