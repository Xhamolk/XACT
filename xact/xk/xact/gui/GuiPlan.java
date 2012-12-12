package xk.xact.gui;



import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiPlan extends GuiScreen {

	protected int paneWidth = 256;
	protected int paneHeight = 202;

	// The x,y position of the map
	protected double guiMapX;
	protected double guiMapY;

	// I don't know what this is for...
	protected double updatedX;
	protected double updatedY;

	protected double lastX;
	protected double lastY;

	// Whether the Mouse Button is down or not
	private boolean isMouseButtonDown = false;

	private int mouseX;
	private int mouseY;

	private String frame;
	private String bg;

	// Add the buttons.
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.controlList.clear();
//		this.controlList.add(new GuiSmallButton(1, this.width / 2 + 24, this.height / 2 + 74, 80, 20, StatCollector.translateToLocal("gui.done")));
//		this.controlList.add(button = new GuiSmallButton(2, (width - paneWidth) / 2 + 24, height / 2 + 74, 125, 20, AchievementPage.getTitle(currentPage)));
	}

	@Override
	public void updateScreen() {

		this.updatedX = this.guiMapX;
		this.updatedY = this.guiMapY;
		double offX = this.lastX - this.guiMapX;
		double offY = this.lastY - this.guiMapY;

		if (offX * offX + offY * offY < 4.0D) {
			this.guiMapX += offX;
			this.guiMapY += offY;
		} else {
			this.guiMapX += offX * 0.85D;
			this.guiMapY += offY * 0.85D;
		}
	}


	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick) {

		// Moves the thingy.
		moveGui( mouseX, mouseY );

		// Paint the "world" background
		this.drawDefaultBackground();

		// Paint the main GUI's background.
		drawBG();

		// Paint the nodes.
		drawNodes(mouseX, mouseY, partialTick);

		// Paint the main GUI's frame
		drawFrame();

		// Draw the title
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		this.drawTitle();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		// paint the buttons.
		super.drawScreen(mouseX, mouseY, partialTick);
	}

	private void drawTitle() {
		int xPos = (this.width - this.paneWidth) / 2;
		int yPos = (this.height - this.paneHeight) / 2;
		this.fontRenderer.drawString("Crafting Blueprint", xPos + 15, yPos + 5, 4210752);
	}

	private void moveGui(int mouseX, int mouseY) {

		if( !Mouse.isButtonDown(0) ) {
			this.isMouseButtonDown = false;
			return;
		}

		int cornerX = (this.width - this.paneWidth) / 2;
		int cornerY = (this.height - this.paneHeight) / 2;
		int canvasX = cornerX + 8;
		int canvasY = cornerY + 17;

		if( mouseX >= canvasX && mouseX < canvasX + 224 && mouseY >= canvasY && mouseY < canvasY + 155 ) {

			if( !isMouseButtonDown ) {
				this.isMouseButtonDown = true;
			} else {
				this.guiMapX -= (double)(mouseX - this.mouseX);
				this.guiMapY -= (double)(mouseY - this.mouseY);

				// IDK what this is for
				this.lastX = this.updatedX = this.guiMapX;
				this.lastY = this.updatedY = this.guiMapY;
			}

			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

//		if (this.lastX < (double)guiMapTop) {
//			this.lastX = (double)guiMapTop;
//		}
//
//		if (this.lastY < (double)guiMapLeft) {
//			this.lastY = (double)guiMapLeft;
//		}
//
//		if (this.lastX >= (double)guiMapBottom) {
//			this.lastX = (double)(guiMapBottom - 1);
//		}
//
//		if (this.lastY >= (double)guiMapRight) {
//			this.lastY = (double)(guiMapRight - 1);
//			}

	}

	private void drawBG() {
		int xCorner = (this.width - this.paneWidth) / 2;
		int yCorner = (this.height - this.paneHeight) / 2;

		this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture( bg ));
		this.drawTexturedModalRect(xCorner + 2, yCorner + 2, 0, 0, this.paneWidth - 4, this.paneHeight - 4);

	}

	private void drawFrame() {
		int xCorner = (this.width - this.paneWidth) / 2;
		int yCorner = (this.height - this.paneHeight) / 2;

		// Draw the frame.
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture( frame ));
		this.drawTexturedModalRect(xCorner, yCorner, 0, 0, this.paneWidth, this.paneHeight);
		GL11.glPopMatrix();
		this.zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void drawNodes(int mouseX, int mouseY, float partialTick) {
		// todo: draw the nodes and lines.
	}

	// todo: buttons and actionHandler.

}
