package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import com.thenextlvl.foliagui.toolkit.editor.EditorSession;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * /fgt edit [id] - 编辑现有 GUI
 * <p>
 * 使用 EditorSession 系统进行可视化编辑
 *
 * @author TheNextLvl
 */
public class EditCommand extends AbstractSubCommand {

    public EditCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.edit")) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;
        GUIManager manager = GUIManager.getInstance();

        if (manager == null) {
            sender.sendMessage("§cGUIManager 未初始化！");
            return true;
        }

        // 如果没有指定 ID，显示正在编辑的会话或让用户选择
        if (args.length == 0) {
            EditorSession session = plugin.getEditorManager().getSession(player);

            if (session != null) {
                GUI currentEdit = session.getTargetGUI();
                sender.sendMessage("§6你正在编辑: " + currentEdit.getId());
                sender.sendMessage("§7使用 §e/fgt save §7保存更改");
                sender.sendMessage("§7使用 §e/fgt edit <id> §7编辑其他 GUI");

                // 重新打开编辑器界面
                session.open();
                return true;
            }

            // 显示可用 GUI 列表
            Set<String> guiIds = manager.getAllGUIIds();
            if (guiIds.isEmpty()) {
                sender.sendMessage("§7没有已注册的 GUI。使用 §e/fgt create §7创建一个。");
                return true;
            }

            sender.sendMessage("§6选择要编辑的 GUI:");
            for (String id : guiIds) {
                sender.sendMessage("§e- " + id);
            }
            sender.sendMessage("§7使用 §e/fgt edit <id> §7选择");
            return true;
        }

        String guiId = args[0];
        GUI gui = manager.getGUI(guiId);

        if (gui == null) {
            sender.sendMessage("§c未找到 GUI: " + guiId);
            sender.sendMessage("§7使用 §e/fgt list §7查看已注册的 GUI");
            return true;
        }

        // 创建编辑会话
        EditorSession session = plugin.getEditorManager().createSession(player, gui);

        sender.sendMessage("§a正在编辑 GUI: " + guiId);
        sender.sendMessage("§7工具栏显示在编辑界面最后一行");
        sender.sendMessage("§7关闭后使用 §e/fgt save " + guiId + " §7保存");

        // 打开编辑器（包含工具栏）
        session.open();

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