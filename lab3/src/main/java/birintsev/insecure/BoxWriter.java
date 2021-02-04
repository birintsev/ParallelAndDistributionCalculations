package birintsev.insecure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxWriter<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxWriter.class
    );

    private final Box<T> box;

    private final T item;

    public BoxWriter(Box<T> writeTo, T writeWhat) {
        this.box = writeTo;
        this.item = writeWhat;
    }

    @Override
    public void run() {
        final String threadName =
            "[WRITER] " + Thread.currentThread().getName();
        while (!box.isEmpty()) {
            LOGGER.info(threadName + " the box is not empty. Waiting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        LOGGER.info(threadName + " put to the box is: " + item);
    }
}
