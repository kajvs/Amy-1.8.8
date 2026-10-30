package cc.kajus.amy.event.potion;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;

/**
 * Called when a potion effect on an entity runs out. Cancelling the event extends the effect (with
 * a practically infinite duration unless {@link #setDuration(int)} says otherwise).
 *
 * <p>This folds together CraftBukkit's {@code PotionEffectRemoveEvent} and the potion expire
 * event: this fork's CraftBukkit does not have that event (the whole potion event family came
 * with other forks' patches), so the handler list, the {@code duration} semantics and the
 * "cancel means extend" behaviour live in this one class.</p>
 *
 * <p>Fired from {@code EntityLiving#bi()} right before an expired effect would be removed, which is
 * what lets the plugin's {@code PvPClass} put a bigger potion back when a smaller one runs out.</p>
 */
public class PotionEffectExpireEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final LivingEntity entity;
    private final PotionEffect effect;

    private int duration = 0;

    public PotionEffectExpireEvent(LivingEntity entity, PotionEffect effect) {
        this.entity = entity;
        this.effect = effect;
    }

    /** The entity the effect is expiring on. */
    public LivingEntity getEntity() {
        return this.entity;
    }

    /** The effect that is expiring. */
    public PotionEffect getEffect() {
        return this.effect;
    }

    /**
     * Get the new duration for the potion effect. This is initially 0.
     */
    public int getDuration() {
        return this.duration;
    }

    /**
     * Set a new duration for the potion effect. Passing 0 to this method un-cancels the event, and
     * passing anything above 0 cancels it.
     */
    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    @Override
    public boolean isCancelled() {
        return this.duration > 0;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.duration = cancel ? Integer.MAX_VALUE : 0;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
