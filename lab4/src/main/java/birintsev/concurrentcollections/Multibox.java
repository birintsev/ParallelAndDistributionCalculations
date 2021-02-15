package birintsev.concurrentcollections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class Multibox<T> {

    public static final int TIMEOUT_TO_WAIT = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(
        Multibox.class
    );

    private CopyOnWriteArrayList<T> items;

    private int maxSize;

    public Multibox(int maxSize, T...items) {
        this.maxSize = maxSize;
        this.items = new CopyOnWriteArrayList<>(Arrays.asList(items));
    }

    public void add(T item) {
        if (items.size() == maxSize) {
            throw new IllegalStateException(
                "The box already contents maxSize (" + maxSize + ") items"
            );
        }
        items.add(item);
    }

    public T get() {
        T item;
        int lastIndex;
        if (isEmpty()) {
            throw new IllegalStateException("The multibox is empty");
        }
        lastIndex = items.size() - 1;
        item = items.get(lastIndex);
        items.remove(lastIndex);
        return item;
    }

    public int getSize() {
        return items.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isFull() {
        return items.size() == maxSize;
    }
}
