package com.matyrobbrt.mirouters.mixin;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import com.matyrobbrt.mirouters.MIRouters;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.compiled.CompiledEnergyOutputModule;
import me.desht.modularrouters.util.MiscUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompiledEnergyOutputModule.class)
public abstract class CompiledEnergyOutputModuleMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lme/desht/modularrouters/block/tile/ModularRouterBlockEntity;getEnergyStorage()Lnet/neoforged/neoforge/energy/IEnergyStorage;", shift = At.Shift.BEFORE), method = "execute", cancellable = true)
    private void executeEU(ModularRouterBlockEntity router, CallbackInfoReturnable<Boolean> cir) {
        var target = ((CompiledModuleAccessor) this).invokeGetTarget();
        var routerStorage = router.getData(MIRouters.STORAGE);
        // TODO - cache
        if (routerStorage.getCapacity() > 0) {
            MIEnergyStorage otherStorage = MiscUtil.getWorldForGlobalPos(target.gPos).getCapability(EnergyApi.SIDED, target.gPos.pos(), target.face); // TODO - cache
            if (otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                cir.setReturnValue(EnergyStorageUtil.move(routerStorage, otherStorage, routerStorage.getTransferRate()) > 0);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
