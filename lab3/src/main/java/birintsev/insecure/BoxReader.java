package birintsev.insecure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxReader<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxReader.class
    );

    private volatile Box<T> box;

    public BoxReader(Box<T> readFrom) {
        this.box = readFrom;
    }

    @Override
    public void run() {
        final String threadName =
            "[READER] " + Thread.currentThread().getName();
        while (box.isEmpty()) {
            LOGGER.info(threadName + " the box is empty. Waiting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        LOGGER.info(
            threadName + " read from box: " + box.get()
        );
    }
}
