package saga.ticex_annihilation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import java.util.Collections;
import java.util.List;

public class MinofskyInterferenceEffect extends MobEffect {
    public MinofskyInterferenceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * 解除アイテム（牛乳など）のリストを「空」にして返すことで、
     * システム的に解除不可能なデバフにします。
     */
    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }
}
