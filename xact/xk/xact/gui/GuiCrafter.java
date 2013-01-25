package xk.xact.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.core.ItemChip;
import xk.xact.core.TileCrafter;
import xk.xact.gui.button.CustomButtons;
import xk.xact.gui.button.GuiButtonCustom;
import xk.xact.gui.button.ICustomButtonMode;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.RecipeDeque;

public class GuiCrafter extends GuiMachine implements InteractiveCraftingGui {

	private TileCrafter crafter;

	public GuiCrafter(TileCrafter crafter, EntityPlayer player) {
		super(new ContainerCrafter(crafter, player));
		this.crafter = crafter;
		this.ySize = 256;
	}

	@SuppressWarnings("unchecked")
	public void onInit() {
		crafter.updateRecipes();
		crafter.updateStates();
		updateGhostContents(-1);
		/*
		 * Buttons: 42, 21. 120, 21 42, 65. 120, 65
		 */
		controlList.clear();

		for (int i = 0; i < 4; i++) {
			int x = (i % 2 == 0 ? 42 : 120) + this.guiLeft;
			int y = (i / 2 == 0 ? 21 : 65) + this.guiTop;

			GuiButtonCustom button = CustomButtons.createdDeviceButton(x, y);
			button.id = i;
			controlList.add(buttons[i] = button);
		}
		invalidated = true;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if (invalidated || crafter.contentsChanged) {

			for (int i = 0; i < 4; i++) {
				ItemStack chip = crafter.circuits.getStackInSlot(i);
				if (chip == null) {
					buttons[i].setMode(ICustomButtonMode.DeviceModes.INACTIVE);
					continue;
				}

				if (chip.getItem() instanceof ItemChip) {
					if (!((ItemChip) chip.getItem()).encoded) {
						CraftRecipe mainRecipe = crafter.getRecipe(4); // the
																		// recipe
																		// on
																		// the
																		// grid
						if (mainRecipe != null && mainRecipe.isValid()) {
							buttons[i]
									.setMode(ICustomButtonMode.DeviceModes.SAVE);
							continue;
						}
						buttons[i]
								.setMode(ICustomButtonMode.DeviceModes.INACTIVE);
						continue;
					}
					buttons[i].setMode(ICustomButtonMode.DeviceModes.CLEAR);
				}
			}
			invalidated = false;
			crafter.contentsChanged = false;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int xPos = (this.xSize - fontRenderer
				.getStringWidth("X.A.C.T. Crafter")) / 2;
		this.fontRenderer.drawString("X.A.C.T. Crafter", xPos, 6, 4210752);
		this.fontRenderer.drawString("Player's Inventory", 8, this.ySize - 94,
				4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX,
			int mouseY) {
		int texture = this.mc.renderEngine
				.getTexture("/gfx/xact/gui/crafter_4.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize,
				this.ySize);

		// Draw crafting grid
		int currentRecipe = getHoveredRecipe(mouseX, mouseY);
		if (hoveredRecipe != currentRecipe) {
			updateGhostContents(currentRecipe);
		}

	}

	@Override
	protected void drawSlotInventory(Slot slot) {
		// grid's contents.
		if (8 <= slot.slotNumber && slot.slotNumber < 18 - 1) {
			int index = slot.slotNumber - 8;
			int color;

			// only paint the grid's real contents if there is no recipe being
			// hovered.
			if (hoveredRecipe == -1) {
				super.drawSlotInventory(slot);
				color = crafter.missingIngredients[index] ? GuiUtils.COLOR_RED
						: GuiUtils.COLOR_GRAY;
			}
			// If a recipe is being hovered, paint those ingredients instead.
			else {
				ItemStack itemToPaint = gridContents[index];

				// Paint the "ghost item"
				GuiUtils.paintItem(itemToPaint, slot.xDisplayPosition,
						slot.yDisplayPosition, this.mc, itemRenderer);
				color = missingIngredients[index] ? GuiUtils.COLOR_RED
						: GuiUtils.COLOR_GRAY;
			}

			// Paint the item's overlay.
			color |= TRANSPARENCY;
			GuiUtils.paintOverlay(slot.xDisplayPosition, slot.yDisplayPosition,
					16, color);

			return;
		}

		if (slot.getHasStack()) {
			// output slots.
			if (slot.slotNumber < 4) {
				// paint slot's colored underlay if the slot is hovered.
				if (slot.slotNumber == hoveredRecipe)
					GuiUtils.paintSlotOverlay(slot, 22,
							getColorFor(slot.getSlotIndex()));
			}
			// show the chip's recipe's output when holding shift.
			else if (slot.slotNumber >= 18 && GuiUtils.isShiftKeyPressed()) {
				ItemStack stack = slot.getStack();
				if (CraftManager.isEncoded(stack)) {
					// paint chip's recipe's result
					CraftRecipe recipe = RecipeUtils.getRecipe(stack,
							this.mc.theWorld);
					if (recipe != null) {
						GuiUtils.paintItem(recipe.getResult(),
								slot.xDisplayPosition, slot.yDisplayPosition,
								this.mc, itemRenderer);
						GuiUtils.paintGreenEffect(slot, itemRenderer);
						return;
					}
				}
			}
		}

		super.drawSlotInventory(slot);
	}

	private int getColorFor(int recipeIndex) {
		int color;
		if (this.mc.thePlayer.capabilities.isCreativeMode) {
			color = GuiUtils.COLOR_BLUE;
		} else if (crafter.isRedState(recipeIndex)) {
			color = GuiUtils.COLOR_RED;
		} else {
			color = GuiUtils.COLOR_GREEN;
		}
		color |= TRANSPARENCY; // transparency layer.

		return color;
	}

	private int getHoveredRecipe(int mouseX, int mouseY) {
		for (int i = 0; i < 4; i++) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);

			if (slot != null && slot.getHasStack()) {
				if (func_74188_c(slot.xDisplayPosition - 3,
						slot.yDisplayPosition - 3, 22, 22, mouseX, mouseY)) {
					return i;
				}
			}
		}
		return -1;
	}

	private Slot getHoveredSlot(int mouseX, int mouseY) {
		for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
			Slot slot = this.inventorySlots.getSlot(i);

			if (slot != null) {
				if (func_74188_c(slot.xDisplayPosition - 3,
						slot.yDisplayPosition - 3, 22, 22, mouseX, mouseY)) {
					return slot;
				}
			}
		}
		return null;
	}

	private int hoveredRecipe = -1;
	public ItemStack[] gridContents = new ItemStack[9];
	public boolean[] missingIngredients = new boolean[9];
	private static final int TRANSPARENCY = 128 << 24; // 50%

	private void updateGhostContents(int newIndex) {
		this.hoveredRecipe = newIndex == -1 ? -1 : newIndex % 4;
		doGhostUpdateLocally();
	}

	private void doGhostUpdateLocally() {
		ContainerCrafter container = (ContainerCrafter) this.inventorySlots;
		TileCrafter crafter = container.crafter;

		CraftRecipe recipe;
		if (hoveredRecipe == -1) {
			recipe = RecipeUtils.getRecipe(crafter.craftGrid.getContents(),
					crafter.worldObj);
		} else if (hoveredRecipe < crafter.getRecipeCount()) {
			recipe = crafter.getRecipe(hoveredRecipe);
		} else {
			throw new IllegalStateException(
					"XACT-GuiCrafter: invalid hoveredRecipe index");
		}

		gridContents = recipe == null ? new ItemStack[9] : recipe
				.getIngredients();
		missingIngredients = hoveredRecipe == -1 ? crafter.missingIngredients
				: crafter.getHandler().getMissingIngredientsArray(recipe);
	}


	// /////////////
	// /// InteractiveCraftingGui
	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		if (ingredients == null) {
			GuiUtils.sendItemToServer(this.mc.getSendQueue(), (byte) -1, null);
			return;
		}
		for (int i = 0; i < ingredients.length; i++) {
			GuiUtils.sendItemToServer(this.mc.getSendQueue(), (byte) (i + 8),
					ingredients[i]);
		}
	}

	// /////////////
	// /// Buttons

	private GuiButtonCustom[] buttons = new GuiButtonCustom[4];

	private boolean invalidated = true;

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button instanceof GuiButtonCustom) {
			int action = ((GuiButtonCustom) button).getAction();

			if (action == 1) { // SAVE
				ItemStack stack = CraftManager.encodeRecipe(crafter
						.getRecipe(4));
				GuiUtils.sendItemToServer(this.mc.getSendQueue(),
						(byte) (4 + button.id), stack);
				return;
			}
			if (action == 3) { // CLEAR
				GuiUtils.sendItemToServer(this.mc.getSendQueue(),
						(byte) (4 + button.id), new ItemStack(
								XActMod.itemRecipeBlank));
			}
		}
	}

	// /////////////
	// /// Key input

	public void handleKeyBinding(String keyDescription) {

		CraftRecipe recipe = null;

		if(keyDescription.equals("xact.clear")){
			recipe = null;
		} else if (keyDescription.equals("xact.load")){
			int mouseX = GuiUtils.getMouseX(this.mc);
			int mouseY = GuiUtils.getMouseY(this.mc);

			Slot hoveredSlot = getHoveredSlot(mouseX, mouseY);
			if (hoveredSlot != null && hoveredSlot.getHasStack()) {
				ItemStack stackInSlot = hoveredSlot.getStack();
				if (CraftManager.isEncoded(stackInSlot)) {
					recipe = RecipeUtils.getRecipe(stackInSlot,
							this.crafter.worldObj);
				}
			}
		} else if (keyDescription.equals("xact.prev")){
			recipe = recipeDeque.getPrevious();
			if (recipe == null) {
				return;
			}
		} else if (keyDescription.equals("xact.next")){
			recipe = recipeDeque.getNext();
			if (recipe == null) {
				return;
			}
		} else if(keyDescription.equals("xact.delete")){
			recipeDeque.clear();
		}
		
		setRecipe(recipe);
	}
	
	
	// /////////////
	// /// Recipe Deque

	private RecipeDeque recipeDeque = new RecipeDeque();

	public void setRecipe(CraftRecipe recipe) {
		ItemStack[] ingredients = (recipe == null) ? null : recipe
				.getIngredients();
		GuiUtils.sendItemsToServer(this.mc.getSendQueue(), ingredients, 8);
	}

	public void pushRecipe(CraftRecipe recipe) {
		recipeDeque.pushRecipe(recipe);
	}

	
}
