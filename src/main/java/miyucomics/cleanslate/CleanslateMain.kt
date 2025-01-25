package miyucomics.cleanslate

import miyucomics.cleanslate.inits.CleanslateBlocks
import miyucomics.cleanslate.inits.CleanslateItems
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier

class CleanslateMain : ModInitializer {
	override fun onInitialize() {
		CleanslateBlocks.init()
		CleanslateItems.init()
	}

	companion object {
		fun id(string: String) = Identifier("cleanslate", string)
	}
}