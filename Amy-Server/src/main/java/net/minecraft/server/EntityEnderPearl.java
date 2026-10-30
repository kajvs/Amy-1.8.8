package net.minecraft.server;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EntityEnderPearl extends EntityProjectile {
	private Location lastValidTeleport;
	private EntityLiving c;

	public EntityEnderPearl(World world) {
		super(world);
		this.loadChunks = world.paperSpigotConfig.loadUnloadedEnderPearls;
	}

	public EntityEnderPearl(World world, EntityLiving entityliving) {
		super(world, entityliving);
		this.c = entityliving;
		this.loadChunks = world.paperSpigotConfig.loadUnloadedEnderPearls;
		this.lastValidTeleport = entityliving.getBukkitEntity().getLocation();
	}

	protected void a(MovingObjectPosition movingobjectposition) {
		EntityLiving entityliving = this.getShooter();
		double fixedY = -1.0E-7;
		if (movingobjectposition.entity != null) {
			if (movingobjectposition.entity == this.c) {
				return;
			}

			fixedY = movingobjectposition.entity.locY;
			movingobjectposition.entity.damageEntity(DamageSource.projectile(this, entityliving), 0.0F);
		}

		if (this.inUnloadedChunk && this.world.paperSpigotConfig.removeUnloadedEnderPearls) {
			this.die();
		}

		for(int i = 0; i < 32; ++i) {
			this.world.addParticle(EnumParticle.PORTAL, this.locX, this.locY + this.random.nextDouble() * (double)2.0F, this.locZ, this.random.nextGaussian(), (double)0.0F, this.random.nextGaussian());
		}

		if (!this.world.isClientSide) {
			if (entityliving instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer)entityliving;
				if (entityplayer.playerConnection.a().g() && entityplayer.world == this.world && !entityplayer.isSleeping()) {
					CraftPlayer player = entityplayer.getBukkitEntity();
					Location location = this.lastValidTeleport.clone();
					location.setPitch(player.getLocation().getPitch());
					location.setYaw(player.getLocation().getYaw());
					if (fixedY != -1.0E-7) {
						location.setY(fixedY);
					}

					location.setY((double)((int)location.getY()));
					PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
					Bukkit.getPluginManager().callEvent(teleEvent);
					if (!teleEvent.isCancelled() && !entityplayer.playerConnection.isDisconnected()) {
						if (entityliving.au()) {
							entityliving.mount((Entity)null);
						}

						entityplayer.playerConnection.teleport(teleEvent.getTo());
						entityliving.fallDistance = 0.0F;
						CraftEventFactory.entityDamage = this;
						entityliving.damageEntity(DamageSource.FALL, 5.0F);
						CraftEventFactory.entityDamage = null;
					}
				}
			} else if (entityliving != null) {
				entityliving.enderTeleportTo(this.locX, this.locY, this.locZ);
				entityliving.fallDistance = 0.0F;
			}

			this.die();
		}

	}

	public void t_() {
		EntityLiving entityliving = this.getShooter();
		if (entityliving != null && entityliving instanceof EntityHuman && !entityliving.isAlive()) {
			this.die();
		} else {
			AxisAlignedBB clearTeleportBoundingBox = AxisAlignedBB.a(this.locX - 0.3, this.locY - 0.05, this.locZ - 0.3, this.locX + 0.3, this.locY + 1.62, this.locZ + 0.3);
			if (this.world.getCubes(this, clearTeleportBoundingBox).isEmpty()) {
				this.lastValidTeleport = this.getBukkitEntity().getLocation();
			}

			super.t_();
		}

	}
}
