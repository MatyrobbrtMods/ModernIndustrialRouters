package com.matyrobbrt.mirouters.mixin;

import aztech.modern_industrialization.api.energy.EnergyApi;
import me.desht.modularrouters.item.module.EnergyDistributorModule;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnergyDistributorModule.class)
public class EnergyDistributorModuleMixin {
    @Inject(at = @At("HEAD"), method = "isValidTarget", cancellable = true)
    private void addEUTargets(UseOnContext ctx, CallbackInfoReturnable<Boolean> cir) {
        if (ctx.getLevel().getCapability(EnergyApi.SIDED, ctx.getClickedPos(), ctx.getClickedFace()) != null) {
            cir.setReturnValue(true);
        }
    }
}
