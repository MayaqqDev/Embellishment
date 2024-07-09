package dev.mayaqq.embellishment.registries;

import dev.mayaqq.embellishment.Embellishment;
import dev.mayaqq.embellishment.registries.blockEntities.MerchantsEffigyBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EmbeBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Embellishment.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MerchantsEffigyBlockEntity>> MERCHANTS_EFFIGY = BLOCK_ENTITIES.register("merchants_effigy", () -> BlockEntityType.Builder.of(MerchantsEffigyBlockEntity::new, EmbeBlocks.MERCHANTS_EFFIGY.get()).build(null));
}
