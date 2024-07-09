package dev.mayaqq.embellishment.registries;

import dev.mayaqq.embellishment.Embellishment;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EmbeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Embellishment.MODID);

    // Block Items
    public static final DeferredItem<BlockItem> MERCHANTS_EFFIGY = ITEMS.register("merchants_effigy", () -> new BlockItem(EmbeBlocks.MERCHANTS_EFFIGY.get(), new BlockItem.Properties()));
}
