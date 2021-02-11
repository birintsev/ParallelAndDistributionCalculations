package birintsev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class BoxReader<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxReader.class
    );

    private volatile Multibox<T> multiBox;

    private int readerPriority;

    private List<T> accumulator;

    private int itemsAmountToRead;

    private final String threadName =
        "[READER] " + Thread.currentThread().getName();

    public BoxReader(
        Multibox<T> multiBox,
        int readerPriority,
        int itemsAmountToRead
    ) {
        this.multiBox = multiBox;
        this.readerPriority = readerPriority;
        this.itemsAmountToRead = itemsAmountToRead;
        this.accumulator = new ArrayList<>(itemsAmountToRead);
    }

    @Override
    public void run() {
        for (int i = 0; i < itemsAmountToRead; i++) {
            T item = multiBox.getManager().get(readerPriority);
            accumulator.add(item);
            LOGGER.info(threadName + " has read: " + item);
        }
        LOGGER.info(
            threadName
                + " has read all "
                + itemsAmountToRead
                + " items: "
                + accumulator
        );
    }

    public int getReaderPriority() {
        return readerPriority;
    }
}
