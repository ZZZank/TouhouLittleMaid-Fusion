package zank.mods.touhou_little_maid_fusion.huh;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

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
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        // area based the fluid tank, but 36 pixels wider
        addRenderableWidget(new GuiInnerScreen(
            this,
            48 - 36,
            18,
            64 + 36,
            48,
            this::getMaidStatusText
        ));
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
            GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getLastEnergyProduced())),
            MekanismLang.CAPACITY.translate(EnergyDisplay.of(tile.getEnergyContainer().getMaxEnergy()))
        )));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
    }

    private List<Component> getMaidStatusText() {
        List<Component> lines = new ArrayList<>();
        
        if (tile.getPinnedMaid() != null) {
            var maid = tile.getPinnedMaid();
            if (maid != null) {
                lines.add(maid.getDisplayName().copy().withStyle(ChatFormatting.GREEN));
                lines.add(Component.translatable("gui.touhou_little_maid_fusion.favorability", maid.getFavorability()).withStyle(ChatFormatting.YELLOW));
            } else {
                lines.add(Component.translatable("gui.touhou_little_maid_fusion.maid_pinned").withStyle(ChatFormatting.GREEN));
            }
        } else {
            lines.add(Component.translatable("gui.touhou_little_maid_fusion.no_maid")
                .withStyle(ChatFormatting.GRAY));
        }
        
        return lines;
    }
}