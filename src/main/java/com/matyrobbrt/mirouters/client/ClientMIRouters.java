package com.matyrobbrt.mirouters.client;

import com.matyrobbrt.mirouters.MIRouters;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ClientMIRouters {
    public static void setup(IEventBus bus) {
        bus.addListener((final RegisterColorHandlersEvent.Item event) -> {
            event.register((stack, idx) -> switch (idx) {
                case 0, 2 -> TintColor.WHITE.getRGB();
                case 1 -> ((ModItems.ITintable) stack.getItem()).getItemTint().getRGB();
                default -> TintColor.BLACK.getRGB();  // shouldn't get here
            }, MIRouters.UPGRADES.stream().map(DeferredHolder::value).toArray(ItemLike[]::new));
        });
    }
}
