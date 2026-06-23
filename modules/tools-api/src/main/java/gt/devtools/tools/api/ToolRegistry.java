package gt.devtools.tools.api;

import java.util.*;

/**
 * Singleton registry that discovers all tools at startup via
 * {@link ServiceLoader} and exposes them for the sidebar tree.
 *
 * <h3>Discovery</h3>
 * Call {@link #discover()} once at startup. It scans the module path
 * for {@link ToolProvider} implementations and registers their factories.
 * Groups are registered separately via {@link #registerGroup} — they
 * are simply folder nodes in the sidebar, not tied to module structure.
 *
 * <h3>Thread safety</h3>
 * {@link #discover()} is synchronized. Reads after discovery are
 * lock-free (backed by {@link LinkedHashMap} snapshots).
 */
public final class ToolRegistry {

    private static final ToolRegistry INSTANCE = new ToolRegistry();

    private final List<ToolGroupDescriptor> groups = new ArrayList<>();
    private final Map<String, ToolFactory<?>> tools = new LinkedHashMap<>();
    private final Map<String, List<ToolFactory<?>>> toolsByGroup = new LinkedHashMap<>();
    private final List<Runnable> reloadListeners = new ArrayList<>();
    private boolean loaded;

    public static ToolRegistry getInstance() { return INSTANCE; }

    private ToolRegistry() {}

    // ---------------------------------------------------------------
    // Discovery
    // ---------------------------------------------------------------

    /**
     * Discover all tools via {@link ServiceLoader}. Idempotent — calling
     * a second time is a no-op.
     */
    public synchronized void discover() {
        if (loaded) return;
        var loader = ServiceLoader.load(ToolProvider.class);
        for (var provider : loader) {
            for (var factory : provider.getTools()) {
                register(factory);
            }
        }
        loaded = true;
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    /** Register a tool factory (called automatically during discovery). */
    public void register(ToolFactory<?> factory) {
        var pres = factory.getPresentation();
        tools.put(pres.id(), factory);
        String groupId = pres.groupId();
        if (groupId != null && !groupId.isEmpty()) {
            toolsByGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(factory);
        }
    }

    /** Register a sidebar group (folder). Call before or after {@link #discover()}. */
    public void registerGroup(ToolGroupDescriptor group) {
        groups.add(group);
    }

    // ---------------------------------------------------------------
    // Query
    // ---------------------------------------------------------------

    /** Look up a tool factory by its stable id. */
    public ToolFactory<?> getTool(String id) { return tools.get(id); }

    /** All registered tool factories in insertion order. */
    public List<ToolFactory<?>> getAllTools() { return List.copyOf(tools.values()); }

    /** All registered groups. */
    public List<ToolGroupDescriptor> getGroups() { return List.copyOf(groups); }

    /** Tools belonging to a specific group, or an empty list. */
    public List<ToolFactory<?>> getToolsByGroup(String groupId) {
        return toolsByGroup.getOrDefault(groupId, List.of());
    }

    /** Tool IDs that are not assigned to any group (shown as top-level items). */
    public Set<String> getUngroupedToolIds() {
        Set<String> grouped = new HashSet<>();
        for (var list : toolsByGroup.values()) {
            for (var f : list) grouped.add(f.getPresentation().id());
        }
        Set<String> result = new LinkedHashSet<>(tools.keySet());
        result.removeAll(grouped);
        return result;
    }

    // ---------------------------------------------------------------
    // Reload support (for plugin-style dynamic registration later)
    // ---------------------------------------------------------------

    public void addReloadListener(Runnable listener) { reloadListeners.add(listener); }
    public void notifyReload() { reloadListeners.forEach(Runnable::run); }

    // ---------------------------------------------------------------
    // Types
    // ---------------------------------------------------------------

    /**
     * Descriptor for a folder in the sidebar tree. Groups are purely
     * organisational; tools reference them via {@link ToolPresentation#groupId()}.
     *
     * @param id               unique group id (referenced by tool factories)
     * @param menuTitle        label shown in the sidebar
     * @param detailTitle      label shown in the group-header panel
     * @param weight           sort order (lower = first)
     * @param initiallyExpanded whether the tree folder starts open
     */
    public record ToolGroupDescriptor(
            String id,
            String menuTitle,
            String detailTitle,
            int weight,
            boolean initiallyExpanded
    ) {}
}
