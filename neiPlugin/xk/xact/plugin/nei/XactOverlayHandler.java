package xk.xact.plugin.nei;


import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingGui;

import java.util.List;

public class XactOverlayHandler implements IOverlayHandler {


	@Override
	public void overlayRecipe(GuiContainer firstGui, List<PositionedStack> ingredients, boolean shift) {

		if( firstGui instanceof InteractiveCraftingGui ) {

			// get the ingredients and align them to the grid.
			ItemStack[] alignedIngredients = getAlignedIngredients( ingredients );

			// send the ingredients to the Gui (client-side)
			((InteractiveCraftingGui) firstGui).sendGridIngredients( alignedIngredients );
		}

	}


	public static ItemStack[] getAlignedIngredients(List<PositionedStack> ingredients) {
		ItemStack[] alignedIngredients = new ItemStack[9];

		for( PositionedStack current : ingredients ) {
			if( current == null )
				continue;

			int row = (current.relx - 25) / 18;
			int column = (current.rely - 6) / 18;

			alignedIngredients[column * 3 + row] = current.items[0];
		}
		return alignedIngredients;
	}

}
