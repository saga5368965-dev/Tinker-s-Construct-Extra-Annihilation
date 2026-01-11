package saga.ticex_annihilation.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class PsychoShardModifier extends Modifier {

    // 1. 名前が表示されない対策：@Overrideを明記し言語ファイルと紐付け
    @Override
    public @NotNull Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.psycho_shard");
    }

    // 2. ツールチップ表示：画像にあった「description」を表示させるための修正
    public void addInformation(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        // 画像で見えていた .description キーを金色で表示
        tooltip.add(Component.translatable("modifier.ticex_annihilation.psycho_shard.description").withStyle(ChatFormatting.GOLD));
    }

    public int afterEntityHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target != null && !target.level().isClientSide) {
            // 直撃：永続
            target.getPersistentData().putBoolean("PsychoShardPermanent", true);
            // 周囲：30秒 (600tick)
            applyAreaLock(target, 20.0D, context.getAttacker());
        }
        return 0;
    }

    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, int toolkitIndex) {
        // 防具として「正しく装備されている」時のみ動作
        if (!level.isClientSide && isCorrectSlot) {
            applyAreaLock(holder, 100.0D, holder);
        }
    }

    private void applyAreaLock(LivingEntity centerEntity, double radius, LivingEntity attacker) {
        AABB area = centerEntity.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = centerEntity.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : targets) {
            if (entity == attacker || !entity.isAlive()) continue;

            // 永続フラグがない場合のみタイマー設定
            if (!entity.getPersistentData().getBoolean("PsychoShardPermanent")) {
                entity.getPersistentData().putInt("PsychoShardTimer", 600);
            }
        }
    }
}