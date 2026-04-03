package com.thenextlvl.foliagui.engine;

import com.thenextlvl.foliagui.annotation.Action;
import com.thenextlvl.foliagui.annotation.ActionChain;
import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

/**
 * 动作执行器
 * <p>
 * 执行内置动作和自定义动作处理器
 * 支持：
 * <ul>
 *   <li>close - 关闭 GUI</li>
 *   <li>refresh - 刷新 GUI</li>
 *   <li>tell:message - 发送消息</li>
 *   <li>command:cmd - 玩家执行命令</li>
 *   <li>console:cmd - 控制台执行命令</li>
 *   <li>sound:sound - 播放音效</li>
 *   <li>open:gui-id - 打开其他 GUI</li>
 *   <li>give:material amount - 给予物品</li>
 *   <li>take:material amount - 取走物品</li>
 *   <li>confirm:dialog-id - 打开确认对话框</li>
 *   <li>input:dialog-id - 打开输入对话框</li>
 * </ul>
 *
 * @author TheNextLvl
 */
public class ActionExecutor {

    private final Plugin plugin;
    private final GUI gui;
    private final Map<String, BiFunction<Player, String, Boolean>> customHandlers = new HashMap<>();

    public ActionExecutor(@NotNull Plugin plugin, @Nullable GUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    /**
     * 注册自定义动作处理器
     *
     * @param name    动作名称
     * @param handler 处理器，返回 true 表示成功
     */
    public void registerHandler(@NotNull String name,
                                 @NotNull BiFunction<Player, String, Boolean> handler) {
        customHandlers.put(name.toLowerCase(), handler);
    }

    /**
     * 执行单个动作
     *
     * @param player 玩家
     * @param action 动作字符串
     * @return 是否成功
     */
    public boolean execute(@NotNull Player player, @NotNull String action) {
        return execute(player, action, Collections.emptyMap());
    }

    /**
     * 执行单个动作
     *
     * @param player  玩家
     * @param action  动作字符串
     * @param context 上下文变量
     * @return 是否成功
     */
    public boolean execute(@NotNull Player player, @NotNull String action,
                           @NotNull Map<String, Object> context) {
        action = action.trim();
        if (action.isEmpty()) return true;

        String lowerAction = action.toLowerCase();

        // close - 关闭 GUI
        if (lowerAction.equals("close")) {
            player.closeInventory();
            return true;
        }

        // refresh - 刷新 GUI
        if (lowerAction.equals("refresh")) {
            if (gui != null) {
                gui.refresh();
            }
            return true;
        }

        // tell:message - 发送消息
        if (lowerAction.startsWith("tell:") || lowerAction.startsWith("tell ")) {
            String message = action.substring(5);
            message = replaceVariables(message, context);
            player.sendMessage(message.replace('&', '§'));
            return true;
        }

        // command:cmd - 玩家执行命令
        if (lowerAction.startsWith("command:") || lowerAction.startsWith("command ")) {
            String cmd = action.substring(8);
            cmd = replaceVariables(cmd, context);
            player.performCommand(cmd);
            return true;
        }

        // console:cmd - 控制台执行命令
        if (lowerAction.startsWith("console:") || lowerAction.startsWith("console ")) {
            String cmd = action.substring(8);
            cmd = replaceVariables(cmd, context);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return true;
        }

        // sound:sound [volume] [pitch] - 播放音效
        if (lowerAction.startsWith("sound:") || lowerAction.startsWith("sound ")) {
            return executeSound(player, action.substring(6));
        }

        // open:gui-id - 打开其他 GUI
        if (lowerAction.startsWith("open:") || lowerAction.startsWith("open ")) {
            String guiId = action.substring(5).trim();
            // TODO: 打开其他 GUI
            return true;
        }

        // give:material amount - 给予物品
        if (lowerAction.startsWith("give:") || lowerAction.startsWith("give ")) {
            return executeGive(player, action.substring(5));
        }

        // take:material amount - 取走物品
        if (lowerAction.startsWith("take:") || lowerAction.startsWith("take ")) {
            return executeTake(player, action.substring(5));
        }

        // confirm:dialog-id - 打开确认对话框
        if (lowerAction.startsWith("confirm:") || lowerAction.startsWith("confirm ")) {
            String dialogId = action.substring(8).trim();
            // TODO: 打开确认对话框
            return true;
        }

        // input:dialog-id - 打开输入对话框
        if (lowerAction.startsWith("input:") || lowerAction.startsWith("input ")) {
            String dialogId = action.substring(6).trim();
            // TODO: 打开输入对话框
            return true;
        }

        // 自定义动作处理器
        String[] parts = action.split("\\s+", 2);
        String handlerName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        BiFunction<Player, String, Boolean> handler = customHandlers.get(handlerName);
        if (handler != null) {
            return handler.apply(player, args);
        }

        return true;
    }

    /**
     * 执行动作链
     *
     * @param player      玩家
     * @param actionChain 动作链
     * @return 是否全部成功
     */
    public boolean executeChain(@NotNull Player player, @NotNull ActionChain actionChain) {
        return executeChain(player, actionChain, Collections.emptyMap());
    }

    /**
     * 执行动作链
     *
     * @param player      玩家
     * @param actionChain 动作链
     * @param context     上下文变量
     * @return 是否全部成功
     */
    public boolean executeChain(@NotNull Player player, @NotNull ActionChain actionChain,
                                 @NotNull Map<String, Object> context) {
        // 检查条件
        if (!actionChain.condition().isEmpty()) {
            if (!ConditionEngine.evaluate(player, actionChain.condition(), context)) {
                // 条件不满足，执行 deny 动作
                for (String deny : actionChain.deny()) {
                    execute(player, deny, context);
                }
                return false;
            }
        }

        boolean allSuccess = true;

        for (Action action : actionChain.value()) {
            // 检查动作条件
            if (action.condition().length() > 0) {
                if (!ConditionEngine.evaluate(player, action.condition(), context)) {
                    // 条件不满足
                    for (String deny : action.deny()) {
                        execute(player, deny, context);
                    }
                    if (!action.continueOnFail()) {
                        allSuccess = false;
                        break;
                    }
                    continue;
                }
            }

            // 延迟执行
            if (action.delay() > 0) {
                final String actionStr = String.join("", action.value());
                final Map<String, Object> ctx = new HashMap<>(context);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (String a : action.value()) {
                        execute(player, a, ctx);
                    }
                }, action.delay());
            } else {
                // 立即执行
                for (String a : action.value()) {
                    boolean success = execute(player, a, context);
                    if (!success && !action.continueOnFail()) {
                        if (!action.failMessage().isEmpty()) {
                            player.sendMessage(action.failMessage().replace('&', '§'));
                        }
                        allSuccess = false;
                        break;
                    }
                }
            }
        }

        // 执行完成后的动作
        if (allSuccess) {
            for (String complete : actionChain.onComplete()) {
                execute(player, complete, context);
            }
        }

        return allSuccess;
    }

    /**
     * 创建点击事件处理器
     *
     * @param actions 动作数组
     * @return 点击事件处理器
     */
    public java.util.function.Consumer<ClickEvent> createClickHandler(@NotNull String[] actions) {
        return event -> {
            for (String action : actions) {
                execute(event.getPlayer(), action);
            }
            event.setCancelled(true);
        };
    }

    // ==================== 内部方法 ====================

    private boolean executeSound(Player player, String soundSpec) {
        String[] parts = soundSpec.trim().split("\\s+");
        try {
            Sound sound = Sound.valueOf(parts[0].toUpperCase().replace(".", "_"));
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean executeGive(Player player, String itemSpec) {
        String[] parts = itemSpec.trim().split("\\s+");
        try {
            Material material = Material.valueOf(parts[0].toUpperCase());
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean executeTake(Player player, String itemSpec) {
        String[] parts = itemSpec.trim().split("\\s+");
        try {
            Material material = Material.valueOf(parts[0].toUpperCase());
            int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

            // 检查是否有足够的物品
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    count += item.getAmount();
                }
            }

            if (count < amount) {
                return false;
            }

            // 取走物品
            int remaining = amount;
            for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == material) {
                    int take = Math.min(remaining, item.getAmount());
                    item.setAmount(item.getAmount() - take);
                    remaining -= take;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String replaceVariables(String text, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}",
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return text;
    }
}