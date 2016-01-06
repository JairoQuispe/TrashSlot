package net.blay09.mods.trashslot;

import net.blay09.mods.trashslot.client.ClientProxy;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SlotTrash extends Slot {

    private final EntityPlayer entityPlayer;
    private ItemStack itemStack;
    private boolean shouldDeleteMouseStack;

    public SlotTrash(EntityPlayer entityPlayer, int x, int y) {
        super(null, 0, x, y);
        this.entityPlayer = entityPlayer;
    }

    @Override
    public ItemStack getStack() {
        return itemStack;
    }

    @Override
    public boolean getHasStack() {
        return itemStack != null;
    }

    @Override
    public void putStack(ItemStack itemStack) {
        shouldDeleteMouseStack = (this.itemStack != null);
        this.itemStack = itemStack;
    }

    @Override
    public int getSlotStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onSlotChanged() {
        if(itemStack != null && shouldDeleteMouseStack) {
            entityPlayer.inventory.setItemStack(null);
        }
    }

    @Override
    public boolean isItemValid(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        if(itemStack != null) {
            ItemStack returnStack;
            if(itemStack.stackSize <= amount) {
                returnStack = itemStack;
                itemStack = null;
                shouldDeleteMouseStack = true;
                return returnStack;
            } else {
                returnStack = itemStack.splitStack(amount);
                shouldDeleteMouseStack = false;
                if(itemStack.stackSize == 0) {
                    itemStack = null;
                    shouldDeleteMouseStack = true;
                }
                return returnStack;
            }
        }
        return null;
    }

    @Override
    public boolean isHere(IInventory inv, int slotIn) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getBackgroundSprite() {
        return TrashSlot.drawSlotBackground ? ClientProxy.trashSlotIcon : null;
    }
}