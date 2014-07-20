package io.endertech.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import io.endertech.client.handler.DrawBlockHighlightEventHandler;
import io.endertech.client.handler.KeyBindingHandler;
import io.endertech.client.renderer.SpinningCubeRenderer;
import io.endertech.tile.TileSpinningCube;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerTickerHandlers()
    {
        super.registerTickerHandlers();

        FMLCommonHandler.instance().bus().register(new KeyBindingHandler());
        MinecraftForge.EVENT_BUS.register(new DrawBlockHighlightEventHandler());
    }

    @Override
    public void registerTESRs()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileSpinningCube.class, new SpinningCubeRenderer());
    }
}