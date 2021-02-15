package birintsev.concurrentcollections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;

public class BoxWriter<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxWriter.class
    );

    final String threadName = "[WRITER] " + Thread.currentThread().getName();

    private volatile Multibox<T> multiBox;

    private Collection<T> itemsToWrite;

    public BoxWriter(
        Multibox<T> writeTo,
        Collection<T> itemsToWrite,
        int writerPriority
    ) {
        setPriority(writerPriority);
        this.multiBox = writeTo;
        this.itemsToWrite = new ArrayList<>(itemsToWrite);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long duration;

        for (T item : itemsToWrite) {
            synchronized (multiBox) {
                while (multiBox.isFull()) {
                    try {
                        multiBox.wait(Multibox.TIMEOUT_TO_WAIT);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
                multiBox.add(item);
            }
        }

        duration = System.currentTimeMillis() - startTime;

        LOGGER.info(
            threadName
                + " has written all items (duration "
                + duration
                + "ms)"
        );
    }
}
