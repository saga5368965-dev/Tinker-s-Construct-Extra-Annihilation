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
    public static final StaticModifier<VersatileSmartBeamEffectModifier> VSBR;
    public static final StaticModifier<CalamityInfinityModifier> CALAMITY_INFINITY;

    // 追加：詠唱時間消失とスロット無限
    public static final StaticModifier<InstantCastModifier> INSTANT_CAST;
    public static final StaticModifier<InfinitySlotModifier> INFINITY_SLOT;

    static {
        ETERNAL_SUPPLY = MODIFIERS.register("eternal_supply", EternalSupplyModifier::new);
        PSYCHO_FUNNEL = MODIFIERS.register("psycho_funnel", PsychoFunnelModifier::new);
        PSYCHO_SHARD = MODIFIERS.register("psycho_shard", PsychoShardModifier::new);
        HIGH_MEGA_LINK = MODIFIERS.register("high_mega_link", HighMegaLinkModifier::new);
        LONG_RANGE_LINK = MODIFIERS.register("long_range_link", LongRangeLinkModifier::new);
        PSYCHO_JACK = MODIFIERS.register("psycho_jack", PsychoJackModifier::new);
        VSBR = MODIFIERS.register("vsbr_annihilation", VersatileSmartBeamEffectModifier::new);
        CALAMITY_INFINITY = MODIFIERS.register("calamity_infinity", CalamityInfinityModifier::new);

        // 新規Modifierの登録
        INSTANT_CAST = MODIFIERS.register("instant_cast", InstantCastModifier::new);
        INFINITY_SLOT = MODIFIERS.register("infinity_slot", InfinitySlotModifier::new);
    }
}