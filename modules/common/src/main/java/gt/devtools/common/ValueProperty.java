package gt.devtools.common;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A simple observable property container with change listeners.
 * <p>
 * Replaces IntelliJ's {@code ObservableMutableProperty} with a plain Java
 * implementation suitable for Swing desktop apps.
 *
 * @param <T> the value type
 */
public class ValueProperty<T> {

    private final T defaultValue;
    private T value;
    private final List<Consumer<ValueProperty<T>>> listeners = new CopyOnWriteArrayList<>();

    public ValueProperty(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (!Objects.equals(oldValue, newValue)) {
            fireChange();
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        set(defaultValue);
    }

    public void addListener(Consumer<ValueProperty<T>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<ValueProperty<T>> listener) {
        listeners.remove(listener);
    }

    private void fireChange() {
        for (var listener : listeners) {
            listener.accept(this);
        }
    }
}
