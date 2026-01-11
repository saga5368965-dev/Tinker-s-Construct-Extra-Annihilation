package saga.ticex_annihilation.registries;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.effect.MinofskyInterferenceEffect;
import saga.ticex_annihilation.effect.NeutronJammerEffect;

public class MobEffectRegistry {
    // MobEffect用のレジストリを作成
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TimexAnnihilation.MODID);

    /**
     * ミノフスキー電波障害
     * - レーダー無効、電子通信（AE2/RS）の遮断を引き起こす
     * - 色: ミノフスキー・ブルー（0x44FFFF）
     */
    public static final RegistryObject<MobEffect> MINOFSKY_INTERFERENCE = EFFECTS.register("minofsky_interference",
            () -> new MinofskyInterferenceEffect(MobEffectCategory.HARMFUL, 0x44FFFF));

    /**
     * ニュートロンジャマー影響下
     * - 核反応の抑制、原子炉の機能不全を象徴するデバフ
     * - 色: セーフティ・パープル（0x9900FF）
     */
    public static final RegistryObject<MobEffect> NEUTRON_JAMMER_ACTIVE = EFFECTS.register("neutron_jammer_active",
            () -> new NeutronJammerEffect(MobEffectCategory.HARMFUL, 0x9900FF));
}