package xk.xact.util;


import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet250CustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CustomPacket {

	private ByteArrayOutputStream byteArray;
	private DataOutputStream dataStream;

	public CustomPacket( byte type ) throws IOException {
		this.byteArray = new ByteArrayOutputStream();
		this.dataStream = new DataOutputStream(byteArray);

		dataStream.writeByte(type);
	}

	public CustomPacket add(Object... objects) throws IOException {
		for( Object o : objects ) {
			if( o == null )
				continue;

			if(o instanceof Byte) {
				dataStream.writeByte((Byte) o);
			} else if(o instanceof Short) {
				dataStream.writeShort((Short) o);
			} else if(o instanceof Integer) {
				dataStream.writeInt((Integer) o);
			} else if(o instanceof ItemStack) {
				addItemStack((ItemStack) o);
			}
		}
		return this;
	}

	public Packet250CustomPayload toPacket() {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "xact_channel";
		packet.data = byteArray.toByteArray();
		packet.length = packet.data.length;
		return packet;
	}

	private void addItemStack( ItemStack stack ) throws IOException {
		/*
		ItemStack:
			(short) itemID
			(byte) stackSize
			(short) itemDamage
		NBT:
			(short) nbtLength
			(byte[]) compressedNBT
		 */

		dataStream.writeShort( (short) stack.itemID );
		dataStream.writeByte( (byte) stack.stackSize );
		dataStream.writeShort( (short) stack.getItemDamage() );

		if( stack.hasTagCompound() ) {
			byte[] compressedNBT = CompressedStreamTools.compress(stack.getTagCompound());
			dataStream.writeShort((short)compressedNBT.length);
			dataStream.write(compressedNBT);
		} else {
			dataStream.writeShort(-1);
		}

	}


}
