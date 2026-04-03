package com.thenextlvl.foliagui.internal.config;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.config.GUIConfigLoader;
import com.thenextlvl.foliagui.api.config.GUIConfigReloader;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * GUI 配置重载器实现
 *
 * @author TheNextLvl
 */
public class GUIConfigReloaderImpl implements GUIConfigReloader {

    private final Map<String, File> configFiles = new HashMap<>();
    private final Map<String, Long> lastModified = new HashMap<>();
    private final GUIConfigLoader loader;
    private final Plugin plugin;
    private Object autoReloadTask;
    private boolean autoReloadEnabled = false;

    public GUIConfigReloaderImpl(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.loader = new YamlConfigLoader();
    }

    @Override
    public boolean reload(@NotNull String guiId) {
        File file = configFiles.get(guiId.toLowerCase());
        if (file == null || !file.exists()) {
            return false;
        }

        try {
            GUI gui = loader.load(file);

            // 注销旧的 GUI
            GUIManager manager = GUIManager.getInstance();
            if (manager != null) {
                // 重新注册
                manager.register(gui);
            }

            lastModified.put(guiId.toLowerCase(), file.lastModified());
            plugin.getLogger().info("[FoliaGUI] Reloaded GUI config: " + guiId);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "[FoliaGUI] Failed to reload GUI config: " + guiId, e);
            return false;
        }
    }

    @Override
    public void reloadAll() {
        for (String guiId : new ArrayList<>(configFiles.keySet())) {
            reload(guiId);
        }
    }

    @Override
    public void registerConfigFile(@NotNull String guiId, @NotNull File configFile) {
        configFiles.put(guiId.toLowerCase(), configFile);
        lastModified.put(guiId.toLowerCase(), configFile.lastModified());
    }

    @Override
    public void unregisterConfigFile(@NotNull String guiId) {
        configFiles.remove(guiId.toLowerCase());
        lastModified.remove(guiId.toLowerCase());
    }

    @Override
    public @Nullable File getConfigFile(@NotNull String guiId) {
        return configFiles.get(guiId.toLowerCase());
    }

    @Override
    public @NotNull Collection<String> getRegisteredGuiIds() {
        return Collections.unmodifiableSet(configFiles.keySet());
    }

    @Override
    public boolean hasUpdate(@NotNull String guiId) {
        File file = configFiles.get(guiId.toLowerCase());
        if (file == null || !file.exists()) {
            return false;
        }

        Long last = lastModified.get(guiId.toLowerCase());
        return last == null || file.lastModified() > last;
    }

    @Override
    public void enableAutoReload(long intervalTicks) {
        if (autoReloadEnabled) {
            disableAutoReload();
        }

        autoReloadEnabled = true;
        autoReloadTask = FoliaScheduler.runTimerGlobal(intervalTicks, intervalTicks, task -> {
            for (String guiId : new ArrayList<>(configFiles.keySet())) {
                if (hasUpdate(guiId)) {
                    reload(guiId);
                }
            }
        });
    }

    @Override
    public void disableAutoReload() {
        if (autoReloadTask != null) {
            if (autoReloadTask instanceof org.bukkit.scheduler.BukkitTask bukkitTask) {
                bukkitTask.cancel();
            } else if (autoReloadTask instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
                foliaTask.cancel();
            }
            autoReloadTask = null;
        }
        autoReloadEnabled = false;
    }

    @Override
    public boolean isAutoReloadEnabled() {
        return autoReloadEnabled;
    }
}