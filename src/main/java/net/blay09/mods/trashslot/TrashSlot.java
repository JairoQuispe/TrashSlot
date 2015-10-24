package net.blay09.mods.trashslot;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.blay09.mods.trashslot.net.NetworkHandler;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = TrashSlot.MOD_ID, name = "TrashSlot", acceptableRemoteVersions = "*")
public class TrashSlot {

    public static final String MOD_ID = "trashslot";

    public static boolean drawSlotBackground;
    public static boolean enableDeleteKey;
    public static boolean trashSlotRelative;
    public static float trashSlotX;
    public static float trashSlotY;

    @Mod.Instance
    public static TrashSlot instance;

    @SidedProxy(serverSide = "net.blay09.mods.trashslot.CommonProxy", clientSide = "net.blay09.mods.trashslot.client.ClientProxy")
    public static CommonProxy proxy;

    private Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        drawSlotBackground = config.getBoolean("drawSlotBackground", "general", true, "Set this to false if you don't want the trash can icon to be rendered inside the trash slot.");
        enableDeleteKey = config.getBoolean("enableDeleteKey", "general", true, "Set this to false if you don't want the delete key to delete the item below the mouse cursor.");
        trashSlotRelative = config.getBoolean("trashSlotRelative", "general", false, "Set this to true if you want the position of the trash slot to be relative to the game window.");
        if(trashSlotRelative) {
            trashSlotX = config.getFloat("trashSlotXRelative", "general", 1f, 0f, 1f, "The relative x position of the trash slot (if trashSlotRelative is set to true)");
        } else {
            trashSlotX = config.getInt("trashSlotX", "general", 56, Integer.MIN_VALUE, Integer.MAX_VALUE, "The absolute x position of the trash slot from the center of the window");
        }
        if(trashSlotRelative) {
            trashSlotY = config.getFloat("trashSlotYRelative", "general", 1f, 0f, 1f, "The relative y position of the trash slot (if trashSlotRelative is set to true)");
        } else {
            trashSlotY = config.getInt("trashSlotY", "general", 69, Integer.MIN_VALUE, Integer.MAX_VALUE, "The absolute y position of the trash slot from the center of the window");
        }
        config.save();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkHandler.init();

        proxy.init(event);
    }

    public static boolean canDropStack(boolean result, int mouseX, int mouseY) {
        return proxy.canDropStack(mouseX, mouseY, result);
    }

    public void saveConfig() {
        int j = 8;
        j -= 2;

        System.out.println(j);

        if(trashSlotRelative) {
            config.get("general", "trashSlotXRelative", 1f, "The relative x position of the trash slot (if trashSlotRelative is set to true)").set(TrashSlot.trashSlotX);
            config.get("general", "trashSlotYRelative", 1f, "The relative y position of the trash slot (if trashSlotRelative is set to true)").set(TrashSlot.trashSlotY);
        } else {
            config.get("general", "trashSlotX", 56, "The absolute x position of the trash slot from the center of the screen").set((int) TrashSlot.trashSlotX);
            config.get("general", "trashSlotY", 69, "The absolute y position of the trash slot from the center of the screen").set((int) TrashSlot.trashSlotY);
        }
        config.save();
    }
}
