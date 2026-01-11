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
    public static final StaticModifier<MinofskyDriveModifier> MINOFSKY_DRIVE;
    public static final StaticModifier<VersatileSmartBeamEffectModifier> VSBR;

    static {
        ETERNAL_SUPPLY = MODIFIERS.register("eternal_supply", EternalSupplyModifier::new);
        PSYCHO_FUNNEL = MODIFIERS.register("psycho_funnel", PsychoFunnelModifier::new);
        PSYCHO_SHARD = MODIFIERS.register("psycho_shard", PsychoShardModifier::new);
        HIGH_MEGA_LINK = MODIFIERS.register("high_mega_link", HighMegaLinkModifier::new);
        LONG_RANGE_LINK = MODIFIERS.register("long_range_link", LongRangeLinkModifier::new);
        PSYCHO_JACK = MODIFIERS.register("psycho_jack", PsychoJackModifier::new);
        MINOFSKY_DRIVE = MODIFIERS.register("minofsky_drive", MinofskyDriveModifier::new);

        // ヴェスバーをレジストリに登録
        VSBR = MODIFIERS.register("vsbr", VersatileSmartBeamEffectModifier::new);
    }
}