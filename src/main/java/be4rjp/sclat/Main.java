package be4rjp.sclat;

import be4rjp.sclat.GUI.ClickListener;
import be4rjp.sclat.GUI.OpenGUI;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.MapData;
import be4rjp.sclat.data.Match;
import be4rjp.sclat.data.PaintData;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.listener.SquidListener;
import be4rjp.sclat.lunachat.LunaChatListener;
import be4rjp.sclat.manager.*;
import be4rjp.sclat.weapon.MainWeapon;
import be4rjp.sclat.weapon.SnowballListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @author Be4rJP
 */
public class Main extends JavaPlugin implements PluginMessageListener{
    
    public static Config conf;
    
    private static Main plugin;
    
    public static Location lobby;
    
    public static Glow glow;
    
    public static List<Player> pdspList;
    
    public static boolean tutorial = false;
    
    public static ServerType type = ServerType.NORMAL;
    
    //API
    public static boolean NoteBlockAPI = true;
    public static boolean LunaChat = true;
    
    //重複しない数字
    //ボム等で使用
    private static int NDNumber = 0;
    

    @Override
    public void onEnable() {
        plugin = this;
        glow = new Glow();
        
        pdspList = new ArrayList<>();
        
        
        //----------------------------APICheck-------------------------------
        NoteBlockAPI = true;
        if (!Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI")){
            getLogger().severe("*** NoteBlockAPI is not installed or not enabled. ***");
            NoteBlockAPI = false;
            return;
        }
        
        //LunaChat
        if (!Bukkit.getPluginManager().isPluginEnabled("LunaChat")){
            getLogger().severe("*** LunaChat is not installed or not enabled. ***");
            LunaChat = false;
        }
        //-------------------------------------------------------------------
        
        
        
        //--------------------------Load config------------------------------
        getLogger().info("Loading config files...");
        conf = new Config();
        conf.LoadConfig();
        for (String mapname : conf.getMapConfig().getConfigurationSection("Maps").getKeys(false))
            Bukkit.createWorld(new WorldCreator(conf.getMapConfig().getString("Maps." + mapname + ".WorldName")));
        if(conf.getConfig().contains("Tutorial"))
            tutorial = conf.getConfig().getBoolean("Tutorial");
        //-------------------------------------------------------------------
        
        
        
        //--------------------------Lobby location---------------------------
        String WorldName = conf.getConfig().getString("Lobby.WorldName");
        Bukkit.createWorld(new WorldCreator(WorldName));
        World w = Bukkit.getWorld(WorldName);
        int ix = conf.getConfig().getInt("Lobby.X");
        int iy = conf.getConfig().getInt("Lobby.Y");
        int iz = conf.getConfig().getInt("Lobby.Z");
        int iyaw = conf.getConfig().getInt("Lobby.Yaw");
        lobby = new Location(w, ix + 0.5, iy, iz + 0.5);
        lobby.setYaw(iyaw);
        //-------------------------------------------------------------------
        
        
        
        //------------------------RegisteredEvents---------------------------
        getLogger().info("RegisteredEvents...");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GameMgr(), this);
        pm.registerEvents(new SquidListener(), this);
        pm.registerEvents(new ClickListener(), this);
        pm.registerEvents(new be4rjp.sclat.weapon.MainWeapon(), this);
        pm.registerEvents(new be4rjp.sclat.weapon.SubWeapon(), this);
        pm.registerEvents(new be4rjp.sclat.weapon.SPWeapon(), this);
        pm.registerEvents(new SnowballListener(), this);
        
        if(LunaChat)
            pm.registerEvents(new LunaChatListener(), this);
        //-------------------------------------------------------------------

        
        
        //------------------------Setup from config--------------------------
        getLogger().info("SetupColor...");
        ColorMgr.SetupColor();
        getLogger().info("SetupMainWeapon...");
        MainWeaponMgr.SetupMainWeapon();
        getLogger().info("WeaponClassSetup...");
        WeaponClassMgr.WeaponClassSetup();
        getLogger().info("Setup Map...");
        getLogger().info("");
        getLogger().info("-----------------MAP LIST-----------------");
        MapDataMgr.SetupMap();
        getLogger().info("------------------------------------------");
        getLogger().info("");
        getLogger().info("MatchSetup...");
        MatchMgr.MatchSetup();
        getLogger().info("Setup is finished!");
        //-------------------------------------------------------------------
        
        
        
        //---------------------Enable mode message---------------------------
        int length = conf.getConfig().getString("WorkMode").length();
        
        StringBuilder buff = new StringBuilder();
        buff.append("### This plugin started in [");
        buff.append(conf.getConfig().getString("WorkMode"));
        buff.append("] mode! ");
        for (int i = 0; i < 7 - length; i++){
            buff.append(" ");
        }
        buff.append("###");
        
        getLogger().info("##############################################");
        getLogger().info("###                                        ###");
        getLogger().info(buff.toString());
        getLogger().info("###                                        ###");
        getLogger().info("##############################################");
        //-------------------------------------------------------------------
        
        
        
        //------------------------Only trial mode----------------------------
        if(conf.getConfig().getString("WorkMode").equals("Trial")){
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();

            Match match = DataMgr.getMatchFromId(MatchMgr.matchcount);

            org.bukkit.scoreboard.Team bteam0 = scoreboard.registerNewTeam(match.getTeam0().getTeamColor().getColorName());
            bteam0.setColor(match.getTeam0().getTeamColor().getChatColor());
            bteam0.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            bteam0.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);

            org.bukkit.scoreboard.Team bteam1 = scoreboard.registerNewTeam(match.getTeam1().getTeamColor().getColorName());
            bteam1.setColor(match.getTeam1().getTeamColor().getChatColor());
            bteam1.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            bteam1.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);

            match.getTeam0().setTeam(bteam0);
            match.getTeam1().setTeam(bteam1);
            
            ArmorStandMgr.ArmorStandEquipPacketSender(w);
        }
        //-------------------------------------------------------------------
        
        
        
        //------------------------BungeeCord setup---------------------------
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        //-------------------------------------------------------------------
        
        
        
        //------------------------Load NBS songs-----------------------------
        if(NoteBlockAPI)
            NoteBlockAPIMgr.LoadSongFiles();
        //-------------------------------------------------------------------
        
        
        
        //--------------------------Server type------------------------------
        if(conf.getConfig().contains("ServerType")) {
            switch (conf.getConfig().getString("ServerType")){
                case "NORMAL":
                    type = ServerType.NORMAL;
                    break;
                case "LOBBY":
                    type = ServerType.LOBBY;
                    break;
                case "MATCH":
                    type = ServerType.MATCH;
                    break;
            }
        }
        //-------------------------------------------------------------------
        
        
        
        //---------------------------Server status---------------------------
        if(type == ServerType.LOBBY)
            ServerStatusManager.setupServerStatusGUI();
        //-------------------------------------------------------------------
    }
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("SomeSubChannel")) {
          
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase("setUpdateBlockCount")){
            if (args.length != 0) {
                String num = args[0];
                boolean result = true;
                for(int i = 0; i < num.length(); i++) {
                    if(Character.isDigit(num.charAt(i))) {
                    }else{
                        result = false;
                        break;
                    }
                }
                if(result){
                    conf.getConfig().set("OneTickUpdateBlocks", Integer.valueOf(num));
                    sender.sendMessage("setConfig [OneTickUpdateBlocks]  :  " + num);
                    return true;
                }else{
                    sender.sendMessage("Please type with number");
                    return false;
                }
            }
        }
        return false;
    }


    @Override
    public void onDisable() {
        
        //Wiremeshの停止
        try {
            for(MapData mData : DataMgr.maplist)
                if(mData.getWiremeshListTask() != null)
                    mData.getWiremeshListTask().stopTask();
        } catch (Exception e) {}
        
        //塗りリセット
        for(PaintData data : DataMgr.getBlockDataMap().values()){
            data.getBlock().setType(data.getOriginalType());
            if(data.getBlockData() != null)
                data.getBlock().setBlockData(data.getBlockData());
        }
        DataMgr.getBlockDataMap().clear();
        
        /*
        for(Block block : DataMgr.rblist){
            block.setType(Material.AIR);
            DataMgr.rblist.remove(block);
        }*/
        
        for(ArmorStand as : DataMgr.getArmorStandMap().keySet())
            as.remove();
        conf.SaveConfig();
        
        for(ArmorStand as : DataMgr.al)
            as.remove();
    
    
        if(type == ServerType.LOBBY){
            ServerStatusManager.stopTask();
        }
    }
    
    public static Main getPlugin(){
        return plugin;
    }
    
    public static int getNotDuplicateNumber(){
        NDNumber++;
        return NDNumber;
    }
    
}


