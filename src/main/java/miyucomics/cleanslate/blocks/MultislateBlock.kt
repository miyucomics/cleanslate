package miyucomics.cleanslate.blocks

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.ICircleComponent
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate
import at.petrak.hexcasting.common.blocks.circles.BlockSlate
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import miyucomics.cleanslate.inits.CleanslateBlocks
import net.minecraft.block.*
import net.minecraft.block.enums.WallMountLocation
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.*

class MultislateBlock : MultifaceGrowthBlock(Settings.copy(Blocks.DEEPSLATE_TILES).strength(4f, 4f)), ICircleComponent, Waterloggable {
	init {
		this.defaultState = defaultState
			.with(BlockAbstractImpetus.ENERGIZED, false)
			.with(WATERLOGGED, false)
	}

	private val grower = LichenGrower(this)

	override fun getStateForNeighborUpdate(blockState: BlockState, direction: Direction, blockState2: BlockState, worldAccess: WorldAccess, blockPos: BlockPos, blockPos2: BlockPos): BlockState {
		if (blockState.get(WATERLOGGED))
			worldAccess.scheduleFluidTick(blockPos, Fluids.WATER, Fluids.WATER.getTickRate(worldAccess))
		return super.getStateForNeighborUpdate(blockState, direction, blockState2, worldAccess, blockPos, blockPos2)
	}

	override fun getFluidState(blockState: BlockState): FluidState {
		if (blockState.get(WATERLOGGED))
			return Fluids.WATER.getStill(false)
		return super.getFluidState(blockState)
	}

	override fun getGrower() = this.grower
	override fun isTransparent(blockState: BlockState, blockView: BlockView, blockPos: BlockPos) = false
	override fun canReplace(blockState: BlockState, itemPlacementContext: ItemPlacementContext) = !itemPlacementContext.shouldCancelInteraction() && itemPlacementContext.stack.isOf(HexItems.SLATE) && super.canReplace(blockState, itemPlacementContext)

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		super.appendProperties(builder)
		builder.add(BlockAbstractImpetus.ENERGIZED, WATERLOGGED)
	}

	override fun acceptControlFlow(image: CastingImage, env: CircleCastEnv, enterDir: Direction, pos: BlockPos, state: BlockState, world: ServerWorld): ControlFlow {
		val exits = this.possibleExitDirections(pos, state, world)
		exits.remove(enterDir.opposite)
		return ControlFlow.Continue(image, exits.map { dir -> exitPositionFromDirection(pos, dir) })
	}

	private fun getOtherAxises(direction: Direction): List<Direction> {
		val other = mutableListOf<Direction>()
		when (direction.axis) {
			Direction.Axis.X -> {
				other.add(Direction.UP)
				other.add(Direction.DOWN)
				other.add(Direction.NORTH)
				other.add(Direction.SOUTH)
			}
			Direction.Axis.Y -> {
				other.add(Direction.WEST)
				other.add(Direction.EAST)
				other.add(Direction.NORTH)
				other.add(Direction.SOUTH)
			}
			Direction.Axis.Z -> {
				other.add(Direction.WEST)
				other.add(Direction.EAST)
				other.add(Direction.UP)
				other.add(Direction.DOWN)
			}
			null -> throw IllegalStateException()
		}
		return other
	}

	override fun canEnterFromDirection(enterDir: Direction, pos: BlockPos, state: BlockState, world: ServerWorld) = getOtherAxises(enterDir).any { hasDirection(state, it) };

	override fun possibleExitDirections(pos: BlockPos, state: BlockState, world: World): EnumSet<Direction> {
		val exits = EnumSet.noneOf(Direction::class.java)
		Direction.stream().filter { hasDirection(state, it) }.forEach { dir -> exits.addAll(getOtherAxises(dir)) }
		return exits
	}

	override fun startEnergized(pos: BlockPos, state: BlockState, world: World): BlockState {
		val new = state.with(BlockAbstractImpetus.ENERGIZED, true)
		world.setBlockState(pos, new)
		return new
	}

	override fun endEnergized(pos: BlockPos, state: BlockState, world: World): BlockState {
		val new = state.with(BlockAbstractImpetus.ENERGIZED, false)
		world.setBlockState(pos, new)
		return new
	}

	override fun isEnergized(pos: BlockPos, state: BlockState, world: World): Boolean = state.get(BlockAbstractImpetus.ENERGIZED)

	companion object {
		private val WATERLOGGED: BooleanProperty = Properties.WATERLOGGED

		@JvmStatic
		fun replaceSlate(context: ItemPlacementContext, cir: CallbackInfoReturnable<BlockState>) {
			val world = context.world
			val pos = context.blockPos
			val state = world.getBlockState(pos)

			if (state.isOf(HexBlocks.SLATE)) {
				val blockEntity = world.getBlockEntity(pos)
				if (blockEntity is BlockEntitySlate && blockEntity.pattern != null)
					return

				val (usedDirection, multislate) = getAttachDirection(world, pos, state)
				if (multislate == null) {
					cir.returnValue = null
					return
				}

				cir.returnValue = findFirstValidDirection(multislate, context.placementDirections, world, pos, usedDirection)
				return
			}

			if (state.isOf(CleanslateBlocks.MULTISLATE_BLOCK))
				cir.returnValue = findFirstValidDirection(state, context.placementDirections, world, pos)
		}

		private fun getAttachDirection(world: World, pos: BlockPos, state: BlockState): Pair<Direction, BlockState?> {
			val multislateBlock = CleanslateBlocks.MULTISLATE_BLOCK.defaultState
				.with(BlockSlate.WATERLOGGED, state.get(BlockSlate.WATERLOGGED))
			val growthInterface = multislateBlock.block as MultifaceGrowthBlock

			return when (state.get(BlockSlate.ATTACH_FACE)!!) {
				WallMountLocation.CEILING -> Direction.UP to growthInterface.withDirection(multislateBlock, world, pos, Direction.UP)
				WallMountLocation.FLOOR -> Direction.DOWN to growthInterface.withDirection(multislateBlock, world, pos, Direction.DOWN)
				WallMountLocation.WALL -> {
					val excludeDirection = state.get(BlockSlate.FACING).opposite
					excludeDirection to growthInterface.withDirection(multislateBlock, world, pos, excludeDirection)
				}
			}
		}

		private fun findFirstValidDirection(multislate: BlockState, placementDirections: Array<Direction>, world: World, pos: BlockPos, exclude: Direction? = null): BlockState? {
			return placementDirections.asSequence().filter { it != exclude }.mapNotNull { direction -> (multislate.block as? MultifaceGrowthBlock)?.withDirection(multislate, world, pos, direction) }.firstOrNull()
		}
	}
}