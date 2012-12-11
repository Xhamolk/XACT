package xk.xact.nodes;


import net.minecraft.src.ItemStack;
import xk.xact.util.InventoryUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class NodeList {

	private ArrayList<Node> nodeList;
	private HashMap<Integer,Integer> ingredientCount;


	public NodeList() {
		this.nodeList = new ArrayList<Node>();
		this.ingredientCount = new HashMap<Integer,Integer>();
	}

	public Node getNode(int nodeID) {
		if( nodeID < getListSize() ) {
			return nodeList.get( nodeID );
		}
		return null;
	}


	public int getNodeID(Node node) {
		for( int i = 0; i < getListSize(); i++ ) {
			if( nodeList.get(i).equalsNode(node) )
				return i;
		}
		return -1;
	}

	public int getNodeID(ItemStack stack) {
		if( stack == null )
			return -1;

		for( int i = 0; i < getListSize(); i++ ) {
			ItemStack nodeStack = nodeList.get(i).getItemStack();
			if( InventoryUtils.similarStacks(stack, nodeStack) )
				return i;
		}
		return -1;
	}

	public int addNode(Node node) {
		int nodeID = getNodeID(node);
		if( nodeID != -1 ) { // node is already listed.
			int previous = ingredientCount.get(nodeID);
			ingredientCount.put(nodeID, previous + node.getItemStack().stackSize);
			return nodeID;
		}

		nodeID = getListSize();
		nodeList.add( node );
		ingredientCount.put(nodeID, node.getItemStack().stackSize);
		return nodeID;
	}

	public boolean containsNode(Node node){
		return node != null && getNodeID( node ) != -1;
	}

	public int getListSize() {
		return nodeList.size();
	}

	public int getIngredientsCount(int nodeID) {
		if( nodeID < getListSize() ) {
			return ingredientCount.get( nodeID );
		}
		return -1;
	}


	public Node createItemNode( Node parent, ItemStack stack ) {
		Node node = new Node(stack);
		int nodeID = addNode( node );
		parent.addChildrenNode( nodeID );
		return null;
	}

}
