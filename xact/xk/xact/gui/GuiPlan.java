package xk.xact.gui;



import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import xk.xact.project.CraftingProject;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.CustomPacket;

import java.io.IOException;

public class GuiPlan extends GuiScreen {

	protected int paneWidth = 256;
	protected int paneHeight = 202;

	// The x,y position of the map
	protected double guiMapX;
	protected double guiMapY;

	// I don't know what this is for...
	protected double lastX;
	protected double lastY;

	protected double currentX;
	protected double currentY;

	// Whether the player is dragging the screen
	private boolean draggingScreen = false;

	private int mouseX;
	private int mouseY;

	private static final String textureFrame = "/gfx/gui/plan_1.png";
	private static final String textureBackground = "/gfx/gui/testBG.png";

	private CraftingProject project;

	public GuiPlan(CraftingProject project) {
		this.project = project;
	}

	// Add the buttons.
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		refreshButtons();
	}

	@Override
	public void updateScreen() {

		this.lastX = this.guiMapX;
		this.lastY = this.guiMapY;
		double offX = this.currentX - this.guiMapX;
		double offY = this.currentY - this.guiMapY;

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

		// Paint the node's lines.
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

	@Override
	protected void actionPerformed(GuiButton button) {
		if( button instanceof ItemButton ) {
			ItemStack itemStack = ((ItemButton) button).item;

			// Set the main recipe.
			if( itemStack == null ) {
				// open the GuiRecipe with no target, nor a recipe.
				this.target = null;
				this.currentRecipe = null;
			}
			// Editing the item's current recipe.
			else if( project.itemHasRecipe(itemStack) ){
				// open the GuiRecipe with the current recipe and target.
				this.target = itemStack;
				this.currentRecipe = project.getRecipeFor( itemStack );
			}
			// Request a recipe for the current item.
			else {
				// open the GuiRecipe with the current target, with no recipe.
				this.target = itemStack;
				this.currentRecipe = null;
			}
			openRecipeGui();

		} else {
			// todo: handle normal button clicks.
			super.actionPerformed(button);
		}
	}


	private void drawTitle() {
		int xPos = (this.width - this.paneWidth) / 2;
		int yPos = (this.height - this.paneHeight) / 2;
		this.fontRenderer.drawString("Crafting Blueprint", xPos + 15, yPos + 5, 4210752);
	}

	private void moveGui(int mouseX, int mouseY) {

		if( !Mouse.isButtonDown(0) ) {
			this.draggingScreen = false;
			return;
		}

		int cornerX = (this.width - this.paneWidth) / 2;
		int cornerY = (this.height - this.paneHeight) / 2;
		int canvasX = cornerX + 8;
		int canvasY = cornerY + 17;

		if( mouseX >= canvasX && mouseX < canvasX + 224 && mouseY >= canvasY && mouseY < canvasY + 155 ) {

			if( !draggingScreen) {
				this.draggingScreen = true;
			} else {
				this.guiMapX -= (double)(mouseX - this.mouseX);
				this.guiMapY -= (double)(mouseY - this.mouseY);

				// IDK what this is for
				this.currentX = this.lastX = this.guiMapX;
				this.currentY = this.lastY = this.guiMapY;
			}

			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

//		if (this.currentX < (double)guiMapTop) {
//			this.currentX = (double)guiMapTop;
//		}
//
//		if (this.currentY < (double)guiMapLeft) {
//			this.currentY = (double)guiMapLeft;
//		}
//
//		if (this.currentX >= (double)guiMapBottom) {
//			this.currentX = (double)(guiMapBottom - 1);
//		}
//
//		if (this.currentY >= (double)guiMapRight) {
//			this.currentY = (double)(guiMapRight - 1);
//		}

	}

	private void drawBG() {
		int xCorner = (this.width - this.paneWidth) / 2;
		int yCorner = (this.height - this.paneHeight) / 2;

		// todo: what about background scrolling? recheck PlanLearning.
		this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture( textureBackground ));
		this.drawTexturedModalRect(xCorner + 2, yCorner + 2, 0, 0, this.paneWidth - 4, this.paneHeight - 4);
	}

	private void drawFrame() {
		int xCorner = (this.width - this.paneWidth) / 2;
		int yCorner = (this.height - this.paneHeight) / 2;

		// Draw the frame.
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture( textureFrame ));
		this.drawTexturedModalRect(xCorner, yCorner, 0, 0, this.paneWidth, this.paneHeight);
		GL11.glPopMatrix();
		this.zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void drawNodes(int mouseX, int mouseY, float partialTick) {
		// todo: draw the nodes (as buttons) and lines.
	}

	private void openRecipeGui() {
		try {
			Packet250CustomPayload packet = new CustomPacket((byte)0x04).toPacket();
			this.mc.getSendQueue().addToSendQueue( packet );
		} catch (IOException ioe) {
			FMLCommonHandler.instance().raiseException(ioe, "GuiPlan: request recipe. custom packet", true);
		}
	}

	private void refreshButtons() {
		this.controlList.clear();

		// add the frame buttons. todo
//		this.controlList.add(new GuiSmallButton(1, this.width / 2 + 24, this.height / 2 + 74, 80, 20, StatCollector.translateToLocal("gui.done")));
//		this.controlList.add(button = new GuiSmallButton(2, (width - paneWidth) / 2 + 24, height / 2 + 74, 125, 20, AchievementPage.getTitle(currentPage)));

		// todo: load all the recipes from the project.
	}


	private void line(int lvl, int children) {
		int color = -1; // white.

		if( children > 1 ) {
			// Draw line.
		}

//		this.drawHorizontalLine(int x1, int x2, int y, color);
//		this.drawVerticalLine(int x, int y1, int y2, color);
	}

	///////////////
	///// Requests for Recipe's GUI.

	private ItemStack target = null;
	private CraftRecipe currentRecipe = null;

	public ItemStack getTarget() {
		return target;
	}

	public CraftRecipe getCurrentRecipe() {
		return currentRecipe;
	}

	// todo: when closing, send the CraftProject instance as a packet, so it's added into the player's inventory.  (key pressed)

}
