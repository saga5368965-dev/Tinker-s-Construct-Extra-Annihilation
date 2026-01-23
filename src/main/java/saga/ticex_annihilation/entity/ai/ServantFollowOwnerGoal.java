package saga.ticex_annihilation.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import saga.ticex_annihilation.entity.ServantEntity;

import java.util.EnumSet;

public class ServantFollowOwnerGoal extends Goal {
    private final ServantEntity mob;
    private LivingEntity owner;
    private final double speed;
    private final float startDist;
    private final float stopDist;
    private final PathNavigation navigation;

    public ServantFollowOwnerGoal(ServantEntity mob, double speed, float startDist, float stopDist) {
        this.mob = mob;
        this.speed = speed;
        this.startDist = startDist;
        this.stopDist = stopDist;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // オーナーを取得（ServantEntityに実装したgetOwnerを想定）
        LivingEntity livingentity = mob.getOwner();
        if (livingentity == null) return false;
        if (livingentity.isSpectator()) return false;
        // モードが「追従(1)」の時だけ動作
        if (mob.getMode() != 1) return false;
        if (this.mob.distanceToSqr(livingentity) < (double)(this.startDist * this.startDist)) return false;

        this.owner = livingentity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) return false;
        if (mob.getMode() != 1) return false;
        return !(this.mob.distanceToSqr(this.owner) <= (double)(this.stopDist * this.stopDist));
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float)this.mob.getMaxHeadXRot());
        this.navigation.moveTo(this.owner, this.speed);
    }
}
