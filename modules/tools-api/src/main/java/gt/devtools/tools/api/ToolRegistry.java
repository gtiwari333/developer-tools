package gt.devtools.tools.api;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central registry that discovers tools via {@link ServiceLoader} and
 * manages the tool tree for the sidebar.
 */
public final class ToolRegistry {

    private static final ToolRegistry INSTANCE = new ToolRegistry();

    private final List<ToolGroupDescriptor> groups = new ArrayList<>();
    private final Map<String, ToolFactory<?>> tools = new LinkedHashMap<>();
    private final Map<String, List<ToolFactory<?>>> toolsByGroup = new LinkedHashMap<>();
    private final List<Runnable> reloadListeners = new CopyOnWriteArrayList<>();
    private boolean loaded;

    public static ToolRegistry getInstance() {
        return INSTANCE;
    }

    private ToolRegistry() {}

    /**
     * Discover all tools via ServiceLoader. Idempotent.
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

    public void register(ToolFactory<?> factory) {
        var pres = factory.getPresentation();
        tools.put(pres.id(), factory);

        String groupId = pres.groupId();
        if (groupId != null && !groupId.isEmpty()) {
            toolsByGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(factory);
        }
    }

    public void registerGroup(ToolGroupDescriptor group) {
        groups.add(group);
    }

    public ToolFactory<?> getTool(String id) {
        return tools.get(id);
    }

    public List<ToolFactory<?>> getAllTools() {
        return List.copyOf(tools.values());
    }

    public List<ToolGroupDescriptor> getGroups() {
        return List.copyOf(groups);
    }

    public List<ToolFactory<?>> getToolsByGroup(String groupId) {
        return toolsByGroup.getOrDefault(groupId, List.of());
    }

    public Set<String> getUngroupedToolIds() {
        Set<String> grouped = new HashSet<>();
        for (var list : toolsByGroup.values()) {
            for (var f : list) grouped.add(f.getPresentation().id());
        }
        Set<String> result = new LinkedHashSet<>(tools.keySet());
        result.removeAll(grouped);
        return result;
    }

    public void addReloadListener(Runnable listener) {
        reloadListeners.add(listener);
    }

    public void notifyReload() {
        for (var listener : reloadListeners) {
            listener.run();
        }
    }

    /**
     * Descriptor for a tool group shown as a folder in the sidebar tree.
     */
    public record ToolGroupDescriptor(
            String id,
            String menuTitle,
            String detailTitle,
            int weight,
            boolean initiallyExpanded
    ) {}
}
