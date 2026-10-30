package dev.cobblesword.nachospigot.knockback;

/**
 * Knockback profile API - parameter set, names and defaults are 1:1 with the
 * standard 1.8 profile layout:
 *
 * <pre>
 *   getName()                getValues()               save()
 *   getVerticalFriction()    setVerticalFriction(double)
 *   getHorizontalFriction()  setHorizontalFriction(double)
 *   getHorizontal()          setHorizontal(double)
 *   getVertical()            setVertical(double)
 *   getVerticalLimit()       setVerticalLimit(double)
 *   getExtraHorizontal()     setExtraHorizontal(double)
 *   getExtraVertical()       setExtraVertical(double)
 *   getMisplaceAdd()         setMisplaceAdd(double)
 *   getMisplaceDec()         setMisplaceDec(double)
 *   getMisplaceLimit()       setMisplaceLimit(double)
 * </pre>
 *
 * Renamed from the old Amy names: {@code getFrictionHorizontal} -&gt;
 * {@code getHorizontalFriction}, {@code getFrictionVertical} -&gt;
 * {@code getVerticalFriction}, {@code getVerticalMax} -&gt;
 * {@code getVerticalLimit}.
 *
 * Removed from the parameter set: {@code verticalMin},
 * {@code stopSprint}, {@code wtapExtraHorizontal/Vertical},
 * {@code addHorizontal/Vertical}, {@code rod/arrow/pearl/snowball/egg},
 * {@code setName}, {@code save(boolean)}, {@code getKnockbackValues()},
 * {@code getProjectilesValues()}.
 */
public interface KnockbackProfile {

	void save();

	String getName();

	String[] getValues();

	double getVerticalFriction();

	void setVerticalFriction(double verticalFriction);

	double getHorizontalFriction();

	void setHorizontalFriction(double horizontalFriction);

	double getHorizontal();

	void setHorizontal(double horizontal);

	double getVertical();

	void setVertical(double vertical);

	double getVerticalLimit();

	void setVerticalLimit(double verticalLimit);

	double getExtraHorizontal();

	void setExtraHorizontal(double extraHorizontal);

	double getExtraVertical();

	void setExtraVertical(double extraVertical);

	/**
	 * Misplace combo. How much "misplace" a player gains per landed hit. The tracker then
	 * offsets that player's teleport packets per viewer by the accumulated amount, so a player
	 * being comboed appears displaced on the opponent's client. 0 (default) disables it.
	 */
	double getMisplaceAdd();

	void setMisplaceAdd(double misplaceAdd);

	/** Misplace combo: how much misplace is lost per tick once the player stops hitting. */
	double getMisplaceDec();

	void setMisplaceDec(double misplaceDec);

	/** Misplace combo: upper bound on the accumulated misplace, in blocks. */
	double getMisplaceLimit();

	void setMisplaceLimit(double misplaceLimit);
}
