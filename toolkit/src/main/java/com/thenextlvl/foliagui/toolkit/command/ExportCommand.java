package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.manager.GUIExportManager;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * /fgt export <gui-id> [format] - 导出 GUI 配置
 *
 * @author TheNextLvl
 */
public class ExportCommand extends AbstractSubCommand {

    public ExportCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.export")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /fgt export <gui-id> [format]");
            sender.sendMessage("§7Formats: yaml, json, java");
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
            return true;
        }

        // 默认格式为 yaml
        String format = args.length > 1 ? args[1].toLowerCase() : "yaml";

        GUIExportManager exportManager = GUIExportManager.getInstance();
        Collection<String> supportedFormats = exportManager.getSupportedFormats();

        if (!supportedFormats.contains(format)) {
            sender.sendMessage("§cUnsupported format: " + format);
            sender.sendMessage("§7Supported formats: " + String.join(", ", supportedFormats));
            return true;
        }

        try {
            // 导出到插件目录下的 exports 文件夹
            File exportDir = new File(plugin.getDataFolder(), "exports");
            File exportFile = exportManager.exportToDirectory(gui, format, exportDir);

            sender.sendMessage("§aSuccessfully exported GUI: " + guiId);
            sender.sendMessage("§7Format: " + format);
            sender.sendMessage("§7File: " + exportFile.getAbsolutePath());

            // 同时在聊天中显示导出内容
            sender.sendMessage("§e--- Export Content ---");
            String content = exportManager.exportToString(gui, format);

            // 分行发送，避免消息过长
            String[] lines = content.split("\n");
            for (int i = 0; i < Math.min(20, lines.length); i++) {
                sender.sendMessage("§7" + lines[i]);
            }
            if (lines.length > 20) {
                sender.sendMessage("§7... (" + (lines.length - 20) + " more lines)");
                sender.sendMessage("§7See full content in file: " + exportFile.getName());
            }

        } catch (Exception e) {
            sender.sendMessage("§cFailed to export: " + e.getMessage());
            plugin.getLogger().warning("Export failed: " + e.getMessage());
        }

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
        } else if (args.length == 2) {
            GUIExportManager exportManager = GUIExportManager.getInstance();
            Collection<String> formats = exportManager.getSupportedFormats();
            String prefix = args[1].toLowerCase();
            for (String format : formats) {
                if (format.toLowerCase().startsWith(prefix)) {
                    completions.add(format);
                }
            }
        }

        return completions;
    }
}