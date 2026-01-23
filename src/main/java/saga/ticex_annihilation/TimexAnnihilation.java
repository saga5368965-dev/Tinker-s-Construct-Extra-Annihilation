package saga.ticex_annihilation;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import saga.ticex_annihilation.client.gui.CalamityArmyScreen;
import saga.ticex_annihilation.client.gui.SATCScreen;
import saga.ticex_annihilation.client.renderer.FunnelRenderer;
import saga.ticex_annihilation.client.renderer.ServantRenderer;
import saga.ticex_annihilation.config.SATCConfig;
import saga.ticex_annihilation.init.MenuInit;
import saga.ticex_annihilation.item.CalamityRingItem;
import saga.ticex_annihilation.registries.network.PacketHandler;
import saga.ticex_annihilation.registries.network.OpenCalamityGuiPacket;
import saga.ticex_annihilation.registries.*;

@Mod(TimexAnnihilation.MODID)
public class TimexAnnihilation {
    public static final String MODID = "ticex_annihilation";
    private static final Logger LOGGER = LogUtils.getLogger();

    // キーバインドの定義
    public static KeyMapping DEPLOY_KEY;
    public static KeyMapping MODE_KEY;
    public static KeyMapping DRIVE_KEY;

    public TimexAnnihilation() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // コンフィグ登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SATCConfig.SPEC);

        // 各種レジストリ登録
        ItemRegistry.ITEMS.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        MenuRegistry.MENUS.register(modEventBus); // 以前のMenuInitからMenuRegistryへ統一
        ModifierRegistry.MODIFIERS.register(modEventBus);

        // イベント登録
        EventRegistry.registerEvents();

        // ライフサイクルリスナー
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onKeyRegister);

        // 汎用イベントバス登録（onClientTickなどの検知用）
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("S.A.T.C. System: Neural Link established. All weapon systems online.");
    }

    // --- クライアント側：描画・レンダラー設定 ---
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // ファンネルの描画登録
            event.registerEntityRenderer(EntityRegistry.FUNNEL.get(), FunnelRenderer::new);
            // しもべ（軍勢）の描画登録：OriginalIDに基づき見た目を偽装する
            event.registerEntityRenderer(EntityRegistry.SERVANT_ENTITY.get(), ServantRenderer::new);
        }
    }

    // --- 共通設定：パケットの初期化 ---
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::init);
    }

    // --- クライアント設定：GUI画面の紐付け ---
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 1. S.A.T.C. (別アイテム) の登録
            // MenuInit.SATC_MENU（またはMenuRegistry内のSATC用レジストリ）と紐付け
            MenuScreens.register(MenuInit.SATC_MENU.get(), SATCScreen::new);

            // 2. 厄災の指輪：支配GUI（軍勢装備）の登録
            // CalamityArmyMenu と CalamityArmyScreen を紐付け
            MenuScreens.register(MenuRegistry.CALAMITY_ARMY_MENU.get(), CalamityArmyScreen::new);

            // 3. 厄災の指輪：飢餓GUI（1000スロット）の登録
            // もし「CalamityRingMenu」用の専用Screen（例: CalamityRingScreen）があるなら、ここで登録します。
            // ※ 現状 SATCScreen を入れるとエラーになる場所です
            // MenuScreens.register(MenuRegistry.CALAMITY_RING_MENU.get(), CalamityRingScreen::new);

            LOGGER.info("S.A.T.C. Interface: Combat Screens synchronized.");
        });
    }

    // --- キーバインド登録処理 ---
    private void onKeyRegister(RegisterKeyMappingsEvent event) {
        DEPLOY_KEY = new KeyMapping(
                "key.ticex_annihilation.deploy",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.ticex_annihilation"
        );
        event.register(DEPLOY_KEY);

        MODE_KEY = new KeyMapping(
                "key.ticex_annihilation.change_mode",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.ticex_annihilation"
        );
        event.register(MODE_KEY);

        DRIVE_KEY = new KeyMapping(
                "key.ticex_annihilation.drive_toggle",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.ticex_annihilation"
        );
        event.register(DRIVE_KEY);
    }

    // --- キー入力のリアルタイム検知 ---
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // クライアント側かつ終了フェーズのみ実行
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            // DEPLOY_KEY (Vキー) のクリック判定
            while (DEPLOY_KEY.consumeClick()) {
                ItemStack ring = Minecraft.getInstance().player.getMainHandItem();
                if (ring.getItem() instanceof CalamityRingItem) {
                    // サーバーへGUIオープン要求パケットを送信
                    PacketHandler.sendToServer(new OpenCalamityGuiPacket());
                }
            }
        }
    }
}