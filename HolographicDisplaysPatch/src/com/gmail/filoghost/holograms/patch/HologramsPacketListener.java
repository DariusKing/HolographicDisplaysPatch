package com.gmail.filoghost.holograms.patch;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.packetwrapper.WrapperPlayServerAttachEntity;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerEntityTeleport;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity.ObjectTypes;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.ScheduledPacket;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

public class HologramsPacketListener extends PacketAdapter {
	
	private static final double OFFSET_HORSE = 58.25;
	private static final double OFFSET_OTHER = 1.2;
	
	private static final Byte ENTITY_INVISIBLE = Byte.valueOf((byte) 32);

	public HologramsPacketListener(Plugin plugin) {
		super(plugin, ListenerPriority.HIGHEST,
				PacketType.Play.Server.SPAWN_ENTITY_LIVING,
				PacketType.Play.Server.SPAWN_ENTITY,
				PacketType.Play.Server.ATTACH_ENTITY,
				PacketType.Play.Server.ENTITY_METADATA,
				PacketType.Play.Server.ENTITY_TELEPORT);
	}

	@Override
	public void onPacketSending(final PacketEvent event) {
		if (!HologramsPatch.hasNewProtocol(event.getPlayer())) {
			// If the player has the 1.7 protocol, do nothing, because holograms will still work
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
			
			WrapperPlayServerSpawnEntityLiving spawnLivingPacket = new WrapperPlayServerSpawnEntityLiving(event.getPacket());
			
			Entity entity = spawnLivingPacket.getEntity(event);
			if (entity == null) {
				return;
			}
			
			if (spawnLivingPacket.getType() == EntityType.HORSE && HolographicDisplaysAPI.isHologramEntity(entity)) {
				
				spawnLivingPacket.setType(30); // Armor stands as living entities ID
				
				List<WrappedWatchableObject> metadata = spawnLivingPacket.getMetadata().getWatchableObjects();
				
				if (metadata != null) {
					fixIndexes(metadata, event.getPlayer());
					metadata.add(new WrappedWatchableObject(0, ENTITY_INVISIBLE));
					spawnLivingPacket.setMetadata(new WrappedDataWatcher(metadata));
				}
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
			
			WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(event.getPacket());
			
			Entity entity = spawnPacket.getEntity(event);
			if (entity == null) {
				return;
			}
			
			if (spawnPacket.getType() == ObjectTypes.WITHER_SKULL && HolographicDisplaysAPI.isHologramEntity(entity)) {
				
				spawnPacket.setType(78); // The object ID for armor stands

				final WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata();
				metadataPacket.setEntityId(spawnPacket.getEntityID());

				List<WrappedWatchableObject> metadata = metadataPacket.getEntityMetadata();
				metadata.add(new WrappedWatchableObject(0, ENTITY_INVISIBLE));
				metadataPacket.setEntityMetadata(metadata);
				
				// Send the metadata packet later, after the spawn packet.
				event.schedule(ScheduledPacket.fromSilent(metadataPacket.getHandle(), event.getPlayer()));
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
			
			WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(event.getPacket());
			
			Entity entity = metadataPacket.getEntity(event);
			if (entity == null) {
				return;
			}
			
			if (entity.getType() == EntityType.HORSE && HolographicDisplaysAPI.isHologramEntity(entity)) {
				
				List<WrappedWatchableObject> metadata = metadataPacket.getEntityMetadata();
				fixIndexes(metadata, event.getPlayer());
				metadata.add(new WrappedWatchableObject(0, ENTITY_INVISIBLE)); // To make the armor stand invisible
				metadataPacket.setEntityMetadata(metadata);
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.ATTACH_ENTITY) {
			
			WrapperPlayServerAttachEntity attachPacket = new WrapperPlayServerAttachEntity(event.getPacket());
			
			Entity vehicle = attachPacket.getVehicle(event);
			Entity passenger = attachPacket.getEntity(event);
			
			if (vehicle != null && passenger != null && HolographicDisplaysAPI.isHologramEntity(vehicle)) {
				// Correct the position of the vehicle, because when the vehicle is spawned it doesn't have a passenger.
				Location loc = vehicle.getLocation();
				final WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport();
				teleportPacket.setEntityID(attachPacket.getVehicleId());
				teleportPacket.setX(loc.getX());
				teleportPacket.setZ(loc.getZ());
				
				if (passenger.getType() == EntityType.HORSE) {
					teleportPacket.setY(loc.getY() - OFFSET_HORSE);
				} else if (passenger.getType() == EntityType.DROPPED_ITEM || passenger.getType() == EntityType.SLIME) {
					teleportPacket.setY(loc.getY() - OFFSET_OTHER);
				}

				event.schedule(ScheduledPacket.fromSilent(teleportPacket.getHandle(), event.getPlayer()));
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {

			WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(event.getPacket());
			
			Entity entity = teleportPacket.getEntity(event);
			if (entity == null) {
				return;
			}
			
			if (entity.getType() == EntityType.WITHER_SKULL && HolographicDisplaysAPI.isHologramEntity(entity)) {

				Entity passenger = entity.getPassenger();
				if (passenger == null) {
					return;
				}
				
				if (passenger.getType() == EntityType.DROPPED_ITEM || passenger.getType() == EntityType.SLIME) {
					teleportPacket.setY(entity.getLocation().getY() - OFFSET_OTHER);
				} else if (passenger.getType() == EntityType.HORSE) {
					teleportPacket.setEntityID(entity.getPassenger().getEntityId());
					teleportPacket.setY(entity.getLocation().getY() - OFFSET_HORSE);
				}
			}
		}
	}
	
	private void fixIndexes(List<WrappedWatchableObject> metadata, Player player) {
		Iterator<WrappedWatchableObject> iter =	metadata.iterator();
		WrappedWatchableObject current;
		
		while (iter.hasNext()) {
			current = iter.next();
			
			if (current.getIndex() == 2) {
				if (current.getValue() != null && current.getValue().getClass() == String.class) {
					String customName = (String) current.getValue();

					if (customName.contains("{player}") || customName.contains("{displayname}")) {
						customName = customName.replace("{player}", player.getName()).replace("{displayname}", player.getDisplayName());
						current.setValue(customName);
					}
				}
				
			} else if (current.getIndex() == 3) {
				// Do nothing here
			} else {
				iter.remove();
			}
		}
	}

}