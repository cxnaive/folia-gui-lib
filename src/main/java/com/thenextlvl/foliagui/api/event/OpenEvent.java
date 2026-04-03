package com.thenextlvl.foliagui.api.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI打开事件
 *
 * @author TheNextLvl
 */
public class OpenEvent {

    private final Player player;
    private final Inventory inventory;

    public OpenEvent(@NotNull Player player, @NotNull Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    /**
     * 获取打开GUI的玩家
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取打开的背包
     */
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}
