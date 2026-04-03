package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * /fgt view <gui-id> - 查看 GUI 配置详情
 *
 * @author TheNextLvl
 */
public class ViewCommand extends AbstractSubCommand {

    public ViewCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.view")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /fgt view <gui-id>");
            return true;
        }

        GUIManager manager = GUIManager.getInstance();
        if (manager == null) {
            sender.sendMessage("§cGUIManager is not initialized!");
            return true;
        }

        String guiId = args[0];
        GUI gui = manager.getGUI(guiId);

        if (gui == null) {
            sender.sendMessage("§cGUI not found: " + guiId);
            sender.sendMessage("§7Use §e/fgt list §7to see registered GUIs");
            return true;
        }

        // 显示 GUI 信息
        sender.sendMessage("§6=== GUI: " + guiId + " ===");

        Inventory inv = gui.getInventory();
        if (inv != null) {
            sender.sendMessage("§eSize: §7" + inv.getSize() + " slots (" + (inv.getSize() / 9) + " rows)");
            sender.sendMessage("§eType: §7" + inv.getType().name());
        }

        // 显示槽位内容概览
        if (inv != null) {
            sender.sendMessage("§eContents:");
            int itemCount = 0;
            for (int i = 0; i < inv.getSize(); i++) {
                if (inv.getItem(i) != null && !inv.getItem(i).getType().isAir()) {
                    itemCount++;
                }
            }
            sender.sendMessage("§7  " + itemCount + " items placed");

            // 显示前几个槽位的物品
            sender.sendMessage("§7  Sample slots:");
            for (int i = 0; i < Math.min(9, inv.getSize()); i++) {
                var item = inv.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                            ? item.getItemMeta().getDisplayName()
                            : item.getType().name();
                    sender.sendMessage("§7    [" + i + "] " + name);
                }
            }
        }

        sender.sendMessage("§7Use §e/fgt export " + guiId + " yaml §7to export config");
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            GUIManager manager = GUIManager.getInstance();
            if (manager != null) {
                Set<String> guiIds = manager.getAllGUIIds();
                String prefix = args[0].toLowerCase();
                for (String id : guiIds) {
                    if (id.toLowerCase().startsWith(prefix)) {
                        completions.add(id);
                    }
                }
            }
        }

        return completions;
    }
}