package com.gmail.filoghost.holograms.patch;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.ProtocolLibrary;

public class HologramsPatch extends JavaPlugin implements Listener {

	private HandshakeListener handshakeListener;
	private static Set<UUID> newProtocolUUIDs;

	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			getLogger().severe("This plugin requires Holographic Displays to work!");
			setEnabled(false);
			return;
		}
		
		if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
			getLogger().severe("This plugin requires ProtocolLib to work!");
			setEnabled(false);
			return;
		}
		
		newProtocolUUIDs = new HashSet<UUID>();
		handshakeListener = new HandshakeListener(this);
		ProtocolLibrary.getProtocolManager().addPacketListener(handshakeListener);
		ProtocolLibrary.getProtocolManager().addPacketListener(new HologramsPacketListener(this));
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		if (handshakeListener.hasNewProtocol(event.getAddress())) {
			newProtocolUUIDs.add(event.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		newProtocolUUIDs.remove(event.getPlayer().getUniqueId());
	}
	
	public static boolean hasNewProtocol(Player player) {
		return newProtocolUUIDs.contains(player.getUniqueId());
	}	
}
