package com.thenextlvl.foliagui.internal.config;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.config.GUIConfigLoader;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

/**
 * YAML 配置加载器
 * 从 YAML 文件加载 GUI 配置
 *
 * @author TheNextLvl
 */
public class YamlConfigLoader implements GUIConfigLoader {

    @Override
    public @NotNull GUI load(@NotNull File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return load(fis);
        }
    }

    @Override
    public @NotNull GUI load(@NotNull InputStream inputStream) throws IOException {
        Map<String, Object> config = parseSimpleYaml(inputStream);
        return loadFromMap(config);
    }

    @Override
    public @NotNull GUI loadFromMap(@NotNull Map<String, Object> config) {
        String id = getString(config, "id", "unknown");
        GUIBuilder builder = GUIBuilder.create(id);

        // 基础配置
        String title = getString(config, "title", "GUI");
        builder.title(title);

        int rows = getInt(config, "rows", 3);
        builder.rows(rows);

        // 边框
        if (getBoolean(config, "border", false)) {
            String borderMaterial = getString(config, "border-material", "BLACK_STAINED_GLASS_PANE");
            try {
                builder.border(Material.valueOf(borderMaterial.toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.border(Material.BLACK_STAINED_GLASS_PANE);
            }
        }

        // 按钮
        Object buttonsObj = config.get("buttons");
        if (buttonsObj instanceof List) {
            loadButtons(builder, (List<?>) buttonsObj);
        }

        // 装饰
        Object decorationsObj = config.get("decorations");
        if (decorationsObj instanceof List) {
            loadDecorations(builder, (List<?>) decorationsObj);
        }

        // 分页
        Object paginationObj = config.get("pagination");
        if (paginationObj instanceof Map) {
            loadPagination(builder, (Map<String, Object>) paginationObj);
        }

        return builder.build();
    }

    @Override
    public @Nullable GUI loadFromResource(@NotNull String resourcePath, @NotNull ClassLoader classLoader) {
        try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            return load(is);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[FoliaGUI] Failed to load GUI from resource: " + resourcePath);
            return null;
        }
    }

    @Override
    public @NotNull String[] getSupportedExtensions() {
        return new String[]{"yml", "yaml"};
    }

    private void loadButtons(GUIBuilder builder, List<?> buttons) {
        for (Object obj : buttons) {
            if (!(obj instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> buttonConfig = (Map<String, Object>) obj;

            int slot = getInt(buttonConfig, "slot", 0);
            ItemStack item = createItem(buttonConfig);

            String action = getString(buttonConfig, "action", null);
            if (action != null) {
                builder.button(slot, item, event -> {
                    executeAction(event.getPlayer(), action);
                    event.setCancelled(true);
                });
            } else {
                builder.button(slot, item);
            }
        }
    }

    private void loadDecorations(GUIBuilder builder, List<?> decorations) {
        for (Object obj : decorations) {
            if (!(obj instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> decoConfig = (Map<String, Object>) obj;

            int slot = getInt(decoConfig, "slot", 0);
            ItemStack item = createItem(decoConfig);
            builder.decoration(slot, item);
        }
    }

    private void loadPagination(GUIBuilder builder, Map<String, Object> pagination) {
        // 分页配置
        Object slotsObj = pagination.get("slots");
        if (slotsObj instanceof List) {
            List<?> slotsList = (List<?>) slotsObj;
            int[] slots = new int[slotsList.size()];
            for (int i = 0; i < slotsList.size(); i++) {
                slots[i] = ((Number) slotsList.get(i)).intValue();
            }

            // 物品列表需要动态提供
            builder.pagination(slots, new ArrayList<>());
        }

        int prevSlot = getInt(pagination, "prev-slot", -1);
        int nextSlot = getInt(pagination, "next-slot", -1);
        if (prevSlot >= 0 && nextSlot >= 0) {
            builder.paginationNav(prevSlot, nextSlot);
        }

        int indicatorSlot = getInt(pagination, "indicator-slot", -1);
        if (indicatorSlot >= 0) {
            builder.paginationIndicator(indicatorSlot);
        }
    }

    private ItemStack createItem(Map<String, Object> config) {
        String materialStr = getString(config, "material", "STONE");
        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }

        ItemBuilder builder = ItemBuilder.of(material);

        String name = getString(config, "name", null);
        if (name != null) {
            builder.name(name.replace('&', '§'));
        }

        Object loreObj = config.get("lore");
        if (loreObj instanceof List) {
            List<String> lore = new ArrayList<>();
            for (Object line : (List<?>) loreObj) {
                lore.add(line.toString().replace('&', '§'));
            }
            builder.lore(lore.toArray(new String[0]));
        }

        int amount = getInt(config, "amount", 1);
        if (amount > 1) {
            builder.amount(amount);
        }

        if (getBoolean(config, "glow", false)) {
            builder.glow();
        }

        int customModelData = getInt(config, "custom-model-data", 0);
        if (customModelData > 0) {
            builder.customModelData(customModelData);
        }

        return builder.build();
    }

    private void executeAction(org.bukkit.entity.Player player, String action) {
        if (action == null || action.isEmpty()) return;

        String lower = action.toLowerCase().trim();

        if (lower.equals("close")) {
            player.closeInventory();
        } else if (lower.startsWith("tell ")) {
            player.sendMessage(action.substring(5).replace('&', '§'));
        } else if (lower.startsWith("command ")) {
            player.performCommand(action.substring(8));
        } else if (lower.startsWith("console ")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.substring(8));
        }
    }

    // ==================== 简单 YAML 解析器 ====================

    private Map<String, Object> parseSimpleYaml(InputStream is) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();

                if (value.startsWith("[") && value.endsWith("]")) {
                    // 简单列表
                    value = value.substring(1, value.length() - 1);
                    List<String> list = new ArrayList<>();
                    for (String item : value.split(",")) {
                        String trimmed = item.trim();
                        if (!trimmed.isEmpty()) {
                            list.add(parseValue(trimmed).toString());
                        }
                    }
                    result.put(key, list);
                } else {
                    result.put(key, parseValue(value));
                }
            }
        }

        return result;
    }

    private Object parseValue(String value) {
        if (value.isEmpty()) return "";
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        if (value.equalsIgnoreCase("null")) return null;

        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // 移除引号
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    // ==================== 辅助方法 ====================

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}