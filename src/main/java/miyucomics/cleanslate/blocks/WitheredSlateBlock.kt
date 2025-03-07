package miyucomics.cleanslate.blocks

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import net.minecraft.block.*
import net.minecraft.block.enums.WallMountLocation
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import java.util.*

class WitheredSlateBlock : BlockCircleComponent(Settings.copy(Blocks.DEEPSLATE_TILES).strength(4f, 4f)), Waterloggable {
	init {
		this.defaultState = stateManager.defaultState
			.with(ENERGIZED, false)
			.with(FACING, Direction.NORTH)
			.with(WATERLOGGED, false)
	}

	override fun particleHeight(pos: BlockPos, state: BlockState, world: World) = 0.5f - 15f / 16f
	override fun isTransparent(state: BlockState, reader: BlockView, pos: BlockPos) = !state.get(WATERLOGGED)
	override fun getFluidState(state: BlockState) = if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
	override fun canEnterFromDirection(enterDir: Direction, pos: BlockPos, state: BlockState, world: ServerWorld) = enterDir != this.normalDir(pos, state, world).opposite

	override fun acceptControlFlow(image: CastingImage, env: CircleCastEnv, enterDir: Direction, pos: BlockPos, state: BlockState, world: ServerWorld): ControlFlow {
		val exits = this.possibleExitDirections(pos, state, world)
		exits.remove(enterDir.opposite)
		return ControlFlow.Continue(image, exits.map { dir -> exitPositionFromDirection(pos, dir) })
	}

	override fun possibleExitDirections(pos: BlockPos, state: BlockState, world: World): EnumSet<Direction> {
		val exits = EnumSet.allOf(Direction::class.java)
		exits.remove(this.normalDir(pos, state, world))
		return exits
	}

	override fun normalDir(pos: BlockPos, state: BlockState, world: World, recursionLeft: Int) = when (state.get(ATTACH_FACE)) {
		WallMountLocation.FLOOR -> Direction.UP
		WallMountLocation.CEILING -> Direction.DOWN
		WallMountLocation.WALL -> state.get(FACING)
		null -> throw IllegalStateException()
	}

	override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = when (state.get<WallMountLocation>(ATTACH_FACE)) {
		WallMountLocation.FLOOR -> AABB_FLOOR
		WallMountLocation.CEILING -> AABB_CEILING
		WallMountLocation.WALL -> when (state.get(FACING)) {
			Direction.NORTH -> AABB_NORTH_WALL
			Direction.EAST -> AABB_EAST_WALL
			Direction.SOUTH -> AABB_SOUTH_WALL
			else -> AABB_WEST_WALL
		}
		null -> throw IllegalStateException()
	}

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		super.appendProperties(builder)
		builder.add(FACING, ATTACH_FACE, WATERLOGGED)
	}

	override fun getPlacementState(context: ItemPlacementContext): BlockState? {
		val fluidState = context.world.getFluidState(context.blockPos)
		for (direction in context.placementDirections) {
			var blockstate = if (direction.axis === Direction.Axis.Y) {
				defaultState.with(ATTACH_FACE, if (direction == Direction.UP) WallMountLocation.CEILING else WallMountLocation.FLOOR).with(FACING, context.horizontalPlayerFacing.opposite)
			} else {
				defaultState.with(ATTACH_FACE, WallMountLocation.WALL).with(FACING, direction.opposite)
			}
			blockstate = blockstate.with(WATERLOGGED, fluidState.isIn(FluidTags.WATER) && fluidState.level == 8)
			if (blockstate.canPlaceAt(context.world, context.blockPos))
				return blockstate
		}
		return null
	}

	override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos) = canAttach(world, pos, getConnectedDirection(state).opposite)

	override fun getStateForNeighborUpdate(state: BlockState, pFacing: Direction, pFacingState: BlockState, world: WorldAccess, pCurrentPos: BlockPos, pFacingPos: BlockPos): BlockState {
		if (state.get(WATERLOGGED))
			world.scheduleFluidTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickRate(world))

		return if (getConnectedDirection(state).opposite == pFacing && !state.canPlaceAt(world, pCurrentPos))
			state.fluidState.blockState
		else
			super.getStateForNeighborUpdate(state, pFacing, pFacingState, world, pCurrentPos, pFacingPos)
	}

	override fun rotate(state: BlockState, rot: BlockRotation) = state.with(FACING, rot.rotate(state.get(FACING)))
	override fun mirror(state: BlockState, mirror: BlockMirror) = state.rotate(mirror.getRotation(state.get(FACING)))

	companion object {
		val WATERLOGGED: BooleanProperty = Properties.WATERLOGGED
		val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
		val ATTACH_FACE: EnumProperty<WallMountLocation> = Properties.WALL_MOUNT_LOCATION

		private const val THICKNESS: Double = 1.0
		val AABB_FLOOR: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, THICKNESS, 16.0)
		val AABB_CEILING: VoxelShape = createCuboidShape(0.0, 16 - THICKNESS, 0.0, 16.0, 16.0, 16.0)
		val AABB_EAST_WALL: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, THICKNESS, 16.0, 16.0)
		val AABB_WEST_WALL: VoxelShape = createCuboidShape(16 - THICKNESS, 0.0, 0.0, 16.0, 16.0, 16.0)
		val AABB_SOUTH_WALL: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, THICKNESS)
		val AABB_NORTH_WALL: VoxelShape = createCuboidShape(0.0, 0.0, 16 - THICKNESS, 16.0, 16.0, 16.0)

		fun canAttach(pReader: WorldView, pos: BlockPos, pDirection: Direction): Boolean {
			val blockpos = pos.offset(pDirection)
			return pReader.getBlockState(blockpos).isSideSolidFullSquare(pReader, blockpos, pDirection.opposite)
		}

		fun getConnectedDirection(state: BlockState): Direction = when (state.get(ATTACH_FACE)) {
			WallMountLocation.CEILING -> Direction.DOWN
			WallMountLocation.FLOOR -> Direction.UP
			else -> state.get(FACING)
		}
	}
}