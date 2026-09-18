package zank.mods.touhou_little_maid_fusion.huh;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * @author ZZZank
 */
public class GuiMaidFusionController extends GuiMekanismTile<TileMaidFusionController, MekanismTileContainer<TileMaidFusionController>> {
    public GuiMaidFusionController(
        MekanismTileContainer<TileMaidFusionController> container,
        Inventory inv,
        Component title
    ) {
        super(container, inv, title);
    }
}
