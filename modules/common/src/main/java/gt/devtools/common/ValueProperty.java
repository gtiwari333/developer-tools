package gt.devtools.common;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A thread-safe observable property that notifies listeners when its value
 * changes. This is the fundamental building block for tool configuration:
 * every persisted setting is backed by a {@code ValueProperty}.
 *
 * <h3>Usage in tool code</h3>
 * <pre>{@code
 * // Register a property via ToolConfiguration (preferred):
 * ValueProperty<Boolean> live = config.register("liveConversion", true);
 *
 * // Listen for changes:
 * live.addListener(p -> {
 *     if (Boolean.TRUE.equals(p.get())) doConversion();
 * });
 *
 * // Read/write:
 * boolean current = live.get();
 * live.set(false);
 * }</pre>
 *
 * <h3>Thread safety</h3>
 * Listeners are stored in a {@link CopyOnWriteArrayList} so reads never
 * block and writes are safe from any thread. Listener callbacks are
 * invoked synchronously on the calling thread — if you need EDT dispatch,
 * do it inside your listener.
 *
 * <h3>Equality</h3>
 * Listeners are only notified when the new value is not
 * {@link Objects#equals} to the old value. Setting the same value
 * repeatedly is a no-op.
 *
 * @param <T> the type of value held by this property
 */
public class ValueProperty<T> {

    private final T defaultValue;
    private T value;
    private final List<Consumer<ValueProperty<T>>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Create a property with the given default (and initial) value.
     *
     * @param defaultValue the initial value and the value restored on {@link #reset()}
     */
    public ValueProperty(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    /** Returns the current value. */
    public T get() {
        return value;
    }

    /**
     * Updates the value and notifies listeners if it changed.
     * Equality is determined by {@link Objects#equals}.
     */
    public void set(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (!Objects.equals(oldValue, newValue)) {
            fireChange();
        }
    }

    /** Returns the default value set at construction time. */
    public T getDefaultValue() {
        return defaultValue;
    }

    /** Resets this property back to its default value. */
    public void reset() {
        set(defaultValue);
    }

    /**
     * Register a listener that is called synchronously whenever the
     * value changes. The listener receives this property so it can
     * call {@link #get()} to read the new value.
     */
    public void addListener(Consumer<ValueProperty<T>> listener) {
        listeners.add(listener);
    }

    /** Remove a previously registered listener. */
    public void removeListener(Consumer<ValueProperty<T>> listener) {
        listeners.remove(listener);
    }

    private void fireChange() {
        for (var listener : listeners) {
            listener.accept(this);
        }
    }
}
