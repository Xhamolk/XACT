package xk.xact.event;


import net.minecraft.src.EntityPlayer;
import xk.xact.recipes.CraftRecipe;

public class EncodeEvent implements XactEvent {

	public final EntityPlayer player;
	public final CraftRecipe recipe;

	public EncodeEvent(EntityPlayer player, CraftRecipe recipe) {
		this.player = player;
		this.recipe = recipe;
	}

}
