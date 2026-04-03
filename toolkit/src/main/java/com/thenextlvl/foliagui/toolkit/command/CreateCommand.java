package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import com.thenextlvl.foliagui.toolkit.editor.EditorManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /fgt create <id> [type] [rows] - 创建新的 GUI
 * <p>
 * 支持的类型：
 * - chest: 箱子（可设置行数1-6，最常用）
 * - hopper: 漏斗（5槽位，适合小型菜单）
 * - dispenser: 发射器（9槽位，3x3布局）
 * - dropper: 投掷器（9槽位，3x3布局）
 *
 * @author TheNextLvl
 */
public class CreateCommand extends AbstractSubCommand {

    // 支持的类型列表
    private static final List<String> TYPES = Arrays.asList(
            "chest", "hopper", "dispenser", "dropper"
    );

    public CreateCommand(@NotNull FoliaGUIToolkit plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, "foliagui.toolkit.create")) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§c用法: /fgt create <id> [type] [rows]");
            sender.sendMessage("§7类型: chest, hopper, dispenser, dropper");
            sender.sendMessage("§7rows 仅对 chest 类型有效 (1-6)");
            return true;
        }

        Player player = (Player) sender;
        String guiId = args[0];

        // 检查 ID 是否已存在
        GUIManager manager = GUIManager.getInstance();
        if (manager != null && manager.isRegistered(guiId)) {
            sender.sendMessage("§cGUI 已存在: " + guiId);
            sender.sendMessage("§7使用 §e/fgt edit " + guiId + " §7编辑现有 GUI");
            return true;
        }

        // 解析类型参数
        InventoryType inventoryType = InventoryType.CHEST;
        int rows = 3;

        if (args.length > 1) {
            String typeStr = args[1].toLowerCase();
            inventoryType = parseInventoryType(typeStr);
            if (inventoryType == null) {
                sender.sendMessage("§c未知的类型: " + args[1]);
                sender.sendMessage("§7可用类型: " + String.join(", ", TYPES));
                return true;
            }
        }

        // 解析行数参数（仅对 chest 类型有效）
        if (args.length > 2 && inventoryType == InventoryType.CHEST) {
            try {
                rows = Integer.parseInt(args[2]);
                if (rows < 1 || rows > 6) {
                    sender.sendMessage("§c行数必须在 1-6 之间！");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§c无效的行数: " + args[2]);
                return true;
            }
        }

        // 创建空白 GUI
        GUIBuilder builder = GUIBuilder.create(guiId)
                .title("§6编辑中: " + guiId)
                .type(inventoryType);

        if (inventoryType == InventoryType.CHEST) {
            builder.rows(rows);
            builder.border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        }

        GUI gui = builder.build();

        // 注册到管理器（如果存在）
        if (manager != null) {
            manager.register(gui);
        }

        // 注意：create 只是创建并预览 GUI，不进入编辑模式
        // 如需编辑，请使用 /fgt edit 命令

        sender.sendMessage("§a已创建新 GUI: " + guiId);
        sender.sendMessage("§7类型: " + inventoryType.name().toLowerCase());
        if (inventoryType == InventoryType.CHEST) {
            sender.sendMessage("§7行数: " + rows);
        }
        sender.sendMessage("§7关闭 GUI 后使用 §e/fgt save " + guiId + " §7保存");
        sender.sendMessage("§7使用 §e/fgt edit " + guiId + " §7进入编辑模式");

        // 打开 GUI 预览
        gui.open(player);

        return true;
    }

    /**
     * 解析库存类型
     */
    private InventoryType parseInventoryType(String typeStr) {
        return switch (typeStr) {
            case "chest" -> InventoryType.CHEST;
            case "hopper" -> InventoryType.HOPPER;
            case "dispenser" -> InventoryType.DISPENSER;
            case "dropper" -> InventoryType.DROPPER;
            default -> null;
        };
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            // 补全类型
            String prefix = args[1].toLowerCase();
            for (String type : TYPES) {
                if (type.startsWith(prefix)) {
                    completions.add(type);
                }
            }
        } else if (args.length == 3) {
            // 补全行数（仅当类型为 chest 时）
            String type = args.length > 1 ? args[1].toLowerCase() : "chest";
            if (type.equals("chest")) {
                for (int i = 1; i <= 6; i++) {
                    if (String.valueOf(i).startsWith(args[2])) {
                        completions.add(String.valueOf(i));
                    }
                }
            }
        }

        return completions;
    }
}