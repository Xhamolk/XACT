package xk.xact;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import xk.xact.event.EncodeEvent;
import xk.xact.event.XactEvent;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.Inventory;

import java.util.ArrayList;

/**
 * Encodes a recipe into a circuit.
 * @author Xhamolk_
 */
public class TileEncoder extends TileMachine {

	/*
	GUI-wise
    	Contains a crafting grid, an output slot(ghost), and the encoding slot.
    	Also has a button that triggers the operation.

    Functionality:
    	Button:
    		when pressed, the encode event is triggered.
    		Based on the currentState conditions, the actions will be performed.

		Encode recipes:
			When the recipe is valid, encode it on the item.

	 */

	public static final byte MODE_ENCODE = 1;
	public static final byte MODE_CLEAR = 2;

	public byte mode = MODE_ENCODE;

	/**
	 * The simulated crafting grid.
	 */
	public final Inventory craftingGrid = new Inventory(9, "crafting grid") {
		public void onInventoryChanged(){
			super.onInventoryChanged();
			updateRecipe();
		}
	};

	/**
	 * The inventory that stores the circuit placed on the slot. 
	 */
	public final Inventory circuitInv = new Inventory(1, "circuit");

	/**
	 * The inventory that contains the crafting recipe's output.
	 */
	public final Inventory outputInv = new Inventory(1, "craft output");


	/**
	 * Gets the recipe that matches the current crafting grid.
	 *
	 * @return the CraftRecipe representation, or null if invalid.
	 */
	public CraftRecipe getCurrentRecipe() {
        return CraftManager.generateRecipe(craftingGrid.getContents(), this.worldObj);
	}

	@Override
	public ArrayList<ItemStack> getDropItems() {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack stack;
		for(int i=0; i<9; i++){
			stack = craftingGrid.getStackInSlotOnClosing(i);
			if( stack != null )
				list.add( stack );
		}
		stack = circuitInv.getStackInSlotOnClosing(0);
		if( stack != null )
			list.add(stack);
		return list;
	}


	///////////////
	///// Current State  

	public void updateRecipe() {
		CraftRecipe recipe = getCurrentRecipe();
		outputInv.setInventorySlotContents(0, recipe != null ? recipe.getResult() : null );
	}


	///////////////
	///// NBT

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		craftingGrid.readFromNBT(compound);
		circuitInv.readFromNBT(compound);
		outputInv.readFromNBT(compound);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		craftingGrid.writeToNBT(compound);
		circuitInv.writeToNBT(compound);
		outputInv.writeToNBT(compound);
	}

}
