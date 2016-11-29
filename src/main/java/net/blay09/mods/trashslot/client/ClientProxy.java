package net.blay09.mods.trashslot.client;

import net.blay09.mods.trashslot.CommonProxy;
import net.blay09.mods.trashslot.SlotTrash;
import net.blay09.mods.trashslot.TrashSlot;
import net.blay09.mods.trashslot.net.MessageDelete;
import net.blay09.mods.trashslot.net.MessageHello;
import net.blay09.mods.trashslot.net.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    public static TextureAtlasSprite trashSlotIcon;

    private final KeyBinding keyBindDelete = new KeyBinding("key.trashslot.delete", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_DELETE, "key.categories.trashslot");
    private final KeyBinding keyBindDeleteAll = new KeyBinding("key.trashslot.delete_all", KeyConflictContext.GUI, KeyModifier.SHIFT, Keyboard.KEY_DELETE, "key.categories.trashslot");

    private boolean sentMissingMessage;
    private GuiTrashSlot guiTrashSlot;
    private boolean wasInCreative;
    private boolean neiLoaded;

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ClientRegistry.registerKeyBinding(keyBindDelete);

        MinecraftForge.EVENT_BUS.register(this);
        neiLoaded = Loader.isModLoaded("NotEnoughItems");
    }

    @Override
    public void addScheduledTask(Runnable runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (TrashSlot.isServerSideInstalled && event.getEntity() == Minecraft.getMinecraft().thePlayer) {
            NetworkHandler.instance.sendToServer(new MessageHello());
            if (findSlotTrash(Minecraft.getMinecraft().thePlayer.inventoryContainer) == null) {
                patchContainer(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().thePlayer.inventoryContainer);
            }
        }
    }

    @SubscribeEvent
    public void onOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntityPlayer().openContainer instanceof GuiContainerCreative.ContainerCreative) {
            unpatchContainer(event.getEntityPlayer().inventoryContainer);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
        if (entityPlayer != null) {
            if (!TrashSlot.isServerSideInstalled) {
                if(!sentMissingMessage) {
                    unpatchContainer(entityPlayer.inventoryContainer);
                    entityPlayer.addChatMessage(new TextComponentTranslation("trashslot.serverNotInstalled"));
                    sentMissingMessage = true;
                }
                return;
            }
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (wasInCreative && !(gui instanceof GuiContainerCreative)) {
                if (findSlotTrash(entityPlayer.inventoryContainer) == null) {
                    patchContainer(entityPlayer, entityPlayer.inventoryContainer);
                    if (gui instanceof GuiInventory) {
                        Slot trashSlot = findSlotTrash(((GuiInventory) gui).inventorySlots);
                        if (trashSlot != null) {
                            guiTrashSlot = new GuiTrashSlot((GuiInventory) gui, trashSlot);
                        }
                    }
                }
                wasInCreative = false;
            }
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        trashSlotIcon = event.getMap().registerSprite(new ResourceLocation("trashslot", "items/trashcan"));
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Pre event) {
        if (TrashSlot.isServerSideInstalled && event.getGui() instanceof GuiContainerCreative) {
            unpatchContainer(Minecraft.getMinecraft().thePlayer.inventoryContainer);
            wasInCreative = true;
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (TrashSlot.isServerSideInstalled && event.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory) event.getGui();
            Slot trashSlot = findSlotTrash(gui.inventorySlots);
            if (trashSlot != null) {
                guiTrashSlot = new GuiTrashSlot(gui, trashSlot);
            }
        }
    }

    @SubscribeEvent
    public void onGuiKeyboard(GuiScreenEvent.KeyboardInputEvent.Post event) {
        int keyCode = Keyboard.getEventKey();
        boolean isDelete = keyBindDelete.isActiveAndMatches(keyCode);
        boolean isDeleteAll = keyBindDeleteAll.isActiveAndMatches(keyCode);
        if(TrashSlot.isServerSideInstalled && TrashSlot.enableDeleteKey && Keyboard.getEventKeyState() && (isDelete || isDeleteAll)) {
            EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
            if (entityPlayer != null && entityPlayer.openContainer == entityPlayer.inventoryContainer && event.getGui() instanceof GuiContainer) {
                Slot mouseSlot = ((GuiContainer) event.getGui()).getSlotUnderMouse();
                if (mouseSlot != null && mouseSlot.getHasStack() && ((mouseSlot.inventory == entityPlayer.inventory && mouseSlot.getSlotIndex() < entityPlayer.inventory.getSizeInventory()) || mouseSlot instanceof SlotTrash)) {
                    NetworkHandler.instance.sendToServer(new MessageDelete(mouseSlot.slotNumber, isDeleteAll));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (event.getGui() instanceof GuiInventory) {
            if (neiLoaded) {
                ((GuiInventory) event.getGui()).guiLeft = event.getGui().width / 2 - ((GuiInventory) event.getGui()).xSize / 2;
                ((GuiInventory) event.getGui()).guiTop = event.getGui().height / 2 - ((GuiInventory) event.getGui()).ySize / 2;
            }
            if (guiTrashSlot != null) {
                guiTrashSlot.update(event.getMouseX(), event.getMouseY());
                guiTrashSlot.drawBackground(event.getMouseX(), event.getMouseY());
            }
        }
    }

}
