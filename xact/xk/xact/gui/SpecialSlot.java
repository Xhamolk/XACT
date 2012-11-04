package xk.xact.gui;

/**
 * Identifies slots that operate on a different way.
 * @author Xhamolk_
 */
public interface SpecialSlot {

	/**
	 * Whether if the item contained on the slot can be picked by the player.
	 * @return if it can be picked.
	 */
	public boolean allowPickUp();
	
}
