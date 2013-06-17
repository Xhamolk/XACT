package xk.xact.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Textures {

	// GUI Textures
	public static final String GUI_CRAFTER = "/mods/xact/textures/gui/GuiCrafter.png";
	public static final String GUI_PAD = "/mods/xact/textures/gui/GuiPad.png";
	public static final String GUI_CASE = "/mods/xact/textures/gui/GuiCase.png";
	public static final String GUI_RECIPE = "/mods/xact/textures/gui/GuiRecipe.png";

	// Items
	public static final String ITEM_CASE = "xact:case";
	public static final String ITEM_CHIP_BLANK = "xact:chip_blank";
	public static final String ITEM_CHIP_ENCODED = "xact:chip_encoded";
	public static final String ITEM_CHIP_INVALID = "xact:chip_invalid";
	public static final String ITEM_PAD_ON = "xact:pad_on";
	public static final String ITEM_PAD_OFF = "xact:pad_off";


	// Block Texture: Crafter
	public static final String CRAFTER_TOP = "xact:crafter_top";
	public static final String CRAFTER_BOTTOM = "xact:crafter_bottom";
	public static final String CRAFTER_FRONT = "xact:crafter_front";
	public static final String CRAFTER_SIDE = "xact:crafter_side";

	// Other
	public static final String MISC_BUTTONS = "/mods/xact/textures/other/buttons_1.png";

	// NEI: Usage Handler
	public static final String NEI_CHIP_HANDLER = "/mods/xact/textures/gui/ChipHandler.png";
}
