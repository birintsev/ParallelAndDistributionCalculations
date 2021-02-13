package birintsev.secure.semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Semaphore;

public class BoxReader<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxReader.class
    );

    private volatile Semaphore boxWasWritten;

    private volatile Semaphore boxWasRead;

    private volatile Box<T> box;

    public BoxReader(
        Box<T> readFrom,
        Semaphore boxWasWritten,
        Semaphore boxWasRead
    ) {
        this.box = readFrom;
        this.boxWasWritten = boxWasWritten;
        this.boxWasRead = boxWasRead;
    }

    @Override
    public void run() {
        final String threadName =
            "[READER] " + Thread.currentThread().getName();
        T item;
        try {
            boxWasWritten.acquire();
            item = box.get();
            LOGGER.info(threadName + " has read: " + item);
            boxWasRead.release();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
