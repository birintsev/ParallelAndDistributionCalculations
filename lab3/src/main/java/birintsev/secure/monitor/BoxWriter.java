package birintsev.secure.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxWriter<T> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BoxWriter.class
    );

    private volatile Box<T> box;

    private final T item;

    public BoxWriter(Box<T> writeTo, T writeWhat) {
        this.box = writeTo;
        this.item = writeWhat;
    }

    @Override
    public void run() {
        final String threadName =
            "[WRITER] " + Thread.currentThread().getName();
        while (true) {
            synchronized (box) {
                if (box.isEmpty()) {
                    box.set(item);
                    LOGGER.info(threadName + ": has written " + item);
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
