package com.matyrobbrt.mirouters.test;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.summary.GitHubActionsStepSummaryDumper;
import org.lwjgl.glfw.GLFW;

@Mod("mirtest")
public class MIRTest {
    public MIRTest(IEventBus bus, ModContainer container) {
        final MutableTestFramework framework = FrameworkConfiguration.builder(ResourceLocation.fromNamespaceAndPath(container.getNamespace(), "tests"))
                .clientConfiguration(() -> ClientConfiguration.builder()
                        .toggleOverlayKey(GLFW.GLFW_KEY_J)
                        .openManagerKey(GLFW.GLFW_KEY_N)
                        .build())
                .enable(Feature.CLIENT_SYNC, Feature.CLIENT_MODIFICATIONS)
                .dumpers(new GitHubActionsStepSummaryDumper())
                .build()
                .create();

        framework.init(bus, container);
    }
}
