package xk.xact.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

/**
 *
 */
public abstract class GuiMachine extends GuiContainer {

	public GuiMachine(Container container) {
		super( container );
	}

	@Override
	public void initGui() {
		super.initGui();
		this.onInit();
	}

	public abstract void onInit();


}
