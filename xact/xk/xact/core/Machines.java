package xk.xact.core;


import net.minecraft.item.ItemStack;
import xk.xact.core.items.ItemMachine;
import xk.xact.util.Textures;

public enum Machines {

	CRAFTER( "crafter", "XACT Crafter" ) {
		@Override
		public String[] getTextureFiles() {
			return new String[] {
					Textures.CRAFTER_BOTTOM, Textures.CRAFTER_TOP, Textures.CRAFTER_FRONT, Textures.CRAFTER_SIDE
			};
		}
	};


	private final String machineName;
	private final String localizedName;

	private Machines(String name, String localizedName) {
		this.machineName = name;
		this.localizedName = localizedName;
	}

	public String getLocalizedName() {
		return localizedName; // temp - until i set up localizations.
	}

	public static int getMachineFromMetadata(int metadata) {
		metadata = (metadata & 0xE) >> 1;
		if( metadata >= 0 && metadata < Machines.values().length ) {
			return Machines.values()[metadata].ordinal();
		}
		return 0; // to maintain backwards compatibility
	}

	public static String getMachineName(ItemStack itemStack) {
		if( itemStack != null && itemStack.getItem() instanceof ItemMachine ) {
			return Machines.values()[itemStack.getItemDamage()].machineName;
		}
		return null;
	}

	public static String[] getTextureFiles(int machine) {
		if( machine >= 0 && machine < Machines.values().length )
			return Machines.values()[machine].getTextureFiles();
		return null;
	}

	// Bottom, Top, Front, Side.
	abstract String[] getTextureFiles();

}
