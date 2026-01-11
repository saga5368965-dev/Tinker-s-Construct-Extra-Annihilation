package saga.ticex_annihilation.registries;

import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.modifier.*;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public class ModifierRegistry {
    public static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TimexAnnihilation.MODID);

    public static final StaticModifier<EternalSupplyModifier> ETERNAL_SUPPLY;
    public static final StaticModifier<PsychoFunnelModifier> PSYCHO_FUNNEL;
    public static final StaticModifier<PsychoShardModifier> PSYCHO_SHARD;
    public static final StaticModifier<HighMegaLinkModifier> HIGH_MEGA_LINK;
    public static final StaticModifier<LongRangeLinkModifier> LONG_RANGE_LINK;
    public static final StaticModifier<PsychoJackModifier> PSYCHO_JACK;

    // --- 新規追記: 電子戦・核封印・サイコミュ基幹 ---
    public static final StaticModifier<MinofskyDriveModifier> MINOFSKY_DRIVE;
    public static final StaticModifier<NeutronJammerModifier> NEUTRON_JAMMER;
    public static final StaticModifier<PsycommuModifier> PSYCOMMU;

    static {
        ETERNAL_SUPPLY = MODIFIERS.register("eternal_supply", EternalSupplyModifier::new);
        PSYCHO_FUNNEL = MODIFIERS.register("psycho_funnel", PsychoFunnelModifier::new);
        PSYCHO_SHARD = MODIFIERS.register("psycho_shard", PsychoShardModifier::new);
        HIGH_MEGA_LINK = MODIFIERS.register("high_mega_link", HighMegaLinkModifier::new);
        LONG_RANGE_LINK = MODIFIERS.register("long_range_link", LongRangeLinkModifier::new);
        PSYCHO_JACK = MODIFIERS.register("psycho_jack", PsychoJackModifier::new);

        // --- 新規登録 ---
        // ミノフスキー・ドライブ：光の翼、広域電波障害
        MINOFSKY_DRIVE = MODIFIERS.register("minofsky_drive", MinofskyDriveModifier::new);
        // ニュートロンジャマー：核・水爆の不発化（携帯・装備型）
        NEUTRON_JAMMER = MODIFIERS.register("neutron_jammer", NeutronJammerModifier::new);
        // サイコミュ：電波障害の完全無効化（すべての前提となる適正）
        PSYCOMMU = MODIFIERS.register("psycommu", PsycommuModifier::new);
    }
}