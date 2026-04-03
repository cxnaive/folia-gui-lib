package com.thenextlvl.foliagui.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Folia调度器工具类 - 统一封装所有Folia调度操作
 * 开发者无需关心当前处于哪个Region，工具类自动处理
 * 
 * 参考 newpillar 项目的 SchedulerUtils 实现
 * 
 * 此类兼容:
 * - Folia 1.20+
 * - Paper 1.20+ (使用Bukkit调度器作为回退)
 * - Spigot/Paper (使用Bukkit调度器作为回退)
 *
 * @author TheNextLvl
 */
public final class FoliaScheduler {

    private static Plugin plugin;
    private static boolean initialized = false;
    private static boolean isFolia = false;

    private FoliaScheduler() {}

    /**
     * 初始化调度器
     * @param plugin 插件实例
     */
    public static void init(@NotNull Plugin plugin) {
        FoliaScheduler.plugin = plugin;
        FoliaScheduler.initialized = true;
        FoliaScheduler.isFolia = detectFolia();
        plugin.getLogger().info("[FoliaScheduler] 调度器已初始化 (Folia: " + isFolia + ")");
    }

    /**
     * 检测是否在Folia环境
     */
    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 检查是否已初始化
     */
    private static void checkInit() {
        if (!initialized) {
            throw new IllegalStateException("FoliaScheduler未初始化！请在插件onEnable中调用FoliaScheduler.init(this)");
        }
    }

    /**
     * 检查是否在Folia环境
     */
    public static boolean isFolia() {
        return isFolia;
    }

    // ==================== 立即执行任务 ====================

    /**
     * 在全局区域执行（世界设置、游戏规则等）
     * @param task 要执行的任务
     */
    public static void runGlobal(@NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 在指定世界执行（在世界的spawn位置执行）
     * @param world 目标世界
     * @param task 要执行的任务
     */
    public static void runOnWorld(@NotNull World world, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            Location spawn = world.getSpawnLocation();
            Bukkit.getRegionScheduler().execute(plugin, spawn, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 在指定位置执行
     * GUI操作通常使用此方法
     * @param location 目标位置
     * @param task 要执行的任务
     */
    public static void runAtLocation(@NotNull Location location, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            Bukkit.getRegionScheduler().execute(plugin, location, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 在实体所在线程执行
     * @param entity 目标实体
     * @param task 要执行的任务
     */
    public static void runOnEntity(@NotNull Entity entity, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            if (entity.isValid()) {
                entity.getScheduler().execute(plugin, task, null, 1L);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 在玩家所在线程执行（便捷方法）
     * @param player 目标玩家
     * @param task 要执行的任务
     */
    public static void runOnPlayer(@NotNull Player player, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            if (player.isOnline()) {
                player.getScheduler().execute(plugin, task, null, 1L);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 在指定区块执行
     * @param world 目标世界
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @param task 要执行的任务
     */
    public static void runOnChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            Location loc = new Location(world, chunkX << 4, 64, chunkZ << 4);
            Bukkit.getRegionScheduler().execute(plugin, loc, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    // ==================== 延迟执行任务 ====================

    /**
     * 延迟后在全局区域执行
     * @param delay 延迟tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterGlobal(long delay, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, 
                scheduledTask -> task.run(), Math.max(1, delay));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1, delay));
            return null;
        }
    }

    /**
     * 延迟后在全局区域执行（带任务参数）
     * @param delay 延迟tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterGlobal(long delay, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task, Math.max(1, delay));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> task.accept(null), Math.max(1, delay));
            return null;
        }
    }

    /**
     * 延迟后在指定世界执行（在世界的spawn位置执行）
     * @param world 目标世界
     * @param delay 延迟tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterOnWorld(@NotNull World world, long delay, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            Location spawn = world.getSpawnLocation();
            return Bukkit.getRegionScheduler().runDelayed(plugin, spawn,
                scheduledTask -> task.run(), Math.max(1, delay));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1, delay));
            return null;
        }
    }

    /**
     * 延迟后在指定位置执行
     * @param location 目标位置
     * @param delay 延迟tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterAtLocation(@NotNull Location location, long delay, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getRegionScheduler().runDelayed(plugin, location,
                scheduledTask -> task.run(), Math.max(1, delay));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1, delay));
            return null;
        }
    }

    /**
     * 延迟后在实体所在线程执行
     * @param entity 目标实体
     * @param delay 延迟tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterOnEntity(@NotNull Entity entity, long delay, @NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            if (!entity.isValid()) {
                return null;
            }
            return entity.getScheduler().runDelayed(plugin,
                scheduledTask -> task.run(), null, Math.max(1, delay));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1, delay));
            return null;
        }
    }

    /**
     * 延迟后在玩家所在线程执行（便捷方法）
     * @param player 目标玩家
     * @param delay 延迟tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterOnPlayer(@NotNull Player player, long delay, @NotNull Runnable task) {
        return runLaterOnEntity(player, delay, task);
    }

    // ==================== 定时重复任务 ====================

    /**
     * 在全局区域定时重复执行
     * @param delay 首次执行延迟tick数
     * @param period 执行间隔tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runTimerGlobal(long delay, long period, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task, Math.max(1, delay), Math.max(1, period));
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null), Math.max(1, delay), Math.max(1, period));
            return null;
        }
    }

    /**
     * 在指定世界定时重复执行（在世界的spawn位置执行）
     * @param world 目标世界
     * @param delay 首次执行延迟tick数
     * @param period 执行间隔tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runTimerOnWorld(@NotNull World world, long delay, long period, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            Location spawn = world.getSpawnLocation();
            return Bukkit.getRegionScheduler().runAtFixedRate(plugin, spawn, task, Math.max(1, delay), Math.max(1, period));
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null), Math.max(1, delay), Math.max(1, period));
            return null;
        }
    }

    /**
     * 在指定位置定时重复执行
     * @param location 目标位置
     * @param delay 首次执行延迟tick数
     * @param period 执行间隔tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runTimerOnLocation(@NotNull Location location, long delay, long period, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task, Math.max(1, delay), Math.max(1, period));
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null), Math.max(1, delay), Math.max(1, period));
            return null;
        }
    }

    /**
     * 在实体所在线程定时重复执行
     * @param entity 目标实体
     * @param delay 首次执行延迟tick数
     * @param period 执行间隔tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runTimerOnEntity(@NotNull Entity entity, long delay, long period, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            if (!entity.isValid()) {
                return null;
            }
            return entity.getScheduler().runAtFixedRate(plugin, task, null, Math.max(1, delay), Math.max(1, period));
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null), Math.max(1, delay), Math.max(1, period));
            return null;
        }
    }

    /**
     * 在玩家所在线程定时重复执行（便捷方法）
     * @param player 目标玩家
     * @param delay 首次执行延迟tick数
     * @param period 执行间隔tick数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runTimerOnPlayer(@NotNull Player player, long delay, long period, @NotNull Consumer<ScheduledTask> task) {
        return runTimerOnEntity(player, delay, period, task);
    }

    // ==================== 传送工具 ====================

    /**
     * 异步传送玩家
     * @param player 要传送的玩家
     * @param location 目标位置
     * @param onComplete 传送完成后的回调（可为null）
     */
    public static void teleport(@NotNull Player player, @NotNull Location location, @Nullable Runnable onComplete) {
        checkInit();
        player.teleportAsync(location).thenRun(() -> {
            if (onComplete != null) {
                // 在目标位置所在线程执行回调
                runAtLocation(location, onComplete);
            }
        });
    }

    /**
     * 异步传送玩家（无回调）
     * @param player 要传送的玩家
     * @param location 目标位置
     */
    public static void teleport(@NotNull Player player, @NotNull Location location) {
        teleport(player, location, null);
    }

    /**
     * 异步传送玩家并在传送后执行玩家相关操作
     * @param player 要传送的玩家
     * @param location 目标位置
     * @param onComplete 传送完成后在玩家线程执行的回调
     */
    public static void teleportAndRunOnPlayer(@NotNull Player player, @NotNull Location location, @NotNull Runnable onComplete) {
        checkInit();
        player.teleportAsync(location).thenRun(() -> {
            runOnPlayer(player, onComplete);
        });
    }

    // ==================== 异步任务 ====================

    /**
     * 在异步线程立即执行任务（用于数据库操作等）
     * @param task 要执行的任务
     */
    public static void runAsync(@NotNull Runnable task) {
        checkInit();
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * 在异步线程定时重复执行任务
     * @param delay 首次执行延迟毫秒数
     * @param period 执行间隔毫秒数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runTimerAsync(long delay, long period, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task, delay, period, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> task.accept(null), delay / 50, period / 50);
            return null;
        }
    }

    /**
     * 延迟后在异步线程执行任务
     * @param delay 延迟毫秒数
     * @param task 要执行的任务
     * @return ScheduledTask实例，可用于取消任务
     */
    @Nullable
    public static ScheduledTask runLaterAsync(long delay, @NotNull Consumer<ScheduledTask> task) {
        checkInit();
        if (isFolia) {
            return Bukkit.getAsyncScheduler().runDelayed(plugin, task, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> task.accept(null), delay / 50);
            return null;
        }
    }

    // ==================== 任务取消 ====================

    /**
     * 取消任务
     * @param task 要取消的ScheduledTask
     */
    public static void cancel(@Nullable ScheduledTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * 安全取消任务（处理null）
     * @param task 要取消的ScheduledTask（可为null）
     */
    public static void cancelSafely(@Nullable ScheduledTask task) {
        cancel(task);
    }
}
