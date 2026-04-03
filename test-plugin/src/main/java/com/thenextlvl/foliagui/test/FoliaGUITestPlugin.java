package com.thenextlvl.foliagui.test;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.component.ToggleButton;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FoliaGUITestPlugin extends JavaPlugin implements Listener {
    
    private GUIManager guiManager;
    
    @Override
    public void onEnable() {
        getLogger().info("========================================");
        getLogger().info("FoliaGUI Test Plugin 启动中...");
        getLogger().info("========================================");
        
        this.guiManager = GUIManager.init(this);
        
        getServer().getPluginManager().registerEvents(this, this);
        
        registerTestGUIs();
        
        getLogger().info("测试GUI注册完成! 使用 /fguitest 命令打开测试菜单");
    }
    
    @Override
    public void onDisable() {
        if (guiManager != null) {
            guiManager.disposeAll();
        }
        getLogger().info("FoliaGUI Test Plugin 已关闭");
    }
    
    private void registerTestGUIs() {
        registerBuilderBasicTest();
        registerBuilderAdvancedTest();
        registerBuilderPlayerInvTest();
        registerBuilderMovableTest();
        registerBuilderPaginationTest();
        registerDeclarativeTest();
        registerToggleButtonTest();
        registerProgressBarTest();
        registerSliderTest();
        registerAnimatedIconTest();
        registerInputFieldTest();
        registerMainTestMenu();
    }
    
    private void registerBuilderBasicTest() {
        GUI gui = GUIBuilder.create("builder-basic")
            .title("&6Builder基础测试")
            .rows(3)
            .border(Material.BLACK_STAINED_GLASS_PANE)
            .button(10,
                ItemBuilder.of(Material.DIAMOND)
                    .name("&b测试按钮1")
                    .lore("&7点击测试基础功能")
                    .glow()
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§a✓ Builder基础测试通过!");
                    event.setCancelled(true);
                }
            )
            .button(16,
                ItemBuilder.of(Material.EMERALD)
                    .name("&a测试按钮2")
                    .lore("&7点击测试基础功能")
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§a✓ Builder基础测试通过!");
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: builder-basic");
    }
    
    private void registerBuilderAdvancedTest() {
        GUI[] guiHolder = new GUI[1];
        guiHolder[0] = GUIBuilder.create("builder-advanced")
            .title("&5Builder高级测试")
            .rows(3)
            .border(Material.PURPLE_STAINED_GLASS_PANE)
            .dynamicButton(11,
                () -> ItemBuilder.of(Material.GOLD_INGOT)
                    .name("&e动态按钮")
                    .lore("&7当前时间: &f" + System.currentTimeMillis())
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§e动态按钮被点击!");
                    event.setCancelled(true);
                }
            )
            .dynamicButton(15,
                () -> ItemBuilder.of(Material.IRON_INGOT)
                    .name("&7静态按钮")
                    .lore("&7当前时间: &f" + System.currentTimeMillis())
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§7静态按钮被点击!");
                    event.setCancelled(true);
                }
            )
            .button(22,
                ItemBuilder.of(Material.CLOCK)
                    .name("&d刷新按钮")
                    .lore("&7点击刷新GUI")
                    .build(),
                event -> {
                    guiHolder[0].refresh();
                    event.getPlayer().sendMessage("§dGUI已刷新!");
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(guiHolder[0]);
        getLogger().info("  ✓ 已注册: builder-advanced");
    }
    
    private void registerBuilderPlayerInvTest() {
        GUI gui = GUIBuilder.create("builder-playerinv")
            .title("&2背包菜单测试")
            .rows(3)
            .border(Material.GREEN_STAINED_GLASS_PANE)
            .usePlayerInventory(0, 1, 2, 3)
            .playerInventoryButton(0,
                ItemBuilder.of(Material.DIAMOND)
                    .name("&b背包按钮1")
                    .lore("&7这是背包中的第一个按钮")
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§b背包按钮1被点击!");
                    event.setCancelled(true);
                }
            )
            .playerInventoryButton(1,
                ItemBuilder.of(Material.EMERALD)
                    .name("&a背包按钮2")
                    .lore("&7这是背包中的第二个按钮")
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§a背包按钮2被点击!");
                    event.setCancelled(true);
                }
            )
            .button(22,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c关闭")
                    .build(),
                event -> {
                    event.getPlayer().closeInventory();
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: builder-playerinv");
    }
    
    private void registerBuilderMovableTest() {
        GUI gui = GUIBuilder.create("builder-movable")
            .title("&d可移动组件测试")
            .rows(3)
            .border(Material.PINK_STAINED_GLASS_PANE)
            .movableButton(11,
                ItemBuilder.of(Material.EMERALD)
                    .name("&a可移动绿宝石")
                    .lore("&7可以取出物品")
                    .build()
            )
            .movableButton(15,
                ItemBuilder.of(Material.GOLD_BLOCK)
                    .name("&6可移动金块")
                    .lore("&7可以取出物品")
                    .build(),
                event -> {
                    event.getPlayer().sendMessage("§e你移动了金块!");
                }
            )
            .movableButton(13,
                ItemBuilder.of(Material.GLASS_BOTTLE)
                    .name("&7可移动玻璃瓶")
                    .lore("&7可以取出物品")
                    .build()
            )
            .button(22,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c返回")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: builder-movable");
    }
    
    private void registerBuilderPaginationTest() {
        List<ItemStack> items = new ArrayList<>();
        Material[] validMaterials = new Material[] {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.REDSTONE, Material.LAPIS_LAZULI, Material.QUARTZ, Material.COAL,
            Material.COPPER_INGOT, Material.AMETHYST_SHARD, Material.NETHERITE_INGOT,
            Material.GLOWSTONE_DUST, Material.GUNPOWDER, Material.SUGAR, Material.CLAY_BALL,
            Material.GOLD_NUGGET, Material.IRON_NUGGET, Material.BRICK, Material.NETHER_BRICK,
            Material.PRISMARINE_SHARD, Material.PRISMARINE_CRYSTALS, Material.FLINT, Material.ECHO_SHARD,
            Material.SCULK
        };
        
        for (int i = 1; i <= 25; i++) {
            items.add(ItemBuilder.of(validMaterials[(i - 1) % validMaterials.length])
                .name("&e物品 #" + i)
                .lore("&7这是第 " + i + " 个物品", "&7点击查看详情")
                .build());
        }
        
        int[] contentSlots = new int[] {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 32, 33, 34};
        
        GUI gui = GUIBuilder.create("builder-pagination")
            .title("&9分页测试")
            .rows(5)
            .border(Material.BLUE_STAINED_GLASS_PANE)
            .pagination(contentSlots, items, event -> {
                event.getPlayer().sendMessage("§a✓ 你点击了物品 #" + (event.getGlobalIndex() + 1));
            })
            .paginationNav(27, 35)
            .paginationIndicator(31)
            .button(40,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c关闭")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .onOpen(player -> player.sendMessage("§9分页测试已打开，共 " + items.size() + " 个物品"))
            .build();
        
        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: builder-pagination");
    }
    
    private void registerDeclarativeTest() {
        DeclarativeShopGUI shopGUI = new DeclarativeShopGUI();
        guiManager.register(shopGUI);
        getLogger().info("  ✓ 已注册: declarative-shop (声明式GUI)");
    }
    
    private void registerToggleButtonTest() {
        GUI gui = GUIBuilder.create("toggle-test")
            .title("&d开关按钮测试")
            .rows(3)
            .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .toggleButton(10,
                ItemBuilder.of(Material.LIME_DYE)
                    .name("&a&l✓ 已启用")
                    .lore("&7点击切换状态")
                    .glow()
                    .build(),
                ItemBuilder.of(Material.GRAY_DYE)
                    .name("&7&l✗ 已禁用")
                    .lore("&7点击切换状态")
                    .build(),
                event -> {
                    Player player = (Player) event.getPlayer();
                    player.sendMessage("§a状态切换为: " + (event.getNewState() ? "§a启用" : "§c禁用"));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                }
            )
            .toggleButton(11,
                ItemBuilder.of(Material.GREEN_WOOL)
                    .name("&a&l✓ 音效开启")
                    .lore("&7点击切换音效")
                    .glow()
                    .build(),
                ItemBuilder.of(Material.RED_WOOL)
                    .name("&c&l✗ 音效关闭")
                    .lore("&7点击切换音效")
                    .build(),
                true,
                event -> {
                    Player player = (Player) event.getPlayer();
                    if (event.getNewState()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    }
                }
            )
            .toggleButton(12,
                ItemBuilder.of(Material.EMERALD_BLOCK)
                    .name("&a&l✓ 自动刷新")
                    .lore("&7此按钮会自动切换")
                    .glow()
                    .build(),
                ItemBuilder.of(Material.REDSTONE_BLOCK)
                    .name("&c&l✗ 自动刷新")
                    .lore("&7此按钮会自动切换")
                    .build()
            )
            .button(16,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c返回")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: toggle-test");
    }

    private void registerProgressBarTest() {
        GUIBuilder builder = GUIBuilder.create("progressbar-test")
            .title("&3进度条测试")
            .rows(4)
            .border(Material.CYAN_STAINED_GLASS_PANE);

        // 单图标进度条 - BLOCK 样式
        builder.progressBar(10)
            .baseItem(ItemBuilder.of(Material.EXPERIENCE_BOTTLE).build())
            .displayName("§bBLOCK样式 (50%)")
            .barLength(20)
            .style(com.thenextlvl.foliagui.api.component.ProgressBar.Style.BLOCK)
            .filledColor("§a")
            .emptyColor("§7")
            .value(50)
            .showPercentage(true);

        // 单图标进度条 - STAR 样式
        builder.progressBar(11)
            .baseItem(ItemBuilder.of(Material.CLOCK).build())
            .displayName("§eSTAR样式 (75%)")
            .barLength(10)
            .style(com.thenextlvl.foliagui.api.component.ProgressBar.Style.STAR)
            .filledColor("§6")
            .emptyColor("§8")
            .value(75)
            .showPercentage(true);

        // 单图标进度条 - DOT 样式
        builder.progressBar(12)
            .baseItem(ItemBuilder.of(Material.REDSTONE).build())
            .displayName("§cDOT样式 (30%)")
            .barLength(15)
            .style(com.thenextlvl.foliagui.api.component.ProgressBar.Style.DOT)
            .filledColor("§c")
            .emptyColor("§7")
            .value(30)
            .showPercentage(true);

        // 单图标进度条 - HEART 样式
        builder.progressBar(13)
            .baseItem(ItemBuilder.of(Material.APPLE).build())
            .displayName("§dHEART样式 (80%)")
            .barLength(10)
            .style(com.thenextlvl.foliagui.api.component.ProgressBar.Style.HEART)
            .filledColor("§c")
            .emptyColor("§7")
            .value(80)
            .showPercentage(true);

        // 单图标进度条 - 自定义样式
        builder.progressBar(14)
            .displayName("§5自定义样式 (60%)")
            .barLength(25)
            .style(com.thenextlvl.foliagui.api.component.ProgressBar.Style.CUSTOM)
            .filledChar("■")
            .emptyChar("□")
            .filledColor("§5")
            .emptyColor("§0")
            .value(60)
            .showPercentage(true);

        GUI gui = builder
            .button(31,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c返回")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .build();

        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: progressbar-test (单图标版本)");
    }

    private void registerSliderTest() {
        GUIBuilder builder = GUIBuilder.create("slider-test")
            .title("&2滑块测试")
            .rows(4)
            .border(Material.GREEN_STAINED_GLASS_PANE);

        // 单图标滑块 - BLOCK 样式
        builder.slider(10)
            .baseItem(ItemBuilder.of(Material.COMPARATOR).build())
            .displayName("§eBLOCK样式滑块")
            .barLength(20)
            .style(com.thenextlvl.foliagui.api.component.Slider.Style.BLOCK)
            .range(0, 100)
            .value(50)
            .step(5)
            .showValue(true)
            .onValueChange(event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.sendMessage("§a滑块1: " + String.format("%.0f", event.getOldValue()) + " → " + String.format("%.0f", event.getNewValue()));
                }
            });

        // 单图标滑块 - ARROW 样式
        builder.slider(11)
            .baseItem(ItemBuilder.of(Material.REPEATER).build())
            .displayName("§6ARROW样式滑块")
            .barLength(15)
            .style(com.thenextlvl.foliagui.api.component.Slider.Style.ARROW)
            .thumbColor("§6")
            .trackColor("§8")
            .range(0, 10)
            .value(5)
            .step(1)
            .showValue(true)
            .onValueChange(event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.sendMessage("§6滑块2: " + String.format("%.0f", event.getOldValue()) + " → " + String.format("%.0f", event.getNewValue()));
                }
            });

        // 单图标滑块 - STAR 样式
        builder.slider(12)
            .displayName("§bSTAR样式滑块")
            .barLength(10)
            .style(com.thenextlvl.foliagui.api.component.Slider.Style.STAR)
            .thumbColor("§e")
            .trackColor("§7")
            .range(0, 100)
            .value(75)
            .step(10)
            .showValue(true);

        // 单图标滑块 - DOT 样式
        builder.slider(13)
            .displayName("§cDOT样式滑块")
            .barLength(20)
            .style(com.thenextlvl.foliagui.api.component.Slider.Style.DOT)
            .thumbColor("§c")
            .trackColor("§7")
            .range(0, 50)
            .value(25)
            .step(5)
            .showValue(true);

        GUI gui = builder
            .button(31,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c返回")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .build();

        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: slider-test");
    }

    private void registerAnimatedIconTest() {
        GUIBuilder builder = GUIBuilder.create("animated-icon-test")
            .title("&d动画图标测试")
            .rows(3)
            .border(Material.PURPLE_STAINED_GLASS_PANE);

        // 动画图标 - 循环播放
        builder.animatedIcon(11)
            .addFrame(ItemBuilder.of(Material.RED_WOOL).name("§c帧1").build())
            .addFrame(ItemBuilder.of(Material.ORANGE_WOOL).name("§6帧2").build())
            .addFrame(ItemBuilder.of(Material.YELLOW_WOOL).name("§e帧3").build())
            .addFrame(ItemBuilder.of(Material.LIME_WOOL).name("§a帧4").build())
            .addFrame(ItemBuilder.of(Material.CYAN_WOOL).name("§b帧5").build())
            .addFrame(ItemBuilder.of(Material.BLUE_WOOL).name("§9帧6").build())
            .addFrame(ItemBuilder.of(Material.PURPLE_WOOL).name("§d帧7").build())
            .interval(10) // 0.5秒
            .playMode(com.thenextlvl.foliagui.api.component.AnimatedIcon.PlayMode.LOOP)
            .play();

        // 动画图标 - 来回播放
        builder.animatedIcon(13)
            .addFrame(ItemBuilder.of(Material.DIAMOND).name("§b钻石").glow().build())
            .addFrame(ItemBuilder.of(Material.EMERALD).name("§a绿宝石").glow().build())
            .addFrame(ItemBuilder.of(Material.GOLD_INGOT).name("§6金锭").glow().build())
            .addFrame(ItemBuilder.of(Material.IRON_INGOT).name("§f铁锭").build())
            .interval(15)
            .playMode(com.thenextlvl.foliagui.api.component.AnimatedIcon.PlayMode.PING_PONG)
            .play();

        // 动画图标 - 反向播放
        builder.animatedIcon(15)
            .addFrame(ItemBuilder.of(Material.CLOCK).name("§e时间 1").build())
            .addFrame(ItemBuilder.of(Material.CLOCK).name("§e时间 2").build())
            .addFrame(ItemBuilder.of(Material.CLOCK).name("§e时间 3").build())
            .addFrame(ItemBuilder.of(Material.CLOCK).name("§e时间 4").build())
            .interval(20)
            .playMode(com.thenextlvl.foliagui.api.component.AnimatedIcon.PlayMode.REVERSE)
            .play();

        GUI gui = builder
            .button(22,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c返回")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .build();

        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: animated-icon-test");
    }

    private void registerInputFieldTest() {
        GUIBuilder builder = GUIBuilder.create("inputfield-test")
            .title("&e输入框测试")
            .rows(4)
            .border(Material.WHITE_STAINED_GLASS_PANE);

        // 添加输入框组件
        builder.inputField(13)
            .placeholder("点击输入文本...")
            .maxLength(20)
            .validator(text -> !text.isEmpty() && text.length() >= 3)
            .onTextChange(event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.sendMessage("§e文本变化: " + event.getOldText() + " → " + event.getNewText());
                }
            })
            .onSubmit(event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.sendMessage("§a提交: " + event.getText());
                }
            });

        GUI gui = builder
            .button(31,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c返回")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "test-main");
                    event.setCancelled(true);
                }
            )
            .build();

        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: inputfield-test");
    }

    private void registerMainTestMenu() {
        GUI gui = GUIBuilder.create("test-main")
            .title("&6&lFoliaGUI 测试菜单")
            .rows(5)
            .border(Material.YELLOW_STAINED_GLASS_PANE)
            .button(10,
                ItemBuilder.of(Material.CRAFTING_TABLE)
                    .name("&bBuilder基础测试")
                    .lore("&7测试内容:", "&f- 基础按钮", "&f- ItemBuilder", "&f- 事件处理", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "builder-basic");
                    event.setCancelled(true);
                }
            )
            .button(12,
                ItemBuilder.of(Material.ANVIL)
                    .name("&5Builder高级测试")
                    .lore("&7测试内容:", "&f- 动态刷新", "&f- 多行描述", "&f- 多种材质", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "builder-advanced");
                    event.setCancelled(true);
                }
            )
            .button(14,
                ItemBuilder.of(Material.CHEST)
                    .name("&2Builder背包菜单")
                    .lore("&7测试内容:", "&f- 玩家背包集成", "&f- 背包按钮交互", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "builder-playerinv");
                    event.setCancelled(true);
                }
            )
            .button(16,
                ItemBuilder.of(Material.MINECART)
                    .name("&d可移动组件测试")
                    .lore("&7测试内容:", "&f- 可移动按钮", "&f- 不可移动按钮", "&f- 物品拿取", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "builder-movable");
                    event.setCancelled(true);
                }
            )
            .button(28,
                ItemBuilder.of(Material.BOOK)
                    .name("&9分页测试")
                    .lore("&7测试内容:", "&f- 多页内容展示", "&f- 上一页/下一页", "&f- 页码显示", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "builder-pagination");
                    event.setCancelled(true);
                }
            )
            .button(30,
                ItemBuilder.of(Material.GOLD_BLOCK)
                    .name("&6声明式GUI测试")
                    .lore("&7测试内容:", "&f- 注解定义GUI", "&f- Layout布局", "&f- Action处理", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "declarative-shop");
                    event.setCancelled(true);
                }
            )
            .button(32,
                ItemBuilder.of(Material.LEVER)
                    .name("&e开关按钮测试")
                    .lore("&7测试内容:", "&f- 状态切换", "&f- 音效播放", "&f- 动态更新", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "toggle-test");
                    event.setCancelled(true);
                }
            )
            .button(34,
                ItemBuilder.of(Material.EXPERIENCE_BOTTLE)
                    .name("&3进度条测试")
                    .lore("&7测试内容:", "&f- 进度显示", "&f- 动态更新", "&f- 百分比显示", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "progressbar-test");
                    event.setCancelled(true);
                }
            )
            .button(38,
                ItemBuilder.of(Material.COMPARATOR)
                    .name("&2滑块测试")
                    .lore("&7测试内容:", "&f- 数值选择", "&f- 步长控制", "&f- 范围限制", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "slider-test");
                    event.setCancelled(true);
                }
            )
            .button(39,
                ItemBuilder.of(Material.CLOCK)
                    .name("&d动画图标测试")
                    .lore("&7测试内容:", "&f- 帧动画", "&f- 播放模式", "&f- 动态更新", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "animated-icon-test");
                    event.setCancelled(true);
                }
            )
            .button(40,
                ItemBuilder.of(Material.WRITABLE_BOOK)
                    .name("&e输入框测试")
                    .lore("&7测试内容:", "&f- 文本输入", "&f- 验证器", "&f- 占位符", "", "&e点击打开")
                    .build(),
                event -> {
                    guiManager.open(event.getPlayer(), "inputfield-test");
                    event.setCancelled(true);
                }
            )
            .button(44,
                ItemBuilder.of(Material.BARRIER)
                    .name("&c关闭菜单")
                    .lore("&7点击关闭")
                    .build(),
                event -> {
                    event.getPlayer().closeInventory();
                    event.setCancelled(true);
                }
            )
            .build();
        
        guiManager.register(gui);
        getLogger().info("  ✓ 已注册: test-main (主菜单)");
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c此命令只能由玩家执行!");
            return true;
        }
        
        if (args.length == 0) {
            guiManager.open(player, "test-main");
            return true;
        }
        
        if (args.length == 1) {
            String guiId = args[0].toLowerCase();
            if (guiManager.isRegistered(guiId)) {
                guiManager.open(player, guiId);
                player.sendMessage("§a已打开GUI: " + guiId);
                return true;
            } else {
                player.sendMessage("§cGUI '" + guiId + "' 未注册!");
                return true;
            }
        }
        
        player.sendMessage("§c用法: /fguitest [gui-id]");
        return true;
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        guiManager.closeAll();
    }
}
