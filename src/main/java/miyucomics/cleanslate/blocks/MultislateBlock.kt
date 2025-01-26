package miyucomics.cleanslate.blocks

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.circles.ICircleComponent
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.block.*
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

	override fun canEnterFromDirection(enterDir: Direction, pos: BlockPos, state: BlockState, world: ServerWorld): Boolean {
		return true;
	}

	override fun possibleExitDirections(pos: BlockPos, state: BlockState, world: World): EnumSet<Direction> {
		val exits = EnumSet.noneOf(Direction::class.java)

		Direction.stream().filter { hasDirection(state, it) }.forEach { dir ->
			when (dir.axis) {
				Direction.Axis.X -> {
					exits.add(Direction.UP)
					exits.add(Direction.DOWN)
					exits.add(Direction.NORTH)
					exits.add(Direction.SOUTH)
				}
				Direction.Axis.Y -> {
					exits.add(Direction.WEST)
					exits.add(Direction.EAST)
					exits.add(Direction.NORTH)
					exits.add(Direction.SOUTH)
				}
				Direction.Axis.Z -> {
					exits.add(Direction.WEST)
					exits.add(Direction.EAST)
					exits.add(Direction.UP)
					exits.add(Direction.DOWN)
				}
				null -> throw IllegalStateException()
			}
		}

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
	}
}