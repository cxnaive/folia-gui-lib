package com.thenextlvl.foliagui.toolkit.command;

import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * 子命令抽象基类
 *
 * @author TheNextLvl
 */
public abstract class AbstractSubCommand {

    protected final FoliaGUIToolkit plugin;

    public AbstractSubCommand(@NotNull FoliaGUIToolkit plugin) {
        this.plugin = plugin;
    }

    /**
     * 执行命令
     * @param sender 发送者
     * @param args 参数
     * @return 是否成功
     */
    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Tab 补全
     * @param sender 发送者
     * @param args 参数
     * @return 补全列表
     */
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }

    /**
     * 检查权限
     * @param sender 发送者
     * @param permission 权限节点
     * @return 是否有权限
     */
    protected boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou don't have permission: " + permission);
            return false;
        }
        return true;
    }
}