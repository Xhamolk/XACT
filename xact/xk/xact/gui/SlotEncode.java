package xk.xact.gui;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import xk.xact.core.TileEncoder;
import xk.xact.XActMod;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;

public class SlotEncode extends Slot {

	private TileEncoder encoder;

	public SlotEncode(TileEncoder encoder, int x, int y) {
		super(encoder.circuitInv, 0, x, y);
		this.encoder = encoder;
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return CraftManager.isValid( itemStack );
	}

	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {
		if( itemStack == null )
			return;

		if( encoder.mode == TileEncoder.MODE_CLEAR ) {
			player.inventory.setItemStack( new ItemStack(XActMod.itemRecipeBlank) );
		}

		if( encoder.mode == TileEncoder.MODE_ENCODE ) {
			CraftRecipe recipe = encoder.getCurrentRecipe();
			if( recipe != null ) {
				// System.out.println("picked: "+recipe + " : " + recipe.ingredientsToString());
				player.inventory.setItemStack( CraftManager.encodeRecipe(recipe) );
			} else {
				player.inventory.setItemStack( new ItemStack(XActMod.itemRecipeBlank) );
			}
		}

	}

}
