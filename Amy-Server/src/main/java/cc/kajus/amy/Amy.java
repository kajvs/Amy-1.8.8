package cc.kajus.amy;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import cc.kajus.amy.async.AsyncUtil;
import cc.kajus.amy.async.pathsearch.SearchHandler;
import cc.kajus.amy.async.thread.CombatThread;
import cc.kajus.amy.commands.KnockbackCommand;
import cc.kajus.amy.commands.MobAICommand;
import cc.kajus.amy.commands.PingCommand;
import cc.kajus.amy.commands.SetMaxSlotCommand;
import cc.kajus.amy.commands.SpawnMobCommand;
import cc.kajus.amy.config.AmyConfig;
import cc.kajus.amy.hitdetection.LagCompensator;
import cc.kajus.amy.protocol.MovementListener;
import cc.kajus.amy.protocol.PacketListener;
import cc.kajus.amy.statistics.StatisticsClient;

import net.minecraft.server.MinecraftServer;
import xyz.sculas.nacho.anticrash.AntiCrash;
import xyz.sculas.nacho.async.AsyncExplosions;

public class Amy {

	private StatisticsClient client;
	
	public static final Logger LOGGER = LogManager.getLogger();
	private static final Logger DEBUG_LOGGER = LogManager.getLogger();
	private static Amy INSTANCE;
	
	private CombatThread knockbackThread;
	
	private final Executor statisticsExecutor = Executors
			.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Amy Statistics Thread")
			.build());
	
	private volatile boolean statisticsEnabled = false;
	
	private final Set<PacketListener> packetListeners = Sets.newConcurrentHashSet();
	private final Set<MovementListener> movementListeners = Sets.newConcurrentHashSet();

	private final LagCompensator lagCompensator = new LagCompensator();

	private Amy() {
		initCmds();
		initStatistics();
		
		// We do not want to initialize this again after a reload
		if (AmyConfig.asyncPathSearches && SearchHandler.getInstance() == null) {
			new SearchHandler();
		}
		
		if (AmyConfig.asyncKnockback) {
			knockbackThread = new CombatThread("Knockback Thread");
		}
		if (AmyConfig.asyncTnt) {
			AsyncExplosions.initExecutor(AmyConfig.fixedPoolSize);
		}
		if (AmyConfig.enableAntiCrash) {
			registerPacketListener(new AntiCrash());
		}
	}

	public void reload() {
		Amy.init();
	}

	private void initCmds() {
		
		SimpleCommandMap commandMap = MinecraftServer.getServer().server.getCommandMap();
		
		if (AmyConfig.mobAiCmd) {
			MobAICommand mobAiCommand = new MobAICommand("mobai");
			commandMap.register(mobAiCommand.getName(), "", mobAiCommand);
		}
		
		if (AmyConfig.pingCmd) {
			PingCommand pingCommand = new PingCommand("ping");
			commandMap.register(pingCommand.getName(), "", pingCommand);
		}
	
		
		
		// NachoSpigot commands
		// TODO: add configuration for all of these
		SetMaxSlotCommand setMaxSlotCommand = new SetMaxSlotCommand("sms"); // [Nacho-0021] Add setMaxPlayers within Bukkit.getServer() and SetMaxSlot Command
		commandMap.register(setMaxSlotCommand.getName(), "ns", setMaxSlotCommand);

		SpawnMobCommand spawnMobCommand = new SpawnMobCommand("spawnmob");
		commandMap.register(spawnMobCommand.getName(), "ns", spawnMobCommand);

		KnockbackCommand knockbackCommand = new KnockbackCommand("kb");
		commandMap.register(knockbackCommand.getName(), "ns", knockbackCommand);
	}

	private void initStatistics() {
		if (AmyConfig.statistics && !statisticsEnabled) {
			Runnable statisticsRunnable = (() -> {
				client = new StatisticsClient();
				try {
					statisticsEnabled = true;

					if (!client.isConnected) {
						// Connect to the statistics server and notify that there is a new server
						client.start("150.230.35.78", 500);
						client.sendMessage("new server");

						while (true) {
							// Keep alive, this tells the statistics server that this server
							// is still online
							client.sendMessage("keep alive packet");

							// Online players, this tells the statistics server how many players
							// are on
							client.sendMessage("player count packet " + Bukkit.getOnlinePlayers().size());

							// Statistics are sent every 40 secs.
							TimeUnit.SECONDS.sleep(40);
						}

					}
				} catch (Exception ignored) {}
			});
			AsyncUtil.run(statisticsRunnable, statisticsExecutor);
		}
	}

	public static void init() {
		if (INSTANCE == null) {
			INSTANCE = new Amy();
		}
	}

	public StatisticsClient getClient() {
		return this.client;
	}
	
	public CombatThread getKnockbackThread() {
		return knockbackThread;
	}
    
	public static void debug(String msg) {
		if (AmyConfig.debugMode)
			DEBUG_LOGGER.info(msg);
	}
	
	public void registerPacketListener(PacketListener packetListener) {
		this.packetListeners.add(packetListener);
	}

	public void unregisterPacketListener(PacketListener packetListener) {
		this.packetListeners.remove(packetListener);
	}

	public Set<PacketListener> getPacketListeners() {
		return this.packetListeners;
	}

	public void registerMovementListener(MovementListener movementListener) {
		this.movementListeners.add(movementListener);
	}

	public void unregisterMovementListener(MovementListener movementListener) {
		this.movementListeners.remove(movementListener);
	}

	public Set<MovementListener> getMovementListeners() {
		return this.movementListeners;
	}

	public LagCompensator getLagCompensator() {
		return this.lagCompensator;
	}
	
	public static Amy getInstance() {
		return INSTANCE;
	}
}
