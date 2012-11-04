package xk.xact;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

/**
 *
 *
 */
public class ItemMachine extends ItemBlock {

	public ItemMachine(int id) {
		super(id);
		this.setHasSubtypes(true);
		this.setCreativeTab(XActMod.xactTab);
	}

	@Override
	public String getItemNameIS(ItemStack itemStack) {
		if( itemStack != null ){
			int type = itemStack.getItemDamage();
			if( type == 0 ){ // encoder
				return "tile.xact.machine.encoder";
			}
			if( type == 1 ){ // crafter
				return "tile.xact.machine.crafter";
			}
		}
		return super.getItemNameIS(itemStack);
	}

	@Override
	public int getMetadata(int stackDamage) {
		return stackDamage & 1;
	}
}
