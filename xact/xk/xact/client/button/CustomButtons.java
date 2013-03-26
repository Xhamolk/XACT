package xk.xact.client.button;


import net.minecraft.item.ItemStack;

public final class CustomButtons {

	private CustomButtons() {
	}

	public static GuiButtonCustom createItemButton(int x, int y, ItemStack item) {
		return new ButtonItem( item, x, y ).setMode( ICustomButtonMode.ItemModes.NORMAL );
	}

	public static GuiButtonCustom createSpecialItemButton(int x, int y, ItemStack item) {
		return new ButtonItem( item, x, y ).setMode( ICustomButtonMode.ItemModes.SPECIAL );
	}

	public static GuiButtonCustom createdDeviceButton(int x, int y) {
		return new ButtonDevice( x, y ).setMode( ICustomButtonMode.DeviceModes.INACTIVE );
	}

	public static GuiButtonCustom createdDeviceButton(int x, int y, ICustomButtonMode mode) {
		return createdDeviceButton( x, y ).setMode( mode ); // the returned value should have its buttonID assigned
	}


}

