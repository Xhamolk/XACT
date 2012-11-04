package xk.xact;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author Xhamolk_
 */
public class BlockMachine extends BlockContainer {

	public static final String[] names = { "encoder", "crafter" };

	public BlockMachine(int itemID) {
		super(itemID, Material.iron);
		this.setStepSound(soundMetalFootstep);
		this.setHardness(2.0f);
		this.setResistance(1.5f);
		this.setCreativeTab(XActMod.xactTab);
	}


	// update block when it's placed on the world.
	public void updateBlockMetadata(World world, int x, int y, int z, int side, float xOff, float yOff, float zOff) {
		int type = world.getBlockMetadata(x, y, z);
		int meta = side << 1 | type;
		world.setBlockMetadata(x, y, z, meta);
	}

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving living) {
		EntityPlayer player = (EntityPlayer) living;
		int meta = world.getBlockMetadata(x, y, z);
		int side = (meta & MASK_FRONT) >> 1;
		if( side == 0 || side == 1 ){
			side = sideByAngles(player, x, y, z);
		}

		// debugging
//		player.sendChatToPlayer("Placed with meta: "+meta);
//		TileEntity entity = world.getBlockTileEntity(x, y, z);
//		if( entity != null ) {
//			player.sendChatToPlayer("Real meta: "+entity.blockMetadata);
//		}

		meta = (side << 1) | (meta & 1);
//		player.sendChatToPlayer("New meta: "+meta);

		world.setBlockMetadata(x, y, z, meta);
	}

	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOff, float yOff, float zOff){

		if( player.isSneaking() ) {
			// debugging
//			player.sendChatToPlayer("Side: "+ (world.isRemote ? "Client" : "Server"));
//			int meta = world.getBlockMetadata(x, y, z);
//			int type = (meta & MASK_TYPE);
//			int front = (meta & MASK_FRONT) >> 1;
//			player.sendChatToPlayer("Metadata: "+ meta+". Type: "+type+". Front: "+ front);
//			return true;

			return false;
		}

		int type = (world.getBlockMetadata(x, y, z) & 1);
		player.openGui(XActMod.instance, type, world, x, y, z);

		return true;
	}

	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		TileMachine entity = (TileMachine) world.getBlockTileEntity(x, y, z);
		
		if( entity != null )
			for( ItemStack stack : entity.getDropItems() ){
				if( stack != null )
					drop(world, x, y, z, stack);
			}

		super.breakBlock(world, x, y, z, par5, par6);
		world.removeBlockTileEntity(x, y, z);
	}

	private void drop(World world, int x, int y, int z, ItemStack stack) {
		Random random = new Random();
		float var10 = random.nextFloat() * 0.8F + 0.1F;
		float var11 = random.nextFloat() * 0.8F + 0.1F;
		EntityItem item;

		for (float var12 = random.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(item))
		{
			int var13 = random.nextInt(21) + 10;

			if (var13 > stack.stackSize)
			{
				var13 = stack.stackSize;
			}

			stack.stackSize -= var13;
			item = new EntityItem(world, (double)(x + var10), (double)(y + var11), (double)(z + var12), new ItemStack(stack.itemID, var13, stack.getItemDamage()));
			float var15 = 0.05F;
			item.motionX = (double)(random.nextGaussian() * var15);
			item.motionY = (double)(random.nextGaussian() * var15 + 0.2F);
			item.motionZ = (double)(random.nextGaussian() * var15);

			if (stack.hasTagCompound())
			{
				item.item.setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
			}
		}
	}
	
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(this, 1, metadata & MASK_TYPE));
		return list;
	}


	public TileEntity createNewTileEntity(World world, int metadata) {
		int metaType = (metadata & MASK_TYPE);
		if( metaType == 0 )
			return new TileEncoder();
		if( metaType == 1 )
			return new TileCrafter();
		return null;
	}
		
	@Override // does nothing.
	public TileEntity createNewTileEntity(World world) { return null; }


	@SuppressWarnings("unchecked")
	public void addCreativeItems(ArrayList itemList){
		itemList.add(new ItemStack(this, 1, 0)); // encoder
		itemList.add(new ItemStack(this, 1, 1)); // crafter
	}

	@SuppressWarnings("unchecked")
	public void getSubBlocks(int itemID, CreativeTabs tab, List itemList){
		itemList.add(new ItemStack(this, 1, 0)); // encoder
		itemList.add(new ItemStack(this, 1, 1)); // crafter
	}


	private int sideByAngles(EntityPlayer player, int x, int y, int z) {
		double Dx = player.posX - x;
		double Dz = player.posZ - z;
		double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315)
			return 4;
		else if (angle < 135)
			return 2;
		else if (angle < 225)
			return 5;
		else
			return 3;
	}
	
	///////////////
	///// Textures
	
	public String getTextureFile(){
		return XActMod.TEXTURE_BLOCKS;
	}

	// Metadata: stores the machine type and the front side.
	// Textures:
		// [0]  top, front, side, bottom // encoder
		// [16] top, front, side, bottom // crafter
	public int getBlockTextureFromSideAndMetadata(int side, int metadata){
		// top, front, side, bottom.
		int base = (metadata & MASK_TYPE) * 16;
		
		switch (side){
			case 0: // bottom
				return base +3;
			case 1: // top
				return base;
			default:
				if( side == (metadata & MASK_FRONT) >> 1 ) // front
					return base +1;
				else // any other side.
					return base +2;
		}
	}

	private static final int MASK_TYPE = 0x1;
	private static final int MASK_FRONT = 0xE;
	
	/*
	Metadata problem:

	I think i can't use the enum to differentiate between my two machines.
	Maybe i should use metadata to store the type of machine, and also the front side?
	As i currently only use metadata for the front side.


	Possible Solution 1:
	pack both numbers on a string. Use one byte for the machine type, and another for the side.
	I only use two byte because the damage value on ItemStack is stored on NBT as a short (2 bytes).

	According to the solution1, I need to create two masks, of 1 byte length both;
		one for the front orientation, the other for the machine type value.
	
	Meta structure: <byte> <byte> <byte:type> <byte:front>
		Type mask: 0xFF00
		Front mask: 0xFF

	 */
	
}
