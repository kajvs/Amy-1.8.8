package net.minecraft.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
// CraftBukkit end
import org.github.paperspigot.PaperSpigotConfig; // PaperSpigot

import cc.kajus.amy.cache.Constants;

public class ItemBucket extends Item {

	private Block a;

	/**
	 * Where the placement that just succeeded put its block, or null when
	 * nothing was placed. {@link #resendChunk} reads it.
	 */
	private BlockPosition placed;

	public ItemBucket(Block block) {
		this.maxStackSize = 1;
		this.a = block;
		this.a(CreativeModeTab.f);
	}

	// The bucket rays behind the "fake lava" fix.
	//
	// The client predicts its own bucket placement from ITS position and ITS look direction. The
	// server used to trace from its own (lagged) position and the last yaw/pitch it had processed,
	// so the two could land on different blocks: the client then kept a lava block the server never
	// placed ("fake lava"). These rays put the client's own numbers into the trace, and the
	// placement path below tries them until one of them actually places - so the block the player
	// saw himself place is always among the candidates.

	/** Server position, client's look direction. */
	public MovingObjectPosition trace(World world, EntityPlayer player, boolean flag) {
		float pitch = player.playerConnection.getBucketPitch();
		float yaw = player.playerConnection.getBucketYaw();

		double x = player.locX;
		double y = player.locY + (double) player.getHeadHeight();
		double z = player.locZ;

		return this.rayTrace(world, x, y, z, yaw, pitch, flag);
	}

	/** Midpoint between the client's position and the server's, client's look direction. */
	public MovingObjectPosition a(World world, EntityPlayer player, boolean flag) {
		float pitch = player.playerConnection.getBucketPitch();
		float yaw = player.playerConnection.getBucketYaw();

		double x = (player.playerConnection.getBucketX() + player.locX) / 2.0D;
		double y = (player.playerConnection.getBucketY() + player.locY) / 2.0D + (double) player.getHeadHeight();
		double z = (player.playerConnection.getBucketZ() + player.locZ) / 2.0D;

		return this.rayTrace(world, x, y, z, yaw, pitch, flag);
	}

	/** Server position, the yaw/pitch the server had processed - what vanilla did. */
	public MovingObjectPosition traceLastTick(World world, EntityPlayer player, boolean flag) {
		return this.rayTrace(world, player.locX, player.locY + (double) player.getHeadHeight(), player.locZ,
				player.yaw, player.pitch, flag);
	}

	/** The position the client reported before this packet, the yaw/pitch from that packet. */
	public MovingObjectPosition traceLast2Tick(World world, EntityPlayer player, boolean flag) {
		return this.rayTrace(world, player.playerConnection.getLastBucketX(),
				player.playerConnection.getLastBucketY() + (double) player.getHeadHeight(),
				player.playerConnection.getLastBucketZ(), player.playerConnection.getLastYaw(),
				player.playerConnection.getLastPitch(), flag);
	}

	/** Server position, the average of the client's and the server's look direction. */
	public MovingObjectPosition traceMiddleTick(World world, EntityPlayer player, boolean flag) {
		float pitch = (player.playerConnection.getBucketPitch() + player.pitch) / 2.0F;
		float yaw = getMiddle(player.playerConnection.getBucketYaw(), player.yaw);

		return this.rayTrace(world, player.locX, player.locY + (double) player.getHeadHeight(), player.locZ, yaw,
				pitch, flag);
	}

	/** Midpoint between the server's position and the client's previous one, averaged look. */
	public MovingObjectPosition traceMiddle2Tick(World world, EntityPlayer player, boolean flag) {
		float pitch = (player.pitch + player.playerConnection.getLastBucketPitch()) / 2.0F;
		float yaw = getMiddle(player.yaw, player.playerConnection.getLastBucketYaw());

		double x = (player.locX + player.playerConnection.getLastBucketX()) / 2.0D;
		double y = (player.locY + player.playerConnection.getLastBucketY()) / 2.0D
				+ (double) player.getHeadHeight();
		double z = (player.locZ + player.playerConnection.getLastBucketZ()) / 2.0D;

		return this.rayTrace(world, x, y, z, yaw, pitch, flag);
	}

	/** The 5.4 block ray every variant above shares. */
	private MovingObjectPosition rayTrace(World world, double x, double y, double z, float yaw, float pitch,
			boolean flag) {
		Vec3D start = new Vec3D(x, y, z);

		float f1 = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
		float f2 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
		float f3 = -MathHelper.cos(-pitch * 0.017453292F);
		float f4 = MathHelper.sin(-pitch * 0.017453292F);
		float f5 = f2 * f3;
		float f6 = f1 * f3;

		double reach = 5.4D;
		Vec3D end = start.add((double) f5 * reach, (double) f4 * reach, (double) f6 * reach);

		return world.rayTrace(start, end, flag, !flag, false);
	}

	/** Averages two yaws across the -180/180 wrap-around. */
	public static float getMiddle(float yaw1, float yaw2) {
		yaw1 = MathHelper.g(yaw1) + 180;
		yaw2 = MathHelper.g(yaw2) + 180;

		if (Math.abs(yaw1 - yaw2) <= 180) {
			return (yaw1 + yaw2) / 2 - 180;
		}

		return ((yaw1 + yaw2) / 2 + 180) % 360 - 180;
	}

	@Override
	public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
		boolean flag = this.a == Blocks.AIR;
		// EntityPlayer is the only EntityHuman in the game, so the view bucket is always available
		EntityPlayer player = (EntityPlayer) entityhuman;

		this.placed = null;

		MovingObjectPosition movingobjectposition = this.trace(world, player, flag);
		PlacementResult res = this.handleMovingObjection(itemstack, world, entityhuman, movingobjectposition, flag);

		if (!res.handled) {
			MovingObjectPosition temp = movingobjectposition;
			movingobjectposition = this.traceLastTick(world, player, flag);

			if (differs(movingobjectposition, temp)) {
				res = this.handleMovingObjection(itemstack, world, entityhuman, movingobjectposition, flag);
			}
		}

		if (!res.handled) {
			MovingObjectPosition temp = movingobjectposition;
			movingobjectposition = this.traceMiddleTick(world, player, flag);

			if (differs(movingobjectposition, temp)) {
				res = this.handleMovingObjection(itemstack, world, entityhuman, movingobjectposition, flag);
			}
		}

		if (!res.handled) {
			MovingObjectPosition temp = movingobjectposition;
			movingobjectposition = this.a(world, player, flag);

			if (differs(movingobjectposition, temp)) {
				res = this.handleMovingObjection(itemstack, world, entityhuman, movingobjectposition, flag);
			}
		}

		// only a filling bucket can land on a liquid, so the two "previous position" rays are only
		// worth trying there
		if (flag) {
			if (!res.handled) {
				MovingObjectPosition temp = movingobjectposition;
				movingobjectposition = this.traceMiddle2Tick(world, player, flag);

				if (differs(movingobjectposition, temp)) {
					res = this.handleMovingObjection(itemstack, world, entityhuman, movingobjectposition, flag);
				}
			}

			if (!res.handled) {
				MovingObjectPosition temp = movingobjectposition;
				movingobjectposition = this.traceLast2Tick(world, player, flag);

				if (differs(movingobjectposition, temp)) {
					res = this.handleMovingObjection(itemstack, world, entityhuman, movingobjectposition, flag);
				}
			}
		}

		if (this.placed != null) {
			this.resendChunk(entityhuman, this.placed);
		}

		return res.stack;
	}

	/** True when the two traces did not land on the same block, so the second one is worth trying. */
	private static boolean differs(MovingObjectPosition one, MovingObjectPosition other) {
		return one == null || other == null
				|| one.type != MovingObjectPosition.EnumMovingObjectType.BLOCK
				|| other.type != MovingObjectPosition.EnumMovingObjectType.BLOCK
				|| !other.a().equals(one.a());
	}

	/**
	 * One placement attempt. {@code handled}
	 * means "stop trying the other rays": either the bucket was used, or the attempt decided not to
	 * (event cancelled, no build permission); {@code false} means this ray found nothing usable, so
	 * the caller tries the next one.
	 */
	private PlacementResult handleMovingObjection(ItemStack itemstack, World world, EntityHuman entityhuman,
			MovingObjectPosition movingobjectposition, boolean flag) {
		if (movingobjectposition == null) {
			return new PlacementResult(itemstack, false);
		}

		if (movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
			BlockPosition blockposition = movingobjectposition.a();

			if (!world.a(entityhuman, blockposition)) {
				return new PlacementResult(itemstack, true);
			}

			if (flag) {
				if (!entityhuman.a(blockposition.shift(movingobjectposition.direction), movingobjectposition.direction,
						itemstack)) {
					return new PlacementResult(itemstack, true);
				}

				IBlockData iblockdata = world.getType(blockposition);
				Material material = iblockdata.getBlock().getMaterial();

				if (material == Material.WATER && iblockdata.get(BlockFluids.LEVEL).intValue() == 0) {
					// CraftBukkit start
					PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman,
							blockposition.getX(), blockposition.getY(), blockposition.getZ(), null, itemstack,
							Items.WATER_BUCKET);

					if (event.isCancelled()) {
						return new PlacementResult(itemstack, true);
					}
					// CraftBukkit end
					world.setAir(blockposition);
					entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
					return new PlacementResult(
							this.a(itemstack, entityhuman, Items.WATER_BUCKET, event.getItemStack()), true);
				}

				if (material == Material.LAVA && iblockdata.get(BlockFluids.LEVEL).intValue() == 0) {
					// CraftBukkit start
					PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman,
							blockposition.getX(), blockposition.getY(), blockposition.getZ(), null, itemstack,
							Items.LAVA_BUCKET);

					if (event.isCancelled()) {
						return new PlacementResult(itemstack, true);
					}
					// CraftBukkit end
					world.setAir(blockposition);
					entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
					return new PlacementResult(this.a(itemstack, entityhuman, Items.LAVA_BUCKET, event.getItemStack()),
							true);
				}
			} else {
				if (this.a == Blocks.AIR) {
					// CraftBukkit start
					PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman,
							blockposition.getX(), blockposition.getY(), blockposition.getZ(),
							movingobjectposition.direction, itemstack);

					if (event.isCancelled()) {
						return new PlacementResult(itemstack, true);
					}

					return new PlacementResult(CraftItemStack.asNMSCopy(event.getItemStack()), true);
					// CraftBukkit end
				}

				BlockPosition blockposition1 = blockposition.shift(movingobjectposition.direction);

				if (!entityhuman.a(blockposition1, movingobjectposition.direction, itemstack)) {
					return new PlacementResult(itemstack, true);
				}

				// CraftBukkit start
				PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman,
						blockposition.getX(), blockposition.getY(), blockposition.getZ(),
						movingobjectposition.direction, itemstack);

				if (event.isCancelled()) {
					return new PlacementResult(itemstack, true);
				}
				// CraftBukkit end

				if (this.a(world, blockposition1) && !entityhuman.abilities.canInstantlyBuild) {
					this.placed = blockposition1;
					entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
					// PaperSpigot start - Stackable Buckets
					if ((this == Items.LAVA_BUCKET && PaperSpigotConfig.stackableLavaBuckets)
							|| (this == Items.WATER_BUCKET && PaperSpigotConfig.stackableWaterBuckets)) {
						if (--itemstack.count <= 0) {
							return new PlacementResult(CraftItemStack.asNMSCopy(event.getItemStack()), true);
						}
						if (!entityhuman.inventory.pickup(CraftItemStack.asNMSCopy(event.getItemStack()))) {
							entityhuman.drop(CraftItemStack.asNMSCopy(event.getItemStack()), false);
						}
						return new PlacementResult(itemstack, true);
					}
					// PaperSpigot end
					return new PlacementResult(CraftItemStack.asNMSCopy(event.getItemStack()), true); // CraftBukkit
				}
			}
		}

		return new PlacementResult(itemstack, false);
	}

	/**
	 * The other half of the fake-lava fix. Even with the right ray the client
	 * keeps whatever IT predicted, so after a successful placement the air blocks of the 7x7 ring
	 * around the placed block are sent back to that one player as a fake multi block change; the
	 * client drops the block it invented.
	 */
	private void resendChunk(EntityHuman entityhuman, BlockPosition placed) {
		Map<Long, List<BlockPosition>> byChunk = new HashMap<>();

		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}

				BlockPosition next = new BlockPosition(placed.getX() + dx, placed.getY(), placed.getZ() + dz);

				if (entityhuman.world.getType(next).getBlock().getMaterial() != Material.AIR) {
					continue;
				}

				long key = ((long) (next.getX() >> 4) << 32) | ((long) (next.getZ() >> 4) & 0xFFFFFFFFL);

				byChunk.computeIfAbsent(key, ignored -> new ArrayList<BlockPosition>()).add(next);
			}
		}

		Player bukkitPlayer = ((EntityPlayer) entityhuman).getBukkitEntity();

		for (Map.Entry<Long, List<BlockPosition>> entry : byChunk.entrySet()) {
			List<BlockPosition> positions = entry.getValue();

			if (positions.isEmpty()) {
				continue;
			}

			int chunkX = (int) (entry.getKey() >> 32);
			int chunkZ = (int) (long) entry.getKey();

			try {
				Chunk chunk = entityhuman.world.getChunkAt(chunkX, chunkZ);
				short[] packed = new short[positions.size()];

				for (int i = 0; i < packed.length; i++) {
					BlockPosition position = positions.get(i);

					packed[i] = (short) ((position.getX() & 15) << 12 | (position.getZ() & 15) << 8
							| (position.getY() & 255));
				}

				// The packet builds each entry from the chunk itself, and every position below is
				// air - which is exactly the answer the client has to be given here.
				PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(packed.length, packed,
						chunk);

				new cc.kajus.amy.chunk.CraftFakeMultiBlockChange(packet).sendTo(bukkitPlayer);
			} catch (Exception ignored) {
				// a chunk that is not loaded, or a client that went away - nothing to resend
			}
		}
	}

	/** What one placement attempt did. */
	private static final class PlacementResult {

		private final ItemStack stack;
		private final boolean handled;

		private PlacementResult(ItemStack stack, boolean handled) {
			this.stack = stack;
			this.handled = handled;
		}
	}

	// CraftBukkit - added ob.ItemStack result - TODO: Is this... the right way to
	// handle this?
	private ItemStack a(ItemStack itemstack, EntityHuman entityhuman, Item item,
			org.bukkit.inventory.ItemStack result) {
		if (entityhuman.abilities.canInstantlyBuild) {
			return itemstack;
		} else if (--itemstack.count <= 0) {
			return CraftItemStack.asNMSCopy(result); // CraftBukkit
		} else {
			if (!entityhuman.inventory.pickup(CraftItemStack.asNMSCopy(result))) {
				entityhuman.drop(CraftItemStack.asNMSCopy(result), false);
			}

			return itemstack;
		}
	}

	public boolean a(World world, BlockPosition blockposition) {
		if (this.a == Blocks.AIR) {
			return false;
		} else {
			Material material = world.getType(blockposition).getBlock().getMaterial();
			boolean flag = !material.isBuildable();

			if (!world.isEmpty(blockposition) && !flag) {
				return false;
			} else {
				if (world.worldProvider.n() && this.a == Blocks.FLOWING_WATER) {
					int i = blockposition.getX();
					int j = blockposition.getY();
					int k = blockposition.getZ();

					world.makeSound(i + 0.5F, j + 0.5F, k + 0.5F, "random.fizz", 0.5F,
							2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

					for (int l = 0; l < 8; ++l) {
						world.addParticle(EnumParticle.SMOKE_LARGE, i + Math.random(), j + Math.random(),
								k + Math.random(), 0.0D, 0.0D, 0.0D, Constants.EMPTY_ARRAY);
					}
				} else {
					if (!world.isClientSide && flag && !material.isLiquid()) {
						world.setAir(blockposition, true);
					}

					world.setTypeAndData(blockposition, this.a.getBlockData(), 3);
				}

				return true;
			}
		}
	}
}
