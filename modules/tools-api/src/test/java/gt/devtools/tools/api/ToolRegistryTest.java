package gt.devtools.tools.api;

import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ToolRegistry")
class ToolRegistryTest {

    @Test
    @DisplayName("should be a singleton")
    void isSingleton() {
        var instance1 = ToolRegistry.getInstance();
        var instance2 = ToolRegistry.getInstance();

        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("should register and retrieve a tool factory")
    void registerAndRetrieveTool() {
        var registry = ToolRegistry.getInstance();
        var factory = new TestToolFactory("test-tool", "Test Tool", "Test Tool", "group-a");

        registry.register(factory);

        assertThat(registry.getTool("test-tool")).isSameAs(factory);
    }

    @Test
    @DisplayName("should return all registered tools")
    void getAllTools() {
        var registry = ToolRegistry.getInstance();
        registry.register(new TestToolFactory("tool-1", "Tool 1", "Tool 1", "group-a"));
        registry.register(new TestToolFactory("tool-2", "Tool 2", "Tool 2", "group-b"));

        List<ToolFxFactory<?>> all = registry.getAllTools();

        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        assertThat(all.stream().map(f -> f.getPresentation().id()))
                .contains("tool-1", "tool-2");
    }

    @Test
    @DisplayName("should group tools by group id")
    void getToolsByGroup() {
        var registry = ToolRegistry.getInstance();
        registry.register(new TestToolFactory("tool-a1", "A1", "A1", "group-a"));
        registry.register(new TestToolFactory("tool-a2", "A2", "A2", "group-a"));
        registry.register(new TestToolFactory("tool-b1", "B1", "B1", "group-b"));

        var groupA = registry.getToolsByGroup("group-a");
        var groupB = registry.getToolsByGroup("group-b");
        var empty = registry.getToolsByGroup("nonexistent");

        assertThat(groupA).hasSizeGreaterThanOrEqualTo(2);
        assertThat(groupB).hasSizeGreaterThanOrEqualTo(1);
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("should identify ungrouped tools")
    void getUngroupedToolIds() {
        var registry = ToolRegistry.getInstance();
        registry.register(new TestToolFactory("no-group-tool", "No Group", "No Group", ""));

        Set<String> ungrouped = registry.getUngroupedToolIds();

        assertThat(ungrouped).contains("no-group-tool");
    }

    @Test
    @DisplayName("should return null for unknown tool id")
    void getUnknownToolReturnsNull() {
        var registry = ToolRegistry.getInstance();

        assertThat(registry.getTool("nonexistent-tool-id")).isNull();
    }

    @Test
    @DisplayName("should register and retrieve groups")
    void registerGroup() {
        var registry = ToolRegistry.getInstance();
        var group = new ToolRegistry.ToolGroupDescriptor(
                "test-group", "Test Group", "Test Group Detail", 10, true);

        registry.registerGroup(group);

        assertThat(registry.getGroups()).contains(group);
    }

    @Test
    @DisplayName("should notify reload listeners")
    void reloadListeners() {
        var registry = ToolRegistry.getInstance();
        var counter = new int[1];
        registry.addReloadListener(() -> counter[0]++);

        registry.notifyReload();

        assertThat(counter[0]).isEqualTo(1);
    }

    // -- helper

    private record TestToolFactory(String id, String menuTitle, String contentTitle, String groupId)
            implements ToolFxFactory<DeveloperToolFx> {
        @Override public String getId() { return id; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of(id, menuTitle, contentTitle).withGroupId(groupId);
        }
        /** Never called by registry tests — throws if invoked. */
        @Override public DeveloperToolFx create(gt.devtools.settings.ToolConfiguration config) {
            throw new UnsupportedOperationException("not needed for registry tests");
        }
    }
}
