package cc.kajus.amy.hitdetection;

/**
 * Helper for the misplace combo: computes the yaw angle between two positions so the teleport
 * a viewer receives for a comboed player can be pushed along that angle.
 */
public class MisplaceHandler {

	public static float getAngle(final double posX, final double posZ, final double locX, final double locZ) {
		final double x = posX - locX;
		final double z = posZ - locZ;
		float newYaw = (float) Math.toDegrees(-Math.atan(x / z));
		if (z < 0.0 && x < 0.0) {
			newYaw = (float) (90.0 + Math.toDegrees(Math.atan(z / x)));
		} else if (z < 0.0 && x > 0.0) {
			newYaw = (float) (-90.0 + Math.toDegrees(Math.atan(z / x)));
		}
		return newYaw;
	}
}
