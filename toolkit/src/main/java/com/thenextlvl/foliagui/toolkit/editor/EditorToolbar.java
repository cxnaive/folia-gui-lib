package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 编辑工具栏
 * <p>
 * 显示在玩家背包最后一行（slots 27-35）
 * 提供编辑工具功能
 *
 * @author TheNextLvl
 */
public class EditorToolbar {

    private final FoliaGUIToolkit plugin;
    private final EditorSession session;

    /**
     * 工具定义 - 固定 9 个槽位
     */
    private static final ToolDefinition[] TOOLS = {
            new ToolDefinition(0, Material.CHEST, "§e物品库", "§7选择预设物品"),
            new ToolDefinition(1, Material.DIAMOND, "§b组件库", "§7选择组件类型"),
            new ToolDefinition(2, Material.WRITABLE_BOOK, "§a编辑模式", "§7点击物品编辑属性"),
            new ToolDefinition(3, Material.BARRIER, "§c删除模式", "§7点击物品删除"),
            new ToolDefinition(4, Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ", ""),
            new ToolDefinition(5, Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ", ""),
            new ToolDefinition(6, Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ", ""),
            new ToolDefinition(7, Material.KNOWLEDGE_BOOK, "§f帮助", "§7查看使用说明"),
            new ToolDefinition(8, Material.RED_DYE, "§c关闭编辑", "§7结束编辑会话")
    };

    public EditorToolbar(@NotNull FoliaGUIToolkit plugin, @NotNull EditorSession session) {
        this.plugin = plugin;
        this.session = session;
    }

    /**
     * 渲染工具栏到玩家背包最后一行（slots 27-35）
     */
    public void renderToPlayerInventory() {
        Player player = session.getPlayer();

        for (int i = 0; i < 9; i++) {
            int slot = 27 + i; // 玩家背包最后一行
            ItemStack item = createToolItem(i);
            player.getInventory().setItem(slot, item);
        }
    }

    /**
     * 创建工具物品
     *
     * @param index 工具索引（0-8）
     * @return 物品
     */
    private ItemStack createToolItem(int index) {
        ToolDefinition tool = TOOLS[index];

        if (tool.material == Material.AIR || tool.material == Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
            return ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build();
        }

        ItemBuilder builder = ItemBuilder.of(tool.material)
                .name(tool.name);

        if (!tool.description.isEmpty()) {
            builder.lore(tool.description);
        }

        // 状态指示
        EditorSession.EditorMode mode = session.getMode();

        if (tool.material == Material.BARRIER && mode == EditorSession.EditorMode.DELETE) {
            builder.lore("", "§c★ 删除模式已开启");
        }

        if (tool.material == Material.WRITABLE_BOOK && mode == EditorSession.EditorMode.SELECT) {
            builder.lore("", "§a★ 编辑模式已开启，点击物品编辑");
        }

        return builder.build();
    }

    /**
     * 处理工具点击
     *
     * @param toolIndex 工具索引（0-8）
     */
    public void handleClick(int toolIndex) {
        if (toolIndex < 0 || toolIndex >= 9) return;

        ToolDefinition tool = TOOLS[toolIndex];
        Player player = session.getPlayer();

        switch (tool.material) {
            // 物品库
            case CHEST -> {
                plugin.getItemLibrary().openLibrary(player, session);
            }

            // 组件库
            case DIAMOND -> {
                plugin.getComponentLibrary().openLibrary(player, session);
            }

            // 编辑模式
            case WRITABLE_BOOK -> {
                if (session.getMode() == EditorSession.EditorMode.SELECT) {
                    session.setMode(EditorSession.EditorMode.NORMAL);
                    player.sendMessage("§e编辑模式已关闭");
                } else {
                    session.setMode(EditorSession.EditorMode.SELECT);
                    player.sendMessage("§a编辑模式已开启，点击物品进行编辑");
                }
            }

            // 删除模式
            case BARRIER -> {
                if (session.getMode() == EditorSession.EditorMode.DELETE) {
                    session.setMode(EditorSession.EditorMode.NORMAL);
                    player.sendMessage("§e删除模式已关闭");
                } else {
                    session.setMode(EditorSession.EditorMode.DELETE);
                    player.sendMessage("§c删除模式已开启，点击物品删除");
                }
            }

            // 帮助
            case KNOWLEDGE_BOOK -> {
                showHelp(player);
            }

            // 关闭编辑
            case RED_DYE -> {
                session.applyChanges();
                session.close();
                plugin.getEditorManager().endSession(player);
            }

            // 空槽位
            default -> {
                // 忽略点击
            }
        }
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(@NotNull Player player) {
        player.sendMessage("§e========== 编辑器帮助 ==========");
        player.sendMessage("§e物品库 §7- 选择预设物品放到光标");
        player.sendMessage("§b组件库 §7- 选择组件类型");
        player.sendMessage("§a编辑模式 §7- 点击编辑区域的物品进行属性编辑");
        player.sendMessage("§c删除模式 §7- 点击编辑区域的物品删除");
        player.sendMessage("§f帮助 §7- 显示此帮助信息");
        player.sendMessage("§c关闭编辑 §7- 结束编辑会话并保存更改");
        player.sendMessage("§e=================================");
    }

    /**
     * 工具定义
     */
    private static class ToolDefinition {
        final int index;
        final Material material;
        final String name;
        final String description;

        ToolDefinition(int index, @NotNull Material material, @NotNull String name, @NotNull String description) {
            this.index = index;
            this.material = material;
            this.name = name;
            this.description = description;
        }
    }
}