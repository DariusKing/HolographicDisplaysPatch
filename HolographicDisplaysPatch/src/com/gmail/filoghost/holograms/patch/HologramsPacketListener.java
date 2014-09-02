package com.gmail.filoghost.holograms.patch;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

public class HologramsPacketListener extends PacketAdapter {
	
	private static final double VERTICAL_OFFSET = 54.56;

	public HologramsPacketListener(Plugin plugin) {
		super(plugin, ListenerPriority.HIGHEST,
				PacketType.Play.Server.ATTACH_ENTITY,
				PacketType.Play.Server.SPAWN_ENTITY,
				PacketType.Play.Server.SPAWN_ENTITY_LIVING,
				PacketType.Play.Server.ENTITY_METADATA);
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
			
			if (spawnLivingPacket.getType() == EntityType.HORSE && HolographicDisplaysAPI.isHologramEntity(spawnLivingPacket.getEntity(event))) {
				// They do not see horses
				event.setCancelled(true);
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
			WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(event.getPacket());
			
			if (spawnEntityPacket.getType() == WrapperPlayServerSpawnEntity.ObjectTypes.WITHER_SKULL) {
				
				Entity witherSkull = spawnEntityPacket.getEntity(event);
				if (HolographicDisplaysAPI.isHologramEntity(witherSkull)) {
					spawnEntityPacket.setY(spawnEntityPacket.getY() - VERTICAL_OFFSET);
					spawnEntityPacket.setType(WrapperPlayServerSpawnEntity.ObjectTypes.MINECART); // Just to see them TODO
				}
			}
			
		} else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
			
			WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(event.getPacket());
			Entity entity = metadataPacket.getEntity(event);
			
			if (HolographicDisplaysAPI.isHologramEntity(entity)) {
				
				if (entity.getType() == EntityType.HORSE) {
					// The horse metadata is applied to the wither skull instead
	
					Entity witherSkull = entity.getVehicle();
					
					metadataPacket.setEntityId(witherSkull.getEntityId());
					
					List<WrappedWatchableObject> metadata = metadataPacket.getEntityMetadata();
					Iterator<WrappedWatchableObject> iter =	metadata.iterator();

					while (iter.hasNext()) {
						int index = iter.next().getIndex();
						
						if (index != 2 && index != 3) {
							iter.remove();
						}
					}
					
					metadataPacket.setEntityMetadata(metadata);
					System.out.println("Sent new metadata: " + metadata.toString());
				
				} else if (entity.getType() == EntityType.WITHER_SKULL) {
					// The skull metadata packet is cancelled, because we use the metadata of the horse
					event.setCancelled(true);
				}
			}
		}
	}

}
