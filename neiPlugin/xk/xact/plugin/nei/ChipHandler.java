package xk.xact.plugin.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import xk.xact.util.Textures;
import xk.xact.client.gui.GuiCrafter;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

import java.util.ArrayList;
import java.util.List;

public class ChipHandler extends TemplateRecipeHandler {

	class EncoderRecipe extends CachedRecipe {

		public EncoderRecipe(int width, int height, Object[] items, ItemStack out, ItemStack chip) {
			result = new PositionedStack( chip, 128, 36 );
			ingredients = new ArrayList<PositionedStack>();
			setIngredients( width, height, items, out );
		}

		public void setIngredients(int width, int height, Object[] items, ItemStack out) {
			for( int x = 0; x < width; x++ ) {
				for( int y = 0; y < height; y++ ) {
					if( items[y * width + x] == null ) {
						continue;
					}
					PositionedStack stack = new PositionedStack( items[y * width + x], 21 + x * 18, 7 + y * 18 );
					stack.setMaxSize( 1 );
					ingredients.add( stack );
				}
			}
			PositionedStack stack = new PositionedStack( out, 93, 10 );
			stack.setMaxSize( 1 );
			ingredients.add( stack );
		}

		@Override
		public List<PositionedStack> getIngredients() {
			return getCycledIngredients( cycleticks / 20, ingredients );
		}

		@Override
		public PositionedStack getResult() {
			return result;
		}

		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		if( CraftManager.isEncoded( ingredient ) ) {

			CraftRecipe recipe = RecipeUtils.getRecipe( ingredient, FMLClientHandler.instance().getClient().theWorld );
			if( recipe != null ) {
				EncoderRecipe recipeT = new EncoderRecipe( 3, 3, recipe.getIngredients(), recipe.getResult(), ingredient );
				arecipes.add( recipeT );
			}
		}
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass() {
		return GuiCrafter.class;
	}

	@Override
	public String getRecipeName() {
		return "Encoded Recipe Chip";
	}

	@Override
	public String getGuiTexture() {
		return Textures.NEI_CHIP_HANDLER;
	}
}
