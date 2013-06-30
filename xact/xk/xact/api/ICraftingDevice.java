package xk.xact.api;

import net.minecraft.world.World;
import xk.xact.recipes.CraftRecipe;

import java.util.List;

/**
 * Used by the CraftingHandler.
 */
public interface ICraftingDevice { // todo: javadoc.

	/**
	 * Provide all the inventories to be included for taking resources from, and for placing remaining items once crafted.
	 * <p/>
	 * You might want (or not) to include the player's inventory at the end of this list.
	 *
	 * @return a list of inventories.
	 * @see CraftingHandler#createCraftingHandler(ICraftingDevice)
	 */
	public abstract List getAvailableInventories();


	public abstract int getRecipeCount();

	public abstract CraftRecipe getRecipe(int index);

	public abstract boolean canCraft(int index);

	public abstract CraftingHandler getHandler();

	public abstract World getWorld();

	/**
	 * This is called after crafting anything, so the device can update it's state.
	 */
	public abstract void updateState();

}
