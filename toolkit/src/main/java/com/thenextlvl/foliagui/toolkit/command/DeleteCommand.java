package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import com.thenextlvl.foliagui.toolkit.editor.EditorManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * /fgt delete <id> - 删除 GUI
 *
 * @author TheNextLvl
 */
public class DeleteCommand extends AbstractSubCommand {

    public DeleteCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.delete")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /fgt delete <id>");
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

        // 检查是否有玩家正在编辑此 GUI
        EditorManager editorManager = plugin.getEditorManager();
        Player editingPlayer = editorManager.getPlayerEditing(guiId);

        if (editingPlayer != null) {
            sender.sendMessage("§cGUI is being edited by: " + editingPlayer.getName());
            sender.sendMessage("§7Ask them to save or close first, or use §e/fgt delete " + guiId + " force§7");
            return true;
        }

        // 确认删除
        if (args.length == 1 || !args[1].equalsIgnoreCase("confirm")) {
            sender.sendMessage("§6Are you sure you want to delete: " + guiId);
            sender.sendMessage("§7This action cannot be undone!");
            sender.sendMessage("§7Type §e/fgt delete " + guiId + " confirm §7to proceed");
            return true;
        }

        // 执行删除
        manager.unregister(guiId);

        sender.sendMessage("§aDeleted GUI: " + guiId);
        sender.sendMessage("§7Note: Exported files are not deleted");
        sender.sendMessage("§7Check " + new File(plugin.getDataFolder(), "saved").getAbsolutePath());

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
            String prefix = args[1].toLowerCase();
            if ("confirm".startsWith(prefix)) {
                completions.add("confirm");
            }
            if ("force".startsWith(prefix)) {
                completions.add("force");
            }
        }

        return completions;
    }
}