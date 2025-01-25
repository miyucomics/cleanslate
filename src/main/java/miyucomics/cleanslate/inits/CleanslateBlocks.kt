package miyucomics.cleanslate.inits

import miyucomics.cleanslate.CleanslateMain
import miyucomics.cleanslate.blocks.*
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object CleanslateBlocks {
	val FLAT_LOOKING_IMPETUS_BLOCK = FlatLookingImpetusBlock()
	val FLAT_REDSTONE_IMPETUS_BLOCK = FlatRedstoneImpetusBlock()
	val FLAT_RIGHT_CLICK_IMPETUS_BLOCK = FlatRightClickImpetusBlock()
	@JvmField
	val MULTISLATE_BLOCK = MultislateBlock()
	@JvmField
	val WITHERED_SLATE_BLOCK = WitheredSlateBlock()

	val FLAT_LOOKING_IMPETUS_BLOCK_ENTITY: BlockEntityType<FlatLookingImpetusBlockEntity> = BlockEntityType.Builder.create(::FlatLookingImpetusBlockEntity, FLAT_LOOKING_IMPETUS_BLOCK).build(null)
	val FLAT_REDSTONE_IMPETUS_BLOCK_ENTITY: BlockEntityType<FlatRedstoneImpetusBlockEntity> = BlockEntityType.Builder.create(::FlatRedstoneImpetusBlockEntity, FLAT_REDSTONE_IMPETUS_BLOCK).build(null)
	val FLAT_RIGHT_CLICK_IMPETUS_BLOCK_ENTITY: BlockEntityType<FlatRightClickImpetusBlockEntity> = BlockEntityType.Builder.create(::FlatRightClickImpetusBlockEntity, FLAT_RIGHT_CLICK_IMPETUS_BLOCK).build(null)

	@JvmStatic
	fun init() {
		Registry.register(Registries.BLOCK, CleanslateMain.id("flat_looking_impetus"), FLAT_LOOKING_IMPETUS_BLOCK)
		Registry.register(Registries.BLOCK, CleanslateMain.id("flat_redstone_impetus"), FLAT_REDSTONE_IMPETUS_BLOCK)
		Registry.register(Registries.BLOCK, CleanslateMain.id("flat_right_click_impetus"), FLAT_RIGHT_CLICK_IMPETUS_BLOCK)
		Registry.register(Registries.BLOCK, CleanslateMain.id("multislate"), MULTISLATE_BLOCK)
		Registry.register(Registries.BLOCK, CleanslateMain.id("withered_slate"), WITHERED_SLATE_BLOCK)

		Registry.register(Registries.BLOCK_ENTITY_TYPE, CleanslateMain.id("flat_looking_impetus"), FLAT_LOOKING_IMPETUS_BLOCK_ENTITY)
		Registry.register(Registries.BLOCK_ENTITY_TYPE, CleanslateMain.id("flat_redstone_impetus"), FLAT_REDSTONE_IMPETUS_BLOCK_ENTITY)
		Registry.register(Registries.BLOCK_ENTITY_TYPE, CleanslateMain.id("flat_right_click_impetus"), FLAT_RIGHT_CLICK_IMPETUS_BLOCK_ENTITY)
	}
}