package pl.kosma.allayduplicationbackport.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AllayEntity.class)
public class AllayEntityMixin {

    static private final int SEARCH_RANGE = 8;

    @Unique
    private int duplicationCooldown;

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        AllayEntity that = (AllayEntity)(Object) this;

        ItemStack playerItemStack = player.getStackInHand(hand);
        ItemStack allayItemStack = that.getStackInHand(Hand.MAIN_HAND);

        if (playerItemStack.getItem() == Items.AMETHYST_SHARD &&
            allayItemStack.getItem() == Items.AIR &&
            this.duplicationCooldown == 0 &&
            this.musicPlayingNearby(that.world, that.getBlockPos())
        )
        {
            // spawn fx
            that.world.playSoundFromEntity(player, that, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            that.world.addParticle(ParticleTypes.HEART, that.getParticleX(1.0), that.getRandomBodyY() + 0.5, that.getParticleZ(1.0), 0.2f, 0.2f, 0.2f);
            // spawn two new allays - workaround for client-side visual item ghosting
            for (int i=0; i<2; i++) {
                AllayEntity newAllay = EntityType.ALLAY.create(that.world);
                if (newAllay != null) {
                    newAllay.refreshPositionAfterTeleport(that.getPos());
                    newAllay.setPersistent();
                    that.world.spawnEntity(newAllay);
                    ((AllayEntityMixin)(Object)newAllay).startCooldown();
                }
            }
            // remove the OG allay
            that.discard();
            // cause side effects
            if (!player.getAbilities().creativeMode)
                playerItemStack.decrement(1);
            // prevent further processing
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        if (this.duplicationCooldown > 0)
            this.duplicationCooldown--;
    }

    private void startCooldown() {
        this.duplicationCooldown = 2500;
    }

    private boolean musicPlayingNearby(World world, BlockPos blockPos) {
        BlockBox box = new BlockBox(blockPos).expand(SEARCH_RANGE);
        for (long x=box.getMinX(); x<=box.getMaxX(); x++)
            for (long y=box.getMinY(); y<=box.getMaxY(); y++)
                for (long z=box.getMinZ(); z<box.getMaxZ(); z++) {
                    BlockState blockState = world.getBlockState(new BlockPos(x, y, z));
                    if (blockState.getBlock() == Blocks.JUKEBOX && blockState.get(Properties.HAS_RECORD)) {
                        return true;
                    }
                }
        return false;
    }
}
