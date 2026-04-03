package com.thenextlvl.foliagui.toolkit;

import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.toolkit.command.ToolkitCommand;
import com.thenextlvl.foliagui.toolkit.editor.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * FoliaGUI 工具插件
 * <p>
 * 提供可视化编辑、配置导出、GUI 管理等功能
 *
 * @author TheNextLvl
 */
public class FoliaGUIToolkit extends JavaPlugin {

    private static FoliaGUIToolkit instance;
    private EditorManager editorManager;
    private EditorListener editorListener;
    private ItemLibrary itemLibrary;
    private ItemEditor itemEditor;
    private ComponentLibrary componentLibrary;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化 GUIManager（因为主库被 shade 打包进来）
        GUIManager.init(this);

        // 初始化编辑管理器
        editorManager = new EditorManager(this);

        // 初始化物品库
        itemLibrary = new ItemLibrary(this);

        // 初始化物品编辑器
        itemEditor = new ItemEditor(this);

        // 初始化组件库
        componentLibrary = new ComponentLibrary(this);

        // 初始化编辑监听器
        editorListener = new EditorListener(this, editorManager);
        getServer().getPluginManager().registerEvents(editorListener, this);

        // 注册命令
        getCommand("fgt").setExecutor(new ToolkitCommand(this));

        getLogger().info("FoliaGUI Toolkit has been enabled!");
        getLogger().info("FoliaGUI library bundled and initialized");
        getLogger().info("Use /fgt to manage and edit GUIs");
    }

    @Override
    public void onDisable() {
        // 保存所有未保存的编辑
        if (editorManager != null) {
            editorManager.saveAll();
        }

        getLogger().info("FoliaGUI Toolkit has been disabled!");
    }

    /**
     * 获取插件实例
     */
    @NotNull
    public static FoliaGUIToolkit getInstance() {
        return instance;
    }

    /**
     * 获取编辑管理器
     */
    @NotNull
    public EditorManager getEditorManager() {
        return editorManager;
    }

    /**
     * 获取编辑监听器
     */
    @NotNull
    public EditorListener getEditorListener() {
        return editorListener;
    }

    /**
     * 获取物品库
     */
    @NotNull
    public ItemLibrary getItemLibrary() {
        return itemLibrary;
    }

    /**
     * 获取物品编辑器
     */
    @NotNull
    public ItemEditor getItemEditor() {
        return itemEditor;
    }

    /**
     * 获取组件库
     */
    @NotNull
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }
}