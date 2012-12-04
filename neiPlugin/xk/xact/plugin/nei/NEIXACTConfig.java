package xk.xact.plugin.nei;


import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import xk.xact.gui.GuiPad;

public class NEIXACTConfig implements IConfigureNEI {

	@Override
	public void loadConfig() {
		// Register the handler for the Craft Pad.
		API.registerGuiOverlayHandler(GuiPad.class, new XactOverlayHandler(), "crafting");
	}

	@Override
	public String getName() {
		return "xact_nei_plugin";
	}

	@Override
	public String getVersion() {
		return "v1.0";
	}
}
