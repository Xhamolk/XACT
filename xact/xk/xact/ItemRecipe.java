package xk.xact;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import xk.xact.recipes.CraftManager;

import java.util.List;

/**
 * The item used for the encoding of recipes.
 * @author Xhamolk_
 */
public class ItemRecipe extends Item {

	/*
	Note: The actual encoding happens on the stack's NBT,
	and is performed by CraftManager.encodeRecipe
	 */

	public final boolean encoded;
	
	public ItemRecipe(int itemID, boolean encoded) {
		super(itemID);
		this.encoded = encoded;
		this.setItemName("recipeChip." + (encoded ? "encoded" : "blank"));
		this.setIconIndex(encoded ? 18 : 17);
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
		if( itemStack.getItem() instanceof ItemRecipe ){
			if( ((ItemRecipe)itemStack.getItem()).encoded ){
				if( isEncoded(itemStack) ) {
					ItemStack result = CraftManager.decodeRecipe(itemStack).getResult();

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

	private static boolean isEncoded(ItemStack stack) {
		return CraftManager.decodeRecipe(stack) != null;
	}


}
