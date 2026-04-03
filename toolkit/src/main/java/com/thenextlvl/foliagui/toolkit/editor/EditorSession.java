package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 编辑会话
 * <p>
 * 统一管理玩家的编辑状态，包括目标 GUI、编辑器 GUI、模式等
 * 工具栏放在玩家背包最后一行（slots 27-35）
 *
 * @author TheNextLvl
 */
public class EditorSession {

    private final FoliaGUIToolkit plugin;
    private final Player player;
    private final GUI targetGUI;           // 正在编辑的目标 GUI
    private final ItemStack[] originalContents; // 原始内容备份
    private final Map<Integer, ItemStack> savedInventoryItems = new HashMap<>(); // 保存玩家背包物品
    private GUI editorGUI;                 // 编辑器 GUI
    private EditorToolbar toolbar;         // 工具栏

    private EditorMode mode = EditorMode.NORMAL;
    private int selectedSlot = -1;
    private Inventory subMenuInventory;    // 当前打开的子菜单

    /**
     * 编辑模式
     */
    public enum EditorMode {
        NORMAL,      // 正常模式 - 允许拖拽物品
        SELECT,      // 选择编辑模式 - 点击选择物品进行编辑
        DELETE,      // 删除模式 - 点击删除物品
        PLACE        // 放置模式 - 从子菜单返回后放置物品
    }

    public EditorSession(@NotNull FoliaGUIToolkit plugin, @NotNull Player player, @NotNull GUI targetGUI) {
        this.plugin = plugin;
        this.player = player;
        this.targetGUI = targetGUI;

        // 备份原始内容
        Inventory targetInv = targetGUI.getInventory();
        if (targetInv != null) {
            this.originalContents = Arrays.copyOf(targetInv.getContents(), targetInv.getSize());
        } else {
            this.originalContents = new ItemStack[0];
        }

        // 创建编辑器 GUI 和工具栏
        createEditorGUI();
    }

    /**
     * 创建编辑器 GUI
     */
    private void createEditorGUI() {
        Inventory targetInv = targetGUI.getInventory();
        if (targetInv == null) {
            throw new IllegalStateException("目标 GUI 没有 Inventory");
        }

        // 构建编辑器 GUI（与目标 GUI 相同大小）
        GUIBuilder builder = GUIBuilder.create("editor-" + targetGUI.getId())
                .title("§e编辑: " + targetGUI.getId())
                .rows(targetInv.getSize() / 9);

        this.editorGUI = builder.build();

        // 复制目标 GUI 内容到编辑器
        Inventory editorInv = editorGUI.getInventory();
        if (editorInv != null) {
            ItemStack[] contents = targetInv.getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && !contents[i].getType().isAir()) {
                    editorInv.setItem(i, contents[i].clone());
                }
            }
        }

        // 创建工具栏
        this.toolbar = new EditorToolbar(plugin, this);
    }

    /**
     * 打开编辑器
     */
    public void open() {
        // 保存玩家背包最后一行物品
        savePlayerInventoryToolbar();

        // 渲染工具栏到玩家背包
        toolbar.renderToPlayerInventory();

        // 打开编辑器 GUI
        editorGUI.open(player);
    }

    /**
     * 关闭编辑器
     */
    public void close() {
        player.closeInventory();
    }

    /**
     * 保存玩家背包最后一行的物品
     */
    private void savePlayerInventoryToolbar() {
        savedInventoryItems.clear();
        for (int i = 27; i <= 35; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                savedInventoryItems.put(i, item.clone());
            }
        }
    }

    /**
     * 恢复玩家背包最后一行的物品
     */
    public void restorePlayerInventoryToolbar() {
        // 先清空工具栏区域
        for (int i = 27; i <= 35; i++) {
            player.getInventory().setItem(i, null);
        }

        // 恢复保存的物品
        for (Map.Entry<Integer, ItemStack> entry : savedInventoryItems.entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue());
        }
        savedInventoryItems.clear();
    }

    /**
     * 渲染工具栏
     */
    public void renderToolbar() {
        toolbar.renderToPlayerInventory();
    }

    /**
     * 处理内容区域点击
     *
     * @param slot  点击的槽位
     * @param event 点击事件
     */
    public void handleContentClick(int slot, @NotNull org.bukkit.event.inventory.InventoryClickEvent event) {
        Inventory editorInv = editorGUI.getInventory();
        if (editorInv == null) return;

        ItemStack clickedItem = event.getCurrentItem();

        switch (mode) {
            case DELETE -> {
                // 删除模式 - 清除物品
                event.setCancelled(true);
                if (clickedItem != null && !clickedItem.getType().isAir()) {
                    editorInv.setItem(slot, null);
                    player.sendMessage("§c已删除槽位 " + slot + " 的物品");
                }
            }
            case SELECT -> {
                // 选择编辑模式 - 打开物品编辑器
                event.setCancelled(true);
                if (clickedItem != null && !clickedItem.getType().isAir()) {
                    selectedSlot = slot;
                    mode = EditorMode.NORMAL;
                    renderToolbar();

                    // 打开物品编辑器
                    plugin.getItemEditor().openEditor(player, clickedItem.clone(), this, editedItem -> {
                        // 保存编辑后的物品
                        Inventory inv = editorGUI.getInventory();
                        if (inv != null) {
                            inv.setItem(slot, editedItem);
                        }
                        // 重新打开编辑器
                        open();
                        player.sendMessage("§a物品已保存");
                    });
                }
            }
            case NORMAL -> {
                // 正常模式 - 允许拖拽
                // 不取消事件，让玩家可以自由移动物品
            }
            case PLACE -> {
                // 放置模式 - 从光标放置物品
                // 不取消事件，让物品可以放置
                mode = EditorMode.NORMAL;
                renderToolbar();
            }
        }
    }

    /**
     * 处理工具栏点击
     *
     * @param inventorySlot 玩家背包槽位（27-35）
     * @return 是否处理了点击
     */
    public boolean handleToolbarClick(int inventorySlot) {
        if (inventorySlot < 27 || inventorySlot > 35) return false;

        int toolIndex = inventorySlot - 27;
        toolbar.handleClick(toolIndex);
        return true;
    }

    /**
     * 检查是否是工具栏槽位
     *
     * @param slot 槽位
     * @return 是否是工具栏槽位
     */
    public boolean isToolbarSlot(int slot) {
        return slot >= 27 && slot <= 35;
    }

    /**
     * 打开子菜单
     *
     * @param subMenuGUI 子菜单 GUI
     */
    public void openSubMenu(@NotNull GUI subMenuGUI) {
        Inventory subInv = subMenuGUI.getInventory();
        if (subInv != null) {
            this.subMenuInventory = subInv;
        }
        subMenuGUI.open(player);
    }

    /**
     * 检查是否是子菜单
     *
     * @param inventory 要检查的 Inventory
     * @return 是否是当前会话的子菜单
     */
    public boolean isSubMenu(@Nullable Inventory inventory) {
        return subMenuInventory != null && subMenuInventory.equals(inventory);
    }

    /**
     * 清除子菜单标记
     */
    public void clearSubMenu() {
        this.subMenuInventory = null;
    }

    /**
     * 检查是否有子菜单正在打开
     *
     * @return 是否有子菜单
     */
    public boolean hasSubMenu() {
        return subMenuInventory != null;
    }

    /**
     * 从子菜单返回
     */
    public void returnFromSubMenu() {
        clearSubMenu();
        open();
    }

    /**
     * 应用编辑结果到目标 GUI
     */
    public void applyChanges() {
        Inventory editorInv = editorGUI.getInventory();
        Inventory targetInv = targetGUI.getInventory();

        if (editorInv == null || targetInv == null) return;

        // 复制编辑器内容到目标 GUI
        int contentSize = targetInv.getSize();
        ItemStack[] newContents = new ItemStack[contentSize];

        for (int i = 0; i < contentSize; i++) {
            ItemStack item = editorInv.getItem(i);
            if (item != null && !item.getType().isAir()) {
                newContents[i] = item.clone();
            }
        }

        targetInv.setContents(newContents);
        player.sendMessage("§a已应用更改到目标 GUI");
    }

    /**
     * 撤销更改，恢复原始内容
     */
    public void revertChanges() {
        Inventory targetInv = targetGUI.getInventory();
        if (targetInv != null) {
            targetInv.setContents(Arrays.copyOf(originalContents, targetInv.getSize()));
            player.sendMessage("§e已撤销更改");
        }
    }

    /**
     * 清理会话资源
     */
    public void cleanup() {
        // 恢复玩家背包物品
        restorePlayerInventoryToolbar();

        // 清理引用
        subMenuInventory = null;
        editorGUI = null;
        toolbar = null;
    }

    // ==================== Getters ====================

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public GUI getTargetGUI() {
        return targetGUI;
    }

    @Nullable
    public GUI getEditorGUI() {
        return editorGUI;
    }

    @NotNull
    public EditorToolbar getToolbar() {
        return toolbar;
    }

    @NotNull
    public EditorMode getMode() {
        return mode;
    }

    public void setMode(@NotNull EditorMode mode) {
        this.mode = mode;
        renderToolbar();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    @NotNull
    public ItemStack[] getOriginalContents() {
        return originalContents;
    }
}