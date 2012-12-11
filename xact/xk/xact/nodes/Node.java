package xk.xact.nodes;

import net.minecraft.src.ItemStack;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.InventoryUtils;

import java.util.ArrayList;


public class Node {

	private ItemStack stack;
	private CraftRecipe recipe;
	protected ArrayList<Integer> childIDs;

	protected Node(ItemStack stack) {
		this.stack = stack;
		this.childIDs = new ArrayList<Integer>();
	}

	/**
	 * The ItemStack represented by this node.
	 */
	public ItemStack getItemStack() {
		return stack;
	}

	/**
	 * Whether if this node represents a raw resource.
	 * Being "Raw" means that there is no recipe associated with it.
	 *
	 * @return true if there is no recipe associated with this, false otherwise.
	 */
	public boolean isRawResource() {
		return recipe == null;
	}

	/**
	 * The CraftRecipe that would craft this node's item.
	 */
	public CraftRecipe getRecipe() {
		return recipe;
	}

	/**
	 * Gets an array of the children nodes' ids.
	 */
	public int[] getChildrenIDs() {
		int[] ids = new int[childIDs.size()];
		for( int i = 0; i < childIDs.size(); i++ ) {
			ids[i] = childIDs.get(i);
		}
		return ids;
	}

	/**
	 * Registers the passed nodeID
	 * @param nodeID the ID of the child node.
	 */
	public void addChildrenNode(int nodeID) {
		childIDs.add( nodeID );
	}

	/**
	 * Sets the recipe that can craft this node's ItemStack.
	 * @param recipe a CraftRecipe that has this node's ItemStack as result.
	 */
	public void setRecipe(CraftRecipe recipe) {
		if( recipe != null && recipe.isValid() ) {
			if( recipeMatchesResult(recipe)  ) {
				this.recipe = recipe;
			}
		}
	}

	/**
	 * Whether if the passed node represents the same ItemStack
	 * @param node the node to compare with
	 * @return true if both nodes represent the same ItemStack.
	 * @see InventoryUtils#similarStacks(net.minecraft.src.ItemStack, net.minecraft.src.ItemStack)
	 */
	public boolean equalsNode(Node node) {
		return node != null && InventoryUtils.similarStacks(this.stack, node.getItemStack());

	}

	private boolean recipeMatchesResult(CraftRecipe recipe) {
		return recipe != null && InventoryUtils.similarStacks(this.stack, recipe.getResult());
	}

}
