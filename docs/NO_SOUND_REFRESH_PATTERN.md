# 无音效刷新模式 (No-Sound Refresh Pattern)

## 问题背景

在Bukkit/Spigot/Paper/Folia插件开发中，当需要更新GUI界面内容时，开发者往往习惯性地调用 `player.openInventory(newInventory)` 来刷新界面。这种方式虽然直观，但有一个显著问题：

**每次调用 `openInventory()` 都会触发箱子打开和关闭的音效。**

当用户频繁操作（如翻页、筛选、批量操作）时，频繁的音效会产生噪音，严重影响用户体验。

## 根本原因

`openInventory()` 方法的内部实现：
1. 如果玩家当前有打开的界面，先关闭它（触发关闭音效）
2. 打开新的界面（触发打开音效）

因此，每次"刷新"实际上是"关闭→打开"的过程。

## 解决方案：无音效刷新模式

### 核心原理

直接操作 `Inventory` 对象的内容，而不重新打开界面：
```java
// 无音效刷新
inventory.setItem(slot, newItem);
player.updateInventory();  // 同步客户端显示
```

### FoliaGUI 已有机制

FoliaGUI 库**已经实现了**无音效刷新机制：

```java
// AbstractGUI.java
@Override
public void refresh() {
    if (!isOpen || viewer == null) return;
    
    FoliaScheduler.runOnPlayer(viewer, () -> {
        refreshInventory();      // 直接更新 Inventory 内容
        viewer.updateInventory(); // 同步客户端
    });
}

@Override
public void updateSlot(int slot) {
    if (!isOpen || viewer == null || inventory == null) return;
    if (slot < 0 || slot >= inventory.getSize()) return;
    
    markDirty(slot);  // 标记为脏，延迟批量更新
}
```

**关键点：**
- `refresh()` 直接调用 `inventory.setItem()` 更新内容
- `updateSlot()` 使用脏检查机制，批量合并更新以提高性能
- **完全不调用 `openInventory()`，因此没有音效**

### 新增的 ContentManager

为了更方便地管理动态内容和解决闭包问题，FoliaGUI 新增了 `ContentManager`：

```java
ContentManager contentManager = new ContentManager();

// 注册动态内容
contentManager.register(10, () -> createItem(currentPageData.get(0)));
contentManager.register(11, () -> createItem(currentPageData.get(1)));

// 刷新所有内容（无音效）
contentManager.refreshContents(inventory);
```

## 闭包问题

在点击事件处理器中使用动态内容时，存在一个常见的闭包问题：

### 问题示例

```java
// ❌ 错误方式：闭包捕获静态变量
for (int i = 0; i < slots.length; i++) {
    StorageEntry entry = entries.get(i);  // 创建时的值被捕获
    builder.button(slots[i], createItem(entry), event -> {
        // 当页面刷新后，entries已更新，但这里仍使用旧的entry
        process(entry);  // 数据不一致！
    });
}
```

### 解决方案

**方案一：槽位索引动态查找**

```java
// ✅ 正确方式：使用槽位索引动态查找
for (int i = 0; i < slots.length; i++) {
    final int slotIndex = i;  // 槽位索引不会变化
    builder.button(slots[i], createItem(entries.get(i)), event -> {
        // 动态获取当前槽位对应的最新数据
        int dataIndex = currentPage * slotsPerPage + slotIndex;
        StorageEntry currentEntry = entries.get(dataIndex);
        process(currentEntry);  // 数据一致！
    });
}
```

**方案二：使用 ContentManager**

```java
// ✅ 使用 ContentManager 提供动态数据查找
ContentManager contentManager = gui.getContentManager();
contentManager.registerPage(slots, entries, pageStartIndex, entry -> createItem(entry));

builder.button(slot, initialItem, event -> {
    // 使用 getSlotData 动态获取当前槽位的数据
    StorageEntry currentEntry = gui.getSlotData(slot, StorageEntry.class);
    process(currentEntry);
});
```

## 使用建议

### 何时使用 refresh()

- 整个界面需要完全刷新（如切换筛选条件）
- 数据结构发生重大变化

### 何时使用 updateSlot()

- 单个槽位更新（如切换开关按钮状态）
- 少量槽位更新

### 何时使用 ContentManager

- 分页显示场景
- 动态数据列表
- 需要在点击处理器中获取关联数据

## 最佳实践

```java
public class StorageGUI {
    
    private void refresh(Player player, StorageSession session) {
        Inventory inventory = session.currentGUI.getInventory();
        
        // 1. 重新加载数据
        updateCachedEntries(player, session);
        
        // 2. 直接更新 Inventory 内容（无音效）
        for (int slot : DISPLAY_SLOTS) {
            inventory.setItem(slot, createDisplayItem(session, slot));
        }
        
        // 3. 更新导航栏
        updateBottomBar(inventory, session);
        
        // 4. 同步客户端
        player.updateInventory();
    }
    
    // 动态数据查找方法
    private StorageEntry getEntryAtSlot(StorageSession session, int slot) {
        int startIdx = session.page * DISPLAY_SLOTS.length;
        int entryIdx = startIdx + slot;
        
        if (entryIdx >= 0 && entryIdx < session.cachedEntries.size()) {
            return session.cachedEntries.get(entryIdx);
        }
        return null;
    }
    
    private GUI buildGUI(StorageSession session) {
        // ...
        for (int i = 0; i < DISPLAY_SLOTS.length; i++) {
            final int slotIndex = i;  // 槽位索引
            builder.button(DISPLAY_SLOTS[i], createItem(session, i), event -> {
                // 动态获取当前数据
                StorageEntry entry = getEntryAtSlot(session, slotIndex);
                if (entry != null) {
                    handle(entry);
                    // 刷新界面（无音效）
                    refresh(player, session);
                }
            });
        }
        // ...
    }
}
```

## 总结

| 方法 | 音效 | 适用场景 |
|------|------|----------|
| `player.openInventory()` | 有 | 首次打开、打开不同界面 |
| `gui.refresh()` | 无 | 整体刷新 |
| `gui.updateSlot()` | 无 | 单槽位更新 |
| `gui.refreshSlots()` | 无 | 多槽位批量更新 |
| `contentManager.refreshContents()` | 无 | 动态内容管理 |

**FoliaGUI 的 refresh 机制并非缺失功能，而是设计完善的特性。**
开发者只需正确使用 `refresh()`、`updateSlot()` 或新增的 `ContentManager`，
即可实现无音效的界面刷新。