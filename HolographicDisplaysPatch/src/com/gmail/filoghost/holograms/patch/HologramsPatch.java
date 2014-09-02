package com.gmail.filoghost.holograms.patch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		
		Boat boat = player.getWorld().spawn(player.getLocation(), Boat.class);
		sendCustomNamePacket(player, boat.getEntityId());
		
		player.sendMessage("Spawned");
		return true;
	}
	
	public static void sendCustomNamePacket(Player player, int entityID) {
		WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata();
		metadataPacket.setEntityId(entityID);
		metadataPacket.setEntityMetadata(Arrays.asList(
			new WrappedWatchableObject(2, "CUSTOM NAME - TEST"),
			new WrappedWatchableObject(3, Byte.valueOf((byte) 1))	
		));
		
		metadataPacket.sendPacket(player);
	}
	
	
}
