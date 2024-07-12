package dev.mayaqq.embellishment.registries.blockEntities;

import com.google.common.collect.Lists;
import dev.mayaqq.embellishment.Embellishment;
import dev.mayaqq.embellishment.registries.EmbeBlockEntities;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


/* TODO: The villager data is only saved as a thing in the nbt and not entity data, might cause problems, also no implementation of special prices yet, wanna do an unique one */
public class MerchantsEffigyBlockEntity extends EffigyBlockEntity implements Merchant, VillagerDataHolder, WorldlyContainer {

    private static final int CONTAINER_SIZE = 3;
    @Nullable
    private Player tradingPlayer;
    @Nullable
    protected MerchantOffers offers;
    // 4 slots, 2 villager inputs, one output and a workstation
    protected NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    @Nullable
    private Player lastTradedPlayer;
    private int villagerXp;
    private long lastRestockGameTime;
    private int numberOfRestocksToday;
    private long lastRestockCheckDayTime;
    private int updateMerchantTimer;
    private boolean increaseProfessionLevelOnUpdate;
    private VillagerData data = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0);
    // I wonder if this tactic with the FakeEntity will work?
    private FakeEntity fakeEntity = new FakeEntity(this);

    protected final RandomSource random = RandomSource.create();

    public MerchantsEffigyBlockEntity(BlockPos pos, BlockState blockState) {
        super(EmbeBlockEntities.MERCHANTS_EFFIGY.get(), pos, blockState);
        this.setVillagerData(this.getVillagerData().setType(VillagerType.PLAINS).setProfession(VillagerProfession.NONE));
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!this.isTrading() && this.updateMerchantTimer > 0) {
            this.updateMerchantTimer--;
            if (this.updateMerchantTimer <= 0) {
                if (this.increaseProfessionLevelOnUpdate) {
                    this.increaseMerchantCareer();
                    this.increaseProfessionLevelOnUpdate = false;
                }
                // normally gets regen here
            }
        }

        if (this.lastTradedPlayer != null && this.level instanceof ServerLevel) {
            this.lastTradedPlayer = null;
        }

        if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.isTrading()) {
            this.stopTrading();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, this.items, provider);
        if (!this.level.isClientSide) {
            MerchantOffers merchantoffers = this.getOffers();
            if (!merchantoffers.isEmpty()) {
                tag.put(
                        "Offers", MerchantOffers.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), merchantoffers).getOrThrow()
                );
            }
        }
        VillagerData.CODEC
                .encodeStart(NbtOps.INSTANCE, this.getVillagerData())
                .resultOrPartial(Embellishment.LOGGER::error)
                .ifPresent(vtag -> tag.put("VillagerData", vtag));
        tag.putInt("Xp", this.villagerXp);
        tag.putLong("LastRestock", this.lastRestockGameTime);
        tag.putInt("RestocksToday", this.numberOfRestocksToday);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, provider);
        if (tag.contains("Offers")) {
            MerchantOffers.CODEC
                    .parse(provider.createSerializationContext(NbtOps.INSTANCE), tag.get("Offers"))
                    .resultOrPartial(Util.prefix("Failed to load offers: ", Embellishment.LOGGER::warn))
                    .ifPresent(offers -> this.offers = offers);
        }
        if (tag.contains("VillagerData", 10)) {
            VillagerData.CODEC
                    .parse(NbtOps.INSTANCE, tag.get("VillagerData"))
                    .resultOrPartial(Embellishment.LOGGER::error)
                    .ifPresent(this::setVillagerData);
        }
        if (tag.contains("Xp", 3)) {
            this.villagerXp = tag.getInt("Xp");
        }
        this.lastRestockGameTime = tag.getLong("LastRestock");
        this.numberOfRestocksToday = tag.getInt("RestocksToday");
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        boolean flag = this.getTradingPlayer() != null && player == null;
        this.tradingPlayer = player;
        if (flag) {
            this.stopTrading();
        }
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
        this.resetSpecialPrices();
    }

    private void resetSpecialPrices() {
        if (!this.level.isClientSide()) {
            for (MerchantOffer merchantoffer : this.getOffers()) {
                merchantoffer.resetSpecialPriceDiff();
            }
        }
    }

    private void startTrading(Player player) {
        this.setTradingPlayer(player);
        //TODO: open trading screen here this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());
    }

    @Override
    public MerchantOffers getOffers() {
        if (level.isClientSide) {
            throw new IllegalStateException("Cannot load Villager offers on the client");
        } else {
            if (this.offers == null) {
                this.offers = new MerchantOffers();
                this.updateTrades();
            }

            return this.offers;
        }
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {

    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.rewardTradeXp(offer);
    }

    protected void rewardTradeXp(MerchantOffer offer) {
        int i = 3 + this.random.nextInt(4);
        this.villagerXp = this.villagerXp + offer.getXp();
        this.lastTradedPlayer = this.getTradingPlayer();
        if (this.shouldIncreaseLevel()) {
            this.updateMerchantTimer = 40;
            this.increaseProfessionLevelOnUpdate = true;
            i += 5;
        }

        if (offer.shouldRewardExp()) {
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ(), i));
        }
    }

    /**
     * Notifies the merchant of a possible merchant recipe being fulfilled or not. Usually, this is just a sound byte being played depending on whether the suggested {@link ItemStack} is not empty.
     *
     * @param stack
     */
    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        // used for sounds in villager, not needed rn
    }

    @Override
    public @NotNull SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    protected SoundEvent getTradeUpdatedSound(boolean isYesSound) {
        return isYesSound ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }


    @Override
    public int getVillagerXp() {
        return villagerXp;
    }

    @Override
    public void overrideXp(int xp) {
        villagerXp = xp;
    }

    public void setVillagerXp(int villagerXp) {
        this.villagerXp = villagerXp;
    }

    private void resetNumberOfRestocks() {
        this.catchUpDemand();
        this.numberOfRestocksToday = 0;
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public boolean canRestock() {
        return true;
    }

    public void restock() {
        this.updateDemand();

        for (MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.resetUses();
        }

        this.resendOffersToTradingPlayer();
        this.lastRestockGameTime = this.level.getGameTime();
        this.numberOfRestocksToday++;
    }

    public void setOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    private boolean shouldIncreaseLevel() {
        int i = this.getVillagerData().getLevel();
        return VillagerData.canLevelUp(i) && this.villagerXp >= VillagerData.getMaxXpPerLevel(i);
    }

    private void increaseMerchantCareer() {
        this.setVillagerData(this.getVillagerData().setLevel(this.getVillagerData().getLevel() + 1));
        this.updateTrades();
    }

    protected void updateTrades() {
        VillagerData villagerdata = this.getVillagerData();
        Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap;
        if (this.level.enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
            Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap1 = VillagerTrades.EXPERIMENTAL_TRADES.get(villagerdata.getProfession());
            int2objectmap = int2objectmap1 != null ? int2objectmap1 : VillagerTrades.TRADES.get(villagerdata.getProfession());
        } else {
            int2objectmap = VillagerTrades.TRADES.get(villagerdata.getProfession());
        }

        if (int2objectmap != null && !int2objectmap.isEmpty()) {
            VillagerTrades.ItemListing[] avillagertrades$itemlisting = int2objectmap.get(villagerdata.getLevel());
            if (avillagertrades$itemlisting != null) {
                MerchantOffers merchantoffers = this.getOffers();
                this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, 2);
            }
        }
    }

    protected void addOffersFromItemListings(MerchantOffers givenMerchantOffers, VillagerTrades.ItemListing[] newTrades, int maxNumbers) {
        ArrayList<VillagerTrades.ItemListing> arraylist = Lists.newArrayList(newTrades);
        int i = 0;

        while (i < maxNumbers && !arraylist.isEmpty()) {
            MerchantOffer merchantoffer = arraylist.remove(this.random.nextInt(arraylist.size())).getOffer(this.fakeEntity, this.random);
            if (merchantoffer != null) {
                givenMerchantOffers.add(merchantoffer);
                i++;
            }
        }
    }

    private void resendOffersToTradingPlayer() {
        MerchantOffers merchantoffers = this.getOffers();
        Player player = this.getTradingPlayer();
        if (player != null && !merchantoffers.isEmpty()) {
            player.sendMerchantOffers(
                    player.containerMenu.containerId,
                    merchantoffers,
                    this.getVillagerData().getLevel(),
                    this.getVillagerXp(),
                    this.showProgressBar(),
                    this.canRestock()
            );
        }
    }

    private boolean needsToRestock() {
        for (MerchantOffer merchantoffer : this.getOffers()) {
            if (merchantoffer.needsRestock()) {
                return true;
            }
        }

        return false;
    }

    private boolean allowedToRestock() {
        return this.numberOfRestocksToday == 0 || this.numberOfRestocksToday < 2 && this.level.getGameTime() > this.lastRestockGameTime + 2400L;
    }

    public boolean shouldRestock() {
        long i = this.lastRestockGameTime + 12000L;
        long j = this.level.getGameTime();
        boolean flag = j > i;
        long k = this.level.getDayTime();
        if (this.lastRestockCheckDayTime > 0L) {
            long l = this.lastRestockCheckDayTime / 24000L;
            long i1 = k / 24000L;
            flag |= i1 > l;
        }

        this.lastRestockCheckDayTime = k;
        if (flag) {
            this.lastRestockGameTime = j;
            this.resetNumberOfRestocks();
        }

        return this.allowedToRestock() && this.needsToRestock();
    }

    private void catchUpDemand() {
        int i = 2 - this.numberOfRestocksToday;
        if (i > 0) {
            for (MerchantOffer merchantoffer : this.getOffers()) {
                merchantoffer.resetUses();
            }
        }

        for (int j = 0; j < i; j++) {
            this.updateDemand();
        }

        this.resendOffersToTradingPlayer();
    }

    private void updateDemand() {
        for (MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.updateDemand();
        }
    }

    @Override
    public void openTradingScreen(Player player, Component displayName, int level) {
        Merchant.super.openTradingScreen(player, displayName, level);
    }

    @Override
    public boolean isClientSide() {
        return this.level.isClientSide;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[0];
    }

    /**
     * Returns {@code true} if automation can insert the given item in the given slot from the given side.
     *
     * @param index
     * @param itemStack
     * @param direction
     */
    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        switch (index) {
            case 0: {
                if (direction == Direction.UP) {
                    return true;
                }
            }
            case 1: {
                // TODO
            }
            case 2: {
                return false;
            }
            case 3: {
                // TODO
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if automation can extract the given item in the given slot from the given side.
     *
     * @param index
     * @param stack
     * @param direction
     */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (index == 2) {
            return true;
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns the stack in the given slot.
     *
     * @param slot
     */
    @Override
    public ItemStack getItem(int slot) {
        return null;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     *
     * @param slot
     * @param amount
     */
    @Override
    public ItemStack removeItem(int slot, int amount) {
        return null;
    }

    /**
     * Removes a stack from the given slot and returns it.
     *
     * @param slot
     */
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return null;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     *
     * @param slot
     * @param stack
     */
    @Override
    public void setItem(int slot, ItemStack stack) {

    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     *
     * @param player
     */
    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    @Override
    public VillagerData getVillagerData() {
        return data;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        VillagerData villagerdata = this.getVillagerData();
        if (villagerdata.getProfession() != data.getProfession()) {
            this.offers = null;
        }

        this.data = data;
    }

    public static class FakeEntity extends Entity implements VillagerDataHolder {

        public MerchantsEffigyBlockEntity be;

        public FakeEntity(EntityType<?> entityType, Level level) {
            super(EntityType.ALLAY, level);
        }

        public FakeEntity(MerchantsEffigyBlockEntity be) {
            this(null, null);
            this.be = be;
            this.setLevel(be.level);
            this.setPosRaw(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ());
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {

        }

        @Override
        protected void readAdditionalSaveData(CompoundTag compound) {}

        @Override
        protected void addAdditionalSaveData(CompoundTag compound) {}

        @Override
        public VillagerData getVillagerData() {
            return be.getVillagerData();
        }

        @Override
        public void setVillagerData(VillagerData data) {
            be.setVillagerData(data);
        }
    }
}
