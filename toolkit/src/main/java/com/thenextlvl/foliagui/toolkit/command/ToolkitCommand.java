package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 工具插件主命令处理器
 * /fgt <subcommand> [args]
 *
 * @author TheNextLvl
 */
public class ToolkitCommand implements CommandExecutor, TabCompleter {

    private final FoliaGUIToolkit plugin;
    private final ListCommand listCommand;
    private final ViewCommand viewCommand;
    private final ExportCommand exportCommand;
    private final CreateCommand createCommand;
    private final EditCommand editCommand;
    private final SaveCommand saveCommand;
    private final DeleteCommand deleteCommand;

    public ToolkitCommand(@NotNull FoliaGUIToolkit plugin) {
        this.plugin = plugin;
        this.listCommand = new ListCommand(plugin);
        this.viewCommand = new ViewCommand(plugin);
        this.exportCommand = new ExportCommand(plugin);
        this.createCommand = new CreateCommand(plugin);
        this.editCommand = new EditCommand(plugin);
        this.saveCommand = new SaveCommand(plugin);
        this.deleteCommand = new DeleteCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        switch (subCommand) {
            case "list":
                return listCommand.execute(sender, subArgs);
            case "view":
                return viewCommand.execute(sender, subArgs);
            case "export":
                return exportCommand.execute(sender, subArgs);
            case "create":
                return createCommand.execute(sender, subArgs);
            case "edit":
                return editCommand.execute(sender, subArgs);
            case "save":
                return saveCommand.execute(sender, subArgs);
            case "delete":
                return deleteCommand.execute(sender, subArgs);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand: " + subCommand);
                sendHelp(sender);
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 补全子命令
            List<String> subCommands = Arrays.asList("list", "view", "export", "create", "edit", "save", "delete", "help");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

            switch (subCommand) {
                case "list":
                    completions.addAll(listCommand.tabComplete(sender, subArgs));
                    break;
                case "view":
                    completions.addAll(viewCommand.tabComplete(sender, subArgs));
                    break;
                case "export":
                    completions.addAll(exportCommand.tabComplete(sender, subArgs));
                    break;
                case "create":
                    completions.addAll(createCommand.tabComplete(sender, subArgs));
                    break;
                case "edit":
                    completions.addAll(editCommand.tabComplete(sender, subArgs));
                    break;
                case "save":
                    completions.addAll(saveCommand.tabComplete(sender, subArgs));
                    break;
                case "delete":
                    completions.addAll(deleteCommand.tabComplete(sender, subArgs));
                    break;
            }
        }

        return completions;
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage("§6=== FoliaGUI Toolkit Help ===");
        sender.sendMessage("§e/fgt list [plugin] §7- List registered GUIs");
        sender.sendMessage("§e/fgt view <gui-id> §7- View GUI configuration");
        sender.sendMessage("§e/fgt export <gui-id> [format] §7- Export GUI (yaml/json/java)");
        sender.sendMessage("§e/fgt create <id> [rows] §7- Create new GUI");
        sender.sendMessage("§e/fgt edit [id] §7- Edit GUI visually");
        sender.sendMessage("§e/fgt save [id] §7- Save GUI to file");
        sender.sendMessage("§e/fgt delete <id> §7- Delete GUI");
        sender.sendMessage("§e/fgt help §7- Show this help");
    }
}