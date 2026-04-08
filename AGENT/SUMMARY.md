# Summary

## 中文概括

`Precipitate Power` 当前已经从 NeoForge 模板工程演进为一个可运行的玩法模组，核心围绕“白袜发电”展开：玩家可获得不同类型的袜子，将其投入沉淀发电机持续产能，袜子会在运行过程中积累沉淀值、获得发电系数、逐步变脏，并最终进入“污渍白袜 -> 清洗恢复”的循环。除基础物品与方块外，项目已经补齐 GUI、方块实体逻辑、配置系统、战利品注入、Curios 槽位兼容、Iron's Spellbooks 可选兼容、Ponder 教学场景、中英文本地化与所需资源文件。

目前玩法链路已经基本闭环：

- 白袜、彩虹白袜、有污渍的白袜三类物品已实现。
- 沉淀发电机支持白袜输入、服务端发电、容器交互和前端界面显示。
- 袜子具有沉淀值、污渍次数、发电倍率、体育生认知等自定义数据。
- 战利品系统可以向箱子 loot 中注入袜子，并按维度/战利品表决定彩虹白袜的稀有来源。
- Curios 安装时会启用 `sock` 栏位与装备属性；未安装时模组保持可加载。
- Iron's Spellbooks 安装时，彩虹白袜会切换为法术相关加成；否则回退为普通战斗属性。

最近一次明确修复是战利品约束问题：原来的全局战利品修改器会对所有 loot context 生效，导致非箱子掉落也有机会产出白袜；现在已经在 `AddSockLootModifier` 中加了 `chests/` 前缀判断，只允许箱子战利品表触发袜子注入。

## Current Project State

### 1. Runtime Bootstrap

- 已替换默认示例模组入口，使用真实注册流程驱动整个项目。
- 模组启动时会注册：
  - 方块与方块物品
  - 袜子物品
  - 方块实体
  - 菜单与客户端界面
  - 全局战利品修改器序列化器
  - 创造标签页
  - 通用配置项
  - 可选兼容逻辑
- 主要入口文件：
  - `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - `src/main/java/top/realme/mc/precipitate_power/PrecipitatePowerClient.java`
  - `src/main/java/top/realme/mc/precipitate_power/Config.java`

### 2. Core Gameplay Loop

- 当前核心循环为：
  1. 玩家从战利品或创造物品栏获得白袜。
  2. 将袜子放入沉淀发电机。
  3. 发电机根据配置公式产出 FE。
  4. 袜子在工作中提高沉淀值，并按概率增加污渍次数。
  5. 污渍到达阈值后转化为污渍白袜。
  6. 玩家可通过已有清洗配方将污渍白袜重新洗回白袜，但原属性不会保留。

- 这个循环已经具备基本的获取、成长、惩罚、回收四个环节。

### 3. Generator Implementation

- `PrecipitateGeneratorBlock` 已实现：
  - 朝向状态
  - 交互打开菜单
  - 正确连接方块实体 tick

- `PrecipitateGeneratorBlockEntity` 已实现：
  - 单输入槽逻辑
  - FE 存储与产能计算
  - 服务端 tick
  - 侧面能力暴露
  - 袜子沉淀与变脏逻辑
  - 变为污渍白袜后的物品替换

- `PrecipitateGeneratorMenu` 与 `PrecipitateGeneratorScreen` 已提供：
  - 服务端容器同步
  - 客户端 GUI 渲染
  - 与机器数据联动的基础展示

- 发电强度由表达式解析器驱动，配置中的公式使用 `x` 作为沉淀等级输入。

### 4. Sock Item Data Model

- 已实现的袜子物品：
  - `white_sock`
  - `rainbow_white_sock`
  - `dirty_white_sock`

- 白袜当前维护的核心数据包括：
  - `precipitation`
  - `dirty_count`
  - `power_coefficient`
  - `athletic_cognition`

- 当前规则：
  - 普通白袜可成长并参与发电。
  - 彩虹白袜具有固定高价值初始属性。
  - 污渍白袜作为惩罚状态存在，不继承原白袜上的发电与附魔收益。
  - 破坏保护、随机附魔和属性文本都已经纳入数据工具类管理。

- `SockDataUtil` 是现阶段袜子自定义数据的中心工具类，承担：
  - NBT/组件数据读写
  - 默认值初始化
  - 彩虹白袜初始化
  - 提示文本拼装
  - 条件判断，如是否可用作发电袜

### 5. Loot System

- 当前 loot 方案采用 NeoForge Global Loot Modifier。
- `AddSockLootModifier` 会向箱子类战利品表追加袜子：
  - 普通白袜为常见结果
  - 下界/末地特定箱子有极低概率给彩虹白袜
  - 普通白袜还会额外随机出发电倍率增幅
  - 普通白袜会生成 `0%` 到 `100%` 的体育生认知，且高值更稀有
  - 部分袜子会被追加耐久附魔

- 最近已修复的问题：
  - 之前 modifier 缺少 loot 类型限制，会影响非箱子流程。
  - 当前已在代码中限制为仅 `chests/` 路径的 loot table 生效。

- 相关文件：
  - `src/main/java/top/realme/mc/precipitate_power/loot/AddSockLootModifier.java`
  - `src/main/java/top/realme/mc/precipitate_power/registry/ModLootModifiers.java`
  - `src/main/resources/data/neoforge/loot_modifiers/global_loot_modifiers.json`
  - `src/main/resources/data/precipitate_power/loot_modifiers/add_white_sock_to_chests.json`

### 6. Curios Compatibility

- 已加入可选 Curios 兼容，而不是强依赖。
- 当前实现内容：
  - 自定义 `sock` 饰品槽
  - 玩家默认拥有两个 `sock` 槽位
  - 三种袜子均可放入该槽位
  - 自定义空槽图标已提供

- 当前属性效果：
  - 普通白袜基于体育生认知提供移速加成
  - 污渍白袜提供 `-10%` 移速惩罚
  - 彩虹白袜提供攻击、防御、生命等额外收益

- 设计上已避免在核心物品类直接硬引用 Curios API，降低缺失依赖时的加载风险。

### 7. Iron's Spellbooks Compatibility

- 已加入可选 Iron's Spellbooks 兼容。
- 彩虹白袜的额外收益会根据环境切换：
  - 安装 Iron's Spellbooks 时，给予法术相关加成
  - 未安装时，回退为常规战斗属性增益

- 这部分逻辑已被隔离在兼容包中，没有污染基础玩法代码。

### 8. Ponder And Teaching Content

- 已将原有教学内容迁移为 Java Ponder 场景。
- 当前场景包括：
  - 发电机基础使用
  - 污渍白袜清洗

- Ponder 内容已与本地资源、语言键、结构文件联动，不再依赖旧的 KubeJS/Ponderer 方式。

### 9. Resources And Data Assets

- 当前资源层已覆盖：
  - 方块状态
  - 方块/物品模型
  - 方块与物品贴图
  - 中英文语言文件
  - 战利品表与 loot modifier 数据
  - Curios 槽位定义与物品标签
  - 清洗与机器配方
  - Ponder 结构 NBT

- 资源完整性相对较高，已经不再是“只有代码没有内容”的阶段。

## Key Files By Responsibility

### Bootstrap

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - 模组主入口、通用注册、可选兼容接入点。

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePowerClient.java`
  - 客户端入口、界面注册、Ponder 插件注册。

- `src/main/java/top/realme/mc/precipitate_power/Config.java`
  - 发电、污渍、战利品等关键配置。

### Generator

- `src/main/java/top/realme/mc/precipitate_power/block/PrecipitateGeneratorBlock.java`
  - 方块状态与交互。

- `src/main/java/top/realme/mc/precipitate_power/block/entity/PrecipitateGeneratorBlockEntity.java`
  - 发电机核心逻辑。

- `src/main/java/top/realme/mc/precipitate_power/menu/PrecipitateGeneratorMenu.java`
  - 容器逻辑。

- `src/main/java/top/realme/mc/precipitate_power/client/PrecipitateGeneratorScreen.java`
  - 客户端界面。

- `src/main/java/top/realme/mc/precipitate_power/util/FormulaParser.java`
  - 发电公式解析。

### Sock Items And Data

- `src/main/java/top/realme/mc/precipitate_power/item/WhiteSockItem.java`
  - 普通白袜基础行为。

- `src/main/java/top/realme/mc/precipitate_power/item/RainbowWhiteSockItem.java`
  - 彩虹白袜初始化行为。

- `src/main/java/top/realme/mc/precipitate_power/util/SockDataUtil.java`
  - 袜子数据工具核心。

### Compat

- `src/main/java/top/realme/mc/precipitate_power/compat/curios/CuriosCompat.java`
  - Curios 物品兼容注册。

- `src/main/java/top/realme/mc/precipitate_power/compat/curios/SockCurio.java`
  - Curios 装备效果实现。

- `src/main/java/top/realme/mc/precipitate_power/compat/ironsspellbooks/IronsSpellbooksCompat.java`
  - Iron's Spellbooks 彩虹白袜增益适配。

### Registries

- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlocks.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModItems.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlockEntities.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModMenus.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModLootModifiers.java`

### Data And Assets

- `src/main/resources/data/precipitate_power/loot_table/blocks/precipitate_generator.json`
- `src/main/resources/data/precipitate_power/recipe/precipitate_generator.json`
- `src/main/resources/data/precipitate_power/recipe/splashing/white_sock_washing.json`
- `src/main/resources/data/precipitate_power/curios/slots/sock.json`
- `src/main/resources/data/precipitate_power/curios/entities/player.json`
- `src/main/resources/data/curios/tags/item/sock.json`
- `src/main/resources/assets/precipitate_power/lang/en_us.json`
- `src/main/resources/assets/precipitate_power/lang/zh_cn.json`

## Recent Important Changes

- 完成白袜发电主玩法闭环，实现机器、袜子、清洗、掉落和教学内容。
- 引入 Curios 可选兼容，允许袜子作为饰品装备。
- 引入 Iron's Spellbooks 可选兼容，为彩虹白袜提供模组感知型加成。
- 将教学内容迁移为 Java Ponder 场景。
- 修复全局战利品修改器作用域过宽的问题，现仅对箱子 loot 生效。

## Verification Status

- 历史记录中已经多次执行并通过：
  - `compileJava`
  - `processResources`

- 但基于当前工作区状态，仍建议在下一轮整理时重新至少执行一次：
  - `./gradlew compileJava`
  - `./gradlew processResources`
  - 如需联机/实际表现确认，再执行 `./gradlew runClient`

## Open Risks And Next Work Suggestions

- 目前箱子战利品约束已经在 Java 代码层修复，但 JSON 条件层仍为空；如果后续希望进一步降低误配风险，可以在数据层再加一次限制，形成双保险。
- `AddSockLootModifier` 中仍保留了一些未使用导入与中间变量，后续可以顺手清理，减少误导。
- 彩虹白袜、体育生认知、发电倍率与 Curios 属性之间的数值平衡仍然偏设计问题，后续最好进入一轮游戏内调参。
- 目前总结基于代码结构和已落地资源，不代表所有功能都已经完成正式联机或长期存档验证。
