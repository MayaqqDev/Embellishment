package dev.mayaqq.embellishment;

import com.mojang.logging.LogUtils;
import dev.mayaqq.embellishment.registries.EmbeBlockEntities;
import dev.mayaqq.embellishment.registries.EmbeBlocks;
import dev.mayaqq.embellishment.registries.EmbeItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Embellishment.MODID)
public class Embellishment {
    public static final String MODID = "embellishment";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Embellishment(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Embellishing this mod into your Instance.");
        EmbeItems.ITEMS.register(modEventBus);
        EmbeBlocks.BLOCKS.register(modEventBus);
        EmbeBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, EmbeConfig.SPEC);
    }
}
