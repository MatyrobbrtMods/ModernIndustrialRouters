package com.matyrobbrt.mirouters;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.compiled.CompiledCreativeModule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class TestModule extends ModuleItem {
    public TestModule(Properties props) {
        super(props, (modularRouterBlockEntity, stack) -> {
            return new CompiledCreativeModule(modularRouterBlockEntity, stack) {

                @Override
                public boolean execute(@NotNull ModularRouterBlockEntity modularRouterBlockEntity) {
                    if (!modularRouterBlockEntity.getLevel().isClientSide)
                        modularRouterBlockEntity.getLevel().setBlockAndUpdate(modularRouterBlockEntity.getBlockPos().above(), Blocks.ACACIA_BUTTON.defaultBlockState());
                    return true;
                }

                @Override
                protected boolean shouldStoreRawFilterItems() {
                    return false;
                }
            };
        });
    }

    @Override
    public TintColor getItemTint() {
        return TintColor.BLACK;
    }

    @Override
    public int getEnergyCost(ItemStack itemStack) {
        return 0;
    }
}
