package saga.ticex_annihilation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import java.util.Collections;
import java.util.List;

/**
 * ニュートロンジャマー状態を定義するクラス。
 * 自身でイベントを登録せず、JammingEventHandlerに処理を任せることで永続バグを回避。
 */
public class NeutronJammerEffect extends MobEffect {
    public NeutronJammerEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        // 牛乳（デバフ無効化アイテム）では絶対に解除できない仕様
        return Collections.emptyList();
    }
}