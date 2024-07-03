package dev.mayaqq.embellishment;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Embellishment.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EmbeConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue EXAMPLE_BOOLEAN = BUILDER
            .comment("Example comment")
            .define("exampleBoolean", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean exampleBoolean;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        exampleBoolean = EXAMPLE_BOOLEAN.get();
    }
}
