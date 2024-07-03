package dev.mayaqq.embellishment.events;

import dev.mayaqq.embellishment.Embellishment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = Embellishment.MODID)
public class EmbeEvents {
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {}
}
