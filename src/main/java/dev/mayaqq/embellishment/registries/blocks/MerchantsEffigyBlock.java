package dev.mayaqq.embellishment.registries.blocks;

import com.mojang.serialization.MapCodec;
import dev.mayaqq.embellishment.registries.EmbeBlockEntities;
import dev.mayaqq.embellishment.registries.blockEntities.MerchantsEffigyBlockEntity;
import dev.mayaqq.embellishment.registries.entities.HollowMerchantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

@SuppressWarnings("NullableProblems")
public class MerchantsEffigyBlock extends EffigyBlock {

    private static final MapCodec<MerchantsEffigyBlock> CODEC = simpleCodec(MerchantsEffigyBlock::new);

    public MerchantsEffigyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, EmbeBlockEntities.MERCHANTS_EFFIGY.get(), (level1, pos, state1, be) -> MerchantsEffigyBlockEntity.tick(level1, pos, state1, be));
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        HollowMerchantEntity merchant = getHollowMerchant(pos, level);
        openTradingScreen(player, merchant.getDisplayName(), 0, merchant);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            HollowMerchantEntity merchant = new HollowMerchantEntity(level, VillagerType.PLAINS);
            setHollowMerchant(merchant, pos, level);
        }
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public void setHollowMerchant(HollowMerchantEntity hollowMerchant, BlockPos pos, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MerchantsEffigyBlockEntity merchantsEffigyBlockEntity) {
            merchantsEffigyBlockEntity.setHollowMerchant(hollowMerchant);
        }
    }

    public @Nullable HollowMerchantEntity getHollowMerchant(BlockPos pos, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MerchantsEffigyBlockEntity merchantsEffigyBlockEntity) {
            return merchantsEffigyBlockEntity.getHollowMerchant();
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MerchantsEffigyBlockEntity(pos, state);
    }

    public void openTradingScreen(Player player, Component displayName, int level, HollowMerchantEntity merchant) {
        OptionalInt optionalint = player.openMenu(
                new SimpleMenuProvider((containerId, playerInventory, user) -> new MerchantMenu(containerId, playerInventory, merchant), displayName)
        );
        if (optionalint.isPresent()) {
            MerchantOffers merchantoffers = merchant.getOffers();
            if (!merchantoffers.isEmpty()) {
                player.sendMerchantOffers(optionalint.getAsInt(), merchantoffers, level, merchant.getVillagerXp(), merchant.showProgressBar(), merchant.canRestock());
            }
        }
    }
}
