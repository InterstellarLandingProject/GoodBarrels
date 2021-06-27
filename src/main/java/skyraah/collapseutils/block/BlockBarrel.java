package skyraah.collapseutils.block;

import net.darkhax.bookshelf.block.BlockTileEntity;
import net.darkhax.bookshelf.block.ITileEntityBlock;
import net.darkhax.bookshelf.block.property.PropertyString;
import net.darkhax.bookshelf.registry.IVariant;
import net.darkhax.bookshelf.util.StackUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import skyraah.collapseutils.CollapseUtils;
import skyraah.collapseutils.block.tileentity.TileEntityBarrel;
import skyraah.collapseutils.gui.GuiElement;


/**
 * @author skyraah
 */
public final class BlockBarrel extends BlockTileEntity implements ITileEntityBlock, IVariant {

    public static final String[] VARIANT = {"open", "close"};

    public BlockBarrel() {
        super(Material.WOOD, MapColor.WOOD);
        this.setLightOpacity(0);
        this.setSoundType(SoundType.WOOD);
        this.setHardness(1.7f);
        this.setHarvestLevel("axe", 0);
        if (this.hasTileEntity(this.getStateFromMeta(0))) {
            this.setCreativeTab(CreativeTabs.DECORATIONS);
        }

    }

    public static AxisAlignedBB BARREL_AABB = new AxisAlignedBB(0.128D, 0.0D, 0.128D, 0.872D, 1.0D, 0.872D);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return BARREL_AABB;
    }

/*    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (state == state.withProperty(this.getVariantProp(), VARIANT[1])) {
            drops.add(new ItemStack(CollapseUtils.BARREL_LID));
        }
        drops.add(new ItemStack(CollapseUtils.BARREL, 1, 0));
    }*/

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            if (state == state.withProperty(this.getVariantProp(), VARIANT[0]) && facing == EnumFacing.UP &&
                ItemStack.areItemStacksEqual(playerIn.inventory.getCurrentItem(), new ItemStack(CollapseUtils.BARREL_LID))) {
                playerIn.inventory.getCurrentItem().shrink(1);
                worldIn.setBlockState(pos, state.withProperty(this.getVariantProp(), VARIANT[1]));
                return true;
            } else if (state == state.withProperty(this.getVariantProp(), VARIANT[1]) && facing == EnumFacing.UP && playerIn.isSneaking()) {
                ItemHandlerHelper.giveItemToPlayer(playerIn, new ItemStack(CollapseUtils.BARREL_LID, 1));
                worldIn.setBlockState(pos, state.withProperty(this.getVariantProp(), VARIANT[0]));
                return true;
            } else if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                final TileEntity tileentity = worldIn.getTileEntity(pos);
                if (tileentity instanceof TileEntityBarrel) {
                    playerIn.openGui(CollapseUtils.INSTANCE, GuiElement.GUI_BARREL, worldIn, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public PropertyString getVariantProp() {
        return new PropertyString("variant", VARIANT);
    }

    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityBarrel.class;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBarrel();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {

        return this.getDefaultState().withProperty(this.getVariantProp(), this.VARIANT[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {

        return this.getVariantProp().getMetaData(state.getValue(this.getVariantProp()));
    }

    @Override
    protected BlockStateContainer createBlockState() {

        return new BlockStateContainer(this, new IProperty[]{ this.getVariantProp() });
    }

    @Override
    public String[] getVariant() {

        return this.VARIANT;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityBarrel) {
            final ItemStackHandler inv = ((TileEntityBarrel) tile).inventory;
            for (int i = 0; i < inv.getSlots(); i++) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(i));
            }
        }
        if (state == state.withProperty(this.getVariantProp(), VARIANT[1])) {
            StackUtils.dropStackInWorld(worldIn, pos, new ItemStack(CollapseUtils.BARREL_LID));
        }
        super.breakBlock(worldIn, pos, state);
        worldIn.removeTileEntity(pos);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}

