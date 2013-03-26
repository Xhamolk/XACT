package xk.xact.core.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import xk.xact.XActMod;

/**
 *
 *
 */
public class ItemMachine extends ItemBlock {

	public ItemMachine(int id) {
		super( id );
		this.setHasSubtypes( true );
		this.setCreativeTab( XActMod.xactTab );
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "tile.xact.machine.crafter";
	}

}
