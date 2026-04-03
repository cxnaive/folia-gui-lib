package com.thenextlvl.foliagui.engine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 条件表达式引擎
 * <p>
 * 解析和执行条件表达式，用于控制组件的显示、动作的执行等
 * <p>
 * 支持的条件类型：
 * <ul>
 *   <li>permission:xxx - 权限检查</li>
 *   <li>player_level >= 10 - 等级比较</li>
 *   <li>has_item:DIAMOND - 拥有物品</li>
 *   <li>has_equipped:SLOT - 装备状态</li>
 *   <li>{variable} - 变量引用</li>
 *   <li>condition1 && condition2 - 逻辑与</li>
 *   <li>condition1 || condition2 - 逻辑或</li>
 *   <li>!condition - 逻辑非</li>
 * </ul>
 *
 * @author TheNextLvl
 */
public class ConditionEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{(\\w+)\\}");
    private static final Pattern COMPARISON_PATTERN = Pattern.compile("(\\w+)\\s*(>=|<=|>|<|==|!=)\\s*(\\d+)");

    // 自定义条件处理器注册表
    private static final Map<String, Function<Player, Boolean>> customConditions = new HashMap<>();
    private static final Map<String, BiFunction<Player, String, Boolean>> customConditionsWithArgs = new HashMap<>();

    /**
     * 注册自定义条件处理器
     *
     * @param name     条件名称
     * @param handler  处理器
     */
    public static void registerCondition(@NotNull String name,
                                          @NotNull Function<Player, Boolean> handler) {
        customConditions.put(name.toLowerCase(), handler);
    }

    /**
     * 注册带参数的自定义条件处理器
     *
     * @param name     条件名称
     * @param handler  处理器，参数为 (player, args)
     */
    public static void registerConditionWithArgs(@NotNull String name,
                                                  @NotNull BiFunction<Player, String, Boolean> handler) {
        customConditionsWithArgs.put(name.toLowerCase(), handler);
    }

    /**
     * 评估条件表达式
     *
     * @param player     玩家
     * @param expression 条件表达式
     * @return 是否满足条件
     */
    public static boolean evaluate(@NotNull Player player, @NotNull String expression) {
        return evaluate(player, expression, Collections.emptyMap());
    }

    /**
     * 评估条件表达式
     *
     * @param player     玩家
     * @param expression 条件表达式
     * @param context    上下文变量
     * @return 是否满足条件
     */
    public static boolean evaluate(@NotNull Player player, @NotNull String expression,
                                    @NotNull Map<String, Object> context) {
        expression = expression.trim();

        // 空表达式默认为 true
        if (expression.isEmpty()) {
            return true;
        }

        // 处理逻辑运算符（注意顺序：先处理 ||，再处理 &&）
        // 实际上应该先处理 &&，因为 && 优先级高于 ||
        // 但为了简化，我们使用括号来明确优先级

        // 处理括号
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return evaluate(player, expression.substring(1, expression.length() - 1), context);
        }

        // 处理逻辑或 ||（最低优先级）
        int orIndex = findLogicalOperator(expression, "||");
        if (orIndex != -1) {
            String left = expression.substring(0, orIndex).trim();
            String right = expression.substring(orIndex + 2).trim();
            return evaluate(player, left, context) || evaluate(player, right, context);
        }

        // 处理逻辑与 &&
        int andIndex = findLogicalOperator(expression, "&&");
        if (andIndex != -1) {
            String left = expression.substring(0, andIndex).trim();
            String right = expression.substring(andIndex + 2).trim();
            return evaluate(player, left, context) && evaluate(player, right, context);
        }

        // 处理逻辑非 !
        if (expression.startsWith("!")) {
            return !evaluate(player, expression.substring(1).trim(), context);
        }

        // 变量替换
        expression = replaceVariables(expression, context);

        // 权限检查 permission:xxx
        if (expression.startsWith("permission:")) {
            String permission = expression.substring(11);
            return player.hasPermission(permission);
        }

        // 等级比较 player_level >= 10
        if (expression.startsWith("player_level")) {
            return evaluateComparison(player.getLevel(), expression.substring(12));
        }

        // 拥有物品 has_item:DIAMOND 或 has_item:DIAMOND:10
        if (expression.startsWith("has_item:")) {
            String itemSpec = expression.substring(9);
            return evaluateHasItem(player, itemSpec);
        }

        // 游戏模式检查
        if (expression.startsWith("gamemode:")) {
            String mode = expression.substring(10).toUpperCase();
            return player.getGameMode().name().equals(mode);
        }

        // 世界检查
        if (expression.startsWith("world:")) {
            String worldName = expression.substring(6);
            return player.getWorld().getName().equals(worldName);
        }

        // 自定义条件检查
        // 先检查带参数的条件
        for (Map.Entry<String, BiFunction<Player, String, Boolean>> entry : customConditionsWithArgs.entrySet()) {
            String prefix = entry.getKey() + ":";
            if (expression.toLowerCase().startsWith(prefix)) {
                String args = expression.substring(prefix.length());
                return entry.getValue().apply(player, args);
            }
        }

        // 再检查不带参数的条件
        Function<Player, Boolean> handler = customConditions.get(expression.toLowerCase());
        if (handler != null) {
            return handler.apply(player);
        }

        // 比较表达式 name >= value
        Matcher matcher = COMPARISON_PATTERN.matcher(expression);
        if (matcher.matches()) {
            String varName = matcher.group(1);
            String operator = matcher.group(2);
            int compareValue = Integer.parseInt(matcher.group(3));

            Object varValue = context.get(varName);
            if (varValue instanceof Number) {
                return evaluateComparison(((Number) varValue).intValue(), operator, compareValue);
            }
        }

        // 布尔值解析
        if (expression.equalsIgnoreCase("true")) return true;
        if (expression.equalsIgnoreCase("false")) return false;

        // 默认：尝试从上下文获取布尔值
        Object contextValue = context.get(expression);
        if (contextValue instanceof Boolean) {
            return (Boolean) contextValue;
        }

        return false;
    }

    /**
     * 查找逻辑运算符位置（忽略括号内的）
     */
    private static int findLogicalOperator(String expression, String operator) {
        int parenthesisDepth = 0;
        for (int i = 0; i < expression.length() - operator.length() + 1; i++) {
            char c = expression.charAt(i);
            if (c == '(') parenthesisDepth++;
            else if (c == ')') parenthesisDepth--;
            else if (parenthesisDepth == 0 && expression.substring(i).startsWith(operator)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 替换变量
     */
    private static String replaceVariables(String expression, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = context.get(varName);
            matcher.appendReplacement(sb, value != null ? value.toString() : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 评估比较表达式
     */
    private static boolean evaluateComparison(int playerValue, String expression) {
        expression = expression.trim();
        Matcher matcher = COMPARISON_PATTERN.matcher("player_level" + expression);
        if (matcher.matches()) {
            String operator = matcher.group(2);
            int compareValue = Integer.parseInt(matcher.group(3));
            return evaluateComparison(playerValue, operator, compareValue);
        }
        return false;
    }

    /**
     * 评估比较
     */
    private static boolean evaluateComparison(int value1, String operator, int value2) {
        return switch (operator) {
            case ">=" -> value1 >= value2;
            case "<=" -> value1 <= value2;
            case ">" -> value1 > value2;
            case "<" -> value1 < value2;
            case "==" -> value1 == value2;
            case "!=" -> value1 != value2;
            default -> false;
        };
    }

    /**
     * 评估是否拥有物品
     */
    private static boolean evaluateHasItem(Player player, String itemSpec) {
        String[] parts = itemSpec.split(":");
        Material material;
        int amount = 1;

        try {
            if (parts.length >= 1) {
                material = Material.valueOf(parts[0].toUpperCase());
            } else {
                return false;
            }

            if (parts.length >= 2) {
                amount = Integer.parseInt(parts[1]);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 检查玩家背包中是否有足够数量的该物品
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }

        return count >= amount;
    }

    /**
     * 清除所有自定义条件处理器
     */
    public static void clearCustomConditions() {
        customConditions.clear();
        customConditionsWithArgs.clear();
    }
}