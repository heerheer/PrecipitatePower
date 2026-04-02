package top.realme.mc.precipitate_power.ponder.scenes;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
public final class DirtyWhiteSockPonderScenes {
    private DirtyWhiteSockPonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(
                ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "dirty_white_sock"),
                helper.asLocation("dirty_white_sock"),
                DirtyWhiteSockPonderScenes::washing
        );
    }

    public static void washing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        BlockPos fanPos = util.grid().at(2, 1, 3);
        BlockPos leavesPos = util.grid().at(2, 1, 2);
        BlockState fanState = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "encased_fan"))
                .defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.NORTH);

        scene.title("dirty_white_sock_washing", "Dirty White Sock");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(20);

        scene.world().setBlock(fanPos, fanState, false);
        scene.world().showSection(util.select().position(fanPos), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(fanPos), -64);
        scene.idle(20);

        scene.world().setBlock(leavesPos, Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT,true)
                        .setValue(LeavesBlock.WATERLOGGED,true),
                false);
        scene.world().showSection(util.select().position(leavesPos), Direction.DOWN);
        scene.idle(20);

        scene.world().createItemEntity(
                new Vec3(2.5D, 2.0D, 1.5D),
                Vec3.ZERO,
                new ItemStack(ModItems.DIRTY_WHITE_SOCK.get())
        );
        scene.overlay().showText(60)
                .sharedText("dirty_white_sock.wash")
                .pointAt(new Vec3(2.5D, 1.0D, 1.5D))
                .attachKeyFrame();
    }
}
