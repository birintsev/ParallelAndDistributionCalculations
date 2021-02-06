package birintsev.secure.atomic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicReference;

public class BoxWriter<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxWriter.class
    );

    private volatile AtomicReference<T> atomicReference;

    private final T item;

    public BoxWriter(AtomicReference<T> writeTo, T writeWhat) {
        this.atomicReference = writeTo;
        this.item = writeWhat;
    }

    @Override
    public void run() {
        final String threadName =
            "[WRITER] " + Thread.currentThread().getName();
        while (!atomicReference.compareAndSet(null, item)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        LOGGER.info(threadName + " has written: " + item);
    }
}
