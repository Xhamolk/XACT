package xk.xact.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import xk.xact.recipes.CraftRecipe;

/**
 * Used by the CraftingHandler.
 */
public interface ICraftingDevice { // todo: javadoc.

	/**
	 * Provide all the inventories to be included for taking resources from, and for placing remaining items once crafted.
	 * <p/>
	 * You might want (or not) to include the player's inventory at the end of this list.
	 *
	 * @return an array of IInventory objects.
	 * @see CraftingHandler#createCraftingHandler(ICraftingDevice)
	 */
	public abstract IInventory[] getAvailableInventories();


	public abstract int getRecipeCount();

	public abstract CraftRecipe getRecipe(int index);

	public abstract CraftingHandler getHandler();

	public abstract World getWorld();

}
