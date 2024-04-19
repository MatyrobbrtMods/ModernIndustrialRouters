package com.matyrobbrt.mirouters.mixin;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import com.llamalad7.mixinextras.sugar.Local;
import com.matyrobbrt.mirouters.MIRouters;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledEnergyDistributorModule;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.MiscUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CompiledEnergyDistributorModule.class)
public class CompiledEnergyDistributorModuleMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lme/desht/modularrouters/block/tile/ModularRouterBlockEntity;getEnergyStorage()Lnet/neoforged/neoforge/energy/IEnergyStorage;"), method = "execute", cancellable = true)
    private void executeEU(ModularRouterBlockEntity router, CallbackInfoReturnable<Boolean> cir, @Local List<ModuleTarget> inRange) {
        var routerStorage = router.getData(MIRouters.STORAGE);
        if (routerStorage.getCapacity() > 0) {
            long toSend = routerStorage.getAmount() / inRange.size();
            boolean doBeam = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2;
            long total = 0;
            for (ModuleTarget target : inRange) {
                MIEnergyStorage otherStorage = MiscUtil.getWorldForGlobalPos(target.gPos).getCapability(EnergyApi.SIDED, target.gPos.pos(), target.face); // TODO - cache
                if (otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                    long sent = EnergyStorageUtil.move(routerStorage, otherStorage, toSend);
                    if (sent > 0 && doBeam) {
                        router.addItemBeam(new BeamData(router.getTickRate(), target.gPos.pos(), 0xE04040));
                    }
                    total += sent;
                }
            }
            cir.setReturnValue(total > 0);
        }
    }
}
