package miyucomics.cleanslate.inits

import at.petrak.hexcasting.api.HexAPI
import miyucomics.cleanslate.CleanslateMain
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.BlockItem
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey

object CleanslateItems {
	val WITHERED_SLATE = BlockItem(CleanslateBlocks.WITHERED_SLATE_BLOCK, Settings())
	@JvmField val FLAT_LOOKING_IMPETUS = BlockItem(CleanslateBlocks.FLAT_LOOKING_IMPETUS_BLOCK, Settings())
	@JvmField val FLAT_REDSTONE_IMPETUS = BlockItem(CleanslateBlocks.FLAT_REDSTONE_IMPETUS_BLOCK, Settings())
	@JvmField val FLAT_RIGHT_CLICK_IMPETUS = BlockItem(CleanslateBlocks.FLAT_RIGHT_CLICK_IMPETUS_BLOCK, Settings())

	@JvmStatic
	fun init() {
		Registry.register(Registries.ITEM, CleanslateMain.id("withered_slate"), WITHERED_SLATE)
		Registry.register(Registries.ITEM, CleanslateMain.id("flat_right_click_impetus"), FLAT_RIGHT_CLICK_IMPETUS)
		Registry.register(Registries.ITEM, CleanslateMain.id("flat_looking_impetus"), FLAT_LOOKING_IMPETUS)
		Registry.register(Registries.ITEM, CleanslateMain.id("flat_redstone_impetus"), FLAT_REDSTONE_IMPETUS)

		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.key, HexAPI.modLoc("hexcasting"))).register { group ->
			group.add(ItemStack(WITHERED_SLATE))
			group.add(ItemStack(FLAT_LOOKING_IMPETUS))
			group.add(ItemStack(FLAT_REDSTONE_IMPETUS))
			group.add(ItemStack(FLAT_RIGHT_CLICK_IMPETUS))
		}
	}
}