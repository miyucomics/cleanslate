package miyucomics.cleanslate

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent
import miyucomics.cleanslate.inits.CleanslateBlocks
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.block.Block
import net.minecraft.block.enums.WallMountLocation
import net.minecraft.data.client.*
import net.minecraft.data.client.VariantSettings.Rotation
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.math.Direction

class CleanslateDatagen : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
		val pack = generator.createPack()
		pack.addProvider(::CleanslateModelGenerator)
	}
}

private class CleanslateModelGenerator(generator: FabricDataOutput) : FabricModelProvider(generator) {
	override fun generateBlockStateModels(generator: BlockStateModelGenerator) {
		generator.excludeFromSimpleItemModelGeneration(CleanslateBlocks.FLAT_LOOKING_IMPETUS_BLOCK)
		generator.excludeFromSimpleItemModelGeneration(CleanslateBlocks.FLAT_REDSTONE_IMPETUS_BLOCK)
		generator.excludeFromSimpleItemModelGeneration(CleanslateBlocks.FLAT_RIGHT_CLICK_IMPETUS_BLOCK)
		generator.excludeFromSimpleItemModelGeneration(CleanslateBlocks.MULTISLATE_BLOCK)
		generator.excludeFromSimpleItemModelGeneration(CleanslateBlocks.WITHERED_SLATE_BLOCK)

		generator.registerWallPlant(CleanslateBlocks.MULTISLATE_BLOCK)

		generator.blockStateCollector.accept(generateSlatelike(CleanslateBlocks.FLAT_LOOKING_IMPETUS_BLOCK, "block/flat_looking_impetus", Properties.FACING))
		generator.blockStateCollector.accept(generateSlatelike(CleanslateBlocks.FLAT_REDSTONE_IMPETUS_BLOCK, "block/flat_redstone_impetus", Properties.FACING))
		generator.blockStateCollector.accept(generateSlatelike(CleanslateBlocks.FLAT_RIGHT_CLICK_IMPETUS_BLOCK, "block/flat_right_click_impetus", Properties.FACING))
		generator.blockStateCollector.accept(generateSlatelike(CleanslateBlocks.WITHERED_SLATE_BLOCK, "block/withered_slate", Properties.HORIZONTAL_FACING))
	}

	override fun generateItemModels(generator: ItemModelGenerator) {}

	companion object {
		private fun generateSlatelike(block: Block, name: String, directionProp: Property<Direction>): VariantsBlockStateSupplier {
			return VariantsBlockStateSupplier.create(block).coordinate(
				BlockStateVariantMap.create(directionProp, Properties.WALL_MOUNT_LOCATION, BlockCircleComponent.ENERGIZED).register { facing, mount, energized ->
					val variant = BlockStateVariant.create()
						.put(VariantSettings.MODEL, if (energized) CleanslateMain.id(name + "_active") else CleanslateMain.id(name))

					val rotationY: Int
					when (mount) {
						WallMountLocation.CEILING -> {
							variant.put(VariantSettings.X, intToRotation(180))
							rotationY = facing.opposite.horizontal * 90
						}
						WallMountLocation.WALL -> {
							variant.put(VariantSettings.X, intToRotation(270))
							variant.put(VariantSettings.UVLOCK, true)
							rotationY = facing.horizontal * 90
						}
						WallMountLocation.FLOOR -> rotationY = facing.horizontal * 90
						else -> throw IllegalStateException()
					}

					variant.put(VariantSettings.Y, intToRotation(rotationY))
				}
			)
		}

		private fun intToRotation(angle: Int): Rotation {
			val normalized = ((angle % 360) + 360) % 360
			return when (normalized) {
				0 -> Rotation.R0
				90 -> Rotation.R90
				180 -> Rotation.R180
				270 -> Rotation.R270
				else -> throw IllegalStateException()
			}
		}
	}
}