package xk.xact.gui;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;

/**
 *
 */
public abstract class GuiMachine extends GuiContainer {

	public GuiMachine(Container container) {
		super(container);
	}

	@Override
	public void initGui() {
		super.initGui();
	}

	public abstract void onInit();



}
