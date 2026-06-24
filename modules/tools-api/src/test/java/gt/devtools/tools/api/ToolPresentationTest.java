package gt.devtools.tools.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ToolPresentation")
class ToolPresentationTest {

    @Test
    @DisplayName("should create with basic fields")
    void basicConstruction() {
        var pres = new ToolPresentation("my-id", "Menu Title", "Content Title");

        assertThat(pres.id()).isEqualTo("my-id");
        assertThat(pres.menuTitle()).isEqualTo("Menu Title");
        assertThat(pres.contentTitle()).isEqualTo("Content Title");
        assertThat(pres.groupId()).isEmpty();
        assertThat(pres.preferredSelected()).isFalse();
        assertThat(pres.internalTool()).isFalse();
        assertThat(pres.description()).isNull();
    }

    @Test
    @DisplayName("should create via factory method")
    void factoryMethod() {
        var pres = ToolPresentation.of("my-id", "Menu", "Content");

        assertThat(pres.id()).isEqualTo("my-id");
        assertThat(pres.menuTitle()).isEqualTo("Menu");
    }

    @Test
    @DisplayName("should derive with group id via fluent API")
    void withGroupId() {
        var pres = ToolPresentation.of("my-id", "Menu", "Content")
                .withGroupId("my-group");

        assertThat(pres.groupId()).isEqualTo("my-group");
        assertThat(pres.id()).isEqualTo("my-id"); // unchanged
        assertThat(pres.menuTitle()).isEqualTo("Menu"); // unchanged
    }

    @Test
    @DisplayName("should derive with preferred selected via fluent API")
    void withPreferredSelected() {
        var pres = ToolPresentation.of("my-id", "Menu", "Content")
                .withPreferredSelected(true);

        assertThat(pres.preferredSelected()).isTrue();
        assertThat(pres.id()).isEqualTo("my-id"); // unchanged
    }

    @Test
    @DisplayName("should derive with description via fluent API")
    void withDescription() {
        var pres = ToolPresentation.of("my-id", "Menu", "Content")
                .withDescription("Does something useful");

        assertThat(pres.description()).isEqualTo("Does something useful");
    }

    @Test
    @DisplayName("should chain fluent derivations")
    void chainedDerivations() {
        var pres = ToolPresentation.of("my-id", "Menu", "Content")
                .withGroupId("group-a")
                .withPreferredSelected(true)
                .withDescription("A useful tool");

        assertThat(pres.id()).isEqualTo("my-id");
        assertThat(pres.groupId()).isEqualTo("group-a");
        assertThat(pres.preferredSelected()).isTrue();
        assertThat(pres.description()).isEqualTo("A useful tool");
    }

    @Test
    @DisplayName("should be immutable — derivation returns new instance")
    void derivationReturnsNewInstance() {
        var original = ToolPresentation.of("my-id", "Menu", "Content");
        var derived = original.withGroupId("group-a");

        assertThat(derived).isNotSameAs(original);
        assertThat(original.groupId()).isEmpty(); // original unchanged
    }

    @Test
    @DisplayName("should support the 4-arg constructor")
    void fourArgConstructor() {
        var pres = new ToolPresentation("t1", "Menu", "Content", "group-x");

        assertThat(pres.groupId()).isEqualTo("group-x");
        assertThat(pres.preferredSelected()).isFalse();
    }
}
