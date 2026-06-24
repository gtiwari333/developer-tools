package gt.devtools.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValueProperty")
class ValuePropertyTest {

    @Test
    @DisplayName("should return default value on construction")
    void defaultValueOnConstruction() {
        var prop = new ValueProperty<>("default");
        assertThat(prop.get()).isEqualTo("default");
        assertThat(prop.getDefaultValue()).isEqualTo("default");
    }

    @Test
    @DisplayName("should return updated value after set")
    void setUpdatesValue() {
        var prop = new ValueProperty<>("initial");
        prop.set("updated");
        assertThat(prop.get()).isEqualTo("updated");
    }

    @Test
    @DisplayName("should not notify listeners when value is unchanged")
    void setSameValueNoNotification() {
        var prop = new ValueProperty<>("hello");
        List<String> notifications = new ArrayList<>();
        prop.addListener(p -> notifications.add(p.get()));

        prop.set("hello");

        assertThat(notifications).isEmpty();
    }

    @Test
    @DisplayName("should notify all listeners when value changes")
    void setDifferentValueNotifiesListeners() {
        var prop = new ValueProperty<>("old");
        List<String> notifications = new ArrayList<>();
        prop.addListener(p -> notifications.add(p.get()));
        prop.addListener(p -> notifications.add(p.get() + "-2"));

        prop.set("new");

        assertThat(notifications).containsExactly("new", "new-2");
    }

    @Test
    @DisplayName("should provide the property itself to listeners")
    void listenerReceivesPropertyItself() {
        var prop = new ValueProperty<>(42);
        List<ValueProperty<Integer>> received = new ArrayList<>();
        prop.addListener(received::add);

        prop.set(99);

        assertThat(received).hasSize(1);
        assertThat(received.get(0)).isSameAs(prop);
        assertThat(received.get(0).get()).isEqualTo(99);
    }

    @Test
    @DisplayName("should allow removing a listener")
    void removeListener() {
        var prop = new ValueProperty<>("x");
        List<String> notifications = new ArrayList<>();
        var listener = (java.util.function.Consumer<ValueProperty<String>>) p -> notifications.add(p.get());
        prop.addListener(listener);

        prop.set("y");
        assertThat(notifications).containsExactly("y");

        prop.removeListener(listener);
        prop.set("z");
        assertThat(notifications).containsExactly("y"); // no new notification
    }

    @Test
    @DisplayName("should reset to default value")
    void resetRestoresDefault() {
        var prop = new ValueProperty<>(10);
        prop.set(20);
        assertThat(prop.get()).isEqualTo(20);

        prop.reset();
        assertThat(prop.get()).isEqualTo(10);
    }

    @Test
    @DisplayName("should notify on reset if value changed")
    void resetNotifiesIfChanged() {
        var prop = new ValueProperty<>(10);
        prop.set(20);
        List<Integer> notifications = new ArrayList<>();
        prop.addListener(p -> notifications.add(p.get()));

        prop.reset();

        assertThat(notifications).containsExactly(10);
    }

    @Test
    @DisplayName("should not notify on reset if value unchanged")
    void resetNoNotificationIfUnchanged() {
        var prop = new ValueProperty<>(10);
        List<Integer> notifications = new ArrayList<>();
        prop.addListener(p -> notifications.add(p.get()));

        prop.reset();

        assertThat(notifications).isEmpty();
    }

    @Test
    @DisplayName("should work with null values")
    void nullValues() {
        var prop = new ValueProperty<String>(null);
        assertThat(prop.get()).isNull();

        prop.set("not null");
        assertThat(prop.get()).isEqualTo("not null");

        prop.set(null);
        assertThat(prop.get()).isNull();
    }

    @Test
    @DisplayName("should work with Boolean values")
    void booleanValues() {
        var prop = new ValueProperty<>(false);
        assertThat(prop.get()).isFalse();

        prop.set(true);
        assertThat(prop.get()).isTrue();

        prop.reset();
        assertThat(prop.get()).isFalse();
    }

    @Test
    @DisplayName("should be thread-safe for concurrent reads")
    void concurrentAccess() throws Exception {
        var prop = new ValueProperty<>(0);
        int threadCount = 4;
        var threads = new Thread[threadCount];
        var errors = new ArrayList<Exception>();

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        prop.set(j);
                        prop.get(); // concurrent read
                    }
                } catch (Exception e) {
                    errors.add(e);
                }
            });
        }

        for (var t : threads) t.start();
        for (var t : threads) t.join();

        assertThat(errors).isEmpty();
    }
}
