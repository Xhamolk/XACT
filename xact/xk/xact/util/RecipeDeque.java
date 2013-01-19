package xk.xact.util;


import xk.xact.recipes.CraftRecipe;
import java.util.Stack;

public class RecipeDeque {

	private CraftRecipe current = null;

	private Stack<CraftRecipe> previousRecipes = new Stack<CraftRecipe>();
	private Stack<CraftRecipe> nextRecipes = new Stack<CraftRecipe>();

	public void pushRecipe(CraftRecipe recipe) {
		if( recipe == null )
			return;
		System.out.println("push.");
		if( current != null && !current.equals(recipe) ) {
			System.out.println("successfully pushing recipe: " + current);
			previousRecipes.push( current );
		}
		current = recipe;
	}

	public CraftRecipe getPrevious() {
		if( previousRecipes.empty() )
			return null;

		if( current != null ) {
			nextRecipes.push( current );
		}
		System.out.println("prev: "+previousRecipes.size());
		return current = previousRecipes.pop();
	}

	public CraftRecipe getNext() {
		if( nextRecipes.empty() )
			return null;

		if( current != null ) {
			previousRecipes.push( current );
		}
		System.out.println("next: "+nextRecipes.size());
		return current = nextRecipes.pop();
	}

	public void clear() {
		current = null;
		previousRecipes.clear();
		nextRecipes.clear();
	}

}
