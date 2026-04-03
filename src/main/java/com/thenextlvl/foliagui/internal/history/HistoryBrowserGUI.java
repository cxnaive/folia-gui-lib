package com.thenextlvl.foliagui.internal.history;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.history.GUIHistory;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * GUI历史浏览界面
 * <p>
 * 可视化展示玩家的GUI导航历史，支持点击跳转
 *
 * @author TheNextLvl
 */
public class HistoryBrowserGUI {

    private final GUIHistoryImpl history;
    private final Player player;
    private GUI browserGUI;

    public HistoryBrowserGUI(@NotNull GUIHistoryImpl history) {
        this.history = history;
        this.player = history.getPlayer();
    }

    /**
     * 打开历史浏览界面
     */
    public void open() {
        List<GUIHistory.HistoryEntry> entries = history.getAllEntries();

        // 计算需要的行数
        int rows = Math.max(3, Math.min(6, (int) Math.ceil((entries.size() + 9) / 9.0)));

        GUIBuilder builder = GUIBuilder.create("history-browser")
                .plugin(getPlugin())
                .title("§8§l历史记录")
                .rows(rows);

        // 边框
        builder.border(Material.GRAY_STAINED_GLASS_PANE);

        // 添加历史条目
        int slot = 10;
        int currentPos = history.getCurrentPosition();

        for (int i = 0; i < entries.size() && slot < rows * 9 - 9; i++) {
            GUIHistory.HistoryEntry entry = entries.get(i);
            boolean isCurrent = (i == currentPos);

            ItemStack item = createEntryItem(entry, isCurrent, i);
            final int position = i;

            builder.button(slot, item, event -> {
                // 跳转到选中的历史
                history.goTo(position);
                event.setCancelled(true);
            });

            slot++;
            // 跳过边框
            if ((slot + 1) % 9 == 0) {
                slot += 2;
            }
        }

        // 后退按钮
        builder.button(rows * 9 - 6,
                ItemBuilder.of(Material.ARROW)
                        .name(isCurrentPageBack() ? "§e← 后退" : "§7← 后退 (不可用)")
                        .lore(
                                history.canGoBack() ? "§7返回上一个菜单" : "§c没有可返回的菜单",
                                "",
                                "§7当前位置: §f" + (currentPos + 1) + "/" + entries.size()
                        )
                        .build(),
                event -> {
                    if (history.canGoBack()) {
                        history.goBack();
                    } else {
                        player.sendMessage("§c没有可返回的菜单");
                    }
                    event.setCancelled(true);
                }
        );

        // 前进按钮
        builder.button(rows * 9 - 4,
                ItemBuilder.of(Material.ARROW)
                        .name(history.canGoForward() ? "§e前进 →" : "§7前进 → (不可用)")
                        .lore(
                                history.canGoForward() ? "§7前往下一个菜单" : "§c没有可前进的菜单"
                        )
                        .build(),
                event -> {
                    if (history.canGoForward()) {
                        history.goForward();
                    } else {
                        player.sendMessage("§c没有可前进的菜单");
                    }
                    event.setCancelled(true);
                }
        );

        // 关闭按钮
        builder.button(rows * 9 - 5,
                ItemBuilder.of(Material.BARRIER)
                        .name("§c关闭")
                        .lore("§7关闭历史浏览")
                        .build(),
                event -> {
                    player.closeInventory();
                    event.setCancelled(true);
                }
        );

        // 清除历史按钮
        if (!entries.isEmpty()) {
            builder.button(rows * 9 - 8,
                    ItemBuilder.of(Material.REDSTONE_BLOCK)
                            .name("§c清除历史")
                            .lore("§7清除所有历史记录")
                            .build(),
                    event -> {
                        history.clear();
                        player.sendMessage("§a历史记录已清除");
                        // 关闭浏览界面
                        player.closeInventory();
                        event.setCancelled(true);
                    }
            );
        }

        // 信息按钮
        builder.button(rows * 9 - 2,
                ItemBuilder.of(Material.BOOK)
                        .name("§e历史信息")
                        .lore(
                                "§7总记录数: §f" + entries.size(),
                                "§7当前位置: §f" + (currentPos + 1),
                                "§7最大记录数: §f" + history.getMaxSize(),
                                "",
                                "§7点击条目可跳转"
                        )
                        .build(),
                event -> event.setCancelled(true)
        );

        browserGUI = builder.build();
        browserGUI.open(player);
    }

    /**
     * 创建历史条目物品
     */
    private ItemStack createEntryItem(GUIHistory.HistoryEntry entry, boolean isCurrent, int index) {
        Material material = isCurrent ? Material.LIME_STAINED_GLASS_PANE : Material.PAPER;
        String prefix = isCurrent ? "§a► " : "§7";

        String timeStr = formatTime(entry.getTimestamp());

        ItemBuilder builder = ItemBuilder.of(material)
                .name(prefix + entry.getTitle())
                .lore(
                        "§7位置: §f#" + (index + 1),
                        "§7时间: §f" + timeStr,
                        "",
                        isCurrent ? "§a当前菜单" : "§e点击跳转"
                );

        // 如果有图标，使用图标
        if (entry.getIcon() != null && entry.getIcon().getType() != Material.AIR) {
            builder = ItemBuilder.of(entry.getIcon().getType())
                    .name(prefix + entry.getTitle())
                    .lore(
                            "§7位置: §f#" + (index + 1),
                            "§7时间: §f" + timeStr,
                            "",
                            isCurrent ? "§a当前菜单" : "§e点击跳转"
                    );
        }

        return builder.build();
    }

    private boolean isCurrentPageBack() {
        return history.canGoBack();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    private org.bukkit.plugin.Plugin getPlugin() {
        GUIManager manager = GUIManager.getInstance();
        return manager != null ? manager.getPlugin() : null;
    }
}
