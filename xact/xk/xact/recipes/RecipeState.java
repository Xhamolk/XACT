package xk.xact.recipes;

/**
 * The state of each recipe on TileCrafter.
 * @author Xhamolk_
 */
public enum RecipeState { // to be implemented on the next version.
	
	OK, LAST, NO_INGREDIENTS, NOT_SET;
	
	public boolean shouldPaintBackground() {
		return this != NOT_SET;
	}
	
	public int getPaintColor() {
		if( this == OK )
			return 0; // green
		if( this == LAST )
			return 0; // red
		if( this == NO_INGREDIENTS )
			return 0; // yellow
		return -1;
	}

}
