package birintsev.secure.semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Semaphore;

public class BoxWriter<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxWriter.class
    );

    private volatile Semaphore semaphore;

    private volatile Box<T> box;

    private final T item;

    public BoxWriter(Box<T> writeTo, T writeWhat, Semaphore semaphore) {
        this.box = writeTo;
        this.item = writeWhat;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        final String threadName =
            "[WRITER] " + Thread.currentThread().getName();
        boolean hasWritten = false;
        while (!hasWritten) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            if (!box.isEmpty()) {
                try {
                    semaphore.release();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            } else {
                box.set(item);
                hasWritten = true;
                semaphore.release();
                LOGGER.info(threadName + " has written: " + item);
            }
        }
    }
}
