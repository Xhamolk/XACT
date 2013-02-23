package xk.xact.project;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import xk.xact.recipes.CraftRecipe;
import xk.xact.inventory.InventoryUtils;
import xk.xact.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


// This stores the recipes
public class IngredientsList implements Iterable<ItemStack> {

	// Contains all the ingredients for the project.
	private ArrayList<ItemStack> ingredients = new ArrayList<ItemStack>();

	// Contains the ingredients with recipes.
	private HashMap<ItemStack, CraftRecipe> recipes = new HashMap<ItemStack, CraftRecipe>();

	private CraftingProject project;

	public IngredientsList(CraftingProject project) {
		this.project = project;
	}


	public void addIngredient(ItemStack ingredient) {
		if( ingredient != null && !containsIngredient( ingredient ) ) {
			ItemStack copy = ingredient.copy();
			copy.stackSize = 1;
			ingredients.add( copy );
		}
	}

	public boolean containsIngredient(ItemStack item) {
		if( item == null )
			return false;
		for( ItemStack current : ingredients ) {
			if( InventoryUtils.similarStacks( current, item, false ) ) {
				return true;
			}
		}
		return false;
	}

	public boolean hasRecipe(ItemStack item) {
		for( ItemStack current : recipes.keySet() ) {
			if( InventoryUtils.similarStacks( current, item, false ) )
				return true;
		}
		return false;
	}

	public CraftRecipe getRecipe(ItemStack item) {
		for( ItemStack current : recipes.keySet() ) {
			if( InventoryUtils.similarStacks( current, item, false ) )
				return recipes.get( current );
		}
		return null;
	}

	public void setRecipe(ItemStack item, CraftRecipe recipe) {
		// is it a valid recipe?

		for( ItemStack c : recipes.keySet() ) {
			if( InventoryUtils.similarStacks( c, item, false ) ) {
				recipes.put( c, recipe );
				return;
			}
		}
		recipes.put( item, recipe );
	}

	public void removeRecipe(ItemStack item) {
		recipes.remove( item );
		refreshIngredients();
	}

	public void refreshIngredients() {
		// this will clear the ingredients list, and repopulate it with the ones that are still required.
		ingredients.clear();

		CraftRecipe mainRecipe = project.getMainRecipe();
		if( mainRecipe != null ) {
			for( ItemStack item : mainRecipe.getSimplifiedIngredients() ) {
				addIngredient( item );
			}
		}

		for( CraftRecipe recipe : recipes.values() ) {
			for( ItemStack item : recipe.getSimplifiedIngredients() ) {
				addIngredient( item );
			}
		}

	}

	public void clear() {
		ingredients.clear();
		recipes.clear();
	}


	///////////////
	///// Iterator

	@Override
	public Iterator<ItemStack> iterator() {
		return ingredients.iterator();
	}

	///////////////
	///// NBT

	public void writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = new NBTTagCompound();

		// Write the ingredients.
		NBTTagList list1 = new NBTTagList();
		for( ItemStack stack : this ) {
			NBTTagCompound tag = new NBTTagCompound();
			Utils.writeItemStackToNBT( nbt, stack, "ingredient" );
			list1.appendTag( tag );
		}
		nbt.setTag( "ingredientList", list1 );

		// Write the recipes
		NBTTagList list2 = new NBTTagList();
		for( ItemStack key : recipes.keySet() ) {
			NBTTagCompound tag = new NBTTagCompound();
			Utils.writeItemStackToNBT( nbt, key, "key" );
			recipes.get( key ).writeToNBT( tag );
			list2.appendTag( tag );
		}
		nbt.setTag( "recipes", list2 );

		compound.setTag( "projectList", nbt );
	}

	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = (NBTTagCompound) compound.getTag( "projectList" );
		if( nbt == null )
			return;

		// Read the ingredients.
		NBTTagList list1 = nbt.getTagList( "ingredientList" );
		for( int i = 0; i < list1.tagCount(); i++ ) {
			NBTTagCompound tag = (NBTTagCompound) list1.tagAt( i );
			ItemStack item = Utils.readStackFromNBT( (NBTTagCompound) tag.getTag( "ingredient" ) );
			if( item != null )
				ingredients.add( item );
		}

		// Read the recipes.
		NBTTagList list2 = nbt.getTagList( "recipes" );
		for( int i = 0; i < list2.tagCount(); i++ ) {
			NBTTagCompound tag = (NBTTagCompound) list2.tagAt( i );
			ItemStack key = Utils.readStackFromNBT( (NBTTagCompound) tag.getTag( "key" ) );
			if( key != null ) {
				CraftRecipe recipe = CraftRecipe.readFromNBT( tag );
				recipes.put( key, recipe );
			}
		}
	}

}