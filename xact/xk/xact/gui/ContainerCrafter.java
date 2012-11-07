package xk.xact.gui;


import net.minecraft.src.*;
import xk.xact.ItemRecipe;
import xk.xact.TileCrafter;
import xk.xact.recipes.CraftManager;


/**
 * The container used for the Crafter's GUI.
 */
public class ContainerCrafter extends ContainerMachine {

	private TileCrafter crafter;
	
	private EntityPlayer player;

	public ContainerCrafter(TileCrafter crafter, EntityPlayer player) {
		this.crafter = crafter;
		this.player = player;
		buildContainer();
	}


	private void buildContainer() {
		// craft results
		for(int i=0; i<4; i++) {
			addSlotToContainer(new SlotCraft(crafter, i, 26 + 36*i, 25));
		}

		// circuits
		for(int i=0; i<4; i++){
			addSlotToContainer(new SlotRestricted(crafter.circuits, i, 26 + 36*i, 47) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return CraftManager.isEncoded(stack);
				}

			});
		}

		// resources
		for(int i=0; i<2; i++){
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 71;
				addSlotToContainer(new Slot(crafter.resources, e + i*9, x, y));
			}
		}
		
		// player's inventory
		for(int i=0; i<3; i++) {
			for(int e=0; e<9; e++){
				int x = 18*e + 8, y = 18*i + 117;
				addSlotToContainer(new Slot(player.inventory, e + i*9 + 9, x, y));
			}
		}
		// player's hot bar
		for(int i=0; i<9; i++){
			addSlotToContainer(new Slot(player.inventory, i, 18*i + 8, 175));
		}
		
	}


	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID){
		Slot slot = (Slot) inventorySlots.get(slotID);

		if( slot == null || !slot.getHasStack() )
			return null;
		ItemStack stackInSlot = slot.getStack();
		ItemStack stack = stackInSlot.copy();

		if( slot instanceof SlotCraft ) {
			// add to the resources buffer.
			if (mergeItemStack(stackInSlot, 8, 8+18, false)) {
				slot.onPickupFromSlot(player, stack);
				slot.onSlotChanged();
				return stack;
			}

			return null;
		}

		// From the crafter to the resources buffer.
		if( slotID < 8 ) {
			if (!mergeItemStack(stackInSlot, 8, 18+8, false))
				return null;
			
		} else if( slotID < 8+18 ){ // from the resources buffer
			// chips first try to go to the chip slots.
			if( stackInSlot.getItem() instanceof ItemRecipe){
				if (!mergeItemStack(stackInSlot, 4, 8, false)) // try add to the chip slots.
					if (!mergeItemStack(stackInSlot, 8+18, inventorySlots.size(), false)) // add to the player's inv.
						return null;

			} else { // any other item goes to the player's inventory.
				if (!mergeItemStack(stackInSlot, 8+18, inventorySlots.size(), false))
					return null;
			}

		} else { // From the player's inventory to the resources buffer.
			if (!mergeItemStack(stackInSlot, 8, 8+18, false))
				return null;
		}

		if ( stackInSlot.stackSize == 0 ) {
			slot.putStack(null);
		}
        slot.onSlotChanged();

		return stack;
	}

	public ItemStack slotClick(int slotID, int mouseButton, int flag, EntityPlayer player) {
		ItemStack retValue = null;
		InventoryPlayer inventoryPlayer = player.inventory;
		Slot slot;
		ItemStack stackInSlot;
		int amount;
		ItemStack var11;

		if ((flag == 0 || flag == 1) && (mouseButton == 0 || mouseButton == 1))
		{
			if (slotID == -999)
			{
				if (inventoryPlayer.getItemStack() != null && slotID == -999)
				{
					if (mouseButton == 0)
					{
						player.dropPlayerItem(inventoryPlayer.getItemStack());
						inventoryPlayer.setItemStack(null);
					}

					if (mouseButton == 1)
					{
						player.dropPlayerItem(inventoryPlayer.getItemStack().splitStack(1));

						if (inventoryPlayer.getItemStack().stackSize == 0)
						{
							inventoryPlayer.setItemStack(null);
						}
					}
				}
			}
			else if (flag == 1)
			{
				slot = (Slot)this.inventorySlots.get(slotID);

				if (slot != null && slot.canTakeStack(player)) {
					stackInSlot = this.transferStackInSlot(player, slotID);

					if (stackInSlot != null) {
						int var12 = stackInSlot.itemID;
						retValue = stackInSlot.copy();

						if (slot != null && slot.getStack() != null && !(slot instanceof SlotCraft) && slot.getStack().itemID == var12) {
							this.retrySlotClick(slotID, mouseButton, true, player);
						}
					}
				}
			}
			else
			{
				if (slotID < 0)
				{
					return null;
				}

				slot = (Slot)this.inventorySlots.get(slotID);

				if (slot != null)
				{
					stackInSlot = slot.getStack();
					ItemStack var13 = inventoryPlayer.getItemStack();

					if (stackInSlot != null)
					{
						retValue = stackInSlot.copy();
					}

					if (stackInSlot == null)
					{
						if (var13 != null && slot.isItemValid(var13))
						{
							amount = mouseButton == 0 ? var13.stackSize : 1;

							if (amount > slot.getSlotStackLimit())
							{
								amount = slot.getSlotStackLimit();
							}

							slot.putStack(var13.splitStack(amount));

							if (var13.stackSize == 0) {
								inventoryPlayer.setItemStack(null);
							}
						}
					} else if (slot.canTakeStack(player)) {
						if (var13 == null) {
							amount = mouseButton == 0 ? stackInSlot.stackSize : (stackInSlot.stackSize + 1) / 2;
							var11 = slot.decrStackSize(amount);
							inventoryPlayer.setItemStack(var11);

							if (stackInSlot.stackSize == 0) {
								slot.putStack(null);
							}

							slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());
						}
						else if (slot.isItemValid(var13))
						{
							if (stackInSlot.itemID == var13.itemID && (!stackInSlot.getHasSubtypes() || stackInSlot.getItemDamage() == var13.getItemDamage()) && ItemStack.areItemStackTagsEqual(stackInSlot, var13))
							{
								amount = mouseButton == 0 ? var13.stackSize : 1;

								if (amount > slot.getSlotStackLimit() - stackInSlot.stackSize)
								{
									amount = slot.getSlotStackLimit() - stackInSlot.stackSize;
								}

								if (amount > var13.getMaxStackSize() - stackInSlot.stackSize)
								{
									amount = var13.getMaxStackSize() - stackInSlot.stackSize;
								}

								var13.splitStack(amount);

								if (var13.stackSize == 0)
								{
									inventoryPlayer.setItemStack(null);
								}

								stackInSlot.stackSize += amount;
							}
							else if (var13.stackSize <= slot.getSlotStackLimit())
							{
								slot.putStack(var13);
								inventoryPlayer.setItemStack(stackInSlot);
							}
						}
						else if (stackInSlot.itemID == var13.itemID && var13.getMaxStackSize() > 1 && (!stackInSlot.getHasSubtypes() || stackInSlot.getItemDamage() == var13.getItemDamage()) && ItemStack.areItemStackTagsEqual(stackInSlot, var13))
						{
							amount = stackInSlot.stackSize;

							if (amount > 0 && amount + var13.stackSize <= var13.getMaxStackSize())
							{
								var13.stackSize += amount;
								stackInSlot = slot.decrStackSize(amount);

								if (stackInSlot.stackSize == 0)
								{
									slot.putStack(null);
								}

								slot.onPickupFromSlot(player, inventoryPlayer.getItemStack());
							}
						}
					}

					slot.onSlotChanged();
				}
			}
		}
		else if (flag == 2 && mouseButton >= 0 && mouseButton < 9)
		{
			slot = (Slot)this.inventorySlots.get(slotID);

			if (slot.canTakeStack(player))
			{
				stackInSlot = inventoryPlayer.getStackInSlot(mouseButton);
				boolean var9 = stackInSlot == null || slot.inventory == inventoryPlayer && slot.isItemValid(stackInSlot);
				amount = -1;

				if (!var9)
				{
					amount = inventoryPlayer.getFirstEmptyStack();
					var9 |= amount > -1;
				}

				if (slot.getHasStack() && var9)
				{
					var11 = slot.getStack();
					inventoryPlayer.setInventorySlotContents(mouseButton, var11);

					if ((slot.inventory != inventoryPlayer || !slot.isItemValid(stackInSlot)) && stackInSlot != null)
					{
						if (amount > -1)
						{
							inventoryPlayer.addItemStackToInventory(stackInSlot);
							slot.putStack(null);
							slot.onPickupFromSlot(player, var11);
						}
					}
					else
					{
						slot.putStack(stackInSlot);
						slot.onPickupFromSlot(player, var11);
					}
				}
				else if (!slot.getHasStack() && stackInSlot != null && slot.isItemValid(stackInSlot))
				{
					inventoryPlayer.setInventorySlotContents(mouseButton, null);
					slot.putStack(stackInSlot);
				}
			}
		}
		else if (flag == 3 && player.capabilities.isCreativeMode && inventoryPlayer.getItemStack() == null && slotID > 0)
		{
			slot = (Slot)this.inventorySlots.get(slotID);

			if (slot != null && slot.getHasStack())
			{
				stackInSlot = slot.getStack().copy();
				stackInSlot.stackSize = stackInSlot.getMaxStackSize();
				inventoryPlayer.setItemStack(stackInSlot);
			}
		}

		return retValue;
	}


}
