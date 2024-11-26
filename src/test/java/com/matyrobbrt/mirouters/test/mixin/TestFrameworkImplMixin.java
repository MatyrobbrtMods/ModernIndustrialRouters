package com.matyrobbrt.mirouters.test.mixin;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.impl.TestFrameworkImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TestFrameworkImpl.class) // TODO - the test framework really needs a better way of doing things
public class TestFrameworkImplMixin {
    @Inject(at = @At("HEAD"), method = "init", cancellable = true)
    private void init(final IEventBus modBus, final ModContainer container, CallbackInfo ci) {
        if (!container.getModId().equals("mirtest")) ci.cancel();
    }
}
