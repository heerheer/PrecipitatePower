# Summary

## 中文概括

`Precipitate Power` 当前已经从早期的“白袜发电”原型，扩展为一个围绕**袜子物品体系、分级发电机、材质混纺、Curios 穿戴与彩蛋物品**展开的 NeoForge 玩法模组。核心不再只是“某几双袜子能发电”，而是演进成了：

- 不同袜子物品拥有不同生成、穿戴、发电或彩蛋行为
- 发电机本体只负责调度，袜子物品自己决定每 tick 做什么
- 袜子实例可以带材质混纺数据，并影响穿戴属性与发电行为
- 部分彩蛋袜不可穿戴、不发电，但可借由特殊交互触发独立玩法

当前项目已经具备完整的内容链路与资源结构：

- 普通袜子、款式袜子、功能袜子、彩蛋袜子均已注册
- 沉淀发电机与高级沉淀发电机均可运行
- 袜子发电逻辑已下沉到物品层
- 材质系统已接入常规袜子
- Curios、战利品注入、成就、药水效果、中英文资源文件均已补齐
- 腐生原味、昭昭原味、大头原味三条彩蛋支线已开始实现，其中昭昭/腐生/大头都已有独立物品和 tooltip 体系

目前玩法上已经形成三条主线：

1. **常规袜子发电线**：通过不同袜子和材质混纺投入发电机获取 FE，并经历沉淀、污渍、耐久等消耗过程。
2. **穿戴属性线**：通过 Curios 的 `sock` 槽位装备袜子，获得材质与特殊袜子的属性收益或惩罚。
3. **彩蛋袜交互线**：通过原味袜子与发电机、作物、生物、村民、玩家等交互，触发独立的特殊机制与成就。

## Current Project State

### 1. Runtime Bootstrap

- 模组主入口、客户端入口和配置系统已稳定存在。
- 当前启动时会注册：
  - 方块与方块物品
  - 袜子物品
  - 方块实体
  - 菜单与界面
  - 全局战利品修改器序列化器
  - 创造标签页
  - 药水效果
  - 成就授予辅助
  - 可选兼容逻辑

- 主要入口文件：
  - `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - `src/main/java/top/realme/mc/precipitate_power/PrecipitatePowerClient.java`
  - `src/main/java/top/realme/mc/precipitate_power/Config.java`

### 2. Core Gameplay Loop

- 当前常规玩法循环为：
  1. 玩家从创造栏或战利品中获得不同袜子。
  2. 常规袜子可能带有随机材质混纺。
  3. 袜子放入沉淀发电机或高级沉淀发电机。
  4. 发电机逐 tick 调用输入物品自己的 `tickInGenerator(...)`。
  5. 袜子根据自身类型决定是否发电、是否积累沉淀、是否掉耐久、是否脏污、是否触发特殊效果。
  6. 普通袜最终可能变为污渍白袜；彩蛋袜则走各自独立支线。

- 和旧版相比，发电机已不再硬编码“某种袜子怎么发电”，而是转成**物品驱动的行为模型**。

### 3. Generator Architecture

- 当前机器实现核心结构为：
  - `AbstractPrecipitateGeneratorBlockEntity`
  - `PrecipitateGeneratorBlockEntity`
  - `AdvancedPrecipitateGeneratorBlockEntity`

- 发电机共用基类目前负责：
  - 输入槽/输出槽管理
  - FE 存储
  - 向四周水平自动出能
  - 通用机器状态持久化
  - 将输入物品识别为 `AbstractSockItem` 后，调用其 `tickInGenerator(...)`

- 机器本体**不再直接决定袜子发电规则**。袜子逻辑已经下沉到物品类。

- `GeneratorTickContext` / `GeneratorTickResult` 已作为统一交互接口存在，用于：
  - 读取服务端环境、能量、沉淀、水耗等上下文
  - 让袜子返回本 tick 的发电量、输入替换、输出产物与是否已完全处理

- 普通沉淀发电机当前特性：
  - 无需流体输入
  - 支持普通袜发电
  - 自动向四周水平输出 FE

- 高级沉淀发电机当前特性：
  - 需要水作为额外资源
  - 发电倍率高于普通机
  - 污渍概率降低
  - 水消耗随沉淀等级上升而增加
  - 暴露标准流体能力

### 4. Sock Item Hierarchy

- 当前袜子体系已经重构为统一的物品层：
  - `AbstractSockItem`
  - `BasicStyledSockItem`
  - `OriginalScentItem`

- `AbstractSockItem` 统一承载：
  - 发电机可处理能力
  - 是否可穿戴判断
  - 材质是否参与 roll 判断
  - 默认普通袜 tick 行为
  - 可附魔、tooltip 和共通耐久处理

- `OriginalScentItem` 当前进一步统一承载：
  - 原味彩蛋袜的基础 tooltip 结构
  - 前置实体右键交互入口 `handleEntityInteraction(...)`
  - 与目标玩家“融为一体”的共通逻辑
  - 对应玩家 ID 字段与命中判定

- 目前已存在的袜子物品可分为以下几类：

#### 常规/款式袜

- `white_sock`
- `over_knee_sock`
- `sport_crew_sock`
- `pantyhose`
- `split_toe_sock`
- `stockings`

这些物品：
- 可穿戴
- 可放入发电机
- 参与材质 roll
- 默认复用普通袜发电逻辑

#### 特殊功能袜

- `rainbow_white_sock`
- `travel_disposable_sock`
- `boat_sock`

这些物品拥有各自独立的发电/穿戴数值逻辑。

#### 惩罚态袜

- `dirty_white_sock`

该物品不继承 `AbstractSockItem`，主要作为普通袜污渍后的结果物。

#### 彩蛋原味袜

- `zhaozhao_original_scent`
- `fusheng_original_scent`
- `datou_original_scent`

这些物品：
- 继承 `OriginalScentItem` 或其子类
- 默认不可穿戴
- 默认不参与材质 roll
- 可进入发电机输入槽
- 通过 Shift 展示彩蛋扩展 tooltip
- 共享“右键实体/玩家”通用交互入口
- 各自携带一个对应玩家 ID，可触发“融为一体”彩蛋分支

### 5. Material System

- 材质系统已经落地为**袜子实例数据**，而不是物品 ID 分裂。
- 当前材质通过 `CustomData` 存在于单个 `ItemStack` 上。

- 已实现材质枚举：
  - `cotton`
  - `wool`
  - `nylon`
  - `bamboo`
  - `polyester`
  - `iron`
  - `diamond`
  - `silk`
  - `flesh`
  - `gold`

- 当前相关数据字段包括：
  - `Materials`
  - `DiamondBonusDurabilityMax`
  - `DiamondBonusDurabilityRemaining`

- 常规袜子当前支持：
  - 生成时随机 1-5 种材质混纺
  - 材质占比归一化
  - 穿戴属性修正
  - 发电平面加成 / 倍率加成
  - 特殊耐久逻辑修正

- `SockDataUtil` 当前是材质系统与袜子实例数据的核心工具类，负责：
  - `CustomData` 读写
  - 沉淀、污渍、发电系数、体育生认知等旧数据维护
  - 材质混纺列表维护
  - 钻石额外耐久缓冲
  - tooltip 拼装

### 6. Curios Compatibility

- Curios 兼容仍为可选加载。
- 当前提供：
  - 自定义 `sock` 饰品槽
  - 玩家两个袜子栏位
  - 常规袜子、款式袜、船袜、旅行袜、彩虹袜可装备
  - `zhaozhao_original_scent` / `fusheng_original_scent` / `datou_original_scent` 不可穿戴

- 当前 Curios 属性逻辑包括：
  - 普通白袜和材质混纺属性
  - 船袜速度惩罚与双船袜额外惩罚
  - 彩虹白袜战斗/生命相关收益
  - 材质带来的移速、生命、幸运、护甲、击退抗性、游泳速度等修正

### 7. Special Socks And Easter Eggs

#### 昭昭的原味

- 物品：`zhaozhao_original_scent`
- 对应玩家 ID：`Research_King`
- 当前能力：
  - 不可穿戴
  - 不参与材质
  - 可进入发电机
  - 右键使用后施加一段链式 buff，并有冷却
  - 第一次使用授予成就 `the_real_taste`
  - 在发电机中不发电，但会耗能并对周围生物施加 `Lust` 效果，同时生成粒子
  - 右键对应玩家时，会崩解为袜子物品粒子与附魔粒子，并与该玩家“融为一体”

#### 腐生的原味

- 物品：`fusheng_original_scent`
- 对应玩家 ID：`fusheng`
- 当前能力：
  - 不可穿戴
  - 不发电
  - 在发电机中每 20 tick 只触发催熟粒子效果
  - 掉在地上时，如果记录了 owner，会每 20 tick 尝试对脚下耕地上的作物进行催熟或无破坏收获
  - 袜子自身记录催熟次数
  - Shift tooltip 会显示催熟计数
  - 达到 100 次时授予成就 `it_was_fusheng`
  - 右键对应玩家时，会崩解为袜子物品粒子与附魔粒子，并与该玩家“融为一体”

- 当前限制：
  - 如果没有 owner（例如系统生成），则不会执行地面催熟，也不会累计成就统计

#### 大头的原味

- 物品：`datou_original_scent`
- 对应玩家 ID：`MarverlousDT`
- 当前能力：
  - 不可穿戴
  - 不发电
  - 右键村民：强制村民等级 +1，并将该村民标记为只可使用一次
  - 右键敌对生物：尝试从该生物的 loot table 中生成一份掉落物，并将其标记为只可使用一次
  - 袜子内部记录：
    - 村民升级数
    - 生物掉落数
  - 当两个计数都达到 100 时，袜子觉醒，并授予成就 `power_belongs_to_dt`
  - 觉醒后可右键玩家，使其背包随机掉落一个物品，并让使用者进入 600 秒冷却
  - 被强制掉落物品的玩家会收到“你闻到了一些刺鼻的味道....”提示
  - 第一次成功让玩家掉物时授予成就 `players_can_generate_power`
  - 若右键的是对应玩家，则优先触发“融为一体”，不会进入觉醒掉物分支

- 当前已修复行为：
  - 通过前置 `PlayerInteractEvent.EntityInteract` 拦截，避免村民右键优先打开原版 GUI
  - 敌对生物掉落逻辑增加重试次数，减少因为 loot table 空结果导致的“多次点击才掉”现象
  - 村民升级成功时加入额外粒子效果

- 当前已知边界：
  - 村民当前会被提升等级，但**不保证立刻刷新出新层级交易池**。等级变化已经生效，但交易表刷新路径尚未完成额外适配。

### 8. Loot System

- 当前 loot 仍采用 NeoForge Global Loot Modifier。
- `AddSockLootModifier` 已从最初的白袜掉落，扩展到多种袜子来源控制。

- 当前掉落大致分层：
  - 基础袜池：普通白袜 + 新款式袜
  - 特殊池：船袜、旅行一次性袜
  - 稀有池：彩虹白袜
  - 彩蛋池：昭昭原味仅末地城等特殊来源

- 白袜/常规袜还可以附带：
  - 发电系数增幅
  - 体育生认知
  - 部分附魔
  - 材质混纺数据

### 9. Effects, Advancements And Events

- 当前新增/存在的效果与成就包括：
  - `LustMobEffect`
  - `the_real_taste`
  - `ultimate_blender`
  - `it_was_fusheng`
  - `power_belongs_to_dt`
  - `players_can_generate_power`
  - `sock_final_home`

- 成就采用统一思路：
  - JSON 中使用 `minecraft:impossible`
  - 代码侧通过 `ModAdvancements.grant(...)` 主动授予

- `ModEvents` 当前承担的主要内容：
  - 玩家材质/Curios 周期效果（跳跃、回血、爬墙等）
  - 情欲状态下清空怪物攻击目标
  - 腐生原味掉落物持续催熟处理
  - 腐生原味 `ItemTossEvent` owner 绑定
  - 所有原味彩蛋袜的前置实体右键拦截与统一分发

### 10. Resources And Assets

- 当前资源层已覆盖：
  - 方块状态
  - 方块/物品模型
  - 方块与物品贴图
  - 中英文语言文件
  - 战利品表与 loot modifier 数据
  - Curios 槽位定义与物品标签
  - 配方与清洗内容
  - 成就 JSON
  - Ponder 结构与场景资源

- 袜子资源现已包含：
  - 基础白袜、彩虹袜、旅行袜、船袜
  - 五个新款式袜子独立模型和贴图
  - 三个原味彩蛋袜独立模型与占位贴图

### 11. Optional Food And Drink Integrations

- 当前已加入以下可选联动依赖：
  - Iron's Spells 'n Spellbooks
  - Immortalers Delight / Farmer's Delight
  - Kaleidoscope Cookery `1.4.1-neoforge+mc1.21.1`
  - Kaleidoscope Tavern `1.2.0-neoforge+mc1.21.1`
- 森罗厨房联动新增 `stir_fried_sock`：使用碗和 `precipitate_power:socks` 标签内任意袜子在炒锅中制作。
- 森罗酒馆联动新增 `original_brew`：酒桶装满 4000 mB 水并放入 `#c:sock` 袜子后开始酿造，使用酒馆原生 1-6 级品质组件与空瓶提取流程。
- `OriginalBrewItem` 继承酒馆的 `DrinkBlockItem`，因此品质会随酒桶时间推进写入成品并显示在 tooltip 中；品质越高，饮用后的微醺时间越短。
- 两项联动配方均带 `neoforge:mod_loaded` 条件，对应模组未安装时不会加载其专用配方。

## Key Files By Responsibility

### Bootstrap

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - 模组主入口、注册流程、创造标签页内容。 

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePowerClient.java`
  - 客户端入口与 UI / Ponder 注册。 

- `src/main/java/top/realme/mc/precipitate_power/Config.java`
  - 发电、污渍、掉落等关键配置。 

### Generator

- `src/main/java/top/realme/mc/precipitate_power/block/entity/AbstractPrecipitateGeneratorBlockEntity.java`
  - 发电机共用逻辑与袜子 tick 调度中心。 

- `src/main/java/top/realme/mc/precipitate_power/block/entity/PrecipitateGeneratorBlockEntity.java`
  - 普通发电机实现。 

- `src/main/java/top/realme/mc/precipitate_power/block/entity/AdvancedPrecipitateGeneratorBlockEntity.java`
  - 高级发电机实现。 

- `src/main/java/top/realme/mc/precipitate_power/item/GeneratorTickContext.java`
  - 袜子发电机 tick 上下文。 

- `src/main/java/top/realme/mc/precipitate_power/item/GeneratorTickResult.java`
  - 袜子发电机 tick 结果对象。 

### Sock Items And Data

- `src/main/java/top/realme/mc/precipitate_power/item/AbstractSockItem.java`
  - 所有可进入发电机处理的袜子基类。 

- `src/main/java/top/realme/mc/precipitate_power/item/BasicStyledSockItem.java`
  - 5 个款式袜的共用实现。 

- `src/main/java/top/realme/mc/precipitate_power/item/OriginalScentItem.java`
  - 原味彩蛋袜共用实现，包括目标玩家 ID、统一实体交互入口与“融为一体”逻辑。 

- `src/main/java/top/realme/mc/precipitate_power/item/FushengOriginalScentItem.java`
  - 腐生原味实现。 

- `src/main/java/top/realme/mc/precipitate_power/item/DatouOriginalScentItem.java`
  - 大头原味实现。 

- `src/main/java/top/realme/mc/precipitate_power/item/ZhaozhaoOriginalScentItem.java`
  - 昭昭原味实现。 

- `src/main/java/top/realme/mc/precipitate_power/item/SockMaterial.java`
  - 材质枚举定义。 

- `src/main/java/top/realme/mc/precipitate_power/item/SockMaterialRoller.java`
  - 材质 roll 逻辑。 

- `src/main/java/top/realme/mc/precipitate_power/util/SockDataUtil.java`
  - 袜子数据工具核心。 

### Compat

- `src/main/java/top/realme/mc/precipitate_power/compat/curios/CuriosCompat.java`
  - Curios 注册入口。 

- `src/main/java/top/realme/mc/precipitate_power/compat/curios/SockCurio.java`
  - Curios 属性逻辑。 

- `src/main/java/top/realme/mc/precipitate_power/compat/ironsspellbooks/IronsSpellbooksCompat.java`
  - 彩虹袜法术兼容。 

### Events / Effects / Advancements

- `src/main/java/top/realme/mc/precipitate_power/event/ModEvents.java`
  - 玩家周期效果、腐生掉落实体 tick、大头右键前置交互等事件处理。 

- `src/main/java/top/realme/mc/precipitate_power/effect/LustMobEffect.java`
  - 情欲状态实现。 

- `src/main/java/top/realme/mc/precipitate_power/registry/ModEffects.java`
  - 药水效果注册。 

- `src/main/java/top/realme/mc/precipitate_power/registry/ModAdvancements.java`
  - 成就授予辅助。 

### Registries

- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlocks.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModItems.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlockEntities.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModMenus.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModLootModifiers.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModEffects.java`
- `src/main/java/top/realme/mc/precipitate_power/registry/ModAdvancements.java`

## Recent Important Changes

- 将袜子逻辑重构为 `AbstractSockItem` 驱动，发电机改为物品调度模型。 
- 加入材质混纺系统与 `SockMaterial` / `SockMaterialRoller`。 
- 新增 5 个独立款式袜子物品，并补齐独立资源。 
- 新增 `OriginalScentItem` 体系，并落地昭昭、腐生、大头三个彩蛋袜。 
- 为 `OriginalScentItem` 增加统一的实体右键入口、目标玩家 ID 字段与“融为一体”共用逻辑。 
- 腐生原味已实现：发电机催熟粒子、地面催熟、作物收获、owner 绑定、100 次成就。 
- 大头原味已实现：村民升级、敌对生物掉落、双计数、觉醒后玩家掉物、两条成就。 
- 将原先只服务大头的前置实体交互拦截扩展为所有原味彩蛋袜共用分发。 
- 为三只彩蛋袜补充对应玩家 ID，并加入“融为一体”粒子、世界消息与成就 `sock_final_home`。 
- 为大头原味加入村民升级粒子效果，并对敌对生物掉落逻辑增加重试。 
- 将腐生、大头原味加入 `沉淀电力` 创造物品栏标签。 
- 新增 `LustMobEffect` 与多个 `impossible` 成就 JSON。 
- 新增森罗厨房联动食物“爆炒袜子”，并建立统一 `precipitate_power:socks` 标签。
- 新增森罗酒馆可选依赖与“原味精酿”：通过 `#c:sock`、水和酒馆酒桶进行六阶段陈酿。

## Verification Status

- 最近已经反复执行并通过：
  - `./gradlew compileJava`
  - `./gradlew build --no-configuration-cache`

- 当前资源和 Java 结构在编译层面可用，但仍有若干玩法行为属于**代码已接通、运行时仍建议进游戏确认**：
  - 大头原味升级村民后是否需要立刻刷新交易池
  - 敌对生物 loot table 抽样在不同怪物上的实际表现
  - 腐生原味地面催熟在多种作物上的手感与性能
  - 三只原味袜命中对应玩家 ID 后，“融为一体”的粒子密度、命中判定与系统消息体验
  - 原味袜彩蛋粒子与右键交互在服务器环境中的最终体验
  - 森罗厨房炒锅能否正确匹配任意袜子标签并产出“爆炒袜子”
  - 森罗酒馆酒桶在六个品质阶段提取“原味精酿”时的品质 tooltip、空瓶返还和饮用体验

## Open Risks And Next Work Suggestions

- **大头原味的村民升级目前不会可靠地立刻补出新层级交易池。** 当前是提升等级并成功拦截右键，但交易池刷新仍需要额外适配。 
- 大头原味敌对生物掉落当前基于 loot table 抽样，虽然已增加重试次数，但不排除个别怪物因为 loot 条件限制而仍然掉不出东西。 
- 三只原味袜的“融为一体”当前按玩家名称或 UUID 字符串匹配，对大小写宽容，但仍依赖实际运行环境中的 `GameProfile` 名称与预设 ID 一致。 
- 腐生原味当前只对 `CropBlock` 做了“成熟后无破坏收获”，其他可骨粉植物主要仍是催熟，不做广义自动采收。 
- 三个原味袜贴图当前以资源占位和风格统一为主，若后续要提升辨识度，可以继续细画各自专属像素图。 
- 创造栏与资源结构已更新到 v1.5 方向，但 `SUMMARY.md` 这份总结不代表所有彩蛋分支都已经完成最终设计或平衡验证。 
