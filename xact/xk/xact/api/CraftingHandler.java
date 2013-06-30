package xk.xact.api;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.oredict.OreDictionary;
import xk.xact.inventory.InventoryUtils;
import xk.xact.network.CommonProxy;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static xk.xact.util.Utils.copyArray;

// Used to craft, check if can craft, etc.

/**
 * This handles the crafting of the recipes for the Crafter machine and for the Chips.
 */
public abstract class CraftingHandler {


	protected final ICraftingDevice device;

	protected CraftingHandler(ICraftingDevice device) {
		this.device = device;
	}

	/**
	 * Easy way to create a CraftingHandler.
	 *
	 * @param device the ICraftingDevice from which to reference the AvailableInventories
	 */
	public static CraftingHandler createCraftingHandler(final ICraftingDevice device) {
		return new CraftingHandler( device ) {
			@Override
			public List getAvailableInventories() {
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
		for( ItemStack cur : ingredients ) {
			if( cur == null ) continue;

			int found = getCountFor( recipe, cur, false );
			if( found < cur.stackSize )
				return false;
		}
		return true;
	}

	public void doCraft(CraftRecipe recipe, EntityPlayer player, ItemStack craftedItem) {
		InventoryCrafting craftMatrix = generateTemporaryCraftingGridFor( recipe, player, true );
		if( craftMatrix == null )
			return;

		craftedItem.onCrafting( device.getWorld(), player, craftedItem.stackSize );
		GameRegistry.onItemCrafted( player, craftedItem, craftMatrix );

		consumeIngredients( craftMatrix, player );

		// Update the device's state.
		device.updateState();
	}

	/**
	 * Manages the crafting of a recipe.
	 * Fires the crafting event with the ingredients provided.
	 *
	 * @param recipe      the CraftRecipe from which to take ingredients of the recipe.
	 * @param craftMatrix the crafting grid with the ingredients for this recipe.
	 */
	public ItemStack getRecipeResult(CraftRecipe recipe, InventoryCrafting craftMatrix) {
		if( recipe == null )
			return null;

		ItemStack craftedItem = recipe.getResult();

		if( craftMatrix != null ) {
			ItemStack stack = recipe.getRecipePointer().getOutputFrom( craftMatrix );
			if( stack == null )
				stack = CraftingManager.getInstance().findMatchingRecipe( craftMatrix, device.getWorld() );
			if( stack != null )
				craftedItem = stack;
		}

		return craftedItem;
	}

	public void consumeIngredients(InventoryCrafting craftMatrix, EntityPlayer player) {
		// consume the items

		ArrayList<ItemStack> remainingList = new ArrayList<ItemStack>();

		mainLoop:
		for( int i = 0; i < craftMatrix.getSizeInventory(); i++ ) {
			ItemStack stackInSlot = craftMatrix.getStackInSlot( i );
			if( stackInSlot == null )
				continue;

			craftMatrix.decrStackSize( i, 1 );

			if( stackInSlot.getItem().hasContainerItem() ) {
				ItemStack containerStack = stackInSlot.getItem().getContainerItemStack( stackInSlot );

				if( containerStack.isItemStackDamageable() && containerStack.getItemDamage() > containerStack.getMaxDamage() ) {
					MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, containerStack ) );
					device.getWorld().playSoundAtEntity( player, "random.break", 0.8F, 0.8F + player.worldObj.rand.nextFloat() * 0.4F );
					containerStack = null;
				}

				if( containerStack != null ) {
					if( stackInSlot.getItem().doesContainerItemLeaveCraftingGrid( stackInSlot ) ) {
						for( Object inventory : getAvailableInventories() ) {
							IInventoryAdapter adapter = InventoryUtils.getInventoryAdapter( inventory );
							if( adapter != null ) {
								containerStack = adapter.placeItem( containerStack );
								if( containerStack == null )
									continue mainLoop;
							}
						}
					}

					remainingList.add( containerStack );
				}
			}
		}
		craftMatrix.onInventoryChanged();

		// give back the remaining items
		for( ItemStack stack : remainingList ) {
			if( !addToInventories( stack ) )
				player.dropPlayerItem( stack );
		}
		ItemStack[] remainingItems = InventoryUtils.getContents( craftMatrix );
		for( ItemStack stack : remainingItems ) {
			if( !addToInventories( stack ) )
				player.dropPlayerItem( stack );
		}
	}

	/**
	 * Tries to add the ItemStack on to the first available inventory,
	 * and will iterate through them until it's correctly placed.
	 * <p/>
	 * Will return true if the stack was completely merged on the inventories.
	 * <p/>
	 * Note: the stack will be manipulated.
	 *
	 * @param stack the stack to be added to the inventories.
	 * @return true on success. False if the stack couldn't be completely merged to the inventories.
	 * @see CraftingHandler#getAvailableInventories()
	 */
	protected boolean addToInventories(ItemStack stack) {
		if( stack == null )
			return true; // technically, counts as a success.

		for( Object inventory : getAvailableInventories() ) {
			IInventoryAdapter adapter = InventoryUtils.getInventoryAdapter( inventory );
			ItemStack result = adapter.placeItem( stack );
			if( result == null ) {
				stack.stackSize = 0;
				return true;
			} else {
				stack.stackSize = result.stackSize;
			}
		}
		return false;
	}


	/**
	 * Provide all the inventories to be included for taking resources from, and for placing remaining items once crafted.
	 * <p/>
	 * You might want (or not) to include the player's inventory at the end of this list.
	 *
	 * @return a list of inventories.
	 */
	public abstract List getAvailableInventories();


	/**
	 * Gets the amount of items of the same kind of the passed stack on the available inventories.
	 *
	 * @param recipe   the CraftRecipe to check
	 * @param stack    the stack to compare with.
	 * @param countAll whether if this should keep counting after finding enough.
	 *                 Enough implies that the amount found is equal or higher than the amount required.
	 * @return the count of the items found.
	 */
	public int getCountFor(CraftRecipe recipe, ItemStack stack, boolean countAll) {
		int found = 0;
		int ingredientIndex = RecipeUtils.getIngredientIndex( recipe, stack );
		if( ingredientIndex == -1 )
			return 0; // not an ingredient! do something!
		for( Object inventory : getAvailableInventories() ) {
			IInventoryAdapter adapter = InventoryUtils.getInventoryAdapter( inventory );
			for( ItemStack item : adapter ) {
				if( !countAll && found >= stack.stackSize ) {
					return found;
				}
				if( item == null )
					continue;
				if( isItemMatchingIngredient( item, recipe, ingredientIndex ) ) {
					found += item.stackSize;
				}
			}
		}
		return found;
	}

	/**
	 * Will generate a temporary crafting grid based on the recipe's ingredients.
	 * The items that populate the grid will be taken from the available inventories.
	 *
	 * @param recipe the recipe from which to take the ingredients.
	 */
	public InventoryCrafting generateTemporaryCraftingGridFor(CraftRecipe recipe, EntityPlayer player, boolean takeItems) {
		if( !canCraft( recipe, player ) ) {
			Utils.debug( "XACT: generateTemporaryCraftingGridFor: !canCraft" );
			return null;
		}
		boolean creativeMod = player != null && !CommonProxy.isFakePlayer( player ) && player.capabilities.isCreativeMode;
		if( creativeMod )
			return InventoryUtils.simulateCraftingInventory( recipe.getIngredients() );

		ItemStack[] contents = findAndGetRecipeIngredients( recipe, takeItems );
		return InventoryUtils.simulateCraftingInventory( contents );
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
		for( int i = 0; i < ingredients.length; i++ ) {
			ItemStack stack = ingredients[i];
			if( stack == null ) {
				retValue[i] = 0;
				continue;
			}
			int found = getCountFor( recipe, stack, false );
			retValue[i] = (found >= stack.stackSize) ? 0 : stack.stackSize - found;
		}
		return retValue;
	}

	/**
	 * Provides a list of the missing ingredients.
	 *
	 * @param recipe the CraftRecipe representation of the recipe.
	 * @return an array of ItemStack, each of which has as stackSize the amount missing.
	 * @see CraftingHandler#getMissingIngredientsCount(xk.xact.recipes.CraftRecipe)
	 */
	public ItemStack[] getMissingIngredients(CraftRecipe recipe) {
		if( recipe == null )
			return new ItemStack[0];

		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		int[] missingCount = getMissingIngredientsCount( recipe );
		ItemStack[] ingredients = copyArray( recipe.getSimplifiedIngredients() );

		for( int i = 0; i < missingCount.length; i++ ) {
			int missing = missingCount[i];
			if( missing > 0 ) {
				ingredients[i].stackSize = missing;
				list.add( ingredients[i] );
			}
		}

		return list.toArray( new ItemStack[list.size()] );
	}

	/**
	 * A string listing the missing ingredients for the specified recipe.
	 * Useful to tell the user what's missing.
	 */
	public String getMissingIngredientsString(CraftRecipe recipe) {
		if( recipe == null )
			return "<invalid recipe>";

		String retValue = "";
		ItemStack[] ingredients = getMissingIngredients( recipe );

		for( int i = 0; i < ingredients.length; i++ ) {
			if( i > 0 )
				retValue += ", ";
			retValue += Utils.stackDescription( ingredients[i] );
		}
		return retValue.equals( "" ) ? "none." : retValue;
	}

	public boolean[] getMissingIngredientsArray(CraftRecipe recipe) {
		boolean[] missingArray = new boolean[9];
		if( recipe == null ) {
			return missingArray;
		}

		ItemStack[] ingredients = recipe.getIngredients();
		ItemStack[] missingIngredients = getMissingIngredients( recipe );

		for( ItemStack currentMissed : missingIngredients ) {
			if( currentMissed == null )
				continue;

			int remaining = currentMissed.stackSize;
			for( int i = 0; remaining > 0 && i < ingredients.length; i++ ) {
				if( ingredients[i] != null && ingredients[i].isItemEqual( currentMissed )
						&& ItemStack.areItemStackTagsEqual( ingredients[i], currentMissed ) ) {
					remaining--;
					missingArray[i] = true;
				}

			}
		}

		return missingArray;
	}

	protected ItemStack[] findAndGetRecipeIngredients(CraftRecipe recipe, boolean doRemove) {
		Map<Integer, int[]> gridIndexes = recipe.getGridIndexes();
		ItemStack[] simplifiedIngredients = recipe.getSimplifiedIngredients();
		ItemStack[] contents = new ItemStack[9];

		ingredient:
		for( int i = 0; i < simplifiedIngredients.length; i++ ) {
			int[] indexes = gridIndexes.get( i );
			int required = simplifiedIngredients[i].stackSize;

			for( Object inventory : getAvailableInventories() ) {
				IInventoryAdapter adapter = InventoryUtils.getInventoryAdapter( inventory );
				for( ItemStack item : adapter ) {
					if( required <= 0 ) {
						continue ingredient; // next ingredient
					}
					if( item == null || item.stackSize <= 0 ) {
						continue; // next inventory item
					}

					int available = item.stackSize;
					for( int index : indexes ) {
						if( required <= 0 || available <= 0 )
							break;

						if( contents[index] != null || !isItemMatchingIngredient( item, recipe, index )) {
							continue; // try on next grid slot.
						}

						if( doRemove ) {
							contents[index] = adapter.takeItem( item, 1 );
						} else {
							contents[index] = item.copy();
							contents[index].stackSize = 1;
						}
						required--;
						available -= contents[index] == null ? 0 : contents[index].stackSize;
					}
				}
			}
		}
		return contents;
	}

	protected boolean isItemMatchingIngredient(ItemStack item, CraftRecipe recipe, int ingredientIndex) {
		if( item == null )
			return false;
		ItemStack ingredient = recipe.getIngredients()[ingredientIndex];

		if( InventoryUtils.similarStacks( item, ingredient, true ) )
			return true;

		// Ore dictionary?
		if( recipe.isOreRecipe() ) {
			int oreID = OreDictionary.getOreID( ingredient );

			if( oreID != -1 ) {
				ArrayList<ItemStack> equivalencies = OreDictionary.getOres( oreID );

				for( ItemStack current : equivalencies ) {
					if( InventoryUtils.similarStacks( item, current, true ) ) // do I need this initial check?
						return true;
					if( item.itemID == current.itemID ) {
						if( current.getItemDamage() == -1 || item.getItemDamage() == current.getItemDamage() )
							return true;
					}
				}
			}
		}

		// regular test: if replacing the item on that spot still matches with the recipe.
		return recipe.matchesIngredient( ingredientIndex, item, device.getWorld() );
	}

}
