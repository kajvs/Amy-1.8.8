package cc.kajus.amy.knockback;

import dev.cobblesword.nachospigot.knockback.KnockbackProfile;

/**
 * Knockback profile implementation - fields, defaults and the YAML keys written
 * by {@link #save()}:
 *
 * <pre>
 *   knockback.profiles.&lt;name&gt;.verticalfriction     = 2.0
 *   knockback.profiles.&lt;name&gt;.horizontalfriction   = 2.0
 *   knockback.profiles.&lt;name&gt;.horizontal           = 0.35
 *   knockback.profiles.&lt;name&gt;.vertical             = 0.35
 *   knockback.profiles.&lt;name&gt;.vertical-limit       = 0.4
 *   knockback.profiles.&lt;name&gt;.extra-horizontal     = 0.425
 *   knockback.profiles.&lt;name&gt;.extra-vertical       = 0.085
 *   knockback.profiles.&lt;name&gt;.misplace-add         = 0.2
 *   knockback.profiles.&lt;name&gt;.misplace-dec         = 0.01
 *   knockback.profiles.&lt;name&gt;.misplace-limit       = 6.0
 * </pre>
 *
 * Note the two friction keys have NO hyphen.
 */
public class CraftKnockbackProfile implements KnockbackProfile {

	private String name;

	private double verticalFriction = 2.0D;
	private double horizontalFriction = 2.0D;
	private double horizontal = 0.35D;
	private double vertical = 0.35D;
	private double verticalLimit = 0.4D;
	private double extraHorizontal = 0.425D;
	private double extraVertical = 0.085D;
	private double misplaceAdd = 0.2D;
	private double misplaceDec = 0.01D;
	private double misplaceLimit = 6.0D;

	public CraftKnockbackProfile(String name) {
		this.name = name;
	}

	@Override
	public void save() {
		final String path = "knockback.profiles." + this.name;

		KnockbackConfig.set(path + ".verticalfriction", this.verticalFriction);
		KnockbackConfig.set(path + ".horizontalfriction", this.horizontalFriction);
		KnockbackConfig.set(path + ".horizontal", this.horizontal);
		KnockbackConfig.set(path + ".vertical", this.vertical);
		KnockbackConfig.set(path + ".vertical-limit", this.verticalLimit);
		KnockbackConfig.set(path + ".extra-horizontal", this.extraHorizontal);
		KnockbackConfig.set(path + ".extra-vertical", this.extraVertical);
		KnockbackConfig.set(path + ".misplace-add", this.misplaceAdd);
		KnockbackConfig.set(path + ".misplace-dec", this.misplaceDec);
		KnockbackConfig.set(path + ".misplace-limit", this.misplaceLimit);

		KnockbackConfig.save();
	}

	@Override
	public String getName() {
		return this.name;
	}

	/** Implementation-only helper (not part of the interface). */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String[] getValues() {
		return new String[] {
				"Vertical Friction: " + this.verticalFriction,
				"Horizontal Friction: " + this.horizontalFriction,
				"Horizontal: " + this.horizontal,
				"Vertical: " + this.vertical,
				"Vertical Limit: " + this.verticalLimit,
				"Extra Horizontal: " + this.extraHorizontal,
				"Extra Vertical: " + this.extraVertical,
				"Misplace Add: " + this.misplaceAdd,
				"Misplace Dec: " + this.misplaceDec,
				"Misplace Limit: " + this.misplaceLimit };
	}

	@Override
	public double getVerticalFriction() {
		return this.verticalFriction;
	}

	@Override
	public void setVerticalFriction(double verticalFriction) {
		this.verticalFriction = verticalFriction;
	}

	@Override
	public double getHorizontalFriction() {
		return this.horizontalFriction;
	}

	@Override
	public void setHorizontalFriction(double horizontalFriction) {
		this.horizontalFriction = horizontalFriction;
	}

	@Override
	public double getHorizontal() {
		return this.horizontal;
	}

	@Override
	public void setHorizontal(double horizontal) {
		this.horizontal = horizontal;
	}

	@Override
	public double getVertical() {
		return this.vertical;
	}

	@Override
	public void setVertical(double vertical) {
		this.vertical = vertical;
	}

	@Override
	public double getVerticalLimit() {
		return this.verticalLimit;
	}

	@Override
	public void setVerticalLimit(double verticalLimit) {
		this.verticalLimit = verticalLimit;
	}

	@Override
	public double getExtraHorizontal() {
		return this.extraHorizontal;
	}

	@Override
	public void setExtraHorizontal(double extraHorizontal) {
		this.extraHorizontal = extraHorizontal;
	}

	@Override
	public double getExtraVertical() {
		return this.extraVertical;
	}

	@Override
	public void setExtraVertical(double extraVertical) {
		this.extraVertical = extraVertical;
	}

	@Override
	public double getMisplaceAdd() {
		return this.misplaceAdd;
	}

	@Override
	public void setMisplaceAdd(double misplaceAdd) {
		this.misplaceAdd = misplaceAdd;
	}

	@Override
	public double getMisplaceDec() {
		return this.misplaceDec;
	}

	@Override
	public void setMisplaceDec(double misplaceDec) {
		this.misplaceDec = misplaceDec;
	}

	@Override
	public double getMisplaceLimit() {
		return this.misplaceLimit;
	}

	@Override
	public void setMisplaceLimit(double misplaceLimit) {
		this.misplaceLimit = misplaceLimit;
	}
}
