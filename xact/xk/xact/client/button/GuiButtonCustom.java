package xk.xact.client.button;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import xk.xact.util.Textures;

// Used by GuiCrafter for the buttons that save/load/delete recipes.
public abstract class GuiButtonCustom extends GuiButton {

	protected static final ResourceLocation TEXTURE_BUTTONS = new ResourceLocation( Textures.MISC_BUTTONS );

	protected GuiButtonCustom(int posX, int posY) {
		this( -1, posX, posY, 0, 0 );
	}

	protected GuiButtonCustom(int buttonID, int posX, int posY, int width, int height) {
		super( buttonID, posX, posY, width, height, "" );
	}

	/**
	 * Whether if this buttons should be drawn or not.
	 */
	public boolean isVisible() {
		return this.drawButton;
	}

	/**
	 * Whether if the mouse is hovering this button.
	 *
	 * @param mouseX the mouse x-coordinate
	 * @param mouseY the mouse y-coordinate
	 * @return true if the mouse's coordinates are withing
	 *         the area determined by xPosition, yPosition, width and height.
	 */
	public boolean isMouseHovering(int mouseX, int mouseY) {
		return mouseX >= this.xPosition && mouseY >= this.yPosition
				&& mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return isVisible() && super.mousePressed( mc, mouseX, mouseY );
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if( !isVisible() )
			return;

		// is hovering?
		this.field_82253_i = isMouseHovering( mouseX, mouseY );

		// The button's background
		drawBackgroundLayer( mc, mouseX, mouseY );

		// The button's contents
		drawForegroundLayer( mc, mouseX, mouseY );

		// tooltip?
		if( field_82253_i ) {
			drawTooltip( mc, mouseX, mouseY );
		}
	}

	protected abstract void drawBackgroundLayer(Minecraft mc, int mouseX, int mouseY);

	protected void drawForegroundLayer(Minecraft mc, int mouseX, int mouseY) {
	}

	protected void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
	}

	///////////////
	///// Action

	public int action = 0;

	// meant to be overrode.
	public int getAction() {
		return action;
	}

	///////////////
	///// MODES

	protected ICustomButtonMode mode = ICustomButtonMode.NULL;

	public ICustomButtonMode getMode() {
		return mode;
	}

	public GuiButtonCustom setMode(ICustomButtonMode mode) {
		if( !isModeValid( mode ) ) {
			throw new IllegalArgumentException( "Invalid mode: " + mode );
		}
		this.mode = mode;
		onModeSet( mode );
		return this;
	}

	protected abstract boolean isModeValid(ICustomButtonMode mode);

	protected void onModeSet(ICustomButtonMode mode) {
	}


}
