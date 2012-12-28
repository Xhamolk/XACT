package xk.xact.plugin.nei;


import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import xk.xact.gui.GuiPad;
import xk.xact.gui.GuiRecipe;

public class NEIXACTConfig implements IConfigureNEI {

	@Override
	public void loadConfig() {
		// Register the handler for the Craft Pad.
		XactOverlayHandler handler = new XactOverlayHandler();

		API.registerGuiOverlayHandler(GuiRecipe.class, handler, "crafting");
		API.registerGuiOverlayHandler(GuiPad.class, handler, "crafting");
	}

	@Override
	public String getName() {
		return "xact_nei_plugin";
	}

	@Override
	public String getVersion() {
		return "v1.1";
	}
}
