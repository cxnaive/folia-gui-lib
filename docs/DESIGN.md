# FoliaGUI 设计理念与架构文档

## 1. 项目概述

FoliaGUI 是一个面向 Minecraft 插件开发者的高版本 GUI 库，以 Folia 为首要目标平台，同时兼容 Paper 和 Spigot。

### 核心定位
- **目标用户**: 插件开发者（Java/Kotlin）
- **使用方式**: 嵌入式库（非独立插件）
- **首要平台**: Folia（Region-based 多线程）
- **设计理念**: 简洁、灵活、高性能

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Developer Code Layer                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Builder    │  │  Annotation │  │  Custom Components  │  │
│  │  Pattern    │  │  Pattern    │  │  & Logic            │  │
│  │  (链式构建)  │  │  (声明式)    │  │  (自定义扩展)       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    FoliaGUI Framework                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Folia      │  │  Component  │  │  Event              │  │
│  │  Scheduler  │  │  System     │  │  System             │  │
│  │  (统一调度)  │  │  (组件系统)  │  │  (事件处理)         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  GUI        │  │  Animation  │  │  Data Binding       │  │
│  │  Manager    │  │  Engine     │  │  (数据绑定)         │  │
│  │  (生命周期)  │  │  (动画引擎)  │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    Platform Adapter Layer                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Folia      │  │  Paper      │  │  Spigot             │  │
│  │  Adapter    │  │  Adapter    │  │  Adapter            │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    Minecraft Native Layer                    │
│              (Bukkit API, NMS, Packets)                      │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 模块职责

| 模块 | 职责 | 核心类 |
|------|------|--------|
| `folia` | Folia 调度器封装 | `FoliaScheduler` |
| `api` | 公共 API 接口 | `GUI`, `View`, `Component`, `GUIEvent` |
| `api/component` | 组件接口与实现 | `Button`, `ToggleButton`, `Pagination` |
| `api/event` | 事件系统 | `ClickEvent`, `OpenEvent`, `CloseEvent` |
| `api/config` | 配置相关 | `GUIConfigProvider`, `GUIConfigSnapshot` |
| `builder` | Builder 模式实现 | `GUIBuilder`, `ItemBuilder` |
| `annotation` | 声明式注解 | `@GUIConfig`, `@Button`, `@Action` |
| `internal` | 内部实现 | `GUIManager`, `AbstractGUI`, `DeclarativeGUI` |

---

## 3. 核心设计原则

### 3.1 Folia 优先

- **Region 感知**: 所有 GUI 操作自动在正确的 Region 线程执行
- **线程安全**: 开发者无需关心线程切换
- **性能优化**: 充分利用 Folia 的并行处理能力

### 3.2 开发者体验优先

- **简洁 API**: 链式调用，流畅的代码体验
- **类型安全**: 编译期检查，减少运行时错误
- **IDE 友好**: 完整的泛型和注解支持

### 3.3 灵活性

- **双模式支持**: Builder 模式 + 声明式注解
- **可扩展**: 易于自定义组件和事件
- **无侵入**: 不强制特定设计模式

---

## 4. 使用模式

### 4.1 Builder 模式（传统）

```java
GUI gui = GUIBuilder.create("shop")
    .title("§6商店")
    .rows(3)
    .button(11, ItemBuilder.of(Material.DIAMOND)
        .name("§b钻石")
        .lore("§7价格: 100金币")
        .build(), 
        event -> buy(event.getPlayer(), Material.DIAMOND))
    .button(15, ItemBuilder.of(Material.BARRIER)
        .name("§c关闭")
        .build(),
        event -> event.getGUI().close(event.getPlayer()))
    .build();

gui.open(player);
```

**适用场景**:
- 动态生成的 GUI
- 复杂的业务逻辑
- 需要运行时计算的场景

### 4.2 声明式注解模式（推荐）

```java
@GUIConfig(
    id = "shop",
    title = "§6商店",
    rows = 3
)
public class ShopMenu extends DeclarativeGUI {
    
    @Button(
        slot = 11,
        material = Material.DIAMOND,
        name = "§b钻石",
        lore = {"§7价格: 100金币"}
    )
    @Action("buy DIAMOND 100")
    private Button diamondButton;
    
    @Button(
        slot = 15,
        material = Material.BARRIER,
        name = "§c关闭"
    )
    @Action("close")
    private Button closeButton;
    
    @ActionHandler("buy")
    public void onBuy(Player player, String args) {
        String[] parts = args.split(" ");
        Material material = Material.valueOf(parts[0]);
        int price = Integer.parseInt(parts[1]);
        // 处理购买逻辑
    }
}
```

**适用场景**:
- 静态或半静态 GUI
- 配置驱动的界面
- 需要导出配置的场景

---

## 5. FoliaScheduler 设计

### 5.1 核心功能

```java
public final class FoliaScheduler {
    // 智能路由 - 自动选择正确的调度器
    public static void runForPlayer(Player player, Runnable task);
    public static void runAtLocation(Location location, Runnable task);
    public static void runGlobal(Runnable task);
    
    // 延迟/定时任务
    public static ScheduledTask runLater(Player player, long delay, Runnable task);
    public static ScheduledTask runTimer(Player player, long delay, long period, Runnable task);
    
    // 异步转同步（关键！）
    public static <T> CompletableFuture<T> supplyAsyncThenReturnToPlayer(
        Player player, Supplier<T> asyncTask);
}
```

### 5.2 自动平台检测

```java
public static boolean isFolia() {
    try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        return true;
    } catch (ClassNotFoundException e) {
        return false;
    }
}
```

---

## 6. 组件系统设计

### 6.1 组件接口

```java
public interface Component {
    String getId();
    ItemStack getDisplayItem();
    void setDisplayItem(ItemStack item);
    boolean isInteractable();
    void onInteract(InteractionEvent event);
    Component clone();
}
```

### 6.2 内置组件

| 组件 | 功能 | 特性 |
|------|------|------|
| `Button` | 基础按钮 | 点击事件、条件显示 |
| `ToggleButton` | 开关按钮 | 状态切换、双向绑定 |
| `Pagination` | 分页组件 | 多页管理、导航按钮 |
| `InputField` | 输入框 | 铁砧/聊天/告示牌输入 |
| `ProgressBar` | 进度条 | 动态进度显示 |
| `AnimatedIcon` | 动画图标 | 帧动画支持 |

---

## 7. 事件系统设计

### 7.1 事件层次

```
GUIEvent (abstract)
├── ClickEvent
│   ├── ButtonClickEvent
│   └── InventoryClickEvent
├── OpenEvent
├── CloseEvent
├── DragEvent
└── CustomEvent (for extension)
```

### 7.2 事件监听

```java
// 方式1: 函数式注册
gui.onClick(event -> {
    // 处理点击
});

// 方式2: 注解方式
@GUIEventHandler(priority = EventPriority.HIGH)
public void onClick(ClickEvent event) {
    // 处理点击
}
```

---

## 8. 声明式配置系统

### 8.1 注解列表

| 注解 | 目标 | 用途 |
|------|------|------|
| `@GUIConfig` | Class | GUI 基础配置 |
| `@Button` | Field | 按钮定义 |
| `@Action` | Field | 点击动作 |
| `@Refresh` | Field | 自动刷新 |
| `@Template` | Field | 模板定义 |
| `@Input` | Field | 输入捕获 |
| `@Pagination` | Class | 分页配置 |
| `@ActionHandler` | Method | 自定义动作处理器 |

### 8.2 配置导出

支持导出格式:
- **YAML**: 供参考和手动修改
- **Java**: 可直接使用的代码
- **JSON**: 数据交换
- **Builder**: 传统 Builder 风格代码

---

## 9. 聊天输入系统

### 9.1 概述

聊天输入系统允许任何按钮触发输入操作，流程如下：
1. 点击按钮后关闭现有GUI
2. 发送提示消息
3. 截取玩家下一次聊天消息
4. 恢复之前的GUI

### 9.2 核心 API

```java
// ChatInputRequest 接口
public interface ChatInputRequest {
    String getId();
    Player getPlayer();
    String getPrompt();
    String[] getCancelKeywords();
    boolean isCancelled();
    void cancel();
    long getTimeout();
    long getRemainingTime();

    // 输入结果
    class InputResult {
        String getInput();      // 输入内容
        boolean isCancelled();  // 是否取消
        boolean isTimedOut();   // 是否超时
        boolean isSuccess();    // 是否成功获取
        Player getPlayer();
    }

    // 构建器
    interface Builder {
        Builder prompt(String prompt);
        Builder cancelKeywords(String... keywords);
        Builder timeout(long milliseconds);
        Builder validator(Predicate<String> validator);
        Builder validationErrorMessage(String message);
        Builder restoreGUI(boolean restore);
        Builder onComplete(Consumer<InputResult> callback);
        Builder onCancel(Runnable callback);
        Builder onTimeout(Runnable callback);
        ChatInputRequest submit();
    }
}
```

### 9.3 使用方式

```java
// 方式1: GUIBuilder 快捷方法
GUIBuilder.create("my-gui")
    .inputButton(10, item, "请输入名称：", result -> {
        if (result.isSuccess()) {
            player.sendMessage("你输入了: " + result.getInput());
        }
    })
    .build();

// 方式2: 自定义配置
.inputButton(11, item, builder -> builder
    .prompt("请输入数字(1-100)：")
    .validator(input -> {
        try {
            int num = Integer.parseInt(input);
            return num >= 1 && num <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    })
    .validationErrorMessage("§c请输入有效数字！")
    .timeout(60000)
    .restoreGUI(true)
    .onComplete(result -> {
        if (result.isSuccess()) {
            int num = Integer.parseInt(result.getInput());
            // 处理数字
        }
    })
)

// 方式3: 直接使用 ChatInputManager
ChatInputManager inputManager = ChatInputManager.getInstance();
inputManager.requestInput(player)
    .prompt("§e请输入内容：")
    .restoreGUI(true)
    .onComplete(result -> {
        // 处理结果
    })
    .submit();
```

### 9.4 特性

| 特性 | 说明 |
|------|------|
| 自动恢复GUI | 输入完成后自动重新打开之前的GUI |
| 输入验证 | 支持自定义验证器和错误消息 |
| 超时处理 | 可设置超时时间，超时后自动取消 |
| 取消关键词 | 支持自定义取消关键词（默认 "cancel", "取消"） |
| 线程安全 | 使用 FoliaScheduler 确保正确的线程调度 |

---

## 10. 全局占位符系统

### 10.1 概述

全局占位符系统允许在物品名称和 Lore 中使用 `%placeholder%` 格式，在显示时自动解析为实际值。

### 10.2 使用方式

```java
// 注册自定义占位符（一次注册，全局可用）
PlaceholderManager.register("balance", player -> economy.getBalance(player));
PlaceholderManager.register("rank", player -> getRank(player));

// 使用占位符 - 无需改变写法习惯
ItemBuilder.of(Material.DIAMOND)
    .name("§b%player% 的钻石")
    .lore(
        "§e余额: %balance%",
        "§7等级: %rank%",
        "§a生命: %health%"
    )
    .build();

// GUI 打开时自动解析
```

### 10.3 内置占位符

| 占位符 | 说明 |
|--------|------|
| `%player%` | 玩家名称 |
| `%player_name%` | 玩家名称 |
| `%player_uuid%` | 玩家 UUID |
| `%player_displayname%` | 玩家显示名 |
| `%world%` | 当前世界 |
| `%x%`, `%y%`, `%z%` | 坐标 |
| `%health%` | 当前生命值 |
| `%max_health%` | 最大生命值 |
| `%food%` | 饱食度 |
| `%level%` | 等级 |
| `%exp%` | 经验百分比 |
| `%gamemode%` | 游戏模式 |
| `%online%` | 在线人数 |
| `%max_players%` | 最大玩家数 |
| `%time%` | 游戏时间 |
| `%date%` | 当前日期 |
| `%time_real%` | 当前时间 |

### 10.4 API

```java
// 注册占位符
PlaceholderManager.register(String name, Function<Player, String> resolver);

// 注销占位符
PlaceholderManager.unregister(String name);

// 解析文本
String result = PlaceholderManager.resolve(Player player, String text);
```

---

## 11. 组件持续刷新

### 11.1 概述

组件支持设置刷新间隔，GUI 打开后自动按间隔刷新显示内容。

### 11.2 使用方式

```java
GUIBuilder.create("stats")
    .button(10, item, event -> {})
    .refreshInterval(20)  // 每 20 tick (1秒) 刷新一次
    .build();
```

### 11.3 刷新机制

- GUI 打开时自动启动刷新任务
- GUI 关闭时自动停止刷新任务
- 只刷新设置了 `refreshInterval > 0` 的组件
- 结合 PlaceholderManager 实现动态更新

---

## 12. 工具插件（FoliaGUI Toolkit）

### 12.1 功能定位

- **只读查看**: 查看其他插件的 GUI 配置
- **独立创建**: 可视化创建新 GUI
- **配置导出**: 导出各种格式的配置
- **不干涉运行**: 不影响其他插件的正常运行

### 9.2 命令设计

```
/fgt list [plugin]              # 列出 GUI
/fgt view <gui-id>              # 查看配置
/fgt export <gui-id> [format]   # 导出配置
/fgt create <id> [rows]         # 创建 GUI
/fgt edit [id]                  # 编辑 GUI
/fgt save [id]                  # 保存
/fgt delete <id>                # 删除
```

---

## 13. 性能优化策略

### 13.1 更新优化 ✅ 已实现

- **脏检查**: ✅ 只更新变化的槽位（markDirty + flushDirtySlots）
- **批量更新**: ✅ 合并多个更新请求（延迟 1 tick 批量处理）
- **延迟刷新**: ✅ 避免频繁刷新

### 13.2 内存优化

- **对象池**: ❌ 复用 ItemStack 对象（未实现）
- **弱引用**: ✅ viewer 使用弱引用避免内存泄漏
- **懒加载**: ✅ 按需创建组件

### 13.3 线程优化 ✅ 已实现

- **Region 绑定**: ✅ FoliaScheduler 减少线程切换
- **异步处理**: ✅ 数据库操作异步化
- **任务合并**: ✅ 相同 Region 的任务合并执行

---

## 14. 与 TrMenu 对比

| 特性 | TrMenu | FoliaGUI |
|------|--------|----------|
| 目标用户 | 服务器管理员 | 插件开发者 |
| 使用方式 | YAML 配置 | Java/Kotlin 代码 |
| Folia 支持 | ❌ | ✅ 原生支持 |
| 灵活性 | 受限于配置 | 代码无限制 |
| 学习成本 | 需学 Kether | 标准 Java |
| 配置导出 | YAML 为主 | 多格式支持 |
| 可视化编辑 | 独立编辑器 | 游戏内编辑 |

---

## 15. 开发计划

### Phase 1: 核心基础 ✅ 已完成

| 模块 | 状态 | 说明 |
|------|------|------|
| 项目架构设计 | ✅ | 多层架构已建立 |
| FoliaScheduler 实现 | ✅ | 支持Folia/Paper/Spigot自动检测 |
| 核心 API 接口 | ✅ | GUI, Component, Event接口 |
| Builder 模式 | ✅ | GUIBuilder, ItemBuilder |
| 声明式注解 | ⚠️ | 注解已定义，DeclarativeGUI基础实现 |
| AbstractGUI 基类 | ✅ | 包含事件处理、刷新等 |
| GUIManager | ✅ | GUI注册、打开、关闭管理 |

### Phase 2: 组件系统

| 组件 | 状态 | 优先级 | 说明 |
|------|------|--------|------|
| Button 组件 | ✅ | - | Button, DynamicButton |
| ToggleButton 组件 | ✅ | - | 开关按钮，带状态切换 |
| Pagination 组件 | ✅ | - | PaginationImpl，支持多页导航 |
| InputField 组件 | ✅ | 高 | 输入框（聊天输入+告示牌输入已完成） |
| ProgressBar 组件 | ✅ | 中 | 单图标组件，Lore显示文本进度条，支持多种样式 |
| Slider 组件 | ✅ | 中 | 单图标组件，Lore显示滑块轨道，支持多种样式 |
| SearchField 组件 | ✅ | 中 | 搜索框组件 |
| SortButton 组件 | ✅ | 中 | 排序按钮组件 |
| ScrollableList 组件 | ✅ | 低 | 可滚动列表，接口和实现已完成 |
| AnimatedIcon 组件 | ✅ | 低 | 帧动画图标，支持多种播放模式 |

### Phase 3: 高级功能

| 功能 | 状态 | 优先级 | 说明 |
|------|------|--------|------|
| 动画引擎 | ✅ | 低 | Animation Engine，支持缓动函数和循环 |
| 数据绑定 | ✅ | 低 | ObservableValue, ObservableList 响应式数据绑定 |
| 玩家 Inventory 菜单 | ✅ | - | usePlayerInventory功能 |
| 聊天输入系统 | ✅ | 高 | ChatInputManager，支持验证、超时、取消 |
| 配置导出系统 | ✅ | 低 | YAML/JSON/Java Builder 导出 |
| 模板系统 | ✅ | 中 | TemplateManager，支持参数化和占位符 |
| 音效系统 | ✅ | 低 | GUISound + SoundConfig，支持自定义音效 |
| GUI历史记录 | ✅ | 中 | GUIHistoryImpl 已实现，支持后退/前进/浏览 |
| ItemSlot 组件 | ✅ | 中 | 物品填充格，支持放入/取出物品，自动返还 |
| 全局占位符系统 | ✅ | 高 | PlaceholderManager，Lore/Name 支持 %placeholder% |
| 组件持续刷新 | ✅ | 中 | refreshInterval 定时刷新，自动更新显示 |

### Phase 4: 工具插件

| 功能 | 状态 | 说明 |
|------|------|------|
| 配置查看器 | ✅ | /fgt list, /fgt view 命令 |
| 可视化编辑器 | ✅ | 浮动工具栏，支持翻页和位置调整 |
| 配置生成器 | ✅ | /fgt export 导出 YAML/JSON/Java |
| 配置创建 | ✅ | /fgt create 创建新 GUI |
| 配置保存 | ✅ | /fgt save 保存 GUI 配置 |

### Phase 5: 声明式配置系统

| 功能 | 状态 | 说明 |
|------|------|------|
| @GUIConfig 注解 | ✅ | 已定义并实现解析 |
| @Button 注解 | ✅ | 已定义并实现解析 |
| @Action 注解 | ✅ | 已定义并实现解析 |
| DeclarativeGUI 基类 | ✅ | AbstractDeclarativeGUI 完整实现 |
| YAML配置加载 | ✅ | YamlConfigLoader 支持 YAML 加载 |
| 配置热重载 | ✅ | GUIConfigReloader 支持运行时重载 |

---

## 16. 最佳实践

### 16.1 选择合适的模式

- **静态菜单**: 使用声明式注解
- **动态菜单**: 使用 Builder 模式
- **复杂逻辑**: Builder + 自定义组件

### 16.2 性能建议

- 避免在 `onRender` 中创建新对象
- 使用 `updateSlot()` 替代 `refresh()`
- 大数据量使用分页组件
- 异步操作使用 `supplyAsyncThenReturnToPlayer()`

### 16.3 线程安全

- 不要直接在异步线程操作 GUI
- 使用 `FoliaScheduler.runForPlayer()` 确保线程安全
- 数据更新使用脏检查机制

---

## 17. 示例项目结构

```
my-plugin/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/myplugin/
│       │       ├── MyPlugin.java
│       │       ├── gui/
│       │       │   ├── ShopMenu.java          # 声明式 GUI
│       │       │   ├── MainMenu.java          # 声明式 GUI
│       │       │   └── DynamicShop.java       # Builder 模式 GUI
│       │       └── listener/
│       └── resources/
│           └── plugin.yml
├── pom.xml
└── README.md
```

---

## 18. 许可证

MIT License - 自由使用，保留署名

---

*文档版本: 1.7*
*最后更新: 2026-03-29*
