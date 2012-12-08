package xk.xact.core;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import xk.xact.XActMod;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

import java.util.List;

/**
 * The item used for the encoding of recipes.
 * @author Xhamolk_
 */
public class ItemChip extends Item {

	/*
	Note: The actual encoding happens on the stack's NBT,
	and is performed by CraftManager.encodeRecipe
	 */

	public final boolean encoded;
	
	public ItemChip(int itemID, boolean encoded) {
		super(itemID);
		this.encoded = encoded;
		this.setItemName("recipeChip." + (encoded ? "encoded" : "blank"));
		this.setTextureFile(XActMod.TEXTURE_ITEMS);
		this.setCreativeTab(XActMod.xactTab);
	}

	@Override
	public int getItemStackLimit() {
		// encoded items can't stack, but blank ones do.
		return encoded ? 1 : 16;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		if( itemStack.getItem() instanceof ItemChip){
			if( ((ItemChip)itemStack.getItem()).encoded ){
				CraftRecipe recipe = RecipeUtils.getRecipe(itemStack, player.worldObj);
				if( recipe != null ) {
					ItemStack result = recipe.getResult();

					String itemName = result.getItem().getItemDisplayName(result);
					list.add("\u00a73" + "Recipe: "+ itemName);
				} else {
					list.add("\u00a7c<invalid>");
				}
			} else {
				// blank recipes.
				list.add("\u00a77" + "<blank>");
			}
		}
	}

    @Override
	public int getIconFromDamage(int itemDamage){
		return this.encoded ? 1 : 0;
	}

}
