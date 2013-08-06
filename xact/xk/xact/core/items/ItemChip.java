package xk.xact.core.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import xk.xact.XActMod;
import xk.xact.util.Textures;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

import java.util.List;

/**
 * The item used for the encoding of recipes.
 *
 * @author Xhamolk_
 */
public class ItemChip extends Item {

	/*
	Note: The actual encoding happens on the stack's NBT,
	and is performed by CraftManager.encodeRecipe
	 */

	public final boolean encoded;

	public ItemChip(int itemID, boolean encoded) {
		super( itemID );
		this.encoded = encoded;
		this.setUnlocalizedName( "recipeChip." + (encoded ? "encoded" : "blank") );
		this.setCreativeTab( XActMod.xactTab );
		if( encoded )
			invalidChip = new ItemStack( this, 1, 1 );
	}

	@Override
	public int getItemStackLimit() {
		// encoded items can't stack, but blank ones do.
		return encoded ? 1 : 16;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		if( itemStack.getItem() instanceof ItemChip ) {
			if( ((ItemChip) itemStack.getItem()).encoded ) {
				CraftRecipe recipe = RecipeUtils.getRecipe( itemStack, player.worldObj );
				if( recipe != null ) {
					ItemStack result = recipe.getResult();

					String itemName = result.getItem().getItemDisplayName( result );
					list.add( "\u00a73" + "Recipe: " + itemName );
				} else {
					list.add( "\u00a7c<invalid>" );
				}
			} else {
				// blank recipes.
				list.add( "\u00a77" + "<blank>" );
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT) // Item Texture
	public void registerIcons(IconRegister iconRegister) {
		if( encoded )
			this.itemIcon = iconRegister.registerIcon( Textures.ITEM_CHIP_ENCODED );
		else
			this.itemIcon = iconRegister.registerIcon( Textures.ITEM_CHIP_BLANK );

		if( invalidChipIcon == null )
			invalidChipIcon = iconRegister.registerIcon( Textures.ITEM_CHIP_INVALID );
	}

	@SideOnly(Side.CLIENT)
	private static Icon invalidChipIcon;

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		if( damage == 1 )
			return invalidChipIcon;
		return super.getIconFromDamage( damage );
	}

	public static ItemStack invalidChip;

}
