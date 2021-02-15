package birintsev.concurrentcollections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class BoxReader<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxReader.class
    );

    private volatile Multibox<T> multiBox;

    private List<T> accumulator;

    private int itemsAmountToRead;

    private final String threadName =
        "[READER] " + Thread.currentThread().getName();

    public BoxReader(
        Multibox<T> multiBox,
        int readerPriority,
        int itemsAmountToRead
    ) {
        setPriority(readerPriority);
        this.multiBox = multiBox;
        this.itemsAmountToRead = itemsAmountToRead;
        this.accumulator = new ArrayList<>(itemsAmountToRead);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long duration;

        for (int i = 0; i < itemsAmountToRead; i++) {
            T item;
            synchronized (multiBox) {
                while (multiBox.isEmpty()) {
                    try {
                        multiBox.wait(Multibox.TIMEOUT_TO_WAIT);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
                item = multiBox.get();
            }
            accumulator.add(item);
            //LOGGER.info(threadName + " has read: " + item);
        }

        duration = System.currentTimeMillis() - startTime;

        LOGGER.info(
            threadName
                + " has read all "
                + itemsAmountToRead
                + " items: "
                + accumulator
                + " (duration "
                + duration
                + "ms)"
        );
    }
}
