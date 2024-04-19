package com.matyrobbrt.mirouters.item;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import com.matyrobbrt.mirouters.MIRouters;
import com.matyrobbrt.mirouters.mixin.CompiledModuleAccessor;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.compiled.CompiledCreativeModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EUOutputModule extends ModuleItem {
    public EUOutputModule() {
        super(ModItems.defaultProps(), Compiled::new);
    }

    @Override
    public TintColor getItemTint() {
        return EUUpgrade.TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack itemStack) {
        return 0;
    }

    public static class Compiled extends CompiledCreativeModule {

        public Compiled(ModularRouterBlockEntity router, ItemStack stack) {
            super(router, stack);
        }

        @Override
        public boolean execute(@NotNull ModularRouterBlockEntity router) {
            var target = ((CompiledModuleAccessor) this).invokeGetTarget();
            if (target == null) {
                return false;
            } else {
                var routerStorage = router.getData(MIRouters.STORAGE);
                // TODO - cache
                MIEnergyStorage otherStorage = MiscUtil.getWorldForGlobalPos(target.gPos).getCapability(EnergyApi.SIDED, target.gPos.pos(), target.face); // TODO - cache
                if (routerStorage.getCapacity() > 0 && otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                    return EnergyStorageUtil.move(routerStorage, otherStorage, routerStorage.getTransferRate()) > 0;
                } else {
                    return false;
                }
            }
        }

        @Override
        protected boolean shouldStoreRawFilterItems() {
            return false;
        }
    }
}
