package xk.xact.gui;


import net.minecraft.src.Gui;
import net.minecraft.src.Slot;
import org.lwjgl.opengl.GL11;

public class GuiUtils {

	private static int grayTone = 139;

	public static final int COLOR_RED = 255 << 16;
	public static final int COLOR_GREEN = 255 << 8;
	public static final int COLOR_BLUE = 255;
	public static final int COLOR_GRAY = (grayTone << 16) | (grayTone << 8) | grayTone << 8;

	public static void paintSlotOverlay(Slot slot, int size, int color) {
		if( slot == null )
			return;

		int off = ( size - 16 ) / 2;
		int minX = slot.xDisplayPosition - off;
		int minY = slot.yDisplayPosition - off;

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		Gui.drawRect(minX, minY, minX + size, minY + size, color);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

}
