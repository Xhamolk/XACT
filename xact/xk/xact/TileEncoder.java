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


	/**
	 * The simulated crafting grid.
	 */
	public final Inventory craftingGrid = new Inventory(9, "crafting grid") {
		public void onInventoryChanged(){
			updateRecipe();
			updateMode();
		}
	};

	/**
	 * The inventory that stores the circuit placed on the slot. 
	 */
	public final Inventory circuitInv = new Inventory(1, "circuit") {
		public void onInventoryChanged() {
			updateMode();
		}
	};

	
	// remove, as it's not used.
	public void encodeRecipe(CraftRecipe recipe) {
		ItemStack circuit = circuitInv.getStackInSlot(0);
		if( circuit == null )
			return;

		// if the recipe is invalid (therefore null), clear the circuit.
		if( recipe == null ){
			circuitInv.setInventorySlotContents(0, new ItemStack(XActMod.itemRecipeBlank, 1));
			return;
		}

		if( CraftManager.isValid(circuit) ) {
			circuitInv.setInventorySlotContents(0, CraftManager.encodeRecipe(recipe));
		}
	}

	/**
	 * Gets the recipe that matches the current crafting grid.
	 *
	 * @return the CraftRecipe representation, or null if invalid.
	 */
	public CraftRecipe getCurrentRecipe() {
        return CraftManager.generateRecipe(craftingGrid.getContents(), this.worldObj);
	}


	@Override
	public void handleEvent(XactEvent event) {
		if( event instanceof EncodeEvent ) {
			EncodeEvent encodeEvent = (EncodeEvent) event;
			ItemStack circuit = circuitInv.getStackInSlot(0);
			if( circuit == null )
				return; // do nothing.

			if( currentMode == Mode.READY ) {
				CraftRecipe recipe = encodeEvent.recipe;
				if( recipe == null ) {
					currentMode = Mode.CLEAR;
					return; // this shouldn't ever happen.
				}

				if( CraftManager.isValid(circuit) ) {
					circuitInv.setInventorySlotContents(0, CraftManager.encodeRecipe(recipe));

					// forge crafting events.
					ItemStack craftedStack = recipe.getResult();
					GameRegistry.onItemCrafted(encodeEvent.player, craftedStack, craftingGrid);
					craftedStack.onCrafting(encodeEvent.player.worldObj, encodeEvent.player, craftedStack.stackSize);

					// consume items.
					consumeIngredients();

					// update recipe.
					updateRecipe();

					currentMode = Mode.SUCCESS;
					return;
				}
			}

			if( currentMode == Mode.CLEAR ) {
				// clear the recipe.
				ItemStack blankRecipe = new ItemStack(XActMod.itemRecipeBlank, 1);
				circuitInv.setInventorySlotContents(0, blankRecipe);

				currentMode = Mode.NONE;
			}
		}
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


	/**
	 * Consumes the items on the crafting grid.
	 */
	private void consumeIngredients() {
		for( int i=0; i<9; i++ ){
			craftingGrid.decrStackSize(i, 1);
		}
	}


	///////////////
	///// Current State  

	public Mode currentMode = Mode.NONE;
	
	public ItemStack currentRecipe = null;

	public void updateRecipe() {
		CraftRecipe recipe = getCurrentRecipe();
		currentRecipe = ( recipe == null ) ? null : recipe.getResult();
	}

	private void updateMode() {
		ItemStack stack = circuitInv.getStackInSlot(0);
		if( stack == null ) {
			currentMode = Mode.NONE;
			return;
		}
		if( CraftManager.isEncoded( stack )){
			currentMode = Mode.CLEAR;
		} else {
			currentMode = (currentRecipe != null) ? Mode.READY : Mode.NONE;
		}
	}


	public static enum Mode {
		READY, SUCCESS, CLEAR, NONE
	}


	///////////////
	///// NBT

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		craftingGrid.readFromNBT(compound);
		circuitInv.readFromNBT(compound);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		craftingGrid.writeToNBT(compound);
		circuitInv.writeToNBT(compound);
	}

}
