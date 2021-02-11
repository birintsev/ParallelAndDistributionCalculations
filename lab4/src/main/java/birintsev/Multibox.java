package birintsev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Multibox<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        Multibox.class
    );

    private List<T> items;

    private int maxSize;

    private volatile MultiboxManager<T> manager;

    public Multibox(int maxSize, T...items) {
        this.maxSize = maxSize;
        this.items = new ArrayList<>(Arrays.asList(items));
        this.manager = new MultiboxManager<>(this);
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
        int lastIndex = items.size() - 1;
        if (isEmpty()) {
            throw new IllegalStateException("The multibox is empty");
        }
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

    public MultiboxManager<T> getManager() {
        return manager;
    }

}
