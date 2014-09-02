package com.gmail.filoghost.holograms.patch;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import com.comphenix.packetwrapper.WrapperPlayServerAttachEntity;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerEntityTeleport;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

public class HologramsPacketListener extends PacketAdapter {
	
	private static final double VERTICAL_OFFSET = 56.8;

	public HologramsPacketListener(Plugin plugin) {
		super(plugin, ListenerPriority.HIGHEST,
				PacketType.Play.Server.ATTACH_ENTITY,
				PacketType.Play.Server.SPAWN_ENTITY_LIVING,
				PacketType.Play.Server.ENTITY_METADATA,
				PacketType.Play.Server.ENTITY_TELEPORT);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
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
				
				spawnLivingPacket.setY(spawnLivingPacket.getY() - VERTICAL_OFFSET);
				spawnLivingPacket.setType(30); // Armor stands
				
				List<WrappedWatchableObject> metadata = spawnLivingPacket.getMetadata().getWatchableObjects();
				
				if (metadata != null) {
					pruneUselessIndexes(metadata);
					metadata.add(new WrappedWatchableObject(0, Byte.valueOf((byte) 32)));
					spawnLivingPacket.setMetadata(new WrappedDataWatcher(metadata));
				}
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.ATTACH_ENTITY) {
			
			WrapperPlayServerAttachEntity attachPacket = new WrapperPlayServerAttachEntity(event.getPacket());
				
			Entity vehicle = attachPacket.getVehicle(event);
			if (vehicle != null && vehicle.getType() == EntityType.WITHER_SKULL && HolographicDisplaysAPI.isHologramEntity(vehicle)) {
				
				Entity passenger = attachPacket.getEntity(event);
				
				if (passenger != null && passenger.getType() == EntityType.HORSE) {
					event.setCancelled(true); // Do not send attach packets
				}
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
			
			WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(event.getPacket());
			Entity entity = metadataPacket.getEntity(event);
			
			if (entity == null) {
				return;
			}
			
			if (HolographicDisplaysAPI.isHologramEntity(entity)) {
				
				if (entity.getType() == EntityType.HORSE) {
					// The horse metadata is applied to the wither skull instead

					List<WrappedWatchableObject> metadata = metadataPacket.getEntityMetadata();
					pruneUselessIndexes(metadata);
					metadataPacket.setEntityMetadata(metadata);
				}
			}
		} else if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {

			WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(event.getPacket());
			
			Entity entity = teleportPacket.getEntity(event);
			
			if (entity == null) {
				return;
			}
			
			if (entity.getType() == EntityType.WITHER_SKULL && entity.getPassenger() != null && entity.getPassenger().getType() == EntityType.HORSE && HolographicDisplaysAPI.isHologramEntity(entity)) {

				teleportPacket.setEntityID(entity.getPassenger().getEntityId());
				teleportPacket.setY(teleportPacket.getY() - VERTICAL_OFFSET);
			}
		}
	}
	
	private void pruneUselessIndexes(List<WrappedWatchableObject> metadata) {
		Iterator<WrappedWatchableObject> iter =	metadata.iterator();

		while (iter.hasNext()) {
			int index = iter.next().getIndex();
			
			if (index != 2 && index != 3) {
				iter.remove();
			}
		}
	}

}
