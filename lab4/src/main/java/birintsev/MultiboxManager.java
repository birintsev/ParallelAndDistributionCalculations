package birintsev;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.PriorityBlockingQueue;

public class MultiboxManager<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        MultiboxManager.class
    );
    
    private static final int WAIT_TIMEOUT = 500;

    private final Multibox<T> multibox;

    private final WritersManagerThread writersManagerThread;

    private final ReadersManagerThread readersManagerThread;

    private final PriorityBlockingQueue<WritePriorityPair<Integer, T>>
        writeQueue = new PriorityBlockingQueue<>();

    private final PriorityBlockingQueue<ReadPriorityPair<Integer, T>>
        readQueue = new PriorityBlockingQueue<>();

    public MultiboxManager(Multibox<T> multibox) {
        this.multibox = multibox;
        this.writersManagerThread = new WritersManagerThread();
        this.readersManagerThread = new ReadersManagerThread();

        this.writersManagerThread.start();
        this.readersManagerThread.start();
    }

    public void add(T item, int priority) {
        writeQueue.add(new WritePriorityPair<>(priority, item));
        synchronized (writeQueue) {
            writeQueue.notifyAll();
        }
    }

    public T get(int priority) {
        T value;
        ReadPriorityPair<Integer, T> readPriorityPair =
            new ReadPriorityPair<>(priority);
        readQueue.add(readPriorityPair);
        synchronized (readQueue) {
            readQueue.notifyAll();
        }
        synchronized (readPriorityPair) {
            while (!readPriorityPair.isRead()) {
                try {
                    readPriorityPair.wait(WAIT_TIMEOUT);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
        value = readPriorityPair.getValue();
        return value;
    }

    @AllArgsConstructor
    @Getter
    private class WritePriorityPair<P extends Comparable<P>, V>
        implements Comparable<WritePriorityPair<P, V>> {

        private final P priority;

        private final V value;

        @Override
        public int compareTo(WritePriorityPair<P, V> o) {
            return priority.compareTo(o.getPriority());
        }
    }

    @Getter
    private class ReadPriorityPair<P extends Comparable<P>, V>
        implements Comparable<ReadPriorityPair<P, V>> {

        private final P priority;

        private V value;

        private boolean read;

        public ReadPriorityPair(P priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(ReadPriorityPair<P, V> o) {
            return priority.compareTo(o.getPriority());
        }

        public void setValue(V value) {
            if (read) {
                throw new IllegalStateException("The value is already set");
            }
            this.value = value;
            read = true;
        }

        public boolean isRead() {
            return read;
        }
    }

    private class WritersManagerThread extends Thread {

        private final String threadName =
            "[WR_MANAGER] " + Thread.currentThread().getName();

        @Override
        public void run() {
            while (true) {
                T itemToWrite;
                synchronized (writeQueue) {
                    while (writeQueue.isEmpty()) {
                        try {
                            //LOGGER.info(
                            //    threadName
                            //        + " the queue is empty. Waiting..."
                            //);
                            writeQueue.wait(WAIT_TIMEOUT);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                    itemToWrite = writeQueue.poll().getValue();
                }
                synchronized (multibox) {
                    while (multibox.getSize() == multibox.getMaxSize()) {
                        try {
                            //LOGGER.info(
                            //    threadName
                            //        + " the box is full. Waiting..."
                            //);
                            multibox.wait(WAIT_TIMEOUT);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                    multibox.add(itemToWrite);
                    //LOGGER.info(threadName + " has written: " + itemToWrite);
                }
            }
        }
    }

    private final class ReadersManagerThread extends Thread {

        private final String threadName =
            "[RE_MANAGER] " + Thread.currentThread().getName();

        @Override
        public void run() {
            while (true) {
                ReadPriorityPair<?, T> readPriorityPair;
                synchronized (readQueue) {
                    while (readQueue.isEmpty()) {
                        try {
                            //LOGGER.info(
                            //    threadName
                            //        + " the queue is empty. Waiting..."
                            //);
                            readQueue.wait(WAIT_TIMEOUT);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                    readPriorityPair = readQueue.poll();
                }
                synchronized (multibox) {
                    while (multibox.isEmpty()) {
                        try {
                            //LOGGER.info(
                            //    threadName
                            //        + " the box is empty. Waiting..."
                            //);
                            multibox.wait(WAIT_TIMEOUT);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                    readPriorityPair.setValue(multibox.get());
                }
                synchronized (readPriorityPair) {
                    // telling that it's OK, and other threads
                    // may get a result out from the readPriorityPair
                    readPriorityPair.notifyAll();
                }
            }
        }
    }
}
