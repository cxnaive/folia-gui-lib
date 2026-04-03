package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.InputField;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import com.thenextlvl.foliagui.api.input.ChatInputRequest;
import com.thenextlvl.foliagui.manager.ChatInputManager;
import com.thenextlvl.foliagui.manager.GUIManager;
import com.thenextlvl.foliagui.manager.SignInputManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 输入框组件实现
 *
 * @author TheNextLvl
 */
public class InputFieldImpl implements InputField {

    private final String id;
    private String text = "";
    private String placeholder = null;
    private int maxLength = -1;
    private Predicate<String> validator;
    private InputMode inputMode = InputMode.CHAT;
    private ItemStack normalItem;
    private ItemStack focusItem;
    private ItemStack errorItem;
    private Supplier<ItemStack> normalItemSupplier;
    private Supplier<ItemStack> focusItemSupplier;
    private Supplier<ItemStack> errorItemSupplier;
    private Consumer<TextChangeEvent> textChangeListener;
    private Consumer<SubmitEvent> submitListener;
    private boolean focus = false;
    private int slot = -1;
    private boolean interactable = true;
    private boolean movable = false;
    private Consumer<ClickEvent> clickHandler;

    public InputFieldImpl(@NotNull String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getText() {
        return text;
    }

    @Override
    public InputField text(@NotNull String text) {
        String oldText = this.text;
        this.text = text;
        if (maxLength > 0 && this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
        }
        if (!oldText.equals(this.text) && textChangeListener != null) {
            TextChangeEvent event = new TextChangeEvent(this, oldText, this.text, null);
            textChangeListener.accept(event);
        }
        return this;
    }

    @Override
    public @Nullable String getPlaceholder() {
        return placeholder;
    }

    @Override
    public InputField placeholder(@Nullable String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public InputField maxLength(int maxLength) {
        this.maxLength = maxLength;
        if (maxLength > 0 && text.length() > maxLength) {
            text = text.substring(0, maxLength);
        }
        return this;
    }

    @Override
    public @Nullable Predicate<String> getValidator() {
        return validator;
    }

    @Override
    public InputField validator(@Nullable Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public @NotNull InputMode getInputMode() {
        return inputMode;
    }

    @Override
    public InputField inputMode(@NotNull InputMode mode) {
        this.inputMode = mode;
        return this;
    }

    @Override
    public @NotNull ItemStack getNormalItem() {
        if (normalItemSupplier != null) {
            return normalItemSupplier.get();
        }
        if (normalItem != null) {
            return normalItem;
        }
        return createDefaultItem(Material.PAPER, "§7点击输入文本", text);
    }

    @Override
    public InputField normalItem(@NotNull ItemStack item) {
        this.normalItem = item.clone();
        this.normalItemSupplier = null;
        return this;
    }

    @Override
    public InputField normalItem(@NotNull Supplier<ItemStack> supplier) {
        this.normalItemSupplier = supplier;
        this.normalItem = null;
        return this;
    }

    @Override
    public @NotNull ItemStack getFocusItem() {
        if (focusItemSupplier != null) {
            return focusItemSupplier.get();
        }
        if (focusItem != null) {
            return focusItem;
        }
        return createDefaultItem(Material.WRITABLE_BOOK, "§e正在输入...", text);
    }

    @Override
    public InputField focusItem(@NotNull ItemStack item) {
        this.focusItem = item.clone();
        this.focusItemSupplier = null;
        return this;
    }

    @Override
    public InputField focusItem(@NotNull Supplier<ItemStack> supplier) {
        this.focusItemSupplier = supplier;
        this.focusItem = null;
        return this;
    }

    @Override
    public @NotNull ItemStack getErrorItem() {
        if (errorItemSupplier != null) {
            return errorItemSupplier.get();
        }
        if (errorItem != null) {
            return errorItem;
        }
        return createDefaultItem(Material.BARRIER, "§c输入无效", text);
    }

    @Override
    public InputField errorItem(@NotNull ItemStack item) {
        this.errorItem = item.clone();
        this.errorItemSupplier = null;
        return this;
    }

    @Override
    public InputField errorItem(@NotNull Supplier<ItemStack> supplier) {
        this.errorItemSupplier = supplier;
        this.errorItem = null;
        return this;
    }

    @Override
    public InputField onTextChange(@NotNull Consumer<TextChangeEvent> listener) {
        this.textChangeListener = listener;
        return this;
    }

    @Override
    public InputField onSubmit(@NotNull Consumer<SubmitEvent> listener) {
        this.submitListener = listener;
        return this;
    }

    @Override
    public boolean hasFocus() {
        return focus;
    }

    @Override
    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    @Override
    public boolean isValid() {
        if (validator == null) {
            return true;
        }
        return validator.test(text);
    }

    @Override
    public boolean submit(@Nullable Player player) {
        if (!isValid()) {
            return false;
        }
        if (submitListener != null) {
            SubmitEvent event = new SubmitEvent(this, text, player);
            submitListener.accept(event);
            return !event.isCancelled();
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        if (!isValid()) {
            return getErrorItem().clone();
        }
        if (focus) {
            return getFocusItem().clone();
        }
        return getNormalItem().clone();
    }

    private @NotNull ItemStack createDefaultItem(@NotNull Material material, @NotNull String displayName, @NotNull String loreText) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            if (!loreText.isEmpty()) {
                lore.add("§f" + loreText);
            } else if (placeholder != null && !placeholder.isEmpty()) {
                lore.add("§7" + placeholder);
            }
            if (maxLength > 0) {
                lore.add("§7" + loreText.length() + "/" + maxLength);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        this.normalItem = item.clone();
        this.focusItem = item.clone();
        this.errorItem = item.clone();
        this.normalItemSupplier = null;
        this.focusItemSupplier = null;
        this.errorItemSupplier = null;
    }

    @Override
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    @Override
    public boolean isMovable() {
        return movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    @Override
    public void onInteract(@NotNull InteractionEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // 处理输入框点击 - 这里可以触发输入流程
            handleInput(player);
        }

        if (clickHandler != null) {
            ClickEvent clickEvent = new ClickEvent(
                event.getPlayer(),
                event.getInventory(),
                event.getSlot(),
                event.getClickType(),
                event.getAction(),
                event.getCurrentItem(),
                event.getCursor()
            );
            clickHandler.accept(clickEvent);
            if (clickEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 处理输入流程
     * @param player 玩家
     */
    private void handleInput(@NotNull Player player) {
        setFocus(true);

        ChatInputManager inputManager = ChatInputManager.getInstance();
        if (inputManager == null) {
            player.sendMessage("§c输入系统未初始化！");
            return;
        }

        switch (inputMode) {
            case CHAT -> {
                inputManager.requestInput(player)
                        .prompt(placeholder != null ? placeholder : "§e请在聊天栏输入文本：")
                        .validator(validator)
                        .validationErrorMessage("§c输入无效，请重新输入：")
                        .restoreGUI(true)
                        .onComplete(result -> {
                            if (result.isSuccess()) {
                                text(result.getInput());
                                if (textChangeListener != null) {
                                    TextChangeEvent event = new TextChangeEvent(this, text, result.getInput(), player);
                                    textChangeListener.accept(event);
                                }
                                submit(player);
                            }
                            setFocus(false);
                        })
                        .onCancel(() -> setFocus(false))
                        .onTimeout(() -> setFocus(false))
                        .submit();
            }
            case ANVIL -> {
                // 铁砧输入模式不需要实现（用户要求跳过）
                player.sendMessage("§c铁砧输入模式暂未实现，请使用聊天输入模式。");
                setFocus(false);
            }
            case SIGN -> {
                SignInputManager signManager = SignInputManager.getInstance();
                if (signManager == null) {
                    player.sendMessage("§c告示牌输入系统未初始化！");
                    setFocus(false);
                    return;
                }

                String[] lines = new String[4];
                lines[0] = placeholder != null ? placeholder : "§e请输入文本";
                lines[1] = "§7(编辑后关闭)";
                lines[2] = "";
                lines[3] = "";

                signManager.requestSignInput(player, lines, result -> {
                    if (result.isSuccess()) {
                        text(result.getText());
                        if (textChangeListener != null) {
                            TextChangeEvent event = new TextChangeEvent(this, text, result.getText(), player);
                            textChangeListener.accept(event);
                        }
                        submit(player);
                    }
                    setFocus(false);
                });
            }
            case VIRTUAL_KEYBOARD -> {
                // TODO: 虚拟键盘输入实现
                player.sendMessage("§c虚拟键盘输入模式暂未实现，请使用聊天输入模式。");
                setFocus(false);
            }
        }
    }

    @Override
    public Component onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public Component permission(@Nullable String permission) {
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return null;
    }

    @Override
    public Component condition(@Nullable Predicate<Player> condition) {
        return this;
    }

    @Override
    public Component refreshInterval(int ticks) {
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return -1; // InputField 不需要自动刷新
    }

    @Override
    public @NotNull Component clone() {
        InputFieldImpl cloned = new InputFieldImpl(id);
        cloned.text = this.text;
        cloned.placeholder = this.placeholder;
        cloned.maxLength = this.maxLength;
        cloned.inputMode = this.inputMode;
        cloned.focus = this.focus;
        cloned.interactable = this.interactable;
        cloned.movable = this.movable;
        cloned.validator = this.validator;
        if (this.normalItem != null) {
            cloned.normalItem = this.normalItem.clone();
        }
        if (this.focusItem != null) {
            cloned.focusItem = this.focusItem.clone();
        }
        if (this.errorItem != null) {
            cloned.errorItem = this.errorItem.clone();
        }
        cloned.normalItemSupplier = this.normalItemSupplier;
        cloned.focusItemSupplier = this.focusItemSupplier;
        cloned.errorItemSupplier = this.errorItemSupplier;
        cloned.textChangeListener = this.textChangeListener;
        cloned.submitListener = this.submitListener;
        return cloned;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }
}
