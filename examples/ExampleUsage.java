package com.thenextlvl.foliagui.examples;

import com.thenextlvl.foliagui.annotation.*;
import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FoliaGUI 使用示例
 * 展示Builder模式和声明式注解两种使用方式
 */
public class ExampleUsage extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // 初始化GUIManager（同时初始化FoliaScheduler）
        GUIManager manager = GUIManager.init(this);
        
        // ========== 方式1: Builder模式 ==========
        
        // 创建简单菜单
        GUI simpleMenu = GUIBuilder.create("simple-menu")
            .title("&6简单菜单")
            .rows(3)
            .border(Material.BLACK_STAINED_GLASS_PANE)
            .button(11, 
                ItemBuilder.of(Material.DIAMOND_SWORD)
                    .name("&b起床战争")
                    .lore("&7点击加入游戏", "", "&e在线玩家: &f%players%")
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§a正在传送至起床战争...");
                    event.setCancelled(true);
                }
            )
            .button(13,
                ItemBuilder.of(Material.BED)
                    .name("&c返回大厅")
                    .build(),
                event -> {
                    event.getPlayer().performCommand("spawn");
                    event.setCancelled(true);
                }
            )
            .button(15,
                ItemBuilder.of(Material.BARRIER)
                    .name("&7关闭")
                    .build(),
                event -> {
                    event.getPlayer().closeInventory();
                    event.setCancelled(true);
                }
            )
            .onOpen(player -> player.sendMessage("§a菜单已打开!"))
            .build();
        
        manager.register(simpleMenu);
        
        // ========== 方式2: 声明式注解（TrMenu风格） ==========
        
        // 注册声明式GUI
        manager.register(new GameSelectorMenu());
        
        // 命令处理器
        getCommand("menu").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return false;
            
            if (args.length > 0 && args[0].equalsIgnoreCase("simple")) {
                manager.open(player, "simple-menu");
            } else {
                manager.open(player, "game-selector");
            }
            return true;
        });
    }
    
    // ========== 声明式GUI示例 ==========
    
    @GUIConfig(
        id = "game-selector",
        title = {"&6欢迎, %player_name%", "&b点击选择游戏"},
        titleUpdate = 40,
        rows = 5,
        usePlayerInventory = true,
        allowPlayerInventoryClick = false
    )
    @Layout({
        "###################",
        "#  A   B   C   D  #",
        "#  E   F   G   H  #",
        "#                 #",
        "###################"
    })
    @PlayerInventoryLayout({
        "         ",
        "         ",
        "    X    ",
        "         "
    })
    public static class GameSelectorMenu extends DeclarativeGUI {
        
        // 游戏图标
        @Icon(id = "A", material = Material.DIAMOND_SWORD, 
              name = "&b起床战争", lore = {"&7模式: &f4v4v4v4", "&7玩家: &f%bw_players%", "", "&e左键加入"})
        @Action(type = Action.ClickType.LEFT, value = "server bw")
        @Action(type = Action.ClickType.RIGHT, value = "tell: &7右键查看详情")
        private Icon bedwars;
        
        @Icon(id = "B", material = Material.STICK, 
              name = "&a空岛战争", lore = {"&7模式: &f单人", "&7玩家: &f%sw_players%", "", "&e左键加入"})
        @Action(value = "server sw")
        private Icon skywars;
        
        @Icon(id = "C", material = Material.IRON_SWORD, 
              name = "&c决斗", lore = {"&7模式: &f1v1", "", "&e左键加入"})
        @Action(value = "server duel")
        private Icon duel;
        
        @Icon(id = "D", material = Material.GRASS_BLOCK, 
              name = "&2生存", lore = {"&7在线: &f%survival_online%", "", "&e左键加入"})
        @Action(value = "server survival")
        private Icon survival;
        
        // 装饰图标
        @Icon(id = "#", material = Material.BLACK_STAINED_GLASS_PANE, name = " ")
        private Icon decoration;
        
        // 玩家背包中的关闭按钮
        @Icon(id = "X", material = Material.BARRIER, name = "&c关闭菜单")
        @Action(value = "close")
        private Icon closeButton;
        
        @Override
        protected void onOpen(Player player) {
            player.sendMessage("§a游戏选择器已打开!");
        }
        
        @Override
        protected void onClose(Player player) {
            player.sendMessage("§7菜单已关闭");
        }
    }
    
    // ========== Builder模式进阶示例 ==========
    
    public void createAdvancedMenu(Player player) {
        GUI advancedMenu = GUIBuilder.create("advanced-menu")
            .title("&5高级菜单")
            .rows(6)
            // 边框
            .border(Material.PURPLE_STAINED_GLASS_PANE)
            // 动态按钮（使用占位符）
            .button(10,
                ItemBuilder.of(Material.PLAYER_HEAD)
                    .skull(player.getName())
                    .name("&e你的信息")
                    .lore(
                        "&7名称: &f%player_name%",
                        "&7等级: &f%player_level%",
                        "&7金币: &f%balance%",
                        "",
                        "&e点击查看详情"
                    )
                    .build(),
                event -> {
                    // 打开详情菜单
                    GUIManager.getInstance().open(event.getPlayer(), "profile-menu");
                    event.setCancelled(true);
                }
            )
            // 带条件的按钮
            .button(13,
                ItemBuilder.of(Material.EMERALD)
                    .name("&aVIP专属")
                    .lore("&7仅VIP玩家可见")
                    .glow()
                    .build(),
                event -> {
                    if (!event.getPlayer().hasPermission("vip.access")) {
                        event.getPlayer().sendMessage("§c你需要VIP权限!");
                        event.setCancelled(true);
                        return;
                    }
                    // VIP功能
                    event.setCancelled(true);
                }
            )
            // 装饰填充
            .fillRange(
                ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build(),
                18, 26
            )
            // 翻页按钮
            .button(45,
                ItemBuilder.of(Material.ARROW).name("&7上一页").build(),
                event -> {
                    // 翻页逻辑
                    event.setCancelled(true);
                }
            )
            .button(53,
                ItemBuilder.of(Material.ARROW).name("&7下一页").build(),
                event -> {
                    // 翻页逻辑
                    event.setCancelled(true);
                }
            )
            .build();
        
        GUIManager.getInstance().register(advancedMenu).open(player, "advanced-menu");
    }
}
