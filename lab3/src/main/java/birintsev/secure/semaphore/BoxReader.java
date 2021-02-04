package birintsev.secure.semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Semaphore;

public class BoxReader<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxReader.class
    );

    private volatile Semaphore semaphore;

    private volatile Box<T> box;

    public BoxReader(Box<T> readFrom, Semaphore semaphore) {
        this.box = readFrom;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        final String threadName =
            "[READER] " + Thread.currentThread().getName();
        boolean hasRead = false;
        T item;
        while (!hasRead) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            if (box.isEmpty()) {
                try {
                    semaphore.release();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            } else {
                item = box.get();
                hasRead = true;
                semaphore.release();
                LOGGER.info(threadName + " has read: " + item);
            }
        }
    }
}
