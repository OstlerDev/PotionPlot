package me.ostlerdev.plugins.potionplot;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.RegionSelection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

public class Potionplot extends JavaPlugin implements Listener {
	Plugin plugin = this;
	private WorldEditPlugin we = null;
	private WorldGuardPlugin wg = null;
	int tenID = 1;
	int twentyID = 1;
	int thirtyID = 1;
	int fourtyID = 1;
	int nextID = 1;
	
    public void onDisable() {
        // TODO: Place any custom disable code here.
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        if (getWorldGuard() != null) System.out.println("[FinalProtect] hooked into WorldGuard.");
        if (getWorldEdit() != null) System.out.println("[FinalProtect] hooked into WorldEdit.");
        plugin = this;
        System.out.println("[FinalProtect] Loading Config.");
        loadConfiguration();
        tenID = plugin.getConfig().getInt("BlockIDsForPlotSizes.10x10");
    	twentyID = plugin.getConfig().getInt("BlockIDsForPlotSizes.20x20");
    	thirtyID = plugin.getConfig().getInt("BlockIDsForPlotSizes.30x30");
    	fourtyID = plugin.getConfig().getInt("BlockIDsForPlotSizes.40x40");
    	nextID = plugin.getConfig().getInt("DoNotEdit.NextId");
        System.out.println("[FinalProtect] Config Loaded.");
    }
    ItemStack item = new ItemStack(0);
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if (event.getPlayer().getItemInHand().getTypeId() == tenID && event.getPlayer().hasPermission("FinalProtect.use.10"))
    	{
    		event.getPlayer().sendMessage("Creating your 10x10 Plot now!");
    		createRegion(event.getPlayer(), PotionType.Potion10x10);
    		event.getPlayer().setItemInHand(item);
    	}
    	else if (event.getPlayer().getItemInHand().getTypeId() == twentyID && event.getPlayer().hasPermission("FinalProtect.use.20"))
    	{
    		event.getPlayer().sendMessage("Creating your 20x20 Plot now!");
    		createRegion(event.getPlayer(), PotionType.Potion20x20);
    		event.getPlayer().setItemInHand(item);
    	}
    	else if (event.getPlayer().getItemInHand().getTypeId() == thirtyID && event.getPlayer().hasPermission("FinalProtect.use.30"))
    	{
    		event.getPlayer().sendMessage("Creating your 30x30 Plot now!");
    		createRegion(event.getPlayer(), PotionType.Potion30x30);
    		event.getPlayer().setItemInHand(item);
    	}
    	else if (event.getPlayer().getItemInHand().getTypeId() == fourtyID && event.getPlayer().hasPermission("FinalProtect.use.40"))
    	{
    		event.getPlayer().sendMessage("Creating your 40x40 Plot now!");
    		createRegion(event.getPlayer(), PotionType.Potion40x40);
    		event.getPlayer().setItemInHand(item);
    	}
    }
    
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	ItemStack item = new ItemStack(1);
    	event.getPlayer().setItemInHand(item);
    }
    
    private WorldGuardPlugin getWorldGuard() {
        plugin = getServer().getPluginManager().getPlugin("WorldGuard");
     
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }
     
        return (WorldGuardPlugin) plugin;
    }
    
    private WorldEditPlugin getWorldEdit() {
        plugin = getServer().getPluginManager().getPlugin("WorldEdit");
     
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null; // Maybe you want throw an exception instead
        }
     
        return (WorldEditPlugin) plugin;
    }
    
    enum PotionType {Potion10x10, Potion20x20, Potion30x30, Potion40x40}
    
    public void createRegion(Player player, PotionType potionType)
    {
    	
    	WorldGuardPlugin worldGuard = getWorldGuard();
    	RegionManager mgr = worldGuard.getRegionManager(player.getWorld());
    	
    	if (!worldGuard.canBuild(player, player.getEyeLocation())) return;
    	
    	plugin = this;
    	ProtectedRegion region = null;
        String id = player.getName()+plugin.getConfig().getInt("DoNotEdit.NextID");//TODO:add in the ID of region from config
        plugin.getConfig().set("DoNotEdit.NextID", 1+plugin.getConfig().getInt("DoNotEdit.NextID"));
        plugin.saveConfig();
        
        Location playerBlock = player.getEyeLocation();
        BlockVector min;
        BlockVector max;
        
		switch(potionType){
        case Potion10x10:
        	min = new BlockVector(playerBlock.getBlockX()-5, playerBlock.getBlockY()-128, playerBlock.getBlockZ()-5);
            max = new BlockVector(playerBlock.getBlockX()+4, playerBlock.getBlockY()+256, playerBlock.getBlockZ()+4);
            region = new ProtectedCuboidRegion(id, min, max);
            double x = playerBlock.getBlockX()-5;
        	double y = playerBlock.getBlockY()-2;
        	double z = playerBlock.getBlockZ()-5;
        	
        	for (int a = 0; a <10; a++){
            
        		for (int i = playerBlock.getBlockX()-5; i < playerBlock.getBlockX()+5; i++)
        		{
        			World world = player.getWorld();
        			Location blockLocation = new Location(world, i, y, z);
        			blockLocation.getBlock().setTypeId(1);
        		}
        		z++;
        	}
        	y++;
        	z = playerBlock.getBlockZ()-5;
        	for (int i = playerBlock.getBlockX()-5; i < playerBlock.getBlockX()+5; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	for (int i = playerBlock.getBlockZ()-5; i < playerBlock.getBlockZ()+5; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	z = playerBlock.getBlockZ()+4;
        	for (int i = playerBlock.getBlockX()-5; i < playerBlock.getBlockX()+5; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	x = playerBlock.getBlockX()+4;
        	for (int i = playerBlock.getBlockZ()-5; i < playerBlock.getBlockZ()+5; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	break;
        case Potion20x20:
        	min = new BlockVector(playerBlock.getBlockX()-10, playerBlock.getBlockY()-128, playerBlock.getBlockZ()-10);
            max = new BlockVector(playerBlock.getBlockX()+9, playerBlock.getBlockY()+256, playerBlock.getBlockZ()+9);
            region = new ProtectedCuboidRegion(id, min, max);
            x = playerBlock.getBlockX()-10;
        	y = playerBlock.getBlockY()-2;
        	z = playerBlock.getBlockZ()-10;
        	
        	for (int a = 0; a <20; a++){
            
        		for (int i = playerBlock.getBlockX()-10; i < playerBlock.getBlockX()+10; i++)
        		{
        			World world = player.getWorld();
        			Location blockLocation = new Location(world, i, y, z);
        			blockLocation.getBlock().setTypeId(1);
        		}
        		z++;
        	}
        	y++;
        	z = playerBlock.getBlockZ()-10;
        	for (int i = playerBlock.getBlockX()-10; i < playerBlock.getBlockX()+10; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	for (int i = playerBlock.getBlockZ()-10; i < playerBlock.getBlockZ()+10; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	z = playerBlock.getBlockZ()+9;
        	for (int i = playerBlock.getBlockX()-10; i < playerBlock.getBlockX()+10; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	x = playerBlock.getBlockX()+9;
        	for (int i = playerBlock.getBlockZ()-10; i < playerBlock.getBlockZ()+10; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	break;
        case Potion30x30:
        	min = new BlockVector(playerBlock.getBlockX()-15, playerBlock.getBlockY()-128, playerBlock.getBlockZ()-15);
            max = new BlockVector(playerBlock.getBlockX()+14, playerBlock.getBlockY()+256, playerBlock.getBlockZ()+14);
            region = new ProtectedCuboidRegion(id, min, max);
            x = playerBlock.getBlockX()-15;
        	y = playerBlock.getBlockY()-2;
        	z = playerBlock.getBlockZ()-15;
        	
        	for (int a = 0; a <30; a++){
            
        		for (int i = playerBlock.getBlockX()-15; i < playerBlock.getBlockX()+15; i++)
        		{
        			World world = player.getWorld();
        			Location blockLocation = new Location(world, i, y, z);
        			blockLocation.getBlock().setTypeId(1);
        		}
        		z++;
        	}
        	y++;
        	z = playerBlock.getBlockZ()-15;
        	for (int i = playerBlock.getBlockX()-15; i < playerBlock.getBlockX()+15; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	for (int i = playerBlock.getBlockZ()-15; i < playerBlock.getBlockZ()+15; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	z = playerBlock.getBlockZ()+14;
        	for (int i = playerBlock.getBlockX()-15; i < playerBlock.getBlockX()+15; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	x = playerBlock.getBlockX()+14;
        	for (int i = playerBlock.getBlockZ()-15; i < playerBlock.getBlockZ()+15; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	break;
        case Potion40x40:
        	min = new BlockVector(playerBlock.getBlockX()-20, playerBlock.getBlockY()-128, playerBlock.getBlockZ()-20);
            max = new BlockVector(playerBlock.getBlockX()+19, playerBlock.getBlockY()+256, playerBlock.getBlockZ()+19);
            region = new ProtectedCuboidRegion(id, min, max);
            x = playerBlock.getBlockX()-20;
        	y = playerBlock.getBlockY()-2;
        	z = playerBlock.getBlockZ()-20;
        	
        	for (int a = 0; a < 40; a++){
            
        		for (int i = playerBlock.getBlockX()-20; i < playerBlock.getBlockX()+20; i++)
        		{
        			World world = player.getWorld();
        			Location blockLocation = new Location(world, i, y, z);
        			blockLocation.getBlock().setTypeId(1);
        		}
        		z++;
        	}
        	y++;
        	z = playerBlock.getBlockZ()-20;
        	for (int i = playerBlock.getBlockX()-20; i < playerBlock.getBlockX()+20; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	for (int i = playerBlock.getBlockZ()-20; i < playerBlock.getBlockZ()+20; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	z = playerBlock.getBlockZ()+19;
        	for (int i = playerBlock.getBlockX()-20; i < playerBlock.getBlockX()+20; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, i, y, z);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	x = playerBlock.getBlockX()+19;
        	for (int i = playerBlock.getBlockZ()-20; i < playerBlock.getBlockZ()+20; i++)
        	{
        		World world = player.getWorld();
    			Location blockLocation = new Location(world, x, y, i);
    			blockLocation.getBlock().setType(Material.FENCE);
        	}
        	break;
        default:
        	min = new BlockVector(playerBlock.getBlockX()-5, playerBlock.getBlockY(), playerBlock.getBlockZ()-5);
            max = new BlockVector(playerBlock.getBlockX()+5, playerBlock.getBlockY(), playerBlock.getBlockZ()+5);
            region = new ProtectedCuboidRegion(id, min, max);
        	break;
        }

        // Get the list of region owners
        String[] stringArray;
        stringArray = new String[1];
        stringArray[0] = player.getDisplayName();
        
        
        region.setOwners(RegionDBUtil.parseDomainString(stringArray, 0));
        
        mgr.addRegion(region);
        
        try {
            mgr.save();
            player.sendMessage(ChatColor.YELLOW + "Region sucessfully created (" + id + ")");
        } catch (ProtectionDatabaseException e) {
            player.sendMessage("Please tell a server admin that PotionPlots has encountered error 1");
        }
    }

    public void loadConfiguration(){
        //See "Creating you're defaults"
    	plugin.getConfig().addDefault("BlockIDsForPlotSizes.10x10", 1);
    	plugin.getConfig().addDefault("BlockIDsForPlotSizes.20x20", 2);
    	plugin.getConfig().addDefault("BlockIDsForPlotSizes.30x30", 3);
    	plugin.getConfig().addDefault("BlockIDsForPlotSizes.40x40", 4);
    	plugin.getConfig().addDefault("DoNotEdit.NextID", 1);
        plugin.getConfig().options().copyDefaults(true); // NOTE: You do not have to use "plugin." if the class extends the java plugin
        //Save the config whenever you manipulate it
        plugin.saveConfig();
    }
}

