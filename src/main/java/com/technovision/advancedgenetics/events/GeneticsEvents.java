package com.technovision.advancedgenetics.events;

import com.technovision.advancedgenetics.api.genetics.Genes;
import com.technovision.advancedgenetics.registry.ComponentRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;

/**
 * Handles genes that trigger on rick clicking an item or block.
 *
 * @author TechnoVision
 */
public class GeneticsEvents {

    public static void registerEvents() {
        // Handles the "Eat Grass" gene
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                if (world.isClient() || !player.getMainHandStack().isEmpty()) return ActionResult.PASS;
                BlockPos pos = hitResult.getBlockPos();
                if (world.getBlockState(pos).getBlock() != Blocks.GRASS_BLOCK) return ActionResult.PASS;
                if (!player.getComponent(ComponentRegistry.PLAYER_GENETICS).hasGene(Genes.EAT_GRASS)) return ActionResult.PASS;
                if (player.getHungerManager().isNotFull()) {
                    player.getHungerManager().add(1, 0.0f);
                    world.setBlockState(pos, Blocks.DIRT.getDefaultState());
                }
                return ActionResult.SUCCESS;
            }
        );

        // Handles the "Milky" and "Meaty" genes
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                    if (world.isClient()) return ActionResult.PASS;
                    if (entity instanceof PlayerEntity clickedPlayer) {
                        // Milk player
                        ItemStack stack = player.getMainHandStack();
                        if (stack.getItem() == Items.BUCKET && clickedPlayer.getComponent(ComponentRegistry.PLAYER_GENETICS).hasGene(Genes.MILKY)) {
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
                        }
                        // Shear porkchops off player
                        if (stack.getItem() == Items.SHEARS && clickedPlayer.getComponent(ComponentRegistry.PLAYER_GENETICS).hasGene(Genes.MEATY)) {
                            clickedPlayer.dropStack(new ItemStack(Items.PORKCHOP));
                            player.getMainHandStack().damage(1, player, (e) -> player.sendToolBreakStatus(player.getActiveHand()));
                        }
                        // Shear wool off player
                        if (stack.getItem() == Items.SHEARS && clickedPlayer.getComponent(ComponentRegistry.PLAYER_GENETICS).hasGene(Genes.WOOLY)) {
                            clickedPlayer.dropStack(new ItemStack(Items.WHITE_WOOL));
                            player.getMainHandStack().damage(1, player, (e) -> player.sendToolBreakStatus(player.getActiveHand()));
                        }
                    }
                    return ActionResult.SUCCESS;
                }
        );

        // Handles "Explosive Exit" gene
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            if (player.getInventory().count(Items.GUNPOWDER) >= 5) {
                if (player.getComponent(ComponentRegistry.PLAYER_GENETICS).hasGene(Genes.EXPLOSIVE_EXIT)) {
                    player.getWorld().createExplosion(player, player.getX(), player.getY(), player.getZ(), 3.0f, Explosion.DestructionType.BREAK);
                }
            }
            return true;
        });
    }
}
