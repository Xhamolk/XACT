package xk.xact.event;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import xk.xact.recipes.CraftRecipe;

public class CraftEvent implements XactEvent {

	public final EntityPlayer player;
	public final CraftRecipe recipe;
	public final ItemStack itemStack;

	public CraftEvent(EntityPlayer player, CraftRecipe recipe, ItemStack itemStack) {
		this.player = player;
		this.recipe = recipe;
		this.itemStack = itemStack;
	}


}
