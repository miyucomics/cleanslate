package miyucomics.cleanslate.mixin;

import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.items.storage.ItemSlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import miyucomics.cleanslate.blocks.MultislateBlock;
import miyucomics.cleanslate.inits.CleanslateBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Shadow public abstract Block getBlock();

	@Inject(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
	private void stackSlate(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		if (getBlock() == HexBlocks.SLATE && !ItemSlate.hasPattern(context.getStack())) {
			World world = context.getWorld();
			BlockPos blockPos = context.getBlockPos();
			BlockState currentBlockState = world.getBlockState(blockPos);

			if (currentBlockState.isOf(HexBlocks.SLATE)) {
				BlockState newBlockState = CleanslateBlocks.MULTISLATE_BLOCK.getDefaultState().with(BlockSlate.WATERLOGGED, currentBlockState.get(BlockSlate.WATERLOGGED));
				MultifaceGrowthBlock growth = (MultifaceGrowthBlock) newBlockState.getBlock();

				Direction excludeDirection = null;
				BlockState finalBlock = null;

				switch (currentBlockState.get(BlockSlate.ATTACH_FACE)) {
					case CEILING -> {
						excludeDirection = Direction.UP;
						finalBlock = growth.withDirection(newBlockState, context.getWorld(), blockPos, Direction.UP);
					}
					case FLOOR -> {
						excludeDirection = Direction.DOWN;
						finalBlock = growth.withDirection(newBlockState, context.getWorld(), blockPos, Direction.DOWN);
					}
					case WALL -> {
						excludeDirection = currentBlockState.get(BlockSlate.FACING).getOpposite();
						finalBlock = growth.withDirection(newBlockState, context.getWorld(), blockPos, excludeDirection);
					}
				}

				if (finalBlock == null) {
					cir.setReturnValue(null);
					return;
				}

				Direction finalExcludeDirection = excludeDirection;
				BlockState finalBlock1 = finalBlock;
				cir.setReturnValue(Arrays.stream(context.getPlacementDirections())
						.filter(direction -> direction != finalExcludeDirection)
						.map(direction -> ((MultifaceGrowthBlock) finalBlock1.getBlock()).withDirection(finalBlock1, world, blockPos, direction))
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null));
			} else if (currentBlockState.isOf(CleanslateBlocks.MULTISLATE_BLOCK)) {
				cir.setReturnValue(Arrays.stream(context.getPlacementDirections()).map(direction -> currentBlockState.with(MultifaceGrowthBlock.getProperty(direction), true)).filter(Objects::nonNull).findFirst().orElse(null));
			}
		}
	}
}