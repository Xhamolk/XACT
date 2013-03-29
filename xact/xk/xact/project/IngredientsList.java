package xk.xact.project;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.ItemsList;
import xk.xact.util.ItemsMap;
import xk.xact.util.ItemsReference;
import xk.xact.util.Utils;

import java.util.Iterator;


// This stores the recipes
public class IngredientsList implements Iterable<ItemStack> {

	// Contains all the ingredients for the project.
	private ItemsList ingredients = new ItemsList();

	// Contains the ingredients with recipes.
	private ItemsMap<CraftRecipe> recipes = new ItemsMap<CraftRecipe>();

	private CraftingProject project;

	public IngredientsList(CraftingProject project) {
		this.project = project;
	}


	public void addIngredient(ItemStack ingredient) {
		if( ingredient != null && !containsIngredient( ingredient ) ) {
			ingredients.addStack( ingredient, 1 );
		}
	}

	public boolean containsIngredient(ItemStack item) {
		return ingredients.contains(item);
	}

	public boolean hasRecipe(ItemStack item) {
		return recipes.containsKey( item );
	}

	public CraftRecipe getRecipe(ItemStack item) {
		return recipes.get( item );
	}

	public void setRecipe(ItemStack item, CraftRecipe recipe) {
		// is it a valid recipe?
		if( recipe == null || !recipe.isValid() )
			return;
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
		return ingredients.itemsIterator();
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
		for( ItemsReference key : recipes.keySet() ) {
			NBTTagCompound tag = new NBTTagCompound();
			Utils.writeItemStackToNBT( nbt, key.toItemStack(), "key" );
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
				ingredients.addStack( item );
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