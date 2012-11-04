package xk.xact.util;


import net.minecraft.src.ItemStack;

public class MissingIngredientsException extends RuntimeException {
	
	public final int amount;
	public final ItemStack ingredient;

	public MissingIngredientsException(int amount, ItemStack ingredient){
		this.amount = amount;
		this.ingredient = ingredient;
	}
	
}
