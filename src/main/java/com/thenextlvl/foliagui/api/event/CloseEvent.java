package com.thenextlvl.foliagui.api.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI关闭事件
 *
 * @author TheNextLvl
 */
public class CloseEvent {

    private final Player player;
    private final Inventory inventory;

    public CloseEvent(@NotNull Player player, @NotNull Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    /**
     * 获取关闭GUI的玩家
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取关闭的背包
     */
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}
