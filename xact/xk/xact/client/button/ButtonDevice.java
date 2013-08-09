package xk.xact.client.button;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import xk.xact.util.Textures;
import xk.xact.client.GuiUtils;


public class ButtonDevice extends GuiButtonCustom {

	private static final int SIZE = 14;

	private int textureIndex = 0;

	ButtonDevice(int posX, int posY) {
		super( -1, posX, posY, SIZE, SIZE );
	}

	@Override
	public boolean isVisible() {
		if( !super.isVisible() )
			return false;
		if( getMode() == ICustomButtonMode.DeviceModes.CLEAR )
			return GuiUtils.isShiftKeyPressed();
		return this.getMode() == ICustomButtonMode.DeviceModes.SAVE;
	}

	@Override
	public boolean isModeValid(ICustomButtonMode mode) {
		return mode instanceof ICustomButtonMode.DeviceModes;
	}

	@Override
	protected void drawBackgroundLayer(Minecraft mc, int mouseX, int mouseY) {
		// Draw button.
		GuiUtils.bindTexture( TEXTURE_BUTTONS );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.drawTexturedModalRect( this.xPosition, this.yPosition, textureIndex * 14, 0, this.width, this.height );
	}

	@Override
	protected void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
		// todo: paint the tool tip with the instructions
	}

	@Override
	protected void onModeSet(ICustomButtonMode mode) {
		this.action = this.textureIndex = ((ICustomButtonMode.DeviceModes) mode).ordinal();
	}

}
