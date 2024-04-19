package com.matyrobbrt.mirouters.mixin;

import com.matyrobbrt.mirouters.MIRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModularRouterBlockEntity.class)
public abstract class RouterBEMixin extends BlockEntity {
    public RouterBEMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Inject(method = "compileUpgrades", at = @At(value = "INVOKE", target = "Lme/desht/modularrouters/block/tile/ModularRouterBlockEntity$RouterEnergyBuffer;updateForEnergyUpgrades(I)V", shift = At.Shift.AFTER))
    private void compileEU(CallbackInfo ci) {
        getData(MIRouters.STORAGE).update();
    }

    @Inject(method = "processClientSync", at = @At("TAIL"))
    private void processClientSyncForEU(CompoundTag compound, CallbackInfo ci) {
        //getData(MIRouters.STORAGE).update();
    }
}
