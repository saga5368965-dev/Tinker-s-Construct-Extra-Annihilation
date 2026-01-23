package saga.ticex_annihilation.registries;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.item.CalamityRingItem;
import saga.ticex_annihilation.item.SATCItem;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TimexAnnihilation.MODID);

    private static final ResourceLocation SATC_ID = new ResourceLocation(TimexAnnihilation.MODID, "satc_item");

    // --- SATC (Super Annihilation Tool: Calamity) ---
    public static final RegistryObject<Item> SATC_ITEM = ITEMS.register("satc_item",
            () -> new SATCItem(new Item.Properties().stacksTo(1).fireResistant(),
                    ToolDefinition.create(SATC_ID)
            ));

    // --- サイコフレーム ---
    public static final RegistryObject<Item> PSYCHO_FRAME = ITEMS.register("psycho_frame",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.ticex_annihilation.psycho_frame.desc")
                            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD));
                    tooltip.add(Component.literal("〜 メビウスの宇宙を越えて 〜")
                            .withStyle(ChatFormatting.AQUA));
                }
            });

    // --- バイオセンサー ---
    public static final RegistryObject<Item> BIO_SENSOR = ITEMS.register("bio_sensor",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
                @Override
                public boolean isFoil(@NotNull ItemStack stack) {
                    return true; // 常にエンチャントのように光らせる
                }

                @Override
                public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.ticex_annihilation.bio_sensor.desc")
                            .withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.literal("〜 水の星に愛をこめて 〜")
                            .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                }
            });

    // --- 厄災の指輪 (Calamity Ring) ---
    public static final RegistryObject<Item> CALAMITY_RING = ITEMS.register("calamity_ring",
            () -> new CalamityRingItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
            ) {
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
                    // 以前作成したヨハネの黙示録関連のキーとも連動
                    tooltip.add(Component.translatable("item.ticex_annihilation.calamity_ring.quote")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                    tooltip.add(Component.translatable("item.ticex_annihilation.calamity_ring.desc")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

                    // 現在のモード表示（UIを開かずとも確認可能に）
                    int mode = ((CalamityRingItem)stack.getItem()).getMode(stack);
                    String modeKey = switch (mode) {
                        case CalamityRingItem.MODE_FAMINE -> "message.ticex_annihilation.mode.equipment";
                        case CalamityRingItem.MODE_CONQUEST -> "message.ticex_annihilation.mode.servant";
                        case CalamityRingItem.MODE_WAR -> "message.ticex_annihilation.mode.war";
                        case CalamityRingItem.MODE_DEATH -> "message.ticex_annihilation.mode.death";
                        default -> "unknown";
                    };
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("message.ticex_annihilation.mode_switch",
                            Component.translatable(modeKey)).withStyle(ChatFormatting.GOLD));
                }

                @Override
                public boolean isFoil(@NotNull ItemStack stack) { return true; }
            });
}