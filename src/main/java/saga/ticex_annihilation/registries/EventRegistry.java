package saga.ticex_annihilation.registries;

import net.minecraftforge.common.MinecraftForge;
import saga.ticex_annihilation.event.PsychoFunnelEventHandler;
import saga.ticex_annihilation.event.PsychoShardEventHandler;

public class EventRegistry {

    /**
     * すべてのカスタムイベントハンドラをForgeのイベントバスに登録する。
     * メインクラスのコンストラクタから呼び出す。
     */
    public static void registerEvents() {
        // 1. サイコシャード（絶対拘束・オレンジのエリア）
        MinecraftForge.EVENT_BUS.register(PsychoShardEventHandler.class);

        // 2. サイコファンネル（追尾・白い輝き）
        MinecraftForge.EVENT_BUS.register(PsychoFunnelEventHandler.class);

        // 他にイベントクラスが増えたらここに追記していく
    }
}
