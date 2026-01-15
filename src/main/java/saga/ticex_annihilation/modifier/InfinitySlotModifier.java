package saga.ticex_annihilation.modifier;

import net.minecraftforge.fml.ModList;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

public class InfinitySlotModifier extends Modifier implements VolatileDataModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
        // Re:Avaritia導入時のみ、volatileDataに対してスロット追加を実行
        if (ModList.get().isLoaded("reavaritia")) {
            volatileData.addSlots(SlotType.UPGRADE, 99);
            volatileData.addSlots(SlotType.ABILITY, 99);
            volatileData.addSlots(SlotType.DEFENSE, 99);
            volatileData.addSlots(SlotType.SOUL, 99);
        }
    }
}