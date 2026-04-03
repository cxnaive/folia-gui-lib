# FoliaGUI 项目结构

## 项目概览

```
folia-gui-lib/
├── pom.xml                          # 根项目POM
├── README.md                        # 项目说明
├── DESIGN.md                        # 设计文档
├── PROJECT_STRUCTURE.md             # 本文件
│
├── src/main/java/com/thenextlvl/foliagui/    # 核心库源码
│   ├── api/                         # API接口层
│   │   ├── GUI.java                # GUI顶级接口
│   │   └── component/
│   │       └── Component.java      # 组件接口
│   │
│   ├── annotation/                  # 声明式注解
│   │   ├── GUIConfig.java          # GUI配置注解
│   │   ├── Layout.java             # 布局注解
│   │   ├── Icon.java               # 图标注解
│   │   ├── Button.java             # 按钮注解
│   │   ├── Action.java             # 动作注解
│   │   └── PlayerInventoryLayout.java  # 背包布局注解
│   │
│   ├── builder/                     # Builder模式
│   │   ├── GUIBuilder.java         # GUI构建器
│   │   └── ItemBuilder.java        # 物品构建器
│   │
│   ├── internal/                    # 内部实现
│   │   ├── AbstractGUI.java        # GUI抽象基类
│   │   └── AbstractDeclarativeGUI.java  # 声明式GUI基类
│   │
│   ├── manager/                     # 管理器
│   │   └── GUIManager.java         # GUI管理器
│   │
│   └── folia/                       # Folia支持
│       └── FoliaScheduler.java     # Folia调度器
│
├── test-plugin/                     # 测试插件
│   ├── pom.xml                     # 测试插件POM
│   └── src/
│       ├── main/java/com/thenextlvl/foliagui/test/
│       │   └── FoliaGUITestPlugin.java   # 测试插件主类
│       └── main/resources/
│           └── plugin.yml          # 插件配置
│
└── examples/                        # 使用示例
    └── ExampleUsage.java           # 示例代码
```

## 模块说明

### 1. API层 (`api/`)
定义了库的所有公共接口，开发者主要使用这些接口。

- **GUI.java**: 所有GUI的顶级接口，定义了生命周期方法
- **Component.java**: GUI组件接口，按钮、装饰等都实现此接口

### 2. 注解层 (`annotation/`)
声明式配置的核心，借鉴TrMenu的设计。

- **@GUIConfig**: 定义GUI的基本属性（ID、标题、行数等）
- **@Layout**: 可视化布局定义，使用字符串数组
- **@Icon**: 定义布局中使用的图标
- **@Button**: 直接在槽位定义按钮
- **@Action**: 定义点击动作
- **@PlayerInventoryLayout**: 定义玩家背包布局

### 3. Builder层 (`builder/`)
流畅的API，用于程序化创建GUI。

- **GUIBuilder**: 链式调用创建GUI
- **ItemBuilder**: 创建和配置ItemStack

### 4. 内部实现 (`internal/`)
库的内部实现，开发者通常不需要直接使用。

- **AbstractGUI**: GUI抽象基类，处理事件和生命周期
- **AbstractDeclarativeGUI**: 声明式GUI基类，解析注解

### 5. 管理器 (`manager/`)
- **GUIManager**: 统一管理所有GUI实例

### 6. Folia支持 (`folia/`)
- **FoliaScheduler**: 统一调度工具，自动处理Folia/Paper/Spigot差异

## 测试插件功能

测试插件包含6个测试GUI：

1. **builder-basic**: Builder基础功能测试
   - 基础按钮
   - ItemBuilder功能
   - 事件处理

2. **builder-advanced**: Builder高级功能测试
   - 动态刷新
   - 多行描述
   - 多种材质

3. **builder-playerinv**: Builder背包菜单测试
   - 玩家背包集成
   - 背包按钮交互

4. **declarative-basic**: 声明式注解测试
   - @GUIConfig
   - @Layout
   - @Icon
   - @Action
   - 动态标题

5. **declarative-playerinv**: 声明式背包菜单测试
   - @PlayerInventoryLayout
   - 背包按钮

6. **test-main**: 主测试菜单
   - 导航到所有测试GUI
   - 显示库信息

## 命令

```
/fguitest              # 打开主菜单
/fguitest basic        # Builder基础测试
/fguitest advanced     # Builder高级测试
/fguitest playerinv    # Builder背包测试
/fguitest declarative  # 声明式测试
/fguitest declinv      # 声明式背包测试
/fguitest reload       # 重新注册GUI
/fguitest info         # 显示信息
```

## 构建说明

### 构建核心库
```bash
cd folia-gui-lib
mvn clean install
```

### 构建测试插件
```bash
cd folia-gui-lib/test-plugin
mvn clean package
```

## 使用方式

### 方式1: 添加为依赖
将核心库安装到本地Maven仓库后，在其他插件的pom.xml中添加：

```xml
<dependency>
    <groupId>com.thenextlvl</groupId>
    <artifactId>folia-gui</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 方式2: 直接复制源码
将核心库源码复制到你的插件项目中。

## 注意事项

1. **Folia兼容性**: 所有GUI操作都通过`FoliaScheduler`执行，自动适配Folia/Paper/Spigot
2. **线程安全**: 不要在异步线程直接操作GUI，使用`FoliaScheduler.runForPlayer()`
3. **资源释放**: 插件关闭时调用`GUIManager.disposeAll()`释放资源
