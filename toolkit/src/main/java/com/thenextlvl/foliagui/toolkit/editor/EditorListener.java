package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 编辑监听器
 * <p>
 * 处理编辑模式下的 Inventory 事件
 * 工具栏在玩家背包最后一行（slots 27-35）
 *
 * @author TheNextLvl
 */
public class EditorListener implements Listener {

    private final FoliaGUIToolkit plugin;
    private final EditorManager editorManager;

    public EditorListener(@NotNull FoliaGUIToolkit plugin, @NotNull EditorManager editorManager) {
        this.plugin = plugin;
        this.editorManager = editorManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        EditorSession session = editorManager.getSession(player);

        if (session == null) {
            return;
        }

        Inventory clickedInv = event.getClickedInventory();
        int rawSlot = event.getRawSlot();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        // 检查点击的是否是玩家背包
        if (clickedInv != null && clickedInv.equals(player.getInventory())) {
            // 检查是否点击了工具栏区域（slots 27-35）
            if (session.isToolbarSlot(slot)) {
                // 处理工具栏点击
                if (session.handleToolbarClick(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }

            // 在删除模式和选择模式下，阻止从背包移动物品
            if (session.getMode() == EditorSession.EditorMode.DELETE
                    || session.getMode() == EditorSession.EditorMode.SELECT) {
                event.setCancelled(true);
            }
            return;
        }

        // 检查点击的是否是编辑 GUI
        GUI editorGUI = session.getEditorGUI();
        if (editorGUI != null) {
            Inventory editorInv = editorGUI.getInventory();
            if (editorInv != null && clickedInv != null && clickedInv.equals(editorInv)) {
                // 处理编辑区域点击
                session.handleContentClick(slot, event);
                return;
            }
        }

        // 点击其他 Inventory（如子菜单）
        // 检查是否是子菜单
        if (session.isSubMenu(clickedInv)) {
            // 不干涉子菜单的点击
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        EditorSession session = editorManager.getSession(player);

        if (session == null) {
            return;
        }

        // 检查是否拖拽到了工具栏区域
        for (int rawSlot : event.getRawSlots()) {
            // 玩家背包 slots 27-35 对应 rawSlot
            // 当编辑 GUI 打开时，玩家背包的 rawSlot 是 GUI size + backpack slot
            // 但对于纯背包拖拽，rawSlot 就是实际 slot

            // 检查两种情况：
            // 1. rawSlot 直接在 27-35 范围
            // 2. rawSlot 减去 GUI size 后在 27-35 范围

            if (rawSlot >= 27 && rawSlot <= 35) {
                event.setCancelled(true);
                return;
            }

            GUI editorGUI = session.getEditorGUI();
            if (editorGUI != null && editorGUI.getInventory() != null) {
                int guiSize = editorGUI.getInventory().getSize();
                int backpackSlot = rawSlot - guiSize;
                if (backpackSlot >= 27 && backpackSlot <= 35) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // 在删除模式和选择模式下，阻止拖拽
        if (session.getMode() == EditorSession.EditorMode.DELETE
                || session.getMode() == EditorSession.EditorMode.SELECT) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        EditorSession session = editorManager.getSession(player);

        if (session == null) {
            return;
        }

        Inventory closedInv = event.getInventory();

        // 检查关闭的是否是子菜单
        if (session.isSubMenu(closedInv)) {
            session.clearSubMenu();
            // 不结束会话，让子菜单的回调处理返回
            return;
        }

        // 检查是否正在使用子菜单（subMenuInventory 已设置）
        // 当打开子菜单时，编辑器 GUI 会自动关闭，但不应该结束会话
        if (session.hasSubMenu()) {
            // 不结束会话，用户正在使用子菜单
            return;
        }

        // 检查关闭的是否是编辑器 GUI
        GUI editorGUI = session.getEditorGUI();
        if (editorGUI != null) {
            Inventory editorInv = editorGUI.getInventory();
            if (editorInv != null && editorInv.equals(closedInv)) {
                // 关闭的是编辑器 GUI，结束会话
                editorManager.endSession(player);
                player.sendMessage("§e编辑已结束。使用 §e/fgt save §7保存更改");
                player.sendMessage("§7或使用 §e/fgt edit §7继续编辑");
            }
        }
    }
}