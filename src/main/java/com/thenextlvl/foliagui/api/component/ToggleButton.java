package com.thenextlvl.foliagui.api.component;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 开关按钮组件接口
 * 支持状态切换（开/关）
 *
 * @author TheNextLvl
 */
public interface ToggleButton extends Component {

    /**
     * 获取当前状态
     * @return true为开启状态，false为关闭状态
     */
    boolean isToggled();

    /**
     * 设置状态
     * @param toggled 状态
     */
    void setToggled(boolean toggled);

    /**
     * 切换状态
     * @return 切换后的新状态
     */
    boolean toggle();

    /**
     * 设置开启状态的显示物品
     * @param item 物品
     * @return 此组件
     */
    ToggleButton onItem(@NotNull ItemStack item);

    /**
     * 设置关闭状态的显示物品
     * @param item 物品
     * @return 此组件
     */
    ToggleButton offItem(@NotNull ItemStack item);

    /**
     * 设置开启状态的显示物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    ToggleButton onItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 设置关闭状态的显示物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    ToggleButton offItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 设置状态切换处理器
     * @param handler 处理器
     * @return 此组件
     */
    ToggleButton onToggle(@NotNull Consumer<ToggleEvent> handler);

    /**
     * 设置状态变化监听器
     * @param listener 监听器
     * @return 此组件
     */
    ToggleButton onStateChange(@NotNull Consumer<Boolean> listener);

    /**
     * 开关事件
     */
    class ToggleEvent {
        private final ToggleButton button;
        private final boolean newState;
        private final boolean oldState;
        private final Player player;
        private boolean cancelled = false;

        public ToggleEvent(ToggleButton button, boolean newState, boolean oldState) {
            this(button, newState, oldState, null);
        }

        public ToggleEvent(ToggleButton button, boolean newState, boolean oldState, @Nullable Player player) {
            this.button = button;
            this.newState = newState;
            this.oldState = oldState;
            this.player = player;
        }

        public ToggleButton getButton() {
            return button;
        }

        public boolean getNewState() {
            return newState;
        }

        public boolean getOldState() {
            return oldState;
        }

        @Nullable
        public Player getPlayer() {
            return player;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
