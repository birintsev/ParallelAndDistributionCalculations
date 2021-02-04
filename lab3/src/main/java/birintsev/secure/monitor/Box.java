package birintsev.secure.monitor;

import java.util.Optional;

public class Box<T> {

    private Optional<T> optional;

    public Box(T item) {
        optional = Optional.ofNullable(item);
    }

    public Box() {
        this(null);
    }

    public T set(T item) {
        T previous = optional.orElse(null);
        optional = Optional.ofNullable(item);
        return previous;
    }

    public T get() {
        T item = optional.orElse(null);
        optional = Optional.empty();
        return item;
    }

    public boolean isEmpty() {
        return !optional.isPresent();
    }

    public T check() {
        return optional.orElse(null);
    }
}
