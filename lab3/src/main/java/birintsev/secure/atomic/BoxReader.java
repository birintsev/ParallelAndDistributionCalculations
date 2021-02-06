package birintsev.secure.atomic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class BoxReader<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxReader.class
    );

    private volatile AtomicReference<T> atomicReference;

    public BoxReader(AtomicReference<T> readFrom) {
        this.atomicReference = readFrom;
    }

    @Override
    public void run() {
        final String threadName =
            "[READER] " + Thread.currentThread().getName();
        while (true) {
            synchronized (atomicReference) {
                if (atomicReference.get() != null) {
                    T item = atomicReference.getAndUpdate(t -> null);
                    LOGGER.info(threadName + " has read: " + item);
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
