# Summary

## 中文概括

本模组目前已经实现了完整的“白袜发电”核心玩法，包括沉淀发电机方块、服务端发电与GUI交互、袜子的沉淀值/污渍值/发电倍率数据系统，以及污渍白袜的转化与清洗流程。战利品系统已经接入箱子掉落，可在指定维度的战利品箱中低概率获得袜子，并额外实现了自带 `x2778` 发电倍率且不可破坏的彩虹白袜。模组同时补齐了配方、掉落表、物品标签、中英文本地化、基础贴图与模型资源，并已将关键教学内容迁移为 Java Ponder 场景，整体内容已具备可编译、可运行、可演示的完整形态。

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
  - dirty count
- Dirty chance scales with precipitation level and is reduced by Unbreaking.
- Dirty socks intentionally do not inherit white sock bonuses/enchantments.
- Core files:
  - `src/main/java/top/realme/mc/precipitate_power/item/WhiteSockItem.java`
  - `src/main/java/top/realme/mc/precipitate_power/util/SockDataUtil.java`
  - `src/main/java/top/realme/mc/precipitate_power/registry/ModItems.java`

### 4. Loot And Data Integration

- Added a global loot modifier that injects white socks into chest loot.
- Loot-generated white socks can roll a random power coefficient bonus.
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

### 7. Verification

- Verified `compileJava` after gameplay implementation.
- Verified `compileJava` again after Ponder migration.
- Verified `processResources` for resources/data/lang changes.

## File Index

### Bootstrap

- `src/main/java/top/realme/mc/precipitate_power/PrecipitatePower.java`
  - Main mod bootstrap and common registration entry.

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
  - White sock item behavior and tooltip/enchantability.

- `src/main/java/top/realme/mc/precipitate_power/util/SockDataUtil.java`
  - Central helper for sock custom data.

- `src/main/java/top/realme/mc/precipitate_power/util/FormulaParser.java`
  - Formula parser/evaluator for generation config.

### Registries

- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlocks.java`
  - Block and block-item registration; capability registration.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModItems.java`
  - Item registration for both socks.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModBlockEntities.java`
  - Block entity type registration.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModMenus.java`
  - Menu type registration.

- `src/main/java/top/realme/mc/precipitate_power/registry/ModLootModifiers.java`
  - Loot modifier serializer registration.

### Loot And Tags

- `src/main/java/top/realme/mc/precipitate_power/loot/AddSockLootModifier.java`
  - Adds white socks to chest loot and optionally rolls coefficient bonus.

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

- `src/main/resources/assets/precipitate_power/lang/en_us.json`
  - English localization for gameplay, config, and Ponder text.

- `src/main/resources/assets/precipitate_power/lang/zh_cn.json`
  - Chinese localization for gameplay, config, and Ponder text.

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

## Notes

- The generator GUI is code-drawn and does not depend on a custom GUI texture.
- Dirty socks do not retain white sock coefficient/enchantment bonuses after conversion.
- Ponder scene body text now uses localization keys; scene titles are still registered in scene code.
