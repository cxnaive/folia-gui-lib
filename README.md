# FoliaGUI - 高版本Minecraft GUI库

一个面向Folia/Paper/Spigot的高版本GUI库，为插件开发者提供简洁、优雅的GUI创建接口。

## ✨ 核心特性

- **🚀 Folia优先** - 原生支持Folia的Region-based调度，同时兼容Paper/Spigot
- **🎯 零调度烦恼** - 内置FoliaScheduler，开发者无需关心线程问题
- **📝 双模式API** - Builder模式 + 声明式注解（TrMenu风格）
- **🎨 可视化布局** - 字符串布局定义，直观易懂
- **🎒 背包菜单** - 支持将玩家背包作为菜单的一部分
- **⚡ 高性能** - 异步支持、智能缓存、最小化性能开销

## 📦 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.thenextlvl</groupId>
    <artifactId>folia-gui</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 初始化

```java
@Override
public void onEnable() {
    // 初始化GUIManager（同时初始化FoliaScheduler）
    GUIManager manager = GUIManager.init(this);
}
```

## 🛠️ 使用方式

### 方式1: Builder模式

```java
GUI menu = GUIBuilder.create("main-menu")
    .title("&6主菜单")
    .rows(3)
    .border(Material.BLACK_STAINED_GLASS_PANE)
    .button(11, 
        ItemBuilder.of(Material.DIAMOND_SWORD)
            .name("&b起床战争")
            .lore("&7点击加入")
            .build(),
        event -> {
            event.getPlayer().sendMessage("§a正在传送...");
            event.setCancelled(true);
        }
    )
    .button(15,
        ItemBuilder.of(Material.BARRIER)
            .name("&c关闭")
            .build(),
        event -> event.getPlayer().closeInventory()
    )
    .build();

manager.register(menu);
manager.open(player, "main-menu");
```

### 方式2: 声明式注解（TrMenu风格）

```java
@GUIConfig(
    id = "game-selector",
    title = {"&6欢迎, %player_name%", "&b点击选择游戏"},
    titleUpdate = 40,
    rows = 5
)
@Layout({
    "###################",
    "#  A   B   C   D  #",
    "#  E   F   G   H  #",
    "#                 #",
    "###################"
})
public class GameSelectorMenu extends DeclarativeGUI {
    
    @Icon(id = "A", material = Material.DIAMOND_SWORD, 
          name = "&b起床战争", lore = {"&7点击加入"})
    @Action(value = "server bw")
    private Icon bedwars;
    
    @Icon(id = "#", material = Material.BLACK_STAINED_GLASS_PANE, name = " ")
    private Icon decoration;
}
```

## 📁 项目结构

```
folia-gui-lib/
├── src/main/java/com/thenextlvl/foliagui/
│   ├── api/                    # 核心API接口
│   │   ├── GUI.java           # GUI接口
│   │   └── component/         # 组件接口
│   │       └── Component.java
│   ├── annotation/            # 声明式注解
│   │   ├── GUIConfig.java     # GUI配置
│   │   ├── Layout.java        # 布局定义
│   │   ├── Icon.java          # 图标定义
│   │   ├── Button.java        # 按钮定义
│   │   ├── Action.java        # 动作定义
│   │   └── PlayerInventoryLayout.java
│   ├── builder/               # Builder模式
│   │   ├── GUIBuilder.java    # GUI构建器
│   │   └── ItemBuilder.java   # 物品构建器
│   ├── internal/              # 内部实现
│   │   └── AbstractGUI.java   # GUI基类
│   ├── manager/               # 管理器
│   │   └── GUIManager.java    # GUI管理器
│   └── folia/                 # Folia支持
│       └── FoliaScheduler.java # 调度器工具
├── examples/                  # 使用示例
│   └── ExampleUsage.java
└── docs/                      # 文档
    └── DESIGN.md             # 设计文档
```

## 🎨 支持的注解

| 注解 | 用途 | 示例 |
|------|------|------|
| `@GUIConfig` | GUI基本配置 | `@GUIConfig(id="menu", title="&6标题", rows=3)` |
| `@Layout` | 可视化布局 | `@Layout({"###", "#A#", "###"})` |
| `@Icon` | 定义图标 | `@Icon(id="A", material=DIAMOND)` |
| `@Button` | 直接定义按钮 | `@Button(slot=10, material=DIAMOND)` |
| `@Action` | 点击动作 | `@Action(value="server lobby")` |
| `@PlayerInventoryLayout` | 背包布局 | `@PlayerInventoryLayout({" X "})` |

## 🔧 内置动作

| 动作 | 说明 | 示例 |
|------|------|------|
| `close` | 关闭GUI | `@Action("close")` |
| `refresh` | 刷新GUI | `@Action("refresh")` |
| `tell: <msg>` | 发送消息 | `@Action("tell: &aHello")` |
| `command: <cmd>` | 玩家执行命令 | `@Action("command: spawn")` |
| `console: <cmd>` | 控制台执行 | `@Action("console: give %player_name% diamond")` |
| `server: <server>` | Bungee传送 | `@Action("server: lobby")` |
| `open: <gui-id>` | 打开其他GUI | `@Action("open: profile-menu")` |
| `sound: <sound>` | 播放音效 | `@Action("sound: ENTITY_PLAYER_LEVELUP")` |

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！
