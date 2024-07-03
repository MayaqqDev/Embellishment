package dev.mayaqq.embellishment.client;

import dev.mayaqq.embellishment.Embellishment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Embellishment.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmbeClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // THis is here just to make things cleaner.
        EmbellishmentClient.init();
    }
}
