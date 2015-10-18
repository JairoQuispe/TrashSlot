package net.blay09.mods.trashslot.coremod;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("TrashSlot")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("net.blay09.mods.trashslot.coremod")
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class TrashSlotLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "net.blay09.mods.trashslot.coremod.GuiContainerClassTransformer",
            "net.blay09.mods.trashslot.coremod.MinecraftClassTransformer",
            "net.blay09.mods.trashslot.coremod.InventoryTweaksClassTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
