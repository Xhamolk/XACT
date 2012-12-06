package xk.xact.api;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.src.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.oredict.OreDictionary;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.FakeCraftingInventory;
import xk.xact.util.InvSlot;
import xk.xact.util.InventoryUtils;

import java.util.ArrayList;

import static xk.xact.util.InventoryUtils.inventoryIterator;

// Used to craft, check if can craft, etc.

/**
 * This handles the crafting of the recipes for the Crafter machine and for the Chips.
 *
 */
public abstract class CraftingHandler {


	protected final ICraftingDevice device;

	protected CraftingHandler(ICraftingDevice device) {
		this.device = device;
	}

	/**
	 * Easy way to create a CraftingHandler.
	 * @param device the ICraftingDevice from which to reference the AvailableInventories
	 */
	public static CraftingHandler createCraftingHandler(final ICraftingDevice device){
		return new CraftingHandler(device) {
			@Override
			public IInventory[] getAvailableInventories() {
				return device.getAvailableInventories();
			}
		};
	}


	/**
	 * Whether if the specified recipe can be crafted.
	 * In other words, if there are enough items to craft it.
	 *
	 * @param recipe the recipe to check.
	 * @return true if contains all the required ingredients.
	 */
	public boolean canCraft(CraftRecipe recipe, EntityPlayer player) {
		if( recipe == null )
			return false;

		if( player != null && player.capabilities.isCreativeMode )
			return true;

		ItemStack[] ingredients = recipe.getSimplifiedIngredients();
		for(ItemStack cur : ingredients) {
			if( cur == null ) continue;

			int found = getCountFor(recipe, cur, false);
			if( found < cur.stackSize )
				return false;
		}
		return true;
	}

	/**
	 * Manages the crafting of a recipe.
	 * Finds the required ingredients, fires the crafting event,
	 * consumes the ingredients, and returns the remaining items to the inventories.
	 *
	 * @param recipe the CraftRecipe from which to take ingredients of the recipe.
	 * @param player the player that does the crafting.
	 * @param pulledStack the item pulled from the crafting slot.
	 */
	public void doCraft(CraftRecipe recipe, EntityPlayer player, ItemStack pulledStack) {
		if( recipe == null )
			return;

		///////////////
		/// crafting event

		FakeCraftingInventory craftMatrix;
		if( player.capabilities.isCreativeMode )
			craftMatrix = FakeCraftingInventory.emulateContents(recipe.getIngredients());
		else
			craftMatrix = generateTemporaryCraftingGridFor(recipe, player);

		if( craftMatrix == null ) {
			player.sendChatToPlayer("Can't craft: "+recipe+". Missing: "+getMissingIngredientsString(recipe));
			return; // how could this possibly happen?
		}

		pulledStack.onCrafting(player.worldObj, player, pulledStack.stackSize);
		GameRegistry.onItemCrafted(player, pulledStack, craftMatrix);

		if( player.capabilities.isCreativeMode )
			return;

		//////////
		/// consume the items

		ArrayList<ItemStack> remainingList = new ArrayList<ItemStack>();

		for( int i=0; i<craftMatrix.getSizeInventory(); i++) {
			ItemStack stackInSlot = craftMatrix.getStackInSlot(i);
			if( stackInSlot == null )
				continue;

			craftMatrix.decrStackSize(i, 1);

			if (stackInSlot.getItem().hasContainerItem()) {
				ItemStack containerStack = stackInSlot.getItem().getContainerItemStack(stackInSlot);

				if (containerStack.isItemStackDamageable()
						&& containerStack.getItemDamage() > containerStack.getMaxDamage()) {
					MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, containerStack));
					containerStack = null;
				}

				if (containerStack != null) {
					if( stackInSlot.getItem().doesContainerItemLeaveCraftingGrid(stackInSlot) )
						if( player.inventory.addItemStackToInventory(containerStack) )
							continue;

					remainingList.add(containerStack);
				}
			}
		}
		craftMatrix.onInventoryChanged();


		//////////
		/// give back the remaining items

		for( ItemStack stack : remainingList ){
			if(!addToInventories(stack) )
				player.dropPlayerItem(stack);
		}
		ItemStack[] remainingItems = craftMatrix.getContents();
		for( ItemStack stack : remainingItems ){
			if( !addToInventories(stack) )
				player.dropPlayerItem(stack);
		}

	}

	/**
	 * Tries to add the ItemStack on to the first available inventory,
	 * and will iterate through them until it's correctly placed.
	 *
	 * Will return true if the stack was completely merged on the inventories.
	 *
	 * Note: the stack will be manipulated.
	 *
	 * @param stack the stack to be added to the inventories.
	 * @see CraftingHandler#getAvailableInventories()
	 * @return true on success. False if the stack couldn't be completely merged to the inventories.
	 */
	protected boolean addToInventories(ItemStack stack) {
		if( stack == null )
			return true; // technically, counts as a success.

		for(IInventory inv : getAvailableInventories()) {
			// Merge with existing stacks.
			ItemStack remaining = InventoryUtils.addStackToInventory(stack, inv, true);
			if( remaining == null ) {
				stack.stackSize = 0;
				return true;
			}

			// Add to the first empty slot available.
			remaining = InventoryUtils.addStackToInventory(remaining, inv, false);
			if( remaining == null ) {
				stack.stackSize = 0;
				return true;
			}

			stack.stackSize = remaining.stackSize;
		}

		return false;
	}


	/**
	 * Provide all the inventories to be included for taking resources from, and for placing remaining items once crafted.
	 *
	 * You might want (or not) to include the player's inventory at the end of this list.
	 *
	 * @return an array of IInventory objects.
	 */
	public abstract IInventory[] getAvailableInventories();


	/**
	 * Gets the amount of items of the same kind of the passed stack on the available inventories.
	 *
	 * @param recipe the CraftRecipe to check
	 * @param stack the stack to compare with.
	 * @param countAll whether if this should keep counting after finding enough.
	 *               Enough implies that the amount found is equal or higher than the amount required.
	 * @return the count of the items found.
	 */
	public int getCountFor(CraftRecipe recipe, ItemStack stack, boolean countAll) {
		int found = 0;
		IInventory[] inventories = getAvailableInventories();
		for( IInventory inv : inventories ) {
            for(InvSlot slot : inventoryIterator(inv)){
                if( !countAll && found >= stack.stackSize )
                    break; // prevent counting on more if found enough already.

                if( slot != null && !slot.isEmpty() && slotContainsIngredient(slot, recipe, stack) )
                    found += slot.stack.stackSize;
            }
		}
		return found;
	}

	/**
	 * Will generate a temporary crafting grid based on the recipe's ingredients.
	 * The items that populate the grid will be taken from the available inventories.
	 *
	 * @param recipe the recipe from which to take the ingredients.
	 * @return A FakeCraftingInventory
	 */
	public FakeCraftingInventory generateTemporaryCraftingGridFor(CraftRecipe recipe, EntityPlayer player) {
        if( !canCraft(recipe, player) ) {
            System.err.println("XACT: generateTemporaryCraftingGridFor: !canCraft");
            return null;
        }
        ItemStack[] contents = findAndGetRecipeIngredients(recipe);
        return FakeCraftingInventory.emulateContents(contents);
	}

	/**
	 * Gets the amount of missing ingredients.
	 * The array contains the amount (associated by index with the ingredient it represents) missing of that item.
	 *
	 * @param recipe the CraftRecipe representation of the recipe.
	 * @return an array of int. any value of 0 represents there's no items left.
	 * @see xk.xact.recipes.CraftRecipe#getSimplifiedIngredients()
	 */
	public int[] getMissingIngredientsCount(CraftRecipe recipe) { // Example: Missing 3 redstone, 2 cobblestone.
		ItemStack[] ingredients = recipe.getSimplifiedIngredients();
		int[] retValue = new int[ingredients.length];
		for( int i=0; i<ingredients.length; i++) {
			ItemStack stack = ingredients[i];
			if( stack == null ){
				retValue[i] = 0;
				continue;
			}
			int found = getCountFor(recipe, stack, false);
			retValue[i] = (found >= stack.stackSize) ? 0 : stack.stackSize - found;
		}
		return retValue;
	}

	/**
	 * Provides a list of the missing ingredients.
	 * @param recipe the CraftRecipe representation of the recipe.
	 * @return an array of ItemStack, each of which has as stackSize the amount missing.
	 * @see CraftingHandler#getMissingIngredientsCount(xk.xact.recipes.CraftRecipe)
	 */
	public ItemStack[] getMissingIngredients(CraftRecipe recipe) {
		if( recipe == null )
			return new ItemStack[0];

		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		int[] missingCount = getMissingIngredientsCount(recipe);
		ItemStack[] ingredients = recipe.getSimplifiedIngredients().clone();

		for( int i=0; i<missingCount.length; i++ ){
			int missing = missingCount[i];
			if( missing > 0 ){
				ingredients[i].stackSize = missing;
				list.add(ingredients[i]);
			}
		}

		return list.toArray(new ItemStack[list.size()]);
	}

	/**
	 * A string listing the missing ingredients for the specified recipe.
	 * Useful to tell the user what's missing.
	 */
	public String getMissingIngredientsString(CraftRecipe recipe) {
		if( recipe == null )
			return "<invalid recipe>";

		String retValue = "";
		ItemStack[] ingredients = getMissingIngredients(recipe);

		for( int i=0; i<ingredients.length; i++ ) {
			if( i > 0 )
				retValue += ", ";
			retValue += InventoryUtils.stackDescription(ingredients[i]);
		}
		return retValue.equals("") ? "none." : retValue;
	}

    protected ItemStack[] findAndGetRecipeIngredients(CraftRecipe recipe) {
        ItemStack[] ingredients = recipe.getIngredients();
        ItemStack[] contents = new ItemStack[recipe.size]; // the return value.

        items: for( int i=0; i<ingredients.length; i++) {
            ItemStack ingredient = ingredients[i];
            if( ingredient == null ) {
                continue;
            }

            int required = ingredient.stackSize;

            // iterate through every slot on every available inventory.
            for( IInventory inv : getAvailableInventories() ) {
                for( InvSlot slot : inventoryIterator(inv) ){
                    if( required <= 0 ) continue items;
                    if( slot == null )
                        continue;

                    if( slotContainsIngredient(slot, recipe, ingredient) ) {
                        ItemStack stackInSlot = inv.getStackInSlot(slot.slotIndex);

                        if( stackInSlot.stackSize > required ){
                            contents[i] = inv.decrStackSize( slot.slotIndex, required );
                            inv.onInventoryChanged();
                            continue items;
                        } else {
                            if( contents[i] == null ){
                                contents[i] = stackInSlot;
                            } else {
                                contents[i].stackSize += stackInSlot.stackSize;
                            }
                            required -= stackInSlot.stackSize;
                            inv.setInventorySlotContents(slot.slotIndex, null);
                            inv.onInventoryChanged();
                        }
                    }
                }
            }
            // should find the all items, since canCraft was true.
        }
        return contents;
    }

    protected boolean slotContainsIngredient(InvSlot slot, CraftRecipe recipe, ItemStack ingredient) {
		if( slot == null || slot.isEmpty() )
			return false;

		// the direct comparison.
		if( slot.containsItemsFrom(ingredient) )
			return true;

		// Ore dictionary?
		if( recipe.isOreRecipe() ){
			int oreID = OreDictionary.getOreID(ingredient);

			if( oreID != -1 ) {
				ArrayList<ItemStack> equivalencies = OreDictionary.getOres(oreID);

				for(ItemStack current : equivalencies) {
//					if( slot.containsItemsFrom(current) ) // do I need this initial check?
//						return true;
					if( slot.stack.itemID == current.itemID ) {
						if( current.getItemDamage() == -1 || slot.stack.getItemDamage() == current.getItemDamage() )
							return true;
					}
				}
			}
		}

		// regular test: if replacing the item with that
		return recipe.matchesIngredient(ingredient, slot.stack, device.getWorld());
    }

}
