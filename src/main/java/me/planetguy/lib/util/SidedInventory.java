package me.planetguy.lib.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;


//Credit to Buildcraft for details
public class SidedInventory implements ISidedInventory {
	
	//Returns an ISidedInventory from the object, wrapping IInventories
	public static ISidedInventory get(Object o) {
		if(o instanceof ISidedInventory) {
			return (ISidedInventory) o;
		} else if(o instanceof IInventory) {
			return new SidedInventory((IInventory) o);
		} else {
			return null;
		}
	}
	
	private final IInventory inv;
	
	private final int[] slots;
	
	private SidedInventory(IInventory realObj) {
		this.inv=realObj;
		slots=new int[getSizeInventory()];
		for(int i=0; i<slots.length; i++) {
			slots[i]=i;
		}
	}
	
	@Override
	public int getSizeInventory() {return inv.getSizeInventory();}

	@Override
	public ItemStack getStackInSlot(int p_70301_1_) {return inv.getStackInSlot(p_70301_1_);}

	@Override
	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) { return inv.decrStackSize(p_70298_1_, p_70298_2_);}

	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_) { return inv.getStackInSlotOnClosing(p_70304_1_);}

	@Override
	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {inv.setInventorySlotContents(p_70299_1_, p_70299_2_);}

	@Override
	public String getInventoryName() {return inv.getInventoryName();}

	@Override
	public boolean hasCustomInventoryName() {return inv.hasCustomInventoryName();}

	@Override
	public int getInventoryStackLimit() {return inv.getInventoryStackLimit();}

	@Override
	public void markDirty() {inv.markDirty();}

	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {return inv.isUseableByPlayer(p_70300_1_);}

	@Override
	public void openInventory() {inv.openInventory();}

	@Override
	public void closeInventory() {inv.closeInventory();}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) { return inv.isItemValidForSlot(p_94041_1_, p_94041_2_);}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return slots;
	}

	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_,
			int p_102007_3_) {
		return isItemValidForSlot(p_102007_1_, p_102007_2_);
	}

	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_,
			int p_102008_3_) {
		return true;
	}

}
