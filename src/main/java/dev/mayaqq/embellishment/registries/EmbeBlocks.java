package dev.mayaqq.embellishment.registries;

import dev.mayaqq.embellishment.Embellishment;
import dev.mayaqq.embellishment.registries.blocks.MerchantsEffigyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EmbeBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Embellishment.MODID);

    public static final DeferredBlock<MerchantsEffigyBlock> MERCHANTS_EFFIGY = BLOCKS.register("merchants_effigy", () -> new MerchantsEffigyBlock(BlockBehaviour.Properties.of()));
}
