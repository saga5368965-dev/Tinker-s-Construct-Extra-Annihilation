package saga.ticex_annihilation.modifier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import saga.ticex_annihilation.registries.MobEffectRegistry;
import slimeknights.tconstruct.library.modifiers.Modifier;

public class NeutronJammerModifier extends Modifier {
    public NeutronJammerModifier() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        // 自分自身と周囲64ブロックを「核使用不能領域」に定義
        // ロジックはエフェクト(NeutronJammerEffect)が自動で処理する
        event.player.level().getEntitiesOfClass(LivingEntity.class, event.player.getBoundingBox().inflate(64.0)).forEach(target -> {
            target.addEffect(new MobEffectInstance(MobEffectRegistry.NEUTRON_JAMMER_ACTIVE.get(), 100, 0, false, false, true));
        });
    }
}