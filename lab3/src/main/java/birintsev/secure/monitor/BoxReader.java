package birintsev.secure.monitor;

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
        T item;
        while (true) {
            synchronized (box) {
                if (!box.isEmpty()) {
                    item = box.get();
                    LOGGER.info(threadName + ": has read " + item);
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
