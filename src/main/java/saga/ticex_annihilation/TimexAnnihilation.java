package saga.ticex_annihilation;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
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
import saga.ticex_annihilation.client.gui.SATCScreen;
import saga.ticex_annihilation.client.renderer.FunnelRenderer;
import saga.ticex_annihilation.client.renderer.MinofskyDiffuserRenderer;
import saga.ticex_annihilation.client.renderer.NeutronJammerRenderer;
import saga.ticex_annihilation.config.SATCConfig;
import saga.ticex_annihilation.init.MenuInit;
import saga.ticex_annihilation.registries.*;

@Mod(TimexAnnihilation.MODID)
public class TimexAnnihilation {
    public static final String MODID = "ticex_annihilation";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static KeyMapping DEPLOY_KEY;
    public static KeyMapping MODE_KEY;
    public static KeyMapping DRIVE_KEY;

    public TimexAnnihilation() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 0. コンフィグの登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SATCConfig.SPEC);

        // 1. 各種レジストリの登録 (saga.ticex_annihilation.registries パッケージのもの)
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        MenuInit.MENUS.register(modEventBus);
        MobEffectRegistry.EFFECTS.register(modEventBus);
        ModifierRegistry.MODIFIERS.register(modEventBus);

        // 2. カスタムイベントハンドラの登録
        EventRegistry.registerEvents();

        // 3. ライフサイクルイベントのリスナー登録
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onKeyRegister);

        // 4. 汎用イベントバスの登録 (サーバー側処理・ワールドイベント用)
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("S.A.T.C. System: Neural Link established. All weapon systems online.");
    }

    // --- クライアント側イベント（レンダラー登録など） ---
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // エンティティ（ファンネル等）の描画登録
            event.registerEntityRenderer(EntityRegistry.FUNNEL.get(), FunnelRenderer::new);

            // ブロックエンティティ（散布機・ジャマー）の描画登録
            event.registerBlockEntityRenderer(BlockEntityRegistry.MINOFSKY_DIFFUSER.get(),
                    MinofskyDiffuserRenderer::new);
            event.registerBlockEntityRenderer(BlockEntityRegistry.NEUTRON_JAMMER.get(),
                    NeutronJammerRenderer::new);
        }
    }

    // --- 共通セットアップ（パケットハンドラなど） ---
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(saga.ticex_annihilation.network.PacketHandler::init);
    }

    // --- クライアントセットアップ（GUIの登録など） ---
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MenuInit.SATC_MENU.get(), SATCScreen::new);
            LOGGER.info("S.A.T.C. Interface: Combat Screens synchronized.");
        });
    }

    // --- キーバインドの登録 ---
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
}