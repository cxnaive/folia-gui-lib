# FoliaGUI 使用说明

FoliaGUI 是一个面向 Minecraft Folia/Paper 服务器的高版本 GUI 库，提供统一的、无需过多调度器操作的接口来优雅地创建 GUI。

## 目录

- [依赖处理](#依赖处理)
- [快速开始](#快速开始)
- [Builder 模式构建 GUI](#builder-模式构建-gui)
- [声明式注解构建 GUI](#声明式注解构建-gui)
- [组件类型](#组件类型)
- [事件处理](#事件处理)
- [高级功能](#高级功能)

---

## 依赖处理

### Maven 依赖

在你的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.thenextlvl</groupId>
    <artifactId>folia-gui</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 本地安装

如果库未发布到 Maven 中央仓库，你需要先本地安装：

```bash
# 克隆仓库后执行
cd folia-gui-lib
mvn clean install -DskipTests
```

### 插件依赖

在你的 `plugin.yml` 中声明依赖：

```yaml
name: YourPlugin
version: 1.0.0
main: com.yourplugin.Main
api-version: '1.21'
dependencies:
  - name: FoliaGUITest  # 如果使用了测试插件作为依赖
    required: false
```

###  shaded 打包（推荐）

如果你希望将库打包进自己的插件，使用 Maven Shade 插件：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>com.thenextlvl.foliagui</pattern>
                                <shadedPattern>com.yourplugin.libs.foliagui</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 快速开始

### 1. 初始化 GUIManager

在你的插件主类中初始化：

```java
public class YourPlugin extends JavaPlugin {
    private GUIManager guiManager;
    
    @Override
    public void onEnable() {
        // 初始化 GUI 管理器
        this.guiManager = GUIManager.init(this);
        
        // 注册你的 GUI
        registerGUIs();
    }
    
    @Override
    public void onDisable() {
        // 清理所有 GUI
        if (guiManager != null) {
            guiManager.disposeAll();
        }
    }
}
```

### 2. 创建简单 GUI

```java
private void registerGUIs() {
    GUI gui = GUIBuilder.create("my-first-gui")
        .title("&6我的第一个GUI")
        .rows(3)
        .border(Material.BLACK_STAINED_GLASS_PANE)
        .button(13,
            ItemBuilder.of(Material.DIAMOND)
                .name("&b点击我")
                .lore("&7这是一个按钮")
                .glow()
                .build(),
            event -> {
                event.getPlayer().sendMessage("§a你点击了按钮！");
                event.setCancelled(true);
            }
        )
        .build();
    
    guiManager.register(gui);
}
```

### 3. 打开 GUI

```java
// 通过命令或其他事件打开
Player player = ...;
guiManager.open(player, "my-first-gui");

// 或者直接使用 GUI 实例
gui.open(player);
```

---

## Builder 模式构建 GUI

### 基础构建器方法

| 方法 | 说明 | 示例 |
|-----|------|------|
| `create(id)` | 创建构建器 | `GUIBuilder.create("shop")` |
| `title(title)` | 设置标题（支持颜色代码） | `.title("&6商店")` |
| `rows(rows)` | 设置行数 (1-6) | `.rows(3)` |
| `border(material)` | 添加边框 | `.border(Material.GLASS_PANE)` |
| `button(slot, item, handler)` | 添加按钮 | `.button(10, item, event -> {...})` |
| `build()` | 构建 GUI | `.build()` |

### 完整示例

```java
GUI gui = GUIBuilder.create("shop")
    .title("&6&l我的商店")
    .rows(4)
    .border(Material.BLACK_STAINED_GLASS_PANE)
    .button(10,
        ItemBuilder.of(Material.DIAMOND)
            .name("&b钻石")
            .lore("&7价格: &e100金币", "&7点击购买")
            .glow()
            .build(),
        event -> {
            Player player = event.getPlayer();
            player.sendMessage("§a购买成功！");
            event.setCancelled(true);
        }
    )
    .button(16,
        ItemBuilder.of(Material.BARRIER)
            .name("&c关闭")
            .build(),
        event -> {
            event.getPlayer().closeInventory();
            event.setCancelled(true);
        }
    )
    .onOpen(player -> player.sendMessage("§e欢迎光临！"))
    .onClose(player -> player.sendMessage("§7再见！"))
    .build();

guiManager.register(gui);
```

### ItemBuilder 链式调用

```java
ItemStack item = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("&c&l传奇之剑")
    .lore(
        "&7伤害: &c+50",
        "&7攻速: &c+20%",
        "",
        "&6&l传说品质"
    )
    .amount(1)
    .glow()                    // 添加附魔光效
    .customModelData(1001)     // 自定义模型数据
    .build();
```

---

## 声明式注解构建 GUI

### 1. 创建声明式 GUI 类

```java
@GUIConfig(
    id = "declarative-shop",
    title = {"&6", "✦ ", "&e商店", " &6✦"},
    rows = 4
)
public class ShopGUI extends AbstractDeclarativeGUI {

    // 定义布局
    @Layout({
        "#########",
        "#  D  E #",
        "#  G  B #",
        "#########"
    })
    
    // 定义图标
    @Icon(id = "D", material = Material.DIAMOND, name = "&b钻石",
          lore = {"&7价格: &e100金币", "", "&8点击购买"},
          action = {"buy DIAMOND 100"})
    @Icon(id = "E", material = Material.EMERALD, name = "&a绿宝石",
          lore = {"&7价格: &e50金币", "", "&8点击购买"},
          action = {"buy EMERALD 50"})
    @Icon(id = "G", material = Material.GOLD_INGOT, name = "&6金锭",
          lore = {"&7价格: &e20金币", "", "&8点击购买"},
          action = {"buy GOLD_INGOT 20"})
    @Icon(id = "B", material = Material.BARRIER, name = "&c关闭",
          lore = {"&7点击关闭商店"},
          action = {"close"})
    private Object layout;

    // 定义动作处理器
    @ActionHandler("buy")
    public void onBuy(Player player, String args) {
        String[] parts = args.split("\\s+");
        String itemName = parts[0];
        int price = Integer.parseInt(parts[1]);
        
        player.sendMessage("§a✓ 购买了 §e" + itemName + " §a，花费 §e" + price + "金币");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @Override
    public void onOpen(Player player) {
        player.sendMessage("§6欢迎来到商店！");
    }

    @Override
    public void onClose(Player player) {
        player.sendMessage("§7欢迎下次光临！");
    }
}
```

### 2. 注册声明式 GUI

```java
private void registerDeclarativeGUIs() {
    ShopGUI shop = new ShopGUI();
    guiManager.register(shop);
}
```

### 注解说明

| 注解 | 用途 | 属性 |
|-----|------|------|
| `@GUIConfig` | GUI 基础配置 | `id`, `title`, `rows` |
| `@Layout` | 定义视觉布局 | `value` (字符串数组) |
| `@Icon` | 定义图标 | `id`, `material`, `name`, `lore`, `action` |
| `@ActionHandler` | 自定义动作处理器 | `value` (处理器名称) |

### 内置动作

在 `@Icon` 的 `action` 属性中可以使用以下内置动作：

| 动作 | 说明 | 示例 |
|-----|------|------|
| `close` | 关闭 GUI | `action = {"close"}` |
| `refresh` | 刷新 GUI | `action = {"refresh"}` |
| `tell <消息>` | 发送消息 | `action = {"tell &a购买成功"}` |
| `command <命令>` | 执行玩家命令 | `action = {"command warp shop"}` |
| `console <命令>` | 执行控制台命令 | `action = {"console give %player% diamond 1"}` |
| `sound <音效>` | 播放音效 | `action = {"sound ENTITY_EXPERIENCE_ORB_PICKUP"}` |
| `<自定义>` | 调用 @ActionHandler | `action = {"buy DIAMOND 100"}` |

---

## 组件类型

### 1. 普通按钮 (Button)

```java
.button(10,
    ItemBuilder.of(Material.DIAMOND).name("&b按钮").build(),
    event -> {
        // 处理点击
        event.setCancelled(true);  // 防止物品移动
    }
)
```

### 2. 动态按钮 (DynamicButton)

内容会在每次刷新时重新生成：

```java
.dynamicButton(10,
    () -> ItemBuilder.of(Material.CLOCK)
        .name("&e当前时间")
        .lore("&7" + System.currentTimeMillis())
        .build(),
    event -> {
        event.getPlayer().sendMessage("§e动态按钮被点击！");
    }
)
```

### 3. 可移动按钮 (MovableButton)

允许玩家取出物品：

```java
.movableButton(10,
    ItemBuilder.of(Material.EMERALD)
        .name("&a可移动物品")
        .build(),
    event -> {
        // 可选：移动时的回调
        event.getPlayer().sendMessage("§e你取出了物品！");
    }
)
```

### 4. 开关按钮 (ToggleButton)

具有两种状态的按钮：

```java
.toggleButton(10,
    // 开启状态物品
    ItemBuilder.of(Material.LIME_DYE)
        .name("&a&l✓ 已启用")
        .build(),
    // 关闭状态物品
    ItemBuilder.of(Material.GRAY_DYE)
        .name("&7&l✗ 已禁用")
        .build(),
    // 初始状态 (true=开启)
    false,
    // 状态切换回调
    event -> {
        Player player = event.getPlayer();
        boolean newState = event.getNewState();
        player.sendMessage("§a状态切换为: " + (newState ? "§a启用" : "§c禁用"));
    }
)
```

### 5. 装饰 (Decoration)

不可交互的装饰物品：

```java
.decoration(10,
    ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
        .name(" ")
        .build()
)
```

---

## 事件处理

### ClickEvent

```java
.button(10, item, event -> {
    Player player = event.getPlayer();           // 获取玩家
    int slot = event.getSlot();                  // 获取点击槽位
    ClickType clickType = event.getClick();      // 获取点击类型
    ItemStack currentItem = event.getCurrentItem(); // 获取当前物品
    
    // 取消事件（防止物品移动）
    event.setCancelled(true);
});
```

### GUI 生命周期事件

```java
GUI gui = GUIBuilder.create("example")
    .title("示例")
    .rows(3)
    .onOpen(player -> {
        // GUI 打开时
        player.sendMessage("§aGUI 已打开！");
    })
    .onClose(player -> {
        // GUI 关闭时
        player.sendMessage("§7GUI 已关闭");
    })
    .onClick(event -> {
        // 全局点击事件（所有点击都会触发）
        Bukkit.getLogger().info("玩家点击了槽位: " + event.getSlot());
    })
    .build();
```

---

## 高级功能

### 1. 分页系统

```java
// 准备物品列表
List<ItemStack> items = new ArrayList<>();
for (int i = 1; i <= 50; i++) {
    items.add(ItemBuilder.of(Material.DIAMOND)
        .name("&e物品 #" + i)
        .build());
}

// 内容槽位（排除边框和导航按钮）
int[] contentSlots = new int[] {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

GUI gui = GUIBuilder.create("pagination-example")
    .title("&9分页示例")
    .rows(5)
    .border(Material.BLUE_STAINED_GLASS_PANE)
    .pagination(contentSlots, items, event -> {
        // 物品点击事件
        event.getPlayer().sendMessage("§a你点击了物品 #" + (event.getGlobalIndex() + 1));
    })
    .paginationNav(27, 35)      // 上一页/下一页按钮槽位
    .paginationIndicator(31)     // 页码显示槽位
    .build();
```

### 2. 玩家背包集成

```java
GUI gui = GUIBuilder.create("backpack-gui")
    .title("&2背包GUI")
    .rows(3)
    .border(Material.GREEN_STAINED_GLASS_PANE)
    // 启用玩家背包的指定槽位
    .usePlayerInventory(0, 1, 2, 3, 4, 5, 6, 7, 8)
    // 在背包槽位添加按钮
    .playerInventoryButton(0,
        ItemBuilder.of(Material.DIAMOND).name("&b背包按钮").build(),
        event -> {
            event.getPlayer().sendMessage("§b你点击了背包按钮！");
            event.setCancelled(true);
        }
    )
    .build();
```

### 3. GUI 刷新

```java
// 在构建时保存 GUI 引用
GUI[] guiHolder = new GUI[1];
guiHolder[0] = GUIBuilder.create("refreshable")
    .title("&d可刷新GUI")
    .rows(3)
    .button(13,
        ItemBuilder.of(Material.CLOCK).name("&d刷新").build(),
        event -> {
            // 刷新 GUI（更新所有动态内容）
            guiHolder[0].refresh();
            event.getPlayer().sendMessage("§dGUI 已刷新！");
        }
    )
    .build();
```

### 4. 更新单个槽位

```java
// 更新特定槽位的显示
gui.updateSlot(10);
```

### 5. 关闭所有 GUI

```java
// 在插件禁用时清理
guiManager.closeAll();
// 或
guiManager.disposeAll();
```

---

## 最佳实践

### 1. 始终取消点击事件

对于不可移动的按钮，记得取消事件：

```java
event -> {
    // 你的逻辑
    event.setCancelled(true);  // 防止物品被移动
}
```

### 2. 使用颜色代码

标题和物品名称支持 `&` 颜色代码：

```java
.title("&6&l金色标题")
.name("&b&l蓝色名称")
.lore("&7灰色描述", "&c红色警告")
```

### 3. 管理 GUI 引用

如果需要在 lambda 中使用 GUI 自身（如刷新），使用数组包装：

```java
GUI[] guiHolder = new GUI[1];
guiHolder[0] = GUIBuilder.create("example")
    .button(10, item, event -> {
        guiHolder[0].refresh();  // 现在可以访问 GUI 实例
    })
    .build();
```

### 4. 错误处理

```java
// 检查 GUI 是否已注册
if (guiManager.isRegistered("shop")) {
    guiManager.open(player, "shop");
} else {
    player.sendMessage("§c商店暂未开放！");
}
```

---

## 完整示例插件

```java
package com.example.myplugin;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    
    private GUIManager guiManager;
    
    @Override
    public void onEnable() {
        this.guiManager = GUIManager.init(this);
        
        // 注册命令
        getCommand("shop").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player player) {
                guiManager.open(player, "shop");
            }
            return true;
        });
        
        createShopGUI();
    }
    
    @Override
    public void onDisable() {
        if (guiManager != null) {
            guiManager.disposeAll();
        }
    }
    
    private void createShopGUI() {
        GUI shop = GUIBuilder.create("shop")
            .title("&6&l商店")
            .rows(3)
            .border(Material.BLACK_STAINED_GLASS_PANE)
            .button(11,
                ItemBuilder.of(Material.DIAMOND)
                    .name("&b钻石剑")
                    .lore("&7价格: &e1000金币", "", "&e点击购买")
                    .glow()
                    .build(),
                event -> {
                    Player player = event.getPlayer();
                    // 检查金币、扣除金币、给予物品等逻辑
                    player.sendMessage("§a✓ 购买成功！");
                    event.setCancelled(true);
                }
            )
            .button(15,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c关闭")
                    .build(),
                event -> {
                    event.getPlayer().closeInventory();
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(shop);
    }
}
```

---

## 故障排除

### 问题："Plugin not set" 错误

**解决：** 确保在调用 `GUIBuilder.create()` 之前已经调用了 `GUIManager.init(this)`。

### 问题：物品不显示

**解决：** 检查槽位是否在有效范围内（0 到 rows*9-1）。

### 问题：点击无反应

**解决：** 确保在点击处理器中调用了 `event.setCancelled(true)`。

### 问题：声明式 GUI 报错 ArrayIndexOutOfBoundsException

**解决：** 确保 `@GUIConfig` 的 `rows` 与 `@Layout` 的行数一致。

---

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。
