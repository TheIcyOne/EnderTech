package io.endertech.multiblock.tile;

import cpw.mods.fml.common.registry.GameRegistry;
import io.endertech.block.ETBlocks;
import io.endertech.gui.client.GuiTank;
import io.endertech.gui.container.ContainerTank;
import io.endertech.multiblock.MultiblockControllerBase;
import io.endertech.multiblock.MultiblockValidationException;
import io.endertech.multiblock.block.BlockTankController;
import io.endertech.multiblock.block.BlockTankPart;
import io.endertech.multiblock.controller.ControllerTank;
import io.endertech.network.PacketETBase;
import io.endertech.reference.Strings;
import io.endertech.util.BlockCoord;
import io.endertech.util.IOutlineDrawer;
import io.endertech.util.helper.LocalisationHelper;
import io.endertech.util.helper.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;

public class TileTankPart extends TileTankPartBase implements IOutlineDrawer
{
    public TileTankPart()
    {
        super();
    }

    public static void init()
    {
        GameRegistry.registerTileEntity(TileTankPart.class, "tile.endertech." + Strings.Blocks.TANK_PART_NAME);
    }

    @Override
    public void isGoodForFrame() throws MultiblockValidationException
    {
        int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        if (BlockTankPart.isFrame(metadata)) { return; }

        throw new MultiblockValidationException(LocalisationHelper.localiseString("info.multiblock.tank.part.unsuitable.frame", xCoord, yCoord, zCoord));
    }

    @Override
    public void isGoodForSides() throws MultiblockValidationException
    {
        // All parts are valid for sides, by default
    }

    @Override
    public void isGoodForTop() throws MultiblockValidationException
    {
        int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        if (BlockTankPart.isFrame(metadata)) { return; }

        throw new MultiblockValidationException(LocalisationHelper.localiseString("info.multiblock.tank.part.unsuitable.top", xCoord, yCoord, zCoord));
    }

    @Override
    public void isGoodForBottom() throws MultiblockValidationException
    {
        int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        if (BlockTankPart.isFrame(metadata)) { return; }

        throw new MultiblockValidationException(LocalisationHelper.localiseString("info.multiblock.tank.part.unsuitable.bottom", xCoord, yCoord, zCoord));
    }

    @Override
    public void isGoodForInterior() throws MultiblockValidationException
    {
        throw new MultiblockValidationException(LocalisationHelper.localiseString("info.multiblock.tank.part.unsuitable.interior", xCoord, yCoord, zCoord));
    }

    @Override
    public void onMachineActivated()
    {
        if (this.worldObj.isRemote) { return; }

        if (getBlockType() == ETBlocks.blockTankController)
        {
            int metadata = this.getBlockMetadata();
            if (BlockTankController.isController(metadata))
            {
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankController.CONTROLLER_ACTIVE, 2);
            }
        }
    }

    @Override
    public void onMachineDeactivated()
    {
        if (this.worldObj.isRemote) { return; }

        if (getBlockType() == ETBlocks.blockTankController)
        {
            int metadata = this.getBlockMetadata();
            if (BlockTankController.isController(metadata))
            {
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankController.CONTROLLER_IDLE, 2);
            }
        }
    }

    @Override
    public void onMachineAssembled(MultiblockControllerBase multiblockController)
    {
        super.onMachineAssembled(multiblockController);

        if (this.worldObj.isRemote) { return; }
        if (multiblockController == null)
        {
            throw new IllegalArgumentException("Being assembled into a null controller. This should never happen. Please report this stacktrace to http://github.com/Drayshak/EnderTech/");
        }

        if (this.getMultiblockController() == null)
        {
            LogHelper.warn(LocalisationHelper.localiseString("warning.multiblock.tank.null_controller_assembly", xCoord, yCoord, zCoord));
            this.onAttached(multiblockController);
        }

        int metadata = this.getBlockMetadata();
        Block blockType = this.getBlockType();
        if (blockType == ETBlocks.blockTankPart)
        {
            if (BlockTankPart.isFrame(metadata))
            {
                this.setCasingMetadataBasedOnWorldPosition();
            }
        } else if (blockType == ETBlocks.blockTankController)
        {
            if (BlockTankController.isController(metadata))
            {
                // This is called during world loading as well, so controllers can start active.
                if (!this.getTankController().isActive())
                {
                    this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankController.CONTROLLER_IDLE, 2);
                } else
                {
                    this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankController.CONTROLLER_ACTIVE, 2);
                }
            }
        }
    }

    @Override
    public void onMachineBroken()
    {
        super.onMachineBroken();

        if (this.worldObj.isRemote) { return; }

        if (getBlockType() == ETBlocks.blockTankPart)
        {
            int metadata = this.getBlockMetadata();
            if (BlockTankPart.isFrame(metadata))
            {
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_METADATA_BASE, 2);
            }
        }
    }

    private void setCasingMetadataBasedOnWorldPosition()
    {
        MultiblockControllerBase controller = this.getMultiblockController();
        assert (controller != null);
        BlockCoord minCoord = controller.getMinimumCoord();
        BlockCoord maxCoord = controller.getMaximumCoord();

        int extremes = 0;
        boolean xExtreme, yExtreme, zExtreme;
        xExtreme = yExtreme = zExtreme = false;

        if (xCoord == minCoord.x)
        {
            extremes++;
            xExtreme = true;
        }
        if (yCoord == minCoord.y)
        {
            extremes++;
            yExtreme = true;
        }
        if (zCoord == minCoord.z)
        {
            extremes++;
            zExtreme = true;
        }

        if (xCoord == maxCoord.x)
        {
            extremes++;
            xExtreme = true;
        }
        if (yCoord == maxCoord.y)
        {
            extremes++;
            yExtreme = true;
        }
        if (zCoord == maxCoord.z)
        {
            extremes++;
            zExtreme = true;
        }

        if (extremes == 3)
        {
            // Corner
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_CORNER, 2);
        } else if (extremes == 2)
        {
            if (!xExtreme)
            {
                // Y/Z - must be east/west
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_EASTWEST, 2);
            } else if (!zExtreme)
            {
                // X/Y - must be north-south
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_NORTHSOUTH, 2);
            } else
            {
                // Not a y-extreme, must be vertical
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_VERTICAL, 2);
            }
        } else if (extremes == 1)
        {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_CENTER, 2);
        } else
        {
            // This shouldn't happen.
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockTankPart.FRAME_METADATA_BASE, 2);
        }
    }

    @Override
    public PacketETBase getPacket()
    {
        PacketETBase tileBasePacket = super.getPacket();
        boolean isConnectedAndSaveDelegate = isConnected() && isMultiblockSaveDelegate();
        tileBasePacket.addBool(isConnectedAndSaveDelegate);

        if (isConnectedAndSaveDelegate)
        {
            return this.getTankController().getPacket(tileBasePacket);
        } else
        {
            return tileBasePacket;
        }
    }

    @Override
    public void handleTilePacket(PacketETBase packetETBase, boolean isServer)
    {
        super.handleTilePacket(packetETBase, isServer);
        boolean tileWasConnectedAndWasDelegate = packetETBase.getBool();

        if (!isServer)
        {
            if (!tileWasConnectedAndWasDelegate)
            {
                //                LogHelper.info("Tile sent a packet despite not being connected and the save delegate.");
                return;
            }

            ControllerTank controller = this.getTankController();
            if (controller == null)
            {
                this.cachedMultiblockPacket = packetETBase;
                //                                LogHelper.info("Caching message for tank part tile: " + this.xCoord + ", " + this.yCoord + ", " + this.zCoord);
            } else
            {
                controller.handleTilePacket(packetETBase, isServer);
                //                LogHelper.info("Decoding message for controller: " + tile.xCoord + ", " + tile.yCoord + ", " + tile.zCoord);
            }
        }
    }

    protected boolean canInteractFromDirection(ForgeDirection from)
    {
        return (isConnected() && this.getTankController().isAssembled() && getOutwardsDir().contains(from));
    }

    protected void updateOutwardNeighbours()
    {
        worldObj.notifyBlockChange(xCoord, yCoord, zCoord, this.getBlockType());
        worldObj.func_147453_f(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
    }

    @Override
    public Object getGuiClient(InventoryPlayer inventory)
    {
        return new GuiTank(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory)
    {
        return new ContainerTank(inventory, this);
    }

    @Override
    public boolean hasGui()
    {
        return this.isConnected() && this.getMultiblockController().isAssembled();
    }
}
