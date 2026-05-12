package com.sirolf2009.necromancy.achievement;

import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

public class AchievementNecromancy
{
    public static Achievement NecronomiconAchieve;
    public static Achievement SewingAchieve;
    public static Achievement SpawnAchieve;

    public static void init()
    {
        NecronomiconAchieve = new Achievement("achievement.necromancy.necronomicon", "necronomicon",
                0, 0, new ItemStack(RegistryNecromancyItems.necronomicon), null).registerStat();
        SewingAchieve = new Achievement("achievement.necromancy.sewing", "sewing",
                1, 0, new ItemStack(RegistryNecromancyItems.bodyparts), NecronomiconAchieve).registerStat();
        SpawnAchieve = new Achievement("achievement.necromancy.spawn", "spawn",
                2, 0, new ItemStack(RegistryNecromancyItems.bodyparts), SewingAchieve).registerStat();
        AchievementPage.registerAchievementPage(new AchievementPage("Necromancy",
                NecronomiconAchieve, SewingAchieve, SpawnAchieve));
    }
}
