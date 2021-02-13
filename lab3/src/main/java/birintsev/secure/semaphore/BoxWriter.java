package birintsev.secure.semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Semaphore;

public class BoxWriter<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxWriter.class
    );

    private volatile Semaphore boxWasWritten;

    private volatile Semaphore boxWasRead;

    private volatile Box<T> box;

    private final T item;

    public BoxWriter(
        Box<T> writeTo,
        T writeWhat,
        Semaphore boxWasWritten,
        Semaphore boxWasRead
    ) {
        this.box = writeTo;
        this.item = writeWhat;
        this.boxWasWritten = boxWasWritten;
        this.boxWasRead = boxWasRead;
    }

    @Override
    public void run() {
        final String threadName =
            "[WRITER] " + Thread.currentThread().getName();
        try {
            boxWasRead.acquire();
            box.set(item);
            LOGGER.info(threadName + " has written: " + item);
            boxWasWritten.release();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
