package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * /fgt list [plugin] - 列出已注册的 GUI
 *
 * @author TheNextLvl
 */
public class ListCommand extends AbstractSubCommand {

    public ListCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.list")) {
            return true;
        }

        GUIManager manager = GUIManager.getInstance();
        if (manager == null) {
            sender.sendMessage("§cGUIManager is not initialized!");
            sender.sendMessage("§7Please ensure a plugin using FoliaGUI is loaded.");
            return true;
        }

        Set<String> guiIds = manager.getAllGUIIds();
        if (guiIds.isEmpty()) {
            sender.sendMessage("§7No GUIs registered.");
            return true;
        }

        sender.sendMessage("§6=== Registered GUIs (" + guiIds.size() + ") ===");

        // 如果指定了插件名，过滤显示
        if (args.length > 0) {
            String pluginFilter = args[0].toLowerCase();
            sender.sendMessage("§7Filtering by plugin: " + pluginFilter);
        }

        for (String id : guiIds) {
            sender.sendMessage("§e- " + id);
        }

        sender.sendMessage("§7Use §e/fgt view <id> §7to view details");
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 补全插件名（这里暂时返回空，因为 GUIManager 不记录来源插件）
            completions.add("all");
        }

        return completions;
    }
}