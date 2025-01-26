package miyucomics.cleanslate.mixin;

import at.petrak.hexcasting.common.items.storage.ItemSlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import miyucomics.cleanslate.blocks.MultislateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Shadow public abstract Block getBlock();

	@Inject(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
	private void stackSlate(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		if (getBlock() == HexBlocks.SLATE && !ItemSlate.hasPattern(context.getStack()))
			// ask a sane language to do it
			MultislateBlock.replaceSlate(context, cir);
	}
}