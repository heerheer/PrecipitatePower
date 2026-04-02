package top.realme.mc.precipitate_power.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModBlocks;
import top.realme.mc.precipitate_power.registry.ModItems;

public final class PrecipitateGeneratorPonderScenes {
    private PrecipitateGeneratorPonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(
                ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "precipitate_generator"),
                helper.asLocation("precipitate_generator"),
                PrecipitateGeneratorPonderScenes::basicUsage
        );
    }

    public static void basicUsage(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        BlockPos generatorPos = util.grid().at(2, 1, 2);
        BlockState generatorState = ModBlocks.PRECIPITATE_GENERATOR.get().defaultBlockState()
                .setValue(top.realme.mc.precipitate_power.block.PrecipitateGeneratorBlock.FACING, Direction.NORTH);

        scene.title("precipitate_generator_basic", "Precipitation Generator");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(20);

        scene.world().setBlock(generatorPos, generatorState, false);
        scene.world().showSection(util.select().position(generatorPos),Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(60)
                .sharedText("precipitate_generator.place")
                .pointAt(util.vector().centerOf(generatorPos))
                .placeNearTarget();

        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showControls(util.vector().topOf(generatorPos), Pointing.DOWN, 60)
                .withItem(new ItemStack(ModItems.WHITE_SOCK.get()))
                .rightClick();
        scene.overlay().showText(60)
                .sharedText("precipitate_generator.insert_sock")
                .pointAt(util.vector().topOf(generatorPos))
                .placeNearTarget();

        scene.idle(20);

        scene.markAsFinished();
    }
}
