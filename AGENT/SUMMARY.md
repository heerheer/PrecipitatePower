# Summary

## 中文概括

本模组目前已经实现了完整的“白袜发电”核心玩法，包括沉淀发电机方块、服务端发电与GUI交互、袜子的沉淀值/污渍值/发电倍率数据系统，以及污渍白袜的转化与清洗流程。战利品系统已经接入箱子掉落，可在指定维度的战利品箱中低概率获得袜子，并额外实现了自带 `x2778` 发电倍率、`100%` 体育生认知且不可破坏的彩虹白袜。模组同时补齐了配方、掉落表、物品标签、中英文本地化、基础贴图与模型资源，并已将关键教学内容迁移为 Java Ponder 场景。除此之外，当前版本已经接入 Curios 的 `sock` 饰品槽位支持：玩家默认拥有两个 `sock` 槽位，白袜、彩虹白袜和有污渍的白袜都可以放入其中；普通白袜在战利品中会随机获得 `0%-100%` 的“体育生认知”，装备后提供对应的移动速度加成，而污渍白袜会降低移动速度。Curios 兼容以可选方式实现，未安装 Curios 时模组仍可正常加载；彩虹白袜的额外施法/攻击速度加成则通过 Iron's Spellbooks 的可选兼容进行分流。

## Module Summary

### 1. Mod Bootstrap And Runtime Wiring

- Replaced the generated NeoForge example scaffold with real mod bootstrap code.
- Registered the mod's blocks, items, block entities, menu, loot modifier serializer, creative tab, config, GUI screen, and client-side Ponder plugin.
- Main entry points:
  - `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - `src/main/java/top/realme/mc/precipitate_power/PrecipitatePowerClient.java`
  - `src/main/java/top/realme/mc/precipitate_power/Config.java`

### 2. Precipitation Generator System

- Implemented the precipitation generator block and block entity.
- Added GUI opening, server ticking, FE generation, inventory rules, and side capabilities.
- Generation is driven by a configurable math formula using precipitation level as `x`.
- White socks inserted into the machine can precipitate, get dirty over time, and eventually become dirty white socks.
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/block/PrecipitateGeneratorBlock.java`
  - `src/main/java/top/realme/mc/precipitate_power/block/entity/PrecipitateGeneratorBlockEntity.java`
  - `src/main/java/top/realme/mc/precipitate_power/menu/PrecipitateGeneratorMenu.java`
  - `src/main/java/top/realme/mc/precipitate_power/client/PrecipitateGeneratorScreen.java`
  - `src/main/java/top/realme/mc/precipitate_power/util/FormulaParser.java`

### 3. Sock Item And NBT Data Model

- Implemented `white_sock` and `dirty_white_sock`.
- White socks store:
  - precipitation level
  - power coefficient
  - athletic cognition
  - dirty count
- Dirty chance scales with precipitation level and is reduced by Unbreaking.
- Dirty socks intentionally do not inherit white sock bonuses/enchantments.
- Rainbow white socks initialize with fixed special values:
  - `x2778` power coefficient
  - `100%` athletic cognition
  - unbreakable state
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/item/WhiteSockItem.java`
  - `src/main/java/top/realme/mc/precipitate_power/util/SockDataUtil.java`
  - `src/main/java/top/realme/mc/precipitate_power/registry/ModItems.java`

### 4. Loot And Data Integration

- Added a global loot modifier that injects white socks into chest loot.
- Loot-generated white socks can roll a random power coefficient bonus.
- Loot-generated white socks can also roll random athletic cognition between `0%` and `100%`.
- Athletic cognition rolls are intentionally weighted so higher values are rarer.
- Added item tags for `c:precipitable` and `precipitate_power:precipitable`.
- Added block loot table for the generator.
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/loot/AddSockLootModifier.java`
  - `src/main/java/top/realme/mc/precipitate_power/registry/ModLootModifiers.java`
  - `src/main/resources/data/neoforge/loot_modifiers/global_loot_modifiers.json`
  - `src/main/resources/data/precipitate_power/loot_modifiers/add_white_sock_to_chests.json`
  - `src/main/resources/data/c/tags/item/precipitable.json`
  - `src/main/resources/data/precipitate_power/tags/item/precipitable.json`
  - `src/main/resources/data/precipitate_power/loot_table/blocks/precipitate_generator.json`

### 5. Assets, Models, Blockstates, Localization

- Wired the provided textures/models into the implemented content.
- Added generator blockstate and item model.
- Replaced template localization with real gameplay/config/Ponder text in both English and Chinese.
- Core files:
  - `src/main/resources/assets/precipitate_power/blockstates/precipitate_generator.json`
  - `src/main/resources/assets/precipitate_power/models/item/precipitate_generator.json`
  - `src/main/resources/assets/precipitate_power/lang/en_us.json`
  - `src/main/resources/assets/precipitate_power/lang/zh_cn.json`

### 6. Java Ponder Migration

- Migrated two KubeJS/Ponderer scenes into Java Ponder scenes.
- Registered them via a mod-local Ponder plugin.
- Replaced dependence on `ponderer:basic` with `scene.configureBasePlate(0, 0, 5)`.
- Scenes implemented:
  - precipitation generator basic usage
  - dirty white sock washing
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/ponder/PrecipitatePowerPonderPlugin.java`
  - `src/main/java/top/realme/mc/precipitate_power/ponder/scenes/PrecipitateGeneratorPonderScenes.java`
  - `src/main/java/top/realme/mc/precipitate_power/ponder/scenes/DirtyWhiteSockPonderScenes.java`

### 7. Optional Curios Sock Slot Integration

- Added Curios integration for a custom `sock` slot with a default size of 2 for players.
- Allowed `white_sock`, `rainbow_white_sock`, and `dirty_white_sock` to be equipped in the `sock` slot.
- Added a custom empty slot icon derived from the white sock silhouette.
- Added Curios-based sock attribute effects:
  - white socks grant movement speed based on athletic cognition
  - dirty white socks apply a `-10%` movement speed penalty
  - rainbow white socks grant `+2` attack damage, `+7` armor, and `+7` max health
  - rainbow white socks grant either an Iron's Spellbooks magic bonus or a fallback combat bonus depending on installed mods
- Implemented Curios support as optional compatibility:
  - the mod now loads normally without Curios installed
  - Curios behavior is registered only when the `curios` mod is present
  - Curios-specific Java references are isolated to a compat package instead of core item classes
- Updated Gradle and mod metadata so Curios is compile-time optional and runtime-optional.
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/compat/curios/CuriosCompat.java`
  - `src/main/java/top/realme/mc/precipitate_power/compat/curios/SockCurio.java`
  - `src/main/resources/data/precipitate_power/curios/slots/sock.json`
  - `src/main/resources/data/precipitate_power/curios/entities/player.json`
  - `src/main/resources/data/curios/tags/item/sock.json`
  - `src/main/resources/assets/precipitate_power/textures/slot/empty_curios_sock_slot.png`
  - `build.gradle`
  - `src/main/templates/META-INF/neoforge.mods.toml`

### 8. Optional Iron's Spellbooks Integration

- Added optional Iron's Spellbooks compat for rainbow white socks.
- When Iron's Spellbooks is present, rainbow white socks grant `+8%` spell cooldown reduction while equipped in the `sock` slot.
- When Iron's Spellbooks is not present, the same rainbow sock bonus falls back to `+8%` attack speed instead.
- Kept all Iron's-specific references isolated to a compat package so the base mod remains loader-safe without Iron's installed.
- Added development/runtime support for Iron's Spellbooks and its runtime dependency set in Gradle.
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/compat/ironsspellbooks/IronsSpellbooksCompat.java`
  - `src/main/java/top/realme/mc/precipitate_power/compat/curios/SockCurio.java`
  - `build.gradle`
  - `gradle.properties`
  - `src/main/templates/META-INF/neoforge.mods.toml`

### 9. Verification

- Verified `compileJava` after gameplay implementation.
- Verified `compileJava` again after Ponder migration.
- Verified `compileJava` after Curios integration.
- Verified `compileJava` again after refactoring Curios support into optional compat.
- Verified `compileJava` after adding athletic cognition and sock attribute effects.
- Verified `compileJava` after adding optional Iron's Spellbooks compatibility.
- Verified `processResources` for resources/data/lang changes.

## File Index

### Bootstrap

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - Main mod bootstrap, common registration entry, and optional Curios compat gate.

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePowerClient.java`
  - Client bootstrap, menu screen registration, Ponder plugin registration.

- `src/main/java/top/realme/mc/precipitate_power/Config.java`
  - Common config values for machine behavior and loot behavior.

### Generator Implementation

- `src/main/java/top/realme/mc/precipitate_power/block/PrecipitateGeneratorBlock.java`
  - Generator block, facing state, menu opening, ticker hookup.

- `src/main/java/top/realme/mc/precipitate_power/block/entity/PrecipitateGeneratorBlockEntity.java`
  - Generator logic, energy, inventory, precipitation, dirtying, sided interaction.

- `src/main/java/top/realme/mc/precipitate_power/menu/PrecipitateGeneratorMenu.java`
  - Server menu/container logic.

- `src/main/java/top/realme/mc/precipitate_power/client/PrecipitateGeneratorScreen.java`
  - Client GUI rendering for the generator.

### Socks And NBT Utilities

- `src/main/java/top/realme/mc/precipitate_power/item/WhiteSockItem.java`
  - White sock item behavior and tooltip/enchantability; no longer directly references Curios APIs.

- `src/main/java/top/realme/mc/precipitate_power/item/RainbowWhiteSockItem.java`
  - Rainbow white sock default initialization and extra tooltip text.

- `src/main/java/top/realme/mc/precipitate_power/util/SockDataUtil.java`
  - Central helper for sock custom data, including power coefficient, athletic cognition, rainbow defaults, and tooltip formatting.

- `src/main/java/top/realme/mc/precipitate_power/util/FormulaParser.java`
  - Formula parser/evaluator for generation config.

### Optional Curios Compat

- `src/main/java/top/realme/mc/precipitate_power/compat/curios/CuriosCompat.java`
  - Registers sock items as Curios only when the Curios mod is present.

- `src/main/java/top/realme/mc/precipitate_power/compat/curios/SockCurio.java`
  - Shared Curios item behavior implementation used for all sock items, including speed penalties/bonuses and rainbow sock combat bonuses.

### Optional Iron's Spellbooks Compat

- `src/main/java/top/realme/mc/precipitate_power/compat/ironsspellbooks/IronsSpellbooksCompat.java`
  - Applies the optional rainbow sock magic-related attribute bonus when Iron's Spellbooks is installed.

### Registries

- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlocks.java`
  - Block and block-item registration; capability registration.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModItems.java`
  - Item registration for white, rainbow white, and dirty white socks.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlockEntities.java`
  - Block entity type registration.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModMenus.java`
  - Menu type registration.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModLootModifiers.java`
  - Loot modifier serializer registration.

### Loot And Tags

- `src/main/java/top/realme/mc/precipitate_power/loot/AddSockLootModifier.java`
  - Adds white socks to chest loot and optionally rolls coefficient and athletic cognition bonuses, with rare rainbow sock generation in Nether/End loot.

- `src/main/resources/data/neoforge/loot_modifiers/global_loot_modifiers.json`
  - Enables the mod's loot modifier.

- `src/main/resources/data/precipitate_power/loot_modifiers/add_white_sock_to_chests.json`
  - Concrete modifier instance data.

- `src/main/resources/data/c/tags/item/precipitable.json`
  - Common precipitable tag entry.

- `src/main/resources/data/precipitate_power/tags/item/precipitable.json`
  - Mod-local precipitable tag entry.

- `src/main/resources/data/precipitate_power/loot_table/blocks/precipitate_generator.json`
  - Generator block self-drop loot table.

- `src/main/resources/data/curios/tags/item/sock.json`
  - Curios item tag assigning the three sock items to the `sock` slot.

- `src/main/resources/data/precipitate_power/curios/slots/sock.json`
  - Defines the custom `sock` slot type with size 2 and a custom icon.

- `src/main/resources/data/precipitate_power/curios/entities/player.json`
  - Grants the `sock` slot type to players.

### Assets And Localization

- `src/main/resources/assets/precipitate_power/blockstates/precipitate_generator.json`
  - Generator facing variants.

- `src/main/resources/assets/precipitate_power/models/item/precipitate_generator.json`
  - Generator item model.

- `src/main/resources/assets/precipitate_power/models/block/precipitate_generator.json`
  - Existing generator block model used by the implementation.

- `src/main/resources/assets/precipitate_power/models/item/white_sock.json`
  - Existing white sock item model.

- `src/main/resources/assets/precipitate_power/models/item/dirty_white_sock.json`
  - Existing dirty white sock item model.

- `src/main/resources/assets/precipitate_power/textures/block/sedimentation_generator.png`
  - Existing generator block texture.

- `src/main/resources/assets/precipitate_power/textures/item/white_sock.png`
  - Existing white sock texture.

- `src/main/resources/assets/precipitate_power/textures/item/dirty_white_sock.png`
  - Existing dirty white sock texture.

- `src/main/resources/assets/precipitate_power/textures/item/rainbow_white_sock.png`
  - Existing rainbow white sock texture.

- `src/main/resources/assets/precipitate_power/textures/slot/empty_curios_sock_slot.png`
  - Custom empty Curios slot icon derived from the white sock outline.

- `src/main/resources/assets/precipitate_power/lang/en_us.json`
  - English localization for gameplay, config, Ponder text, Curios slot labels, and sock stat tooltips.

- `src/main/resources/assets/precipitate_power/lang/zh_cn.json`
  - Chinese localization for gameplay, config, Ponder text, Curios slot labels, and sock stat tooltips.

### Ponder

- `src/main/java/top/realme/mc/precipitate_power/ponder/PrecipitatePowerPonderPlugin.java`
  - Ponder plugin entry for the mod.

- `src/main/java/top/realme/mc/precipitate_power/ponder/scenes/PrecipitateGeneratorPonderScenes.java`
  - Java Ponder scene for generator basic usage.

- `src/main/java/top/realme/mc/precipitate_power/ponder/scenes/DirtyWhiteSockPonderScenes.java`
  - Java Ponder scene for dirty sock washing.

- `src/main/resources/assets/precipitate_power/ponder/precipitate_generator.nbt`
  - Existing Ponder structure resource in assets.

- `src/main/resources/assets/precipitate_power/ponder/dirty_white_sock.nbt`
  - Existing Ponder structure resource in assets.

### Existing Project Data Touched By Context

- `src/main/resources/data/precipitate_power/recipe/precipitate_generator.json`
  - Existing recipe resource for the generator.

- `src/main/resources/data/precipitate_power/recipe/splashing/white_sock_washing.json`
  - Existing/Create washing recipe resource for sock cleaning.

- `build.gradle`
  - Build configuration with optional Curios and Iron's Spellbooks API dependencies plus local runtime entries for Curios, Iron's, GeckoLib, Iron's Patreon Lib, playerAnimator, and other dev mods.

- `gradle.properties`
  - Shared version properties, including Curios and Iron's Spellbooks development versions.

- `src/main/templates/META-INF/neoforge.mods.toml`
  - Mod metadata and dependency declarations, including Curios and Iron's Spellbooks as optional dependencies.

## Notes

- The generator GUI is code-drawn and does not depend on a custom GUI texture.
- Dirty socks do not retain white sock coefficient/enchantment bonuses after conversion.
- Ponder scene body text now uses localization keys; scene titles are still registered in scene code.
- Curios support is implemented as optional compatibility rather than a hard dependency, so core item classes remain loader-safe without Curios installed.
- Rainbow sock bonus routing is mod-aware: with Iron's Spellbooks installed it grants a magic cooldown bonus, otherwise it grants attack speed instead.
