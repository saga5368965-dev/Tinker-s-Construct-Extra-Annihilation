package saga.ticex_annihilation.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SATCConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MINOFSKY_JAMMING;
    public static final ForgeConfigSpec.IntValue MINOFSKY_RANGE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NEUTRON_JAMMER;
    public static final ForgeConfigSpec.IntValue NEUTRON_JAMMER_RANGE;

    static {
        BUILDER.push("General Settings");

        ENABLE_MINOFSKY_JAMMING = BUILDER
                .translation("config.ticex_annihilation.enable_minofsky")
                .define("enableMinofskyJamming", true);

        MINOFSKY_RANGE = BUILDER
                .translation("config.ticex_annihilation.minofsky_range")
                .defineInRange("minofskyRange", 128, 1, 1024);

        ENABLE_NEUTRON_JAMMER = BUILDER
                .translation("config.ticex_annihilation.enable_nj")
                .define("enableNeutronJammer", true);

        NEUTRON_JAMMER_RANGE = BUILDER
                .translation("config.ticex_annihilation.nj_range")
                .defineInRange("neutronJammerRange", 512, 1, 4096);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
