package com.thenextlvl.foliagui.api.template;

import com.thenextlvl.foliagui.api.GUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * GUI模板接口
 * <p>
 * 模板允许开发者预定义GUI结构，然后通过参数动态生成具体的GUI实例。
 * 支持占位符替换、条件渲染和动态内容生成。
 *
 * @author TheNextLvl
 */
public interface GUITemplate {

    /**
     * 获取模板ID
     * @return 模板唯一标识符
     */
    @NotNull String getId();

    /**
     * 获取模板名称
     * @return 模板名称
     */
    @NotNull String getName();

    /**
     * 设置模板名称
     * @param name 名称
     * @return 此模板
     */
    GUITemplate name(@NotNull String name);

    /**
     * 获取模板描述
     * @return 描述信息
     */
    @Nullable String getDescription();

    /**
     * 设置模板描述
     * @param description 描述
     * @return 此模板
     */
    GUITemplate description(@Nullable String description);

    /**
     * 使用指定参数创建GUI实例
     * @param player 目标玩家
     * @param parameters 模板参数
     * @return 生成的GUI实例
     */
    @NotNull GUI create(@NotNull Player player, @NotNull Map<String, Object> parameters);

    /**
     * 使用默认参数创建GUI实例
     * @param player 目标玩家
     * @return 生成的GUI实例
     */
    @NotNull GUI create(@NotNull Player player);

    /**
     * 添加参数定义
     * @param name 参数名
     * @param type 参数类型
     * @param required 是否必需
     * @param defaultValue 默认值
     * @return 此模板
     */
    GUITemplate parameter(@NotNull String name, @NotNull Class<?> type, boolean required, @Nullable Object defaultValue);

    /**
     * 添加可选参数
     * @param name 参数名
     * @param type 参数类型
     * @return 此模板
     */
    default GUITemplate parameter(@NotNull String name, @NotNull Class<?> type) {
        return parameter(name, type, false, null);
    }

    /**
     * 添加必需参数
     * @param name 参数名
     * @param type 参数类型
     * @return 此模板
     */
    default GUITemplate requiredParameter(@NotNull String name, @NotNull Class<?> type) {
        return parameter(name, type, true, null);
    }

    /**
     * 注册占位符处理器
     * @param placeholder 占位符名称（不包含%符号）
     * @param resolver 解析函数
     * @return 此模板
     */
    GUITemplate registerPlaceholder(@NotNull String placeholder, @NotNull Function<TemplateContext, String> resolver);

    /**
     * 设置模板行数
     * @param rows 行数（1-6）
     * @return 此模板
     */
    GUITemplate rows(int rows);

    /**
     * 获取模板行数
     * @return 行数
     */
    int getRows();

    /**
     * 设置模板标题（支持占位符）
     * @param title 标题
     * @return 此模板
     */
    GUITemplate title(@NotNull String title);

    /**
     * 获取模板标题
     * @return 标题
     */
    @NotNull String getTitle();

    /**
     * 设置布局定义
     * @param layout 布局字符数组
     * @return 此模板
     */
    GUITemplate layout(@NotNull String... layout);

    /**
     * 获取布局定义
     * @return 布局数组
     */
    @NotNull String[] getLayout();

    /**
     * 注册字符对应的物品提供器
     * @param character 字符
     * @param itemProvider 物品提供器
     * @return 此模板
     */
    GUITemplate registerItem(char character, @NotNull Function<TemplateContext, ItemStack> itemProvider);

    /**
     * 注册字符对应的点击处理器
     * @param character 字符
     * @param action 动作名称
     * @return 此模板
     */
    GUITemplate registerAction(char character, @NotNull String action);

    /**
     * 设置父模板（继承配置）
     * @param parent 父模板
     * @return 此模板
     */
    GUITemplate parent(@Nullable GUITemplate parent);

    /**
     * 获取父模板
     * @return 父模板，如果没有则返回null
     */
    @Nullable GUITemplate getParent();

    /**
     * 验证参数是否合法
     * @param parameters 参数映射
     * @return 验证结果
     */
    ValidationResult validate(@NotNull Map<String, Object> parameters);

    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, @Nullable String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public @Nullable String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(@NotNull String message) {
            return new ValidationResult(false, message);
        }
    }
}
