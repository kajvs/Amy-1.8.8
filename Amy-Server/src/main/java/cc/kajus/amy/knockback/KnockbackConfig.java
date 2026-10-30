package cc.kajus.amy.knockback;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sugarcanemc.sugarcane.util.yaml.YamlCommenter;

import com.google.common.base.Throwables;
import cc.kajus.amy.Amy;

import dev.cobblesword.nachospigot.knockback.KnockbackProfile;

/**
 * Knockback configuration.
 *
 * <p>
 * One profile is seeded from {@link #BUILT_IN_PROFILES} on first start: Default, which carries
 * the Lunar values. Every value below can then be edited per profile in knockback.yml, or
 * in-game with {@code /kb}. Each profile carries these values:
 *
 * <pre>
 *   verticalfriction   = per-profile
 *   horizontalfriction = per-profile
 *   horizontal         = per-profile
 *   vertical           = per-profile
 *   vertical-limit     = per-profile
 *   extra-horizontal   = per-profile
 *   extra-vertical     = per-profile
 *   misplace-add       = 0.2
 *   misplace-dec       = 0.01
 *   misplace-limit     = 6.0
 * </pre>
 *
 * The per-projectile values, wtap-extra-*, add-*, stop-sprint and vertical-min
 * that the old Amy profiles carried are gone.
 */
public class KnockbackConfig {
	private static final Logger LOGGER = LogManager.getLogger(KnockbackConfig.class);
	private static File CONFIG_FILE;
	protected static final YamlCommenter c = new YamlCommenter();
	private static final String HEADER = "This is the knockback configuration file for Amy.\n"
			+ "Each profile carries 10 values; see the class javadoc of KnockbackConfig for the list.";
	static YamlConfiguration config;

	private static volatile KnockbackProfile currentKb;
	private static volatile Set<KnockbackProfile> kbProfiles = new HashSet<>();

	/** Profile used when knockback.current is missing or points at an unknown name. */
	private static final String DEFAULT_PROFILE = "Default";

	/**
	 * Built-in profiles, seeded into knockback.yml on first start. The generic loading pass
	 * below then lets the file override every value.
	 * <p>
	 * Columns: name, verticalfriction, horizontalfriction, horizontal, vertical,
	 * vertical-limit, extra-horizontal, extra-vertical.
	 */
	private static final Object[][] BUILT_IN_PROFILES = {
			{ "Default", 2.5D,    2.5D,    0.53D,  0.5D,   0.36D,     0.435D, 0.0D },
	};

	public static void init(File configFile) {
		CONFIG_FILE = configFile;
		config = new YamlConfiguration();
		try {
			Amy.LOGGER.info("Loading Amy knockback config from " + configFile.getName());
			config.load(CONFIG_FILE);
		} catch (IOException ignored) {
		} catch (InvalidConfigurationException ex) {
			LOGGER.log(Level.ERROR, "Could not load knockback.yml, please correct your syntax errors", ex);
			throw Throwables.propagate(ex);
		}
		config.options().copyDefaults(true);
		c.setHeader(HEADER);

		// Seed the built-in profiles before the generic loading pass below.
		kbProfiles = new HashSet<>();
		for (Object[] values : BUILT_IN_PROFILES) {
			CraftKnockbackProfile profile = new CraftKnockbackProfile((String) values[0]);
			profile.setVerticalFriction((Double) values[1]);
			profile.setHorizontalFriction((Double) values[2]);
			profile.setHorizontal((Double) values[3]);
			profile.setVertical((Double) values[4]);
			profile.setVerticalLimit((Double) values[5]);
			profile.setExtraHorizontal((Double) values[6]);
			profile.setExtraVertical((Double) values[7]);
			kbProfiles.add(profile);
		}

		// Make sure they exist in the file before the generic loading pass below.
		Set<String> keys = getKeys("knockback.profiles");
		for (Object[] values : BUILT_IN_PROFILES) {
			if (!keys.contains((String) values[0])) {
				getKbProfileByName((String) values[0]).save();
				keys = getKeys("knockback.profiles");
			}
		}

		for (String key : keys) {
			final String path = "knockback.profiles." + key;
			CraftKnockbackProfile profile = (CraftKnockbackProfile) getKbProfileByName(key);
			if (profile == null) {
				profile = new CraftKnockbackProfile(key);
				kbProfiles.add(profile);
			}

			profile.setVerticalFriction(getDouble(path + ".verticalfriction", 2.0D));
			profile.setHorizontalFriction(getDouble(path + ".horizontalfriction", 2.0D));
			profile.setHorizontal(getDouble(path + ".horizontal", 0.35D));
			profile.setVertical(getDouble(path + ".vertical", 0.35D));
			profile.setVerticalLimit(getDouble(path + ".vertical-limit", 0.4D));
			profile.setExtraHorizontal(getDouble(path + ".extra-horizontal", 0.425D));
			profile.setExtraVertical(getDouble(path + ".extra-vertical", 0.085D));
			profile.setMisplaceAdd(getDouble(path + ".misplace-add", 0.2D));
			profile.setMisplaceDec(getDouble(path + ".misplace-dec", 0.01D));
			profile.setMisplaceLimit(getDouble(path + ".misplace-limit", 6.0D));
		}

		currentKb = getKbProfileByName(getString("knockback.current", DEFAULT_PROFILE));
		if (currentKb == null) {
			Amy.LOGGER.warn("Knockback profile selected was not found, using profile '" + DEFAULT_PROFILE + "' for now!");
			currentKb = getKbProfileByName(DEFAULT_PROFILE);
			set("knockback.current", DEFAULT_PROFILE);
		}
		save();
	}

	public static KnockbackProfile getCurrentKb() {
		if (currentKb == null) {
			setCurrentKb(getKbProfileByName(DEFAULT_PROFILE));
		}
		return currentKb;
	}

	public static void setCurrentKb(KnockbackProfile kb) {
		currentKb = kb;
	}

	public static KnockbackProfile getKbProfileByName(String name) {
		for (KnockbackProfile profile : kbProfiles) {
			if (profile.getName().equalsIgnoreCase(name)) {
				return profile;
			}
		}
		return null;
	}

	public static Set<KnockbackProfile> getKbProfiles() {
		return kbProfiles;
	}

	public static void save() {
		try {
			config.save(CONFIG_FILE);
		} catch (IOException ex) {
			LOGGER.log(Level.ERROR, "Could not save " + CONFIG_FILE, ex);
		}
	}

	public static void set(String path, Object val) {
		config.set(path, val);

		save();
	}

	public static Set<String> getKeys(String path) {
		if (!config.isConfigurationSection(path)) {
			config.createSection(path);
			return new HashSet<>();
		}

		return config.getConfigurationSection(path).getKeys(false);
	}

	private static boolean getBoolean(String path, boolean def) {
		config.addDefault(path, def);
		return config.getBoolean(path, config.getBoolean(path));
	}

	private static double getDouble(String path, double def) {
		config.addDefault(path, def);
		return config.getDouble(path, config.getDouble(path));
	}

	private static float getFloat(String path, float def) {
		config.addDefault(path, def);
		return config.getFloat(path, config.getFloat(path));
	}

	private static int getInt(String path, int def) {
		config.addDefault(path, def);
		return config.getInt(path, config.getInt(path));
	}

	private static <T> List getList(String path, T def) {
		config.addDefault(path, def);
		return config.getList(path, config.getList(path));
	}

	private static String getString(String path, String def) {
		config.addDefault(path, def);
		return config.getString(path, config.getString(path));
	}
}
