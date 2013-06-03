package xk.xact.client.gui;


import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.client.GuiUtils;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.RecipeDeque;

public abstract class GuiCrafting extends GuiXACT implements InteractiveCraftingGui {

	public GuiCrafting(Container container) {
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
		CraftRecipe recipe;

		if( keyDescription.equals( "xact.clear" ) ) {
			setRecipe( null );

		} else if( keyDescription.equals( "xact.load" ) ) {
			Slot hoveredSlot = GuiUtils.getHoveredSlot( guiLeft, guiTop );

			if( hoveredSlot != null && hoveredSlot.getHasStack() ) {
				ItemStack stackInSlot = hoveredSlot.getStack();
				if( CraftManager.isEncoded( stackInSlot ) ) {
					recipe = RecipeUtils.getRecipe( stackInSlot, GuiUtils.getWorld() );
					if( recipe != null && recipe.isValid() ) {
						setRecipe( recipe );
					}
				}
			}

		} else if( keyDescription.equals( "xact.prev" ) ) {
			recipe = getPreviousRecipe();
			if( recipe != null ) {
				setRecipe( recipe );
			}

		} else if( keyDescription.equals( "xact.next" ) ) {
			recipe = getNextRecipe();
			if( recipe != null ) {
				setRecipe( recipe );
			}

		} else if( keyDescription.equals( "xact.delete" ) ) {
			clearRecipeDeque();
		}

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
