package saga.ticex_annihilation.modifier;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import net.minecraft.world.entity.LivingEntity;

public class PsycommuModifier extends Modifier {

    // 装備している間、エンティティのNBTに「適正あり」のフラグを書き込む、
    // またはPsycommuHandlerから直接このModifierのレベルを参照させる

    // ※ここに「ファンネルの操作数増加」や「探知距離延長」のロジックを後で追加可能
}
