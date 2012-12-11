package xk.xact.nodes;

import net.minecraft.src.ItemStack;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.StackList;
import xk.xact.util.StackReference;

import java.util.HashMap;


public class NodeTree {


	private Node rootNode;

	private NodeList list;

	protected NodeTree(Node rootNode) {
		this.rootNode = rootNode;
		this.list = new NodeList();
		list.addNode(rootNode);
	}


	/**
	 * Get the root node.
	 */
	public Node getRoot() {
		return rootNode;
	}

	/**
	 * Get the node by it's ID.
	 * @param nodeID the ID of the node.
	 */
	public Node getNode(int nodeID) {
		return getNodeList().getNode( nodeID );
	}

	/**
	 * Get the ID of the node that represents the passed ItemStack
	 * @param stack the ItemStack
	 */
	public int getNodeID(ItemStack stack) {
		return getNodeList().getNodeID(stack);
	}

	/**
	 * Get the NodeList object that stores all the nodes for this NodeTree.
	 */
	public NodeList getNodeList() {
		return list;
	}

	/**
	 * Assign a recipe to a node.
	 * This implies that item will no longer be considered as "raw" ingredient.
	 *
	 * @param nodeID the ID of the node.
	 * @param recipe the CraftRecipe that would craft that item.
	 * @see xk.xact.nodes.Node#isRawResource()
	 */
	public void registerRecipe(int nodeID, CraftRecipe recipe) {
		if( recipe == null || nodeID == -1 )
			return;

		// Associate the nodeID with the recipe.
		Node parent = getNodeList().getNode(nodeID);
		if( parent == null )
			return;

		parent.setRecipe( recipe );

		// Register the recipe's ingredients
		ItemStack[] ingredients = recipe.getSimplifiedIngredients();
		for( ItemStack ingredient : ingredients ) {

			Node child = getNodeList().createItemNode( parent, ingredient );
			getNodeList().addNode(child);
		}
	}

	/**
	 * Get a map containing all the "raw" materials to craft the root item.
	 * The key is the ItemStack and the value is the amount required of that item.
	 */
	public HashMap<ItemStack,Integer> getRawMaterials() {
		StackList list = new StackList();
		addRawMaterials(getRoot(), list);

		HashMap<ItemStack,Integer> materials = new HashMap<ItemStack,Integer>();
		for( StackReference current : list ) {
			materials.put( current.stack, current.amount );
		}

		return materials;
	}

	private void addRawMaterials(Node node, StackList list) {
		if( node.isRawResource() ) {
			list.addStack( node.getItemStack() );
			return;
		}

		int[] childrenIDs = node.getChildrenIDs();
		for( int childID : childrenIDs ) {
			Node child = getNode( childID );
			if( child != null )
				addRawMaterials( child, list );
		}
	}

	// the "Crafting Project" map only needs to show the recipes' ingredients.
		// note: the repeated ingredients will be painted as many times as needed, but they all point to the same node.

	// the "Required Ingredients" information will be displayed elsewhere.


}
