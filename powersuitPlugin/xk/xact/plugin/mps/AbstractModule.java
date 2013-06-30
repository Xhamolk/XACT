package xk.xact.plugin.mps;

import net.machinemuse.api.IModularItem;
import net.machinemuse.api.IPowerModule;
import net.machinemuse.api.IPropertyModifier;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local replacement (pretty much a copy) of PowerModuleBase.
 *
 * @author Xhamolk_
 */
public abstract class AbstractModule implements IPowerModule {

	private static String STRING_ONLINE = "Active"; // MuseItemUtils.ONLINE

	protected List<ItemStack> installCost;
	protected List<IModularItem> validItems;
	protected Map<String, List<IPropertyModifier>> propertyModifiers;

	protected NBTTagCompound defaultTag;

	protected Icon icon;


	public AbstractModule(String name, List<IModularItem> validItems) {
		this.validItems = validItems;
		this.installCost = new ArrayList<ItemStack>();
		this.propertyModifiers = new HashMap<String, List<IPropertyModifier>>();
		this.defaultTag = new NBTTagCompound();
		this.defaultTag.setBoolean( STRING_ONLINE, true );
	}

	public AbstractModule(List<IModularItem> validItems) {
		this.validItems = validItems;
		this.installCost = new ArrayList<ItemStack>();
		this.propertyModifiers = new HashMap<String, List<IPropertyModifier>>();
		this.defaultTag = new NBTTagCompound();
		this.defaultTag.setBoolean( STRING_ONLINE, true );
	}

	@Override
	public Icon getIcon(ItemStack item) {
		return icon;
	}

	@Override
	public abstract void registerIcon(IconRegister register);

	@Override
	public List<ItemStack> getInstallCost() {
		return installCost;
	}

	@Override
	public boolean isValidForItem(ItemStack stack, EntityPlayer player) {
		Item item = stack.getItem();
		return item instanceof IModularItem && this.validItems.contains( item );
	}

	@Override
	public Map<String, List<IPropertyModifier>> getPropertyModifiers() {
		return propertyModifiers;
	}

	@Override
	public double applyPropertyModifiers(NBTTagCompound itemTag, String propertyName, double propertyValue) {
		Iterable<IPropertyModifier> propertyModifiersIterable = propertyModifiers.get( propertyName );
		if( propertyModifiersIterable != null && itemTag.hasKey( this.getDataName() ) ) {
			NBTTagCompound moduleTag = itemTag.getCompoundTag( this.getDataName() );
			for( IPropertyModifier modifier : propertyModifiersIterable ) {
				propertyValue = modifier.applyModifier( moduleTag, propertyValue );
			}
		}
		return propertyValue;
	}

	@Override
	public NBTTagCompound getNewTag() {
		return (NBTTagCompound) defaultTag.copy();
	}

	@Override
	public boolean isAllowed() {
		return true; // I might not need to deal with this at all.
	}

	@Override
	public String getStitchedTexture(ItemStack item) {
		return "/gui/items.png";
	}

}
