package moe.sylvi.bitexchange.block;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.block.entity.BitFactoryBlockEntity;
import moe.sylvi.bitexchange.block.entity.BitLiquefierBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BitLiquefierBlock extends BlockWithEntity {
    public static IntProperty LUMINANCE = IntProperty.of("luminance", 0, 15);

    public BitLiquefierBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getStateManager().getDefaultState().with(LUMINANCE, 0));
    }

    public static int getLuminance(BlockState state) {
        return state.get(LUMINANCE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BitLiquefierBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BitExchange.BIT_LIQUEFIER_BLOCK_ENTITY, BitLiquefierBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BitLiquefierBlockEntity liquefier) {
                ContainerItemContext context = ContainerItemContext.ofPlayerHand(player, hand);
                Storage itemStorage = context.find(FluidStorage.ITEM);
                Storage ownStorage = FluidStorage.SIDED.find(world, pos, hit.getSide());
                if (itemStorage != null && ownStorage != null) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        SoundEvent soundEvent;
                        ResourceAmount resourceAmount = StorageUtil.findExtractableContent(itemStorage,transaction);
                        if (resourceAmount != null) {
                            soundEvent = ((FluidVariant) resourceAmount.resource()).getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
                            var inserted = StorageUtil.move(itemStorage, ownStorage, variant -> true, resourceAmount.amount(), transaction);

                            if (inserted > 0) {
                                if (soundEvent != null) {
                                    player.playSound(soundEvent, SoundCategory.BLOCKS, 1f,1f);
                                }
                                transaction.commit();

                                liquefier.markDirty();
                                return ActionResult.SUCCESS;
                            }
                        }
                    }

                    try (Transaction transaction = Transaction.openOuter()) {
                        Optional<SoundEvent> soundEvent;
                        ResourceAmount resourceAmount = StorageUtil.findExtractableContent(ownStorage,transaction);
                        if (resourceAmount != null) {
                            soundEvent = ((FluidVariant) resourceAmount.resource()).getFluid().getBucketFillSound();
                            var extracted = StorageUtil.move(ownStorage, itemStorage, variant -> true, resourceAmount.amount(), transaction);
                            if (extracted > 0) {
                                if (soundEvent.isPresent()) {
                                    player.playSound(soundEvent.get(),SoundCategory.BLOCKS,1f,1f);
                                    world.playSound(pos.getX(), pos.getY(), pos.getZ(), soundEvent.get(), SoundCategory.BLOCKS, 1f, 1f, true);
                                }
                                transaction.commit();

                                liquefier.markDirty();
                                return ActionResult.SUCCESS;
                            }
                        }
                    }
                }


                //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
                //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

                if (screenHandlerFactory != null) {
                    //With this call the server will request the client to open the appropriate Screenhandler
                    player.openHandledScreen(screenHandlerFactory);
                }

                if (!liquefier.outputFluid.isResourceBlank()) {
                    var fluid = liquefier.outputFluid.variant.getFluid();
                    BitExchange.log(Level.INFO, "Stored fluid: " + Registry.FLUID.getId(fluid));
                    BitExchange.log(Level.INFO, "Stored amount: " + ((float)liquefier.outputFluid.amount / FluidConstants.BUCKET));
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BitLiquefierBlockEntity liquefier) {
                // convert the fluid back to bits
                liquefier.consumeFluid(liquefier.outputFluid);
                // drop items
                ItemScatterer.spawn(world, pos, liquefier);
                // update comparators
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LUMINANCE);
    }
}
