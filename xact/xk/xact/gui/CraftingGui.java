package xk.xact.gui;


import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.RecipeDeque;

public abstract class CraftingGui extends GuiContainer implements InteractiveCraftingGui {

	public CraftingGui(Container container) {
		super( container );
	}

	public void setRecipe(CraftRecipe recipe) {
		ItemStack[] ingredients = (recipe == null || !recipe.isValid()) ? null : recipe.getIngredients();
		sendGridIngredients( ingredients );
	}

	///////////////
	///// InteractiveCraftingGui

	public abstract void sendGridIngredients(ItemStack[] ingredients);

	@Override
	public void handleKeyBinding(int keyCode, String keyDescription) {
		CraftRecipe recipe = null;

		if( keyDescription.equals( "xact.clear" ) ) {
			recipe = null;

		} else if( keyDescription.equals( "xact.load" ) ) {
			Slot hoveredSlot = GuiUtils.getHoveredSlot();

			if( hoveredSlot != null && hoveredSlot.getHasStack() ) {
				ItemStack stackInSlot = hoveredSlot.getStack();
				if( CraftManager.isEncoded( stackInSlot ) ) {
					recipe = RecipeUtils.getRecipe( stackInSlot, GuiUtils.getWorld() );
					if( !recipe.isValid() )
						recipe = null;
				}
			}

		} else if( keyDescription.equals( "xact.prev" ) ) {
			recipe = getPreviousRecipe();
			if( recipe == null ) {
				return;
			}

		} else if( keyDescription.equals( "xact.next" ) ) {
			recipe = getNextRecipe();
			if( recipe == null ) {
				return;
			}

		} else if( keyDescription.equals( "xact.delete" ) ) {
			clearRecipeDeque();
			return;
		}

		setRecipe( recipe );
	}


	///////////////
	///// Recipe Deque

	protected RecipeDeque recipeDeque = new RecipeDeque();

	public void pushRecipe(CraftRecipe recipe) {
		recipeDeque.pushRecipe( recipe );
	}

	protected CraftRecipe getPreviousRecipe() {
		return recipeDeque.getPrevious();
	}

	protected CraftRecipe getNextRecipe() {
		return recipeDeque.getNext();
	}

	protected void clearRecipeDeque() {
		recipeDeque.clear();
	}

}
