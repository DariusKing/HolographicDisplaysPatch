package com.gmail.filoghost.holograms.patch;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

public class HologramsPatch extends JavaPlugin implements Listener {

	private HandshakeListener handshakeListener;
	private static Set<Player> newProtocolPlayers;

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
		
		newProtocolPlayers = new HashSet<Player>();
		handshakeListener = new HandshakeListener(this);
		ProtocolLibrary.getProtocolManager().addPacketListener(handshakeListener);
		ProtocolLibrary.getProtocolManager().addPacketListener(new HologramsPacketListener(this));
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (handshakeListener.hasNewProtocol(event.getPlayer())) {
			newProtocolPlayers.add(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		newProtocolPlayers.remove(event.getPlayer());
		handshakeListener.clear(event.getPlayer());
	}

	
	public static boolean hasNewProtocol(Player player) {
		return newProtocolPlayers.contains(player);
	}	
}
