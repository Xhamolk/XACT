package xk.xact.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Textures {

	public static final String TEXTURES_ROOT = "xact:textures/";

	// GUI Textures
	public static final String GUI_CRAFTER = TEXTURES_ROOT + "gui/GuiCrafter.png";
	public static final String GUI_PAD = TEXTURES_ROOT + "gui/GuiPad.png";
	public static final String GUI_CASE = TEXTURES_ROOT + "gui/GuiCase.png";
	public static final String GUI_RECIPE = TEXTURES_ROOT + "gui/GuiRecipe.png";
	public static final String GUI_WORKBENCH = "textures/gui/container/crafting_table.png";

	// NEI: Usage Handler
	public static final String NEI_CHIP_HANDLER = TEXTURES_ROOT + "gui/ChipHandler.png";

	// Other
	public static final String MISC_BUTTONS = TEXTURES_ROOT + "other/buttons_1.png";


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

	// Block Texture: Workbench
	public static final String WORKBENCH_TOP = "crafting_table_top";
	public static final String WORKBENCH_BOTTOM = "planks_spruce";
	public static final String WORKBENCH_FRONT = "crafting_table_front";
	public static final String WORKBENCH_SIDE = "crafting_table_side";

}
