package com.thenextlvl.foliagui.test;

import com.thenextlvl.foliagui.annotation.Action;
import com.thenextlvl.foliagui.annotation.ActionHandler;
import com.thenextlvl.foliagui.annotation.Button;
import com.thenextlvl.foliagui.annotation.GUIConfig;
import com.thenextlvl.foliagui.annotation.Icon;
import com.thenextlvl.foliagui.annotation.Layout;
import com.thenextlvl.foliagui.internal.AbstractDeclarativeGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * 声明式GUI示例 - 使用注解定义GUI
 * 类似YAML的代码定义方式
 */
@GUIConfig(id = "declarative-shop", title = {"§6", "✦ ", "§e商店", " §6✦"}, rows = 4)
public class DeclarativeShopGUI extends AbstractDeclarativeGUI {

    @Layout({
        "#########",
        "#  D  E #",
        "#  G  B #",
        "#########"
    })
    @Icon(id = "D", material = Material.DIAMOND, name = "§b钻石", 
          lore = {"§7价格: §e100金币", "", "§8点击购买"},
          action = {"buy DIAMOND 100"})
    @Icon(id = "E", material = Material.EMERALD, name = "§a绿宝石", 
          lore = {"§7价格: §e50金币", "", "§8点击购买"},
          action = {"buy EMERALD 50"})
    @Icon(id = "G", material = Material.GOLD_INGOT, name = "§6金锭", 
          lore = {"§7价格: §e20金币", "", "§8点击购买"},
          action = {"buy GOLD_INGOT 20"})
    @Icon(id = "B", material = Material.BARRIER, name = "§c关闭", 
          lore = {"§7点击关闭商店"},
          action = {"close"})
    private Object layout;

    @ActionHandler("buy")
    public void onBuy(Player player, String args) {
        String[] parts = args.split("\\s+");
        if (parts.length >= 2) {
            String materialName = parts[0];
            int price = Integer.parseInt(parts[1]);
            player.sendMessage("§a✓ 你购买了 §e" + materialName + " §a，花费 §e" + price + "金币");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    @Override
    public void onOpen(Player player) {
        player.sendMessage("§6欢迎来到商店！");
    }

    @Override
    public void onClose(Player player) {
        player.sendMessage("§7欢迎下次光临！");
    }
}
