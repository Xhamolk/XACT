package xk.xact.client.render;


import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import xk.xact.client.GuiUtils;
import xk.xact.core.items.ItemChip;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

public class ChipRenderer implements IItemRenderer {

	// Prevents an infinite loop when drawing a recipe chip on a recipe chip...
	private boolean drawing = false;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return canRevealRecipe() && item.getItem() instanceof ItemChip
				&& ((ItemChip) item.getItem()).encoded && type == ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false; // No, this is not a block.
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data) {
		drawing = true;

		GL11.glEnable( GL11.GL_LIGHTING );

		CraftRecipe recipe = RecipeUtils.getRecipe( itemStack, Minecraft.getMinecraft().theWorld );
		if( recipe != null ) {
			GuiUtils.paintItem( recipe.getResult(), 0, 0, Minecraft.getMinecraft(), GuiUtils.itemRender );

			// Green overlay
			GuiUtils.paintEffectOverlay( 0, 0, GuiUtils.itemRender, 0.25f, 0.55f, 0.3f, 0.85f );
		} else {
			// paint invalid chip icon
			GuiUtils.paintItem( ItemChip.invalidChip, 0, 0, Minecraft.getMinecraft(), GuiUtils.itemRender );
		}

		drawing = false;
		GL11.glEnable( GL11.GL_CULL_FACE );
	}

	private boolean canRevealRecipe() {
		return !drawing && GuiUtils.isRevealKeyPressed();
	}

}
