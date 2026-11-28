package com.dmzscaling;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;

/**
 * Registers /dmzscaling commands:
 * - /dmzscaling status  -> shows player's DMZ BASE power and live scaling preview for Mob / Miniboss / Boss
 * - /dmzscaling bosses  -> shows surviving Apotheosis BossRegistry counts per dimension
 * - /dmzscaling rescale -> forces immediate re-scaling of all Bosses & Mini-Bosses within 128 blocks
 */
public final class DmzScalingCommands {

    private DmzScalingCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.m_82127_("dmzscaling")
                        .executes(ctx -> executeStatus(ctx.getSource()))
                        .then(Commands.m_82127_("status")
                                .executes(ctx -> executeStatus(ctx.getSource())))
                        .then(Commands.m_82127_("bosses")
                                .executes(ctx -> executeBosses(ctx.getSource())))
                        .then(Commands.m_82127_("rescale")
                                .requires(src -> src.m_6761_(2))
                                .executes(ctx -> executeRescale(ctx.getSource())))
        );
    }

    private static int executeStatus(CommandSourceStack source) {
        ServerPlayer player = source.m_230896_();
        DmzScalingHelpers.PlayerBasePower power = (player != null)
                ? VanillaScalingEvents.readDmzBasePower(player)
                : DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT;

        DmzScalingHelpers.ComputedScaling mob = DmzScalingHelpers.computeVanillaMobScaling(power);
        DmzScalingHelpers.ComputedScaling warden = DmzScalingHelpers.computeVanillaMinibossScaling(500.0, power);
        DmzScalingHelpers.ComputedScaling dragon = DmzScalingHelpers.computeVanillaBossScaling(200.0, power);
        DmzScalingHelpers.ComputedScaling wither = DmzScalingHelpers.computeVanillaBossScaling(300.0, power);

        String header = String.format(
                "§5=== [DMZ Apothic & Vanilla Scaling v1.1.0] ===\n"
                        + "§7Player BASE Power: §eLv.%d §8| §cBase MaxHP: %.0f §8| §6Base Offense: %.1f\n"
                        + "§7Party Mode: §b%s §8(+%.0f%% HP/extra player, cap %.1fx)\n"
                        + "§7- §aHostile Mob (20 HP): §f%.0f HP §8(x%.2f HP | x%.2f DMG | +%.1f Armor)\n"
                        + "§7- §bWarden Mini-Boss (500 HP): §f%,.0f HP §8(x%.2f HP | x%.2f DMG | Cap %.0f%%)\n"
                        + "§7- §dEnder Dragon (200 HP): §f%,.0f HP §8(x%.2f HP | x%.2f DMG | Cap %.0f%% | DoT %.0f%%)\n"
                        + "§7- §dWither Boss (300 HP): §f%,.0f HP §8(x%.2f HP | x%.2f DMG | Regen Scaled: %s)",
                power.dmzLevel(), power.baseMaxHp(), power.baseOffense(),
                DmzScalingConfig.getPartyScalingMode(),
                DmzScalingConfig.getPartyBonusHpPerExtraPlayer() * 100.0,
                DmzScalingConfig.getMaxPartyHpBonusMultiplier(),
                20.0 * mob.hpMultiplier(), mob.hpMultiplier(), mob.damageMultiplier(), mob.bonusArmor(),
                500.0 * warden.hpMultiplier(), warden.hpMultiplier(), warden.damageMultiplier(),
                DmzScalingConfig.getMinibossMaxHitPct() * 100.0,
                200.0 * dragon.hpMultiplier(), dragon.hpMultiplier(), dragon.damageMultiplier(),
                DmzScalingConfig.getBossMaxHitPct() * 100.0, DmzScalingConfig.getBossMaxDotHitPct() * 100.0,
                300.0 * wither.hpMultiplier(), wither.hpMultiplier(), wither.damageMultiplier(),
                DmzScalingConfig.isScaleWitherPassiveRegen() ? "ON" : "OFF"
        );
        source.m_288197_(() -> Component.m_237113_(header), false);
        return 1;
    }

    private static int executeBosses(CommandSourceStack source) {
        Map<String, Integer> byDim = DmzScalingHelpers.getLastLoadedBossCountsByDimension();
        int total = DmzScalingHelpers.getLastLoadedBossTotal();
        StringBuilder sb = new StringBuilder();
        sb.append("§5=== [Apotheosis BossRegistry Status] ===\n");
        sb.append("§7Total active Apotheosis boss definitions: §e").append(total).append("\n");
        if (byDim.isEmpty()) {
            sb.append("§8(No Apotheosis BossRegistry entries recorded or running in standalone mode without Apotheosis.)");
        } else {
            for (Map.Entry<String, Integer> e : byDim.entrySet()) {
                sb.append("§7- §b").append(e.getKey()).append("§7: §a").append(e.getValue()).append(" bosses\n");
            }
        }
        String out = sb.toString().trim();
        source.m_288197_(() -> Component.m_237113_(out), false);
        return 1;
    }

    private static int executeRescale(CommandSourceStack source) {
        ServerLevel level = source.m_81372_();
        Vec3 pos = source.m_81371_();
        AABB box = new AABB(pos.f_82479_ - 128.0, pos.f_82480_ - 128.0, pos.f_82481_ - 128.0,
                pos.f_82479_ + 128.0, pos.f_82480_ + 128.0, pos.f_82481_ + 128.0);
        List<LivingEntity> entities = level.m_45976_(LivingEntity.class, box);
        int rescaled = 0;
        for (LivingEntity living : entities) {
            ResourceLocation key = EntityType.m_20613_(living.m_6095_());
            if (key == null) {
                continue;
            }
            DmzScalingHelpers.ScalingTier tier = VanillaScalingEvents.classifyEntity(living, key.toString());
            if (tier == DmzScalingHelpers.ScalingTier.VANILLA_BOSS || tier == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS) {
                VanillaScalingEvents.applyOrRefreshScaling(level, living, tier, true);
                rescaled++;
            }
        }
        int finalCount = rescaled;
        source.m_288197_(() -> Component.m_237113_(
                "§5[DMZ Scaling] §aRe-scaled §e" + finalCount + " §aboss(es)/mini-boss(es) within 128 blocks."
        ), true);
        return finalCount;
    }
}
