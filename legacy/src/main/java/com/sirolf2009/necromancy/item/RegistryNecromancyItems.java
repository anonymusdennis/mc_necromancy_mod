package com.sirolf2009.necromancy.item;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;

public class RegistryNecromancyItems
{
    public static Item apprenticeArmorHead;
    public static Item apprenticeArmorTorso;
    public static Item apprenticeArmorLeggings;
    public static Item apprenticeArmorBoots;
    public static Item genericItems;
    public static Item necronomicon;
    public static Item scythe;
    public static Item scytheBone;
    public static Item bucketBlood;
    public static Item organs;
    public static Item bodyparts;
    public static Item spawner;
    public static Item isaacsHead;

    public static ArmorMaterial isaac = EnumHelper.addArmorMaterial(
            "ISAAC", "necromancy:isaac", Integer.MAX_VALUE, new int[] { 0, 0, 0, 0 }, 0, null, 0);

    public static void initItems()
    {
        genericItems = new ItemGeneric().setUnlocalizedName("ItemNecromancy").setRegistryName("necromancy", "generic");
        register(genericItems);

        necronomicon = new ItemNecronomicon().setUnlocalizedName("Necronomicon").setRegistryName("necromancy", "necronomicon");
        register(necronomicon);

        scythe = new ItemScythe(ItemScythe.toolScythe).setUnlocalizedName("ItemScythe").setRegistryName("necromancy", "scythe");
        register(scythe);

        scytheBone = new ItemScythe(ItemScythe.toolScytheBone).setUnlocalizedName("ItemScytheBone").setRegistryName("necromancy", "scythe_bone");
        register(scytheBone);

        bucketBlood = new ItemBucketBlood(RegistryBlocksNecromancy.blood).setUnlocalizedName("BucketBlood").setRegistryName("necromancy", "bucket_blood");
        register(bucketBlood);

        organs = new ItemOrgans().setUnlocalizedName("Organs").setRegistryName("necromancy", "organs");
        register(organs);

        bodyparts = new ItemBodyPart().setUnlocalizedName("BodyParts").setRegistryName("necromancy", "bodyparts");
        register(bodyparts);

        isaacsHead = new ItemIsaacsHead(isaac, Necromancy.proxy.addArmour("Isaac"), 0)
                .setRegistryName("necromancy", "isaacs_head");
        register(isaacsHead);

        spawner = new ItemSpawner().setUnlocalizedName("NecroSpawner").setRegistryName("necromancy", "spawner");
        register(spawner);
    }

    private static void register(Item item)
    {
        GameRegistry.findRegistry(Item.class).register(item);
    }

    public static void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(necronomicon), new Object[] {
                "LSL", "IBF", "LNL",
                'B', Items.BOOK, 'L', Items.LEATHER, 'S', ItemGeneric.getItemStackFromName("Jar of Blood"),
                'I', new ItemStack(Items.DYE, 1, 0), 'F', Items.FEATHER, 'N', Items.NETHER_WART });
        GameRegistry.addRecipe(ItemGeneric.getItemStackFromName("Bone Needle"), new Object[] {
                "X", 'X', new ItemStack(Items.DYE, 1, 15) });
        GameRegistry.addRecipe(new ItemStack(scythe), new Object[] {
                "IH", " S", " B",
                'I', Blocks.OBSIDIAN, 'H', Items.IRON_HOE, 'S', Items.STICK,
                'B', ItemGeneric.getItemStackFromName("Jar of Blood") });
        GameRegistry.addRecipe(new ItemStack(scytheBone), new Object[] {
                "IH", " S", " B",
                'I', Blocks.OBSIDIAN, 'H', scythe, 'S', Items.BONE, 'B', Items.DIAMOND });
        GameRegistry.addRecipe(ItemGeneric.getItemStackFromName("Brain on a Stick"), new Object[] {
                "# ", " X", '#', Items.FISHING_ROD, 'X', new ItemStack(organs, 1, 0) });
        GameRegistry.addShapelessRecipe(ItemGeneric.getItemStackFromName("Jar of Blood", 8), new Object[] {
                new ItemStack(bucketBlood), Items.GLASS_BOTTLE, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE,
                Items.GLASS_BOTTLE, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE });
        GameRegistry.addShapelessRecipe(new ItemStack(bucketBlood), new Object[] {
                Items.BUCKET,
                ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood"),
                ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood"),
                ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood"),
                ItemGeneric.getItemStackFromName("Jar of Blood"), ItemGeneric.getItemStackFromName("Jar of Blood") });
    }
}
