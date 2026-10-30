package cc.kajus.amy.commands;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.kajus.amy.commons.ClickableBuilder;
import cc.kajus.amy.knockback.CraftKnockbackProfile;
import cc.kajus.amy.knockback.KnockbackConfig;

import dev.cobblesword.nachospigot.knockback.KnockbackProfile;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * /kb - knockback profile management.
 *
 * <p>
 * The parameter set is 8 values, with the same names as the keys in
 * knockback.yml. The old per-projectile, wtap-extra, add-*, stop-sprint and
 * vertical-min parameters were removed together with their profile fields.
 */
public class KnockbackCommand extends Command {

	private final String separator = "§8§m-=-------------------------=-";

	/**
	 * Editable parameters, in the SAME ORDER as {@link KnockbackProfile#getValues()}
	 * so the display strings and the clickable edit commands stay in sync.
	 * These are also the keys used in knockback.yml.
	 */
	private static final String[] PARAMS = { "verticalfriction", "horizontalfriction", "horizontal", "vertical",
			"vertical-limit", "extra-horizontal", "extra-vertical", "misplaceadd", "misplacedec", "misplacelimit" };

	public KnockbackCommand(String name) {
		super(name);
		this.description = "Assists in knockback configuration.";
		this.setPermission("amy.command.knockback");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!testPermission(sender) || !(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;

		switch (args.length) {
		case 2: {
			switch (args[0].toLowerCase()) {
			case "create": {
				if (!isProfileName(args[1])) {
					CraftKnockbackProfile profile = new CraftKnockbackProfile(args[1]);
					KnockbackConfig.getKbProfiles().add(profile);
					profile.save();
					knockbackCommandMain(player);
					player.sendMessage("§aThe profile §e" + args[1] + " §ahas been created.");
					return true;
				} else {
					player.sendMessage("§cA knockback profile with that name already exists.");
				}
				break;
			}
			case "delete": {
				if (KnockbackConfig.getCurrentKb().getName().equalsIgnoreCase(args[1])) {
					knockbackCommandMain(player);
					player.sendMessage("§cYou cannot delete the profile that is being used.");
					return false;
				}
				if (KnockbackConfig.getKbProfiles()
						.removeIf(profile -> profile.getName().equalsIgnoreCase(args[1]))) {
					KnockbackConfig.set("knockback.profiles." + args[1], null);
					knockbackCommandMain(player);
					player.sendMessage("§aThe profile §e" + args[1] + " §ahas been removed.");
					return true;
				} else {
					player.sendMessage("§cThis profile doesn't exist.");
				}
				break;
			}
			case "load": {
				KnockbackProfile profile = KnockbackConfig.getKbProfileByName(args[1]);
				if (profile != null) {
					if (KnockbackConfig.getCurrentKb().getName().equalsIgnoreCase(args[1])) {
						player.sendMessage("§cThis profile is loaded.");
						return false;
					}
					KnockbackConfig.setCurrentKb(profile);
					KnockbackConfig.set("knockback.current", profile.getName());
					KnockbackConfig.save();
					knockbackCommandMain(player);
					player.sendMessage("§aThe profile §e" + args[1] + " §ahas been loaded.");
					return true;
				} else {
					player.sendMessage("§cThis profile doesn't exist.");
				}
				break;
			}
			case "view": {
				KnockbackProfile profile = KnockbackConfig.getKbProfileByName(args[1]);
				if (profile != null) {
					knockbackCommandView(player, profile);
					return true;
				}
				player.sendMessage("§cThis profile doesn't exist.");
				break;
			}
			default: {
				knockbackCommandMain(player);
			}
			}
			break;
		}
		case 3: {
			if ("set".equalsIgnoreCase(args[0])) {
				KnockbackProfile profile = KnockbackConfig.getKbProfileByName(args[1]);
				if (profile == null) {
					sender.sendMessage("§cA profile with that name could not be found.");
					return false;
				}
				Player target = Bukkit.getPlayer(args[2]);
				if (target == null) {
					sender.sendMessage("§cThat player is not online.");
					return false;
				}
				target.setKnockbackProfile(profile);
				sender.sendMessage("§aApplied profile §e" + profile.getName() + " §ato §e" + target.getName() + "§a.");
				break;
			}
			knockbackCommandMain(player);
			break;
		}
		case 4: {
			if (!"edit".equalsIgnoreCase(args[0])) {
				knockbackCommandMain(player);
				break;
			}

			KnockbackProfile profile = KnockbackConfig.getKbProfileByName(args[1]);
			if (profile == null) {
				player.sendMessage("§cThis profile doesn't exist.");
				return false;
			}

			final String param = args[2].toLowerCase();
			final String raw = args[3];

			if (!NumberUtils.isNumber(raw)) {
				player.sendMessage("§4" + raw + " §cis not a number.");
				return false;
			}

			switch (param) {
			case "verticalfriction": {
				profile.setVerticalFriction(Double.parseDouble(raw));
				break;
			}
			case "horizontalfriction": {
				profile.setHorizontalFriction(Double.parseDouble(raw));
				break;
			}
			case "horizontal": {
				profile.setHorizontal(Double.parseDouble(raw));
				break;
			}
			case "vertical": {
				profile.setVertical(Double.parseDouble(raw));
				break;
			}
			case "vertical-limit": {
				profile.setVerticalLimit(Double.parseDouble(raw));
				break;
			}
			case "extra-horizontal": {
				profile.setExtraHorizontal(Double.parseDouble(raw));
				break;
			}
			case "extra-vertical": {
				profile.setExtraVertical(Double.parseDouble(raw));
				break;
			}
			case "misplaceadd": {
				profile.setMisplaceAdd(Double.parseDouble(raw));
				break;
			}
			case "misplacedec": {
				profile.setMisplaceDec(Double.parseDouble(raw));
				break;
			}
			case "misplacelimit": {
				profile.setMisplaceLimit(Double.parseDouble(raw));
				break;
			}
			default: {
				player.sendMessage("§cUnknown parameter §4" + args[2] + "§c. Valid: §e" + String.join("§c, §e", PARAMS));
				return false;
			}
			}

			profile.save();
			knockbackCommandView(player, profile);
			player.sendMessage("§aValue edited and saved.");
			break;
		}
		default: {
			knockbackCommandMain(player);
		}
		}
		return false;
	}

	private void knockbackCommandMain(Player player) {
		player.sendMessage(separator + "\n" + "§5§lKnockback profile list:\n");

		for (KnockbackProfile profile : KnockbackConfig.getKbProfiles()) {
			boolean current = KnockbackConfig.getCurrentKb().getName().equals(profile.getName());

			TextComponent line = new ClickableBuilder("§8§l(§e§l➜§8§l) ")
					.setHover("§c[Click here to apply this profile to a player] ")
					.setClick("/kb set " + profile.getName() + " ", ClickEvent.Action.SUGGEST_COMMAND).build();
			TextComponent load = new ClickableBuilder("§8§l(" + (current ? "§a" : "§f") + "✔§8§l) ")
					.setHover(current ? "§c[This profile is loaded] " : "§a[Click here to load this profile]")
					.setClick("/kb load " + profile.getName(), ClickEvent.Action.RUN_COMMAND).build();
			TextComponent delete = new ClickableBuilder("§8§l(§c§l✖§8§l) ")
					.setHover("§c[Click here to delete this profile] ")
					.setClick("/kb delete " + profile.getName(), ClickEvent.Action.RUN_COMMAND).build();
			TextComponent edit = new ClickableBuilder("§f " + profile.getName() + " §8§l[§6§l✎§8§l]")
					.setHover("§e[Click here to edit this profile]")
					.setClick("/kb view " + profile.getName(), ClickEvent.Action.RUN_COMMAND).build();
			player.spigot().sendMessage(line, load, delete, edit);
		}

		player.spigot()
				.sendMessage(new ClickableBuilder("§a[Create new profile]")
						.setHover("§c[Click here to create a new profile]")
						.setClick("/kb create ", ClickEvent.Action.SUGGEST_COMMAND).build());
		player.sendMessage(separator);
	}

	private void knockbackCommandView(Player player, KnockbackProfile profile) {
		player.sendMessage(separator + "\n" + "§5§lKnockback values:\n");

		String[] values = profile.getValues();
		for (int i = 0; i < values.length; i++) {
			TextComponent value = new TextComponent(" §d" + values[i]);
			TextComponent edit = new ClickableBuilder(" §8§l[§e§l✎§8§l]")
					.setHover("§e[Click to edit]")
					.setClick("/kb edit " + profile.getName() + " " + PARAMS[i] + " ",
							ClickEvent.Action.SUGGEST_COMMAND)
					.build();
			player.spigot().sendMessage(value, edit);
		}

		TextComponent page = new ClickableBuilder("§b[Click to back]").setHover("§c[Click to back]")
				.setClick("/kb", ClickEvent.Action.RUN_COMMAND).build();
		player.spigot().sendMessage(page);
		player.sendMessage(separator);
	}

	private boolean isProfileName(String name) {
		for (KnockbackProfile profile : KnockbackConfig.getKbProfiles()) {
			if (profile.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
