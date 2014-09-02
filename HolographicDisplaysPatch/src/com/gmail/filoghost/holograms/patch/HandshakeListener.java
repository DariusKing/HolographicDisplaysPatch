package com.gmail.filoghost.holograms.patch;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.packetwrapper.WrapperHandshakeClientSetProtocol;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class HandshakeListener extends PacketAdapter {

	// Clients connecting with 1.8
	private Set<String> newProtocolClients;
	
	public HandshakeListener(Plugin plugin) {
		super(plugin, ListenerPriority.MONITOR, PacketType.Handshake.Client.SET_PROTOCOL);
		newProtocolClients = new HashSet<String>();
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		
		WrapperHandshakeClientSetProtocol handshakePacket = new WrapperHandshakeClientSetProtocol(event.getPacket());
		
		int protocolVersion = handshakePacket.getProtocolVersion();
		String host = HologramsPatch.getIP(event.getPlayer());
		
		if (host.equals("localhost")) {
			host = "127.0.0.1";
		}
		
		if (protocolVersion > 5) { // 1.8
			newProtocolClients.add(host);
		}
	}
	
	public boolean hasNewProtocol(Player player) {
		return newProtocolClients.contains(HologramsPatch.getIP(player));
	}
}
