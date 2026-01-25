package github.amvern.twodimensionalreloaded.client.config;

import github.amvern.twodimensionalreloaded.TwoDimensionalReloaded;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = TwoDimensionalReloaded.MOD_ID)
public class ClientConfig implements ConfigData {
    public boolean renderBlockPlacementGuide = false;
    public boolean renderPlacementOutline = true;
    public static RenderStyle blockRenderMode = RenderStyle.GHOST_BLOCK;

    public enum RenderStyle {
        FULL_BLOCK,
        GHOST_BLOCK
    }

    public int placeableOutlineColor = 0x8000FF00;
    public int nonPlaceableOutlineColor = 0x80FF0000;

    public boolean renderFogEnvironments = true;
}