package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.manager.GUIExportManager;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import com.thenextlvl.foliagui.toolkit.editor.EditorSession;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * /fgt save [id] - 保存 GUI 到文件
 * <p>
 * 使用 EditorSession 系统获取正在编辑的 GUI
 *
 * @author TheNextLvl
 */
public class SaveCommand extends AbstractSubCommand {

    public SaveCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.save")) {
            return true;
        }

        GUIManager manager = GUIManager.getInstance();
        if (manager == null) {
            sender.sendMessage("§cGUIManager 未初始化！");
            return true;
        }

        // 确定要保存的 GUI
        GUI gui = null;
        String guiId = null;

        if (args.length > 0) {
            guiId = args[0];
            gui = manager.getGUI(guiId);
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            EditorSession session = plugin.getEditorManager().getSession(player);
            if (session != null) {
                // 先应用编辑会话的更改
                session.applyChanges();
                gui = session.getTargetGUI();
                guiId = gui.getId();
            }
        }

        if (gui == null || guiId == null) {
            if (args.length == 0 && !(sender instanceof Player)) {
                sender.sendMessage("§c用法: /fgt save <id>");
            } else {
                sender.sendMessage("§c没有 GUI 可保存。请指定 ID 或处于编辑模式。");
            }
            return true;
        }

        // 导出 GUI 到多个格式
        GUIExportManager exportManager = GUIExportManager.getInstance();
        File exportDir = new File(plugin.getDataFolder(), "saved");

        try {
            // 保存为 YAML（默认）
            File yamlFile = exportManager.exportToDirectory(gui, "yaml", exportDir);
            sender.sendMessage("§a已保存 GUI: " + guiId);
            sender.sendMessage("§7YAML: " + yamlFile.getName());

            // 同时保存为 Java Builder 格式
            File javaDir = new File(exportDir, "java");
            File javaFile = exportManager.exportToDirectory(gui, "java", javaDir);
            sender.sendMessage("§7Java: " + javaFile.getName());

            // 结束编辑会话
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getEditorManager().endSession(player);
            }

            sender.sendMessage("§7文件保存位置: " + exportDir.getAbsolutePath());

        } catch (Exception e) {
            sender.sendMessage("§c保存失败: " + e.getMessage());
            plugin.getLogger().warning("保存失败: " + e.getMessage());
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
        }

        return completions;
    }
}