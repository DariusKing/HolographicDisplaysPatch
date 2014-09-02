package com.gmail.filoghost.holograms.patch;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

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
		String serverHostname = event.getPlayer().getAddress().getHostName();
		
		if (serverHostname.equals("localhost")) {
			serverHostname = "127.0.0.1";
		}
		
		if (protocolVersion > 5) { // 1.8
			newProtocolClients.add(serverHostname);
		}
	}
	
	public boolean hasNewProtocol(InetAddress inetAddress) {
		return newProtocolClients.contains(inetAddress.getHostAddress());
	}
}
