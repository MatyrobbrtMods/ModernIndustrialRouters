package com.matyrobbrt.mirouters.mixin;

import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CompiledModule.class)
public interface CompiledModuleAccessor {
    @Invoker
    ModuleTarget invokeGetTarget();
}
