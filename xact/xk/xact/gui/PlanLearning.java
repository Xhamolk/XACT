package xk.xact.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import net.minecraftforge.common.AchievementPage;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PlanLearning extends GuiScreen {


	/** The left y coordinate of the achievement map */
	private static final int guiMapLeft = AchievementList.minDisplayRow * 24 - 112;

	/** The right y coordinate of the achievement map */
	private static final int guiMapRight = AchievementList.maxDisplayRow * 24 - 77;

	/** The top x coordinate of the achievement map */
	private static final int guiMapTop = AchievementList.minDisplayColumn * 24 - 112;

	/** The bottom x coordinate of the achievement map */
	private static final int guiMapBottom = AchievementList.maxDisplayColumn * 24 - 77;


	protected int paneWidth = 256;
	protected int paneHeight = 202;

	// The current mouse x,y coordinates.
	protected int mouseX = 0;
	protected int mouseY = 0;

	// The x,y position of the achievement map
	protected double guiMapX;
	protected double guiMapY;

	// I don't know what this is for...
	protected double updatedX;
	protected double updatedY;

	protected double currentX;
	protected double currentY;

	// Whether the Mouse Button is down or not
	private int isMouseButtonDown = 0;

	// to be removed...
	private StatFileWriter statFileWriter;
	private int currentPage = -1;
	private GuiSmallButton button;
	private LinkedList<Achievement> minecraftAchievements = new LinkedList<Achievement>();

	// to be removed...
	public PlanLearning(StatFileWriter statFileWriter) {
		this.statFileWriter = statFileWriter;
		short var2 = 141;
		short var3 = 141;
		this.updatedX = this.guiMapX = this.currentX = (double)(AchievementList.openInventory.displayColumn * 24 - var2 / 2 - 12);
		this.updatedY = this.guiMapY = this.currentY = (double)(AchievementList.openInventory.displayRow * 24 - var3 / 2);
		minecraftAchievements.clear();

		for (Object achievement : AchievementList.achievementList) {
			if (!AchievementPage.isAchievementInPages((Achievement) achievement)) {
				minecraftAchievements.add((Achievement)achievement);
			}
		}
	}

	// Add the buttons.
	@SuppressWarnings("unchecked")
	public void initGui() {
		this.controlList.clear();
		this.controlList.add(new GuiSmallButton(1, this.width / 2 + 24, this.height / 2 + 74, 80, 20, StatCollector.translateToLocal("gui.done")));
		this.controlList.add(button = new GuiSmallButton(2, (width - paneWidth) / 2 + 24, height / 2 + 74, 125, 20, AchievementPage.getTitle(currentPage)));
	}


	// Handles the button clicks.
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 1) { // exit button
			this.mc.displayGuiScreen(null);
			this.mc.setIngameFocus();
		}

		if (par1GuiButton.id == 2) { // next page
			currentPage++;
			if (currentPage >= AchievementPage.getAchievementPages().size()) {
				currentPage = -1;
			}
			button.displayString = AchievementPage.getTitle(currentPage);
		}

		super.actionPerformed(par1GuiButton);
	}

	// close the GUI. todo: GuiRecipe should override this.
	protected void keyTyped(char par1, int par2) {
		if (par2 == this.mc.gameSettings.keyBindInventory.keyCode) {
			this.mc.displayGuiScreen(null);
			this.mc.setIngameFocus();
		} else {
			super.keyTyped(par1, par2);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTick) {

		// Holding the mouse and moving it will move the screen as well.
		if (Mouse.isButtonDown(0)) {
			int cornerX = (this.width - this.paneWidth) / 2;
			int cornerY = (this.height - this.paneHeight) / 2;
			int canvasX = cornerX + 8;
			int canvasY = cornerY + 17;

			// This moves the screen
			if ((this.isMouseButtonDown == 0 || this.isMouseButtonDown == 1) && mouseX >= canvasX && mouseX < canvasX + 224 && mouseY >= canvasY && mouseY < canvasY + 155) {
				if (this.isMouseButtonDown == 0) {
					this.isMouseButtonDown = 1;
				} else {
					this.guiMapX -= (double)(mouseX - this.mouseX);
					this.guiMapY -= (double)(mouseY - this.mouseY);

					// IDK what this is for
					this.currentX = this.updatedX = this.guiMapX;
					this.currentY = this.updatedY = this.guiMapY;
				}

				this.mouseX = mouseX;
				this.mouseY = mouseY;
			}

			if (this.currentX < (double)guiMapTop) {
				this.currentX = (double)guiMapTop;
			}

			if (this.currentY < (double)guiMapLeft) {
				this.currentY = (double)guiMapLeft;
			}

			if (this.currentX >= (double)guiMapBottom) {
				this.currentX = (double)(guiMapBottom - 1);
			}

			if (this.currentY >= (double)guiMapRight) {
				this.currentY = (double)(guiMapRight - 1);
			}
		} else {
			this.isMouseButtonDown = 0;
		}

		// Actually start drawing the stuff.

		this.drawDefaultBackground(); // this is the "world" background.

		this.paintTheBackground(mouseX, mouseY, partialTick); // this is the real method.

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		this.drawTitle();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {

		this.updatedX = this.guiMapX;
		this.updatedY = this.guiMapY;
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

	/**
	 * Draws the "Achievements" title at the top of the GUI.
	 */
	protected void drawTitle() {
		int xPos = (this.width - this.paneWidth) / 2;
		int yPos = (this.height - this.paneHeight) / 2;
		this.fontRenderer.drawString("Crafting Blueprint", xPos + 15, yPos + 5, 4210752);
	}

	protected void paintTheBackground(int mouseX, int mouseY, float renderPartialTicks) {

		int xCoord = MathHelper.floor_double(this.updatedX + (this.guiMapX - this.updatedX) * (double)renderPartialTicks);
		int yCoord = MathHelper.floor_double(this.updatedY + (this.guiMapY - this.updatedY) * (double)renderPartialTicks);

		if (xCoord < guiMapTop) {
			xCoord = guiMapTop;
		}

		if (yCoord < guiMapLeft) {
			yCoord = guiMapLeft;
		}

		if (xCoord >= guiMapBottom) {
			xCoord = guiMapBottom - 1;
		}

		if (yCoord >= guiMapRight) {
			yCoord = guiMapRight - 1;
		}

		int terrainTexture = this.mc.renderEngine.getTexture("/terrain.png");
		int achievementsBG = this.mc.renderEngine.getTexture("/achievement/bg.png");

		int xCorner = (this.width - this.paneWidth) / 2;
		int yCorner = (this.height - this.paneHeight) / 2;

		int minX = xCorner + 16;
		int minY = yCorner + 17;
		this.zLevel = 0.0F;

		GL11.glDepthFunc(GL11.GL_GEQUAL);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		this.mc.renderEngine.bindTexture(terrainTexture);
		int var12 = xCoord + 288 >> 4;
		int var13 = yCoord + 288 >> 4;
		int var14 = (xCoord + 288) % 16;
		int var15 = (yCoord + 288) % 16;


		// This generates the background. I don't think I'll be doing this.

		for (int i = 0; i * 16 - var15 < 155; ++i) {
			float var23 = 0.6F - (float)(var13 + i) / 25.0F * 0.3F;
			GL11.glColor4f(var23, var23, var23, 1.0F);

			for (int e = 0; e * 16 - var14 < 224; ++e) {

				Random random = new Random();
				random.setSeed((long) (1234 + var12 + e));
				random.nextInt();
				int var25 = random.nextInt(1 + var13 + i) + (var13 + i) / 2;
				int var26 = Block.sand.blockIndexInTexture;

				if (var25 <= 37 && var13 + i != 35)
				{
					if (var25 == 22) {
						if (random.nextInt(2) == 0) {
							var26 = Block.oreDiamond.blockIndexInTexture;
						} else {
							var26 = Block.oreRedstone.blockIndexInTexture;
						}
					} else if (var25 == 10) {
						var26 = Block.oreIron.blockIndexInTexture;
					} else if (var25 == 8) {
						var26 = Block.oreCoal.blockIndexInTexture;
					} else if (var25 > 4) {
						var26 = Block.stone.blockIndexInTexture;
					} else if (var25 > 0) {
						var26 = Block.dirt.blockIndexInTexture;
					}
				} else {
					var26 = Block.bedrock.blockIndexInTexture;
				}

				this.drawTexturedModalRect(minX + e * 16 - var14, minY + i * 16 - var15, var26 % 16 << 4, var26 >> 4 << 4, 16, 16);
			}
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		// I guess this is what paints the lines.
		List<Achievement> achievementList = (currentPage == -1 ? minecraftAchievements : AchievementPage.getAchievementPage(currentPage).getAchievements());
		for (int index = 0; index < achievementList.size(); ++index) {
			Achievement currentAchievement = achievementList.get(index);

			if (currentAchievement.parentAchievement != null && achievementList.contains(currentAchievement.parentAchievement))
			{
				int var24 = currentAchievement.displayColumn * 24 - xCoord + 11 + minX;
				int var25 = currentAchievement.displayRow * 24 - yCoord + 11 + minY;
				int var26 = currentAchievement.parentAchievement.displayColumn * 24 - xCoord + 11 + minX;
				int var27 = currentAchievement.parentAchievement.displayRow * 24 - yCoord + 11 + minY;
				boolean unlocked = this.statFileWriter.hasAchievementUnlocked(currentAchievement);
				boolean canUnlock = this.statFileWriter.canUnlockAchievement(currentAchievement);



				int color = -16777216; // I guess this is the default, grey line.

				if (unlocked) {
					color = -9408400;
				} else if (canUnlock) {
					// blinking.
					int alpha = Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * Math.PI * 2.0D) > 0.6D ? 255 : 130;
					color = 65280 + (alpha << 24);
				}

				this.drawHorizontalLine(var24, var26, var25, color);
				this.drawVerticalLine(var26, var25, var27, color);
			}
		}
		// end: Lines



		Achievement hoveringAchievement = null;
		RenderItem renderItem = new RenderItem();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		// Renders the achievements.
		for (Achievement achievement : achievementList) {
			int tempX = achievement.displayColumn * 24 - xCoord;
			int tempY = achievement.displayRow * 24 - yCoord;

			// Only paint the nodes that are inside the canvas area (which seems to be 244x155).
			if (tempX >= -24 && tempY >= -24 && tempX <= 224 && tempY <= 155) {

				// Change the color with which the button is painted.
					// Unlocked: full color.
					// can unlock: blinking
					// otherwise: gray.

				float colorMask = 0.03F;
				if (this.statFileWriter.hasAchievementUnlocked(achievement)) {
					colorMask = 1.0F;
				} else if (this.statFileWriter.canUnlockAchievement(achievement)) {
					colorMask = Math.sin((double) (Minecraft.getSystemTime() % 600L) / 600.0D * Math.PI * 2.0D) < 0.6D ? 0.6F : 0.8F;
				}

				GL11.glColor4f(colorMask, colorMask, colorMask, 1.0F);

				this.mc.renderEngine.bindTexture(achievementsBG);
				int buttonX = minX + tempX;
				int buttonY = minY + tempY;

				// Draw the "button"
				if (achievement.getSpecial()) {
					this.drawTexturedModalRect(buttonX - 2, buttonY - 2, 26, 202, 26, 26);
				} else {
					this.drawTexturedModalRect(buttonX - 2, buttonY - 2, 0, 202, 26, 26);
				}

				// Gray-out "unlockable" achievements.
				if (!this.statFileWriter.canUnlockAchievement(achievement)) {
					float grayTone = 0.1F;
					GL11.glColor4f(grayTone, grayTone, grayTone, 1.0F);
					renderItem.field_77024_a = false;
				}

				// Render the achievement's item
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_CULL_FACE);
				renderItem.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, achievement.theItemStack, buttonX + 3, buttonY + 3);
				GL11.glDisable(GL11.GL_LIGHTING);

				if (!this.statFileWriter.canUnlockAchievement(achievement)) {
					renderItem.field_77024_a = true;
				}

				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

				// If the mouse is hovering the achievement...
				if (mouseX >= minX && mouseY >= minY && mouseX < minX + 224 && mouseY < minY + 155) {
					if( mouseX >= buttonX && mouseX <= buttonX + 22 && mouseY >= buttonY && mouseY <= buttonY + 22)
						hoveringAchievement = achievement;
				}
			}
		}

		// Draw the frame.
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(achievementsBG);
		this.drawTexturedModalRect(xCorner, yCorner, 0, 0, this.paneWidth, this.paneHeight);
		GL11.glPopMatrix();
		this.zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		super.drawScreen(mouseX, mouseY, renderPartialTicks); // paint the buttons.

		// Is hovering an achievement?
		if (hoveringAchievement != null) {
			// yes, so paint it's description as well.

			String achievementName = StatCollector.translateToLocal(hoveringAchievement.getName());
			String achievementDescription = hoveringAchievement.getDescription();
			int tooltipX = mouseX + 12;
			int tooltipY = mouseY - 4;

			// Unlocked: paint the description tooltip.
			if (this.statFileWriter.canUnlockAchievement(hoveringAchievement)) {
				int maxStringWidth = Math.max(this.fontRenderer.getStringWidth(achievementName), 120);
				int stringWidth = this.fontRenderer.splitStringWidth(achievementDescription, maxStringWidth);

				boolean unlocked = this.statFileWriter.hasAchievementUnlocked(hoveringAchievement);
				if ( unlocked ) {
					stringWidth += 12;
				}

				this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + maxStringWidth + 3, tooltipY + stringWidth + 3 + 12, -1073741824, -1073741824);
				this.fontRenderer.drawSplitString(achievementDescription, tooltipX, tooltipY + 12, maxStringWidth, -6250336);

				if ( unlocked ) {
					this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("achievement.taken"), tooltipX, tooltipY + stringWidth + 4, -7302913);
				}

			} else {
			// Locked: paint the requirements tooltip.

				int maxStringWidth = Math.max(this.fontRenderer.getStringWidth(achievementName), 120);
				String stringRequired = StatCollector.translateToLocalFormatted("achievement.requires", StatCollector.translateToLocal(hoveringAchievement.parentAchievement.getName()));
				int stringWidth = this.fontRenderer.splitStringWidth(stringRequired, maxStringWidth);
				this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + maxStringWidth + 3, tooltipY + stringWidth + 12 + 3, -1073741824, -1073741824);
				this.fontRenderer.drawSplitString(stringRequired, tooltipX, tooltipY + 12, maxStringWidth, -9416624);
			}

			this.fontRenderer.drawStringWithShadow(achievementName, tooltipX, tooltipY, this.statFileWriter.canUnlockAchievement(hoveringAchievement) ? (hoveringAchievement.getSpecial() ? -128 : -1) : (hoveringAchievement.getSpecial() ? -8355776 : -8355712));
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.disableStandardItemLighting();
	}

	public boolean doesGuiPauseGame() {
		return true;
	}



}
