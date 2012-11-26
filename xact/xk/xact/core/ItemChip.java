package xk.xact.core;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import xk.xact.XActMod;
import xk.xact.recipes.CraftManager;

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
		if( itemStack.getItem() instanceof ItemChip){
			if( ((ItemChip)itemStack.getItem()).encoded ){
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

	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if( itemStack.stackSize > 1 ) {
			if( player.worldObj.isRemote)
				player.sendChatToPlayer("Can't encode multiple chips at once.");
			return itemStack;
		}
		player.openGui(XActMod.instance, 2, world, 0, 0, 0);
		return itemStack;
	}


}
