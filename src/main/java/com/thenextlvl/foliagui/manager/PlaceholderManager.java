package com.thenextlvl.foliagui.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 占位符管理器
 * <p>
 * 全局占位符系统，支持在 Lore、DisplayName 中使用 %placeholder% 格式
 * <pre>
 * // 注册占位符
 * PlaceholderManager.register("player", player -> player.getName());
 * PlaceholderManager.register("balance", player -> economy.getBalance(player));
 *
 * // 解析占位符
 * String result = PlaceholderManager.resolve(player, "玩家: %player%, 余额: %balance%");
 * </pre>
 *
 * @author TheNextLvl
 */
public final class PlaceholderManager {

    private static final Map<String, Function<Player, String>> placeholders = new ConcurrentHashMap<>();
    private static final String PLACEHOLDER_PREFIX = "%";
    private static final String PLACEHOLDER_SUFFIX = "%";

    private PlaceholderManager() {}

    /**
     * 注册占位符
     * <p>
     * 占位符名称不区分大小写，自动转换为小写存储
     *
     * @param name     占位符名称（不包含 %%）
     * @param resolver 解析函数
     */
    public static void register(@NotNull String name, @NotNull Function<Player, String> resolver) {
        placeholders.put(name.toLowerCase(), resolver);
    }

    /**
     * 批量注册占位符
     *
     * @param placeholders 占位符映射
     */
    public static void registerAll(@NotNull Map<String, Function<Player, String>> placeholders) {
        for (Map.Entry<String, Function<Player, String>> entry : placeholders.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 注销占位符
     *
     * @param name 占位符名称
     */
    public static void unregister(@NotNull String name) {
        placeholders.remove(name.toLowerCase());
    }

    /**
     * 检查占位符是否存在
     *
     * @param name 占位符名称
     * @return 是否存在
     */
    public static boolean exists(@NotNull String name) {
        return placeholders.containsKey(name.toLowerCase());
    }

    /**
     * 获取占位符解析器
     *
     * @param name 占位符名称
     * @return 解析器，不存在则返回 null
     */
    @Nullable
    public static Function<Player, String> getResolver(@NotNull String name) {
        return placeholders.get(name.toLowerCase());
    }

    /**
     * 解析单个占位符
     *
     * @param name   占位符名称
     * @param player 玩家上下文
     * @return 解析后的值，如果占位符不存在则返回原始占位符
     */
    @NotNull
    public static String resolveOne(@NotNull String name, @Nullable Player player) {
        Function<Player, String> resolver = placeholders.get(name.toLowerCase());
        if (resolver != null && player != null) {
            try {
                return resolver.apply(player);
            } catch (Exception e) {
                return PLACEHOLDER_PREFIX + name + PLACEHOLDER_SUFFIX;
            }
        }
        return PLACEHOLDER_PREFIX + name + PLACEHOLDER_SUFFIX;
    }

    /**
     * 解析文本中的所有占位符
     *
     * @param player 玩家上下文
     * @param text   包含占位符的文本
     * @return 解析后的文本
     */
    @NotNull
    public static String resolve(@Nullable Player player, @NotNull String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 快速检查是否包含占位符
        if (!text.contains(PLACEHOLDER_PREFIX)) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int start = 0;
        int prefixIndex;

        while ((prefixIndex = text.indexOf(PLACEHOLDER_PREFIX, start)) != -1) {
            // 添加前缀之前的内容
            result.append(text, start, prefixIndex);

            // 查找后缀
            int suffixIndex = text.indexOf(PLACEHOLDER_SUFFIX, prefixIndex + 1);
            if (suffixIndex == -1) {
                // 没有后缀，直接添加剩余内容
                result.append(text.substring(prefixIndex));
                break;
            }

            // 提取占位符名称
            String placeholderName = text.substring(prefixIndex + 1, suffixIndex);
            result.append(resolveOne(placeholderName, player));

            start = suffixIndex + 1;
        }

        // 添加剩余内容
        if (start < text.length()) {
            result.append(text.substring(start));
        }

        return result.toString();
    }

    /**
     * 解析字符串数组中的所有占位符
     *
     * @param player 玩家上下文
     * @param lines  字符串数组
     * @return 解析后的数组
     */
    @NotNull
    public static String[] resolve(@Nullable Player player, @NotNull String[] lines) {
        if (lines == null) {
            return new String[0];
        }
        String[] result = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = resolve(player, lines[i]);
        }
        return result;
    }

    /**
     * 清除所有已注册的占位符
     */
    public static void clear() {
        placeholders.clear();
    }

    /**
     * 获取已注册占位符数量
     *
     * @return 数量
     */
    public static int size() {
        return placeholders.size();
    }

    /**
     * 获取所有已注册的占位符名称
     *
     * @return 名称集合
     */
    @NotNull
    public static java.util.Set<String> getRegisteredPlaceholders() {
        return new java.util.HashSet<>(placeholders.keySet());
    }

    // ==================== 内置占位符 ====================

    static {
        // 注册内置占位符
        register("player", Player::getName);
        register("player_name", Player::getName);
        register("player_uuid", player -> player.getUniqueId().toString());
        register("player_displayname", player -> player.getDisplayName());
        register("world", player -> player.getWorld().getName());
        register("x", player -> String.valueOf(player.getLocation().getBlockX()));
        register("y", player -> String.valueOf(player.getLocation().getBlockY()));
        register("z", player -> String.valueOf(player.getLocation().getBlockZ()));
        register("health", player -> String.format("%.1f", player.getHealth()));
        register("max_health", player -> String.format("%.1f", player.getMaxHealth()));
        register("food", player -> String.valueOf(player.getFoodLevel()));
        register("level", player -> String.valueOf(player.getLevel()));
        register("exp", player -> String.format("%.1f", player.getExp() * 100));
        register("gamemode", player -> player.getGameMode().name());
        register("online", player -> String.valueOf(
                player.getServer().getOnlinePlayers().size()));
        register("max_players", player -> String.valueOf(
                player.getServer().getMaxPlayers()));
        register("time", player -> {
            long time = player.getWorld().getTime();
            int hours = (int) ((time / 1000 + 6) % 24);
            int minutes = (int) ((time % 1000) * 60 / 1000);
            return String.format("%02d:%02d", hours, minutes);
        });
        register("date", player -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(new java.util.Date());
        });
        register("time_real", player -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            return sdf.format(new java.util.Date());
        });
    }
}