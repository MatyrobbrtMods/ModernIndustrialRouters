package com.matyrobbrt.mirouters.test;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.api.energy.EnergyApi;
import com.matyrobbrt.mirouters.MIRouters;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.ModuleTargetList;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.List;

public class MIREnergyTest {
    @GameTest
    @TestHolder
    @EmptyTemplate
    static void testEnergyOutputTransfer(RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1).maxSpeed();
        router.router().getUpgrades().insertItem(1, MIRouters.LV.toStack(), false);
        router.addDirectionalModule(ModItems.ENERGY_OUTPUT_MODULE, RelativeDirection.UP);

        // this is a bit hacky, but setUpgradesFrom triggers a recomp immediately
        router.router().setUpgradesFrom(router.router().getUpgrades());

        helper.setBlock(1, 2, 1, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:assembler")));

        // fill up the buffer a bit
        for (int i = 0; i < 5; i++)
            helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 1, 1), Direction.UP).receive(32 * 2, false);

        helper.startSequence()
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> helper.assertValueEqual(helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 2, 1), Direction.UP).getAmount(), 32 * 2 * 2L, "energy amount"))

                .thenExecute(() -> helper.assertValueEqual(helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 1, 1), Direction.UP).getAmount(), 32 * 2 * 5L - 32 * 2 * 2L, "router energy stored"))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void testEnergyDistributorTransfer(RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1).maxSpeed();
        router.router().getUpgrades().insertItem(1, MIRouters.LV.toStack(), false);

        var stack = ModItems.ENERGY_DISTRIBUTOR_MODULE.toStack();
        stack.set(ModDataComponents.MODULE_TARGET_LIST, new ModuleTargetList(List.of(
                new ModuleTarget(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), Direction.UP),
                new ModuleTarget(helper.getLevel(), helper.absolutePos(new BlockPos(1, 3, 1)), Direction.UP)
        )));
        router.addModule(stack);

        // this is a bit hacky, but setUpgradesFrom triggers a recomp immediately
        router.router().setUpgradesFrom(router.router().getUpgrades());

        helper.setBlock(1, 2, 1, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:assembler")));
        helper.setBlock(1, 3, 1, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("modern_industrialization:assembler")));

        // fill up the buffer a bit
        for (int i = 0; i < 5; i++)
            helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 1, 1), Direction.UP).receive(32 * 2, false);

        helper.startSequence()
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> helper.assertValueEqual(helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 2, 1), Direction.UP).getAmount(), 32 * 2 * 2L, "energy amount first"))
                .thenExecute(() -> helper.assertValueEqual(helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 3, 1), Direction.UP).getAmount(), 32 * 2 * 2L, "energy amount second"))

                .thenExecute(() -> helper.assertValueEqual(helper.requireCapability(EnergyApi.SIDED, new BlockPos(1, 1, 1), Direction.UP).getAmount(), 32 * 2 * 5L - 32 * 2 * 4L, "router energy stored"))
                .thenSucceed();
    }
}
