package birintsev;

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

    private int writerPriority;

    public BoxWriter(
        Multibox<T> writeTo,
        Collection<T> itemsToWrite,
        int writerPriority
    ) {
        this.multiBox = writeTo;
        this.itemsToWrite = new ArrayList<>(itemsToWrite);
        this.writerPriority = writerPriority;
    }

    @Override
    public void run() {
        MultiboxManager<T> manager = multiBox.getManager();
        for (T item : itemsToWrite) {
            manager.add(item, writerPriority);
        }
    }
}
