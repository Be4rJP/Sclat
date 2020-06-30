
package be4rjp.sclat.weapon.subweapon;

import be4rjp.sclat.Main;
import static be4rjp.sclat.Main.conf;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.manager.DamageMgr;
import be4rjp.sclat.manager.DeathMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.raytrace.BoundingBox;
import be4rjp.sclat.raytrace.RayTrace;
import be4rjp.sclat.weapon.Gear;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_13_R1.EnumItemSlot;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityEquipment;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class SplashShield {
    public static void SplashShieldThrowRunnable(Player player){
        BukkitRunnable task = new BukkitRunnable(){
            int i = 0;
            Player p = player;
            Item drop;
            float yaw = 0;
            Vector vec;
            
            @Override
            public void run(){
                try{
                    if(i == 0){
                        p.setExp(p.getExp() - 0.649F);
                        drop = p.getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.ACACIA_FENCE));
                        drop.setVelocity(p.getEyeLocation().getDirection().multiply(0.7));
                        yaw = p.getEyeLocation().getYaw();
                        vec = p.getEyeLocation().getDirection().normalize();
                    }

                    for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                        if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_Bomb()){
                            if(drop.getWorld() == o_player.getWorld()){
                                if(o_player.getLocation().distance(drop.getLocation()) < conf.getConfig().getInt("ParticlesRenderDistance")){
                                    Particle.DustOptions dustOptions = new Particle.DustOptions(DataMgr.getPlayerData(p).getTeam().getTeamColor().getBukkitColor(), 1);
                                    o_player.spawnParticle(Particle.REDSTONE, drop.getLocation(), 1, 0, 0, 0, 50, dustOptions);
                                }
                            }
                        }
                    }

                    if(!DataMgr.getPlayerData(p).isInMatch()){
                        drop.remove();
                        cancel();
                    }
                    
                    if(drop.getLocation().getY() < 0 || drop.getLocation().getY() < p.getLocation().getY() - 100 || drop.isDead())
                        DataMgr.getPlayerData(p).setCanUseSubWeapon(true);

                    if(drop.isOnGround()){
                        Location loc = drop.getLocation();
                        loc.setYaw(yaw);
                        try{
                            for(SplashShieldData ssdata : DataMgr.getSplashShieldDataMapWithPlayer().values()){
                                if(ssdata.getPlayer() == p){
                                    for(ArmorStand as : ssdata.getArmorStandList())
                                        as.remove();
                                    ssdata.getTask().cancel();
                                }
                            }
                        }catch(Exception e){
                            
                        }
                        SplashShieldData ssdata = new SplashShieldData(p);
                        SplashShieldRunnable(p, loc, vec, ssdata);
                        drop.remove();
                        cancel();
                        return;
                    }
                
                    i++;
                }catch(Exception e){
                    cancel();
                    drop.remove();
                }
            }
        };
        if(player.getExp() > 0.65F)
            task.runTaskTimer(Main.getPlugin(), 0, 1);
        else
            player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
        
        BukkitRunnable cooltime = new BukkitRunnable(){
            @Override
            public void run(){
                DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
            }
        };
        cooltime.runTaskLater(Main.getPlugin(), 10);
    }
    
    public static void SplashShieldRunnable(Player player, Location loc, Vector vec, SplashShieldData ssdata){
        List<ArmorStand> list = new ArrayList<ArmorStand>();
        BukkitRunnable task = new BukkitRunnable(){
            int c = 0;
            Player p = player;
            Vector pv = vec.clone();
            @Override
            public void run(){
                try{
                    if(c == 0){
                        p.getWorld().playSound(loc, Sound.ENTITY_ARMOR_STAND_FALL, 1F, 1F);
                        Vector pv2 = pv.clone().multiply(-0.25);
                        float yaw = loc.getYaw();
                        Vector vec1 = new Vector(pv.getZ() * -1, 0, pv.getX()).normalize();
                        Vector vec2 = new Vector(pv.getZ(), 0, pv.getX() * -1).normalize();

                        RayTrace rayTrace1 = new RayTrace(loc.clone().add(0, 0.8, 0).toVector(), vec1);
                        ArrayList<Vector> positions1 = rayTrace1.traverse(3, 0.2);

                        RayTrace rayTrace2 = new RayTrace(loc.clone().add(0, 0.8, 0).toVector(), vec2);
                        ArrayList<Vector> positions2 = rayTrace2.traverse(3, 0.2);

                        ArmorStand as1 = (ArmorStand)player.getWorld().spawnEntity(loc.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
                        list.add(as1);
                        ArmorStand as2 = (ArmorStand)player.getWorld().spawnEntity(loc.clone().add(0, 0.0, 0), EntityType.ARMOR_STAND);
                        list.add(as2);
                        ArmorStand as3 = (ArmorStand)player.getWorld().spawnEntity(loc.clone().add(0, 0.6, 0), EntityType.ARMOR_STAND);
                        list.add(as3);
                        ArmorStand as4 = (ArmorStand)player.getWorld().spawnEntity(loc.clone().add(0, 1.05, 0), EntityType.ARMOR_STAND);
                        list.add(as4);
                        ArmorStand as5 = (ArmorStand)player.getWorld().spawnEntity(loc.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
                        list.add(as5);
                        Location l6 = positions2.get(5).toLocation(loc.getWorld()).add(0, 0.25, 0);
                        l6.setYaw(yaw);
                        ArmorStand as6 = (ArmorStand)player.getWorld().spawnEntity(l6, EntityType.ARMOR_STAND);
                        list.add(as6);
                        Location l7 = positions1.get(4).toLocation(loc.getWorld());
                        l7.setYaw(yaw);
                        ArmorStand as7 = (ArmorStand)player.getWorld().spawnEntity(l7, EntityType.ARMOR_STAND);
                        list.add(as7);
                        Location l8 = positions1.get(7).toLocation(loc.getWorld());
                        l8.setYaw(yaw);
                        ArmorStand as8 = (ArmorStand)player.getWorld().spawnEntity(l8, EntityType.ARMOR_STAND);
                        list.add(as8);
                        Location l9 = positions2.get(4).toLocation(loc.getWorld());
                        l9.setYaw(yaw);
                        ArmorStand as9 = (ArmorStand)player.getWorld().spawnEntity(l9, EntityType.ARMOR_STAND);
                        list.add(as9);
                        Location l10 = positions2.get(7).toLocation(loc.getWorld());
                        l10.setYaw(yaw);
                        ArmorStand as10 = (ArmorStand)player.getWorld().spawnEntity(l10, EntityType.ARMOR_STAND);
                        list.add(as10);
                        Location l11 = positions2.get(4).toLocation(loc.getWorld()).add(0, -0.45, 0);
                        l11.setYaw(yaw);
                        ArmorStand as11 = (ArmorStand)player.getWorld().spawnEntity(l11, EntityType.ARMOR_STAND);
                        list.add(as11);
                        Location l12 = positions2.get(3).toLocation(loc.getWorld()).clone().add(pv2.getX(), -0.1, pv2.getZ());
                        l12.setYaw(yaw);
                        ArmorStand as12 = (ArmorStand)player.getWorld().spawnEntity(l12, EntityType.ARMOR_STAND);
                        as12.setSmall(true);
                        list.add(as12);
                        Location l13 = positions2.get(3).toLocation(loc.getWorld()).clone().add(pv2.getX(), -0.5, pv2.getZ());
                        l13.setYaw(yaw);
                        ArmorStand as13 = (ArmorStand)player.getWorld().spawnEntity(l13, EntityType.ARMOR_STAND);
                        as13.setSmall(true);
                        list.add(as13);
                        Location l14 = positions2.get(10).toLocation(loc.getWorld());
                        l14.setYaw(yaw);
                        ArmorStand as14 = (ArmorStand)player.getWorld().spawnEntity(l14, EntityType.ARMOR_STAND);
                        list.add(as14);

                        ssdata.setArmorStandList(list);

                        int i = 1;
                        for(ArmorStand a : list){
                            DataMgr.setSplashShieldDataWithARmorStand(a, ssdata);
                            DataMgr.ssa.add(a);
                            a.setVisible(false);
                            a.setBasePlate(false);
                            a.setGravity(false);
                            a.setCustomName("SplashShield");
                            //if(i > 6)
                                //a.teleport(new Location(a.getWorld(), a.getLocation().getX(), a.getLocation().getY(), a.getLocation().getZ(), yaw, 0F));


                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(i <= 5)
                                    ((CraftPlayer)target).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(a.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.BLAZE_ROD))));
                                if(i > 11 && i < 14)
                                    ((CraftPlayer)target).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(a.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(p).getTeam().getTeamColor().getGlass()))));
                            }
                            i++;
                        }
                        as1.setHeadPose(new EulerAngle(0, 0, Math.toRadians(135)));
                        as2.setHeadPose(new EulerAngle(0, 0, Math.toRadians(135)));
                        as3.setHeadPose(new EulerAngle(0, 0, Math.toRadians(135)));
                        as4.setHeadPose(new EulerAngle(0, 0, Math.toRadians(135)));
                        as5.setHeadPose(new EulerAngle(0, 0, Math.toRadians(135)));
                        as6.setHeadPose(new EulerAngle(0, 0, Math.toRadians(270)));
                        as7.setHeadPose(new EulerAngle(0, 0, Math.toRadians(45)));
                        as8.setHeadPose(new EulerAngle(0, 0, Math.toRadians(45)));
                        as9.setHeadPose(new EulerAngle(0, 0, Math.toRadians(45)));
                        as10.setHeadPose(new EulerAngle(0, 0, Math.toRadians(45)));
                    }

                    if(c == 10){
                        int i = 1;
                        for(ArmorStand a : list){
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(i > 5 && i <= 11)
                                    ((CraftPlayer)target).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(a.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.STICK))));
                            }
                            i++;
                        }
                        p.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F);
                    }

                    if(c >= 15 && c % 2 == 0){
                        Vector vec1 = new Vector(pv.getZ() * -1, 0, pv.getX()).normalize();
                        Vector vec2 = new Vector(pv.getZ(), 0, pv.getX() * -1).normalize();

                        RayTrace rayTrace1 = new RayTrace(loc.clone().add(0, 2.7, 0).toVector(), vec1);
                        ArrayList<Vector> positions1 = rayTrace1.traverse(3, 0.38);

                        RayTrace rayTrace2 = new RayTrace(loc.clone().add(0, 2.7, 0).toVector(), vec2);
                        ArrayList<Vector> positions2 = rayTrace2.traverse(3, 0.38);

                        Vector sv = pv.clone().multiply(-0.25);

                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool().createBlockData();
                        ray : for(int i = 0; i < positions1.size() - 4;i++){
                            Location position = positions1.get(i).toLocation(p.getLocation().getWorld());
                            PaintMgr.PaintHightestBlock(position, p, false, false);
                            double damage = 10;
                            RayTrace rayTrace4 = new RayTrace(position.clone().add(sv.getX(), 0, sv.getZ()).toVector(), new Vector(0, -1, 0));
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(target.getWorld() == p.getWorld()){
                                    if(rayTrace4.intersects(new BoundingBox((Entity)target), 3, 0.5) && DataMgr.getPlayerData(target).getTeam() != DataMgr.getPlayerData(p).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)){
                                        if(target.getHealth() + DataMgr.getPlayerData(target).getArmor() > damage){
                                            DamageMgr.SclatGiveStrongDamage(target, damage, player);
                                            PaintMgr.Paint(target.getLocation(), player, true);
                                        }else{
                                            target.setGameMode(GameMode.SPECTATOR);
                                            DeathMgr.PlayerDeathRunnable(target, player, "subWeapon");
                                            PaintMgr.Paint(target.getLocation(), player, true);
                                        }

                                        //AntiNoDamageTime
                                        BukkitRunnable task = new BukkitRunnable(){
                                            Player p = target;
                                            @Override
                                            public void run(){
                                                target.setNoDamageTicks(0);
                                            }
                                        };
                                        task.runTaskLater(Main.getPlugin(), 1);
                                    }
                                }
                            }


                            for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                o_player.spawnParticle(org.bukkit.Particle.FALLING_DUST, position.add(sv.getX(), 0, sv.getZ()), 1, 0, 0, 0, 200, bd);
                            }
                        }

                        ray : for(int i = 0; i < positions2.size() - 1;i++){
                            Location position = positions2.get(i).toLocation(p.getLocation().getWorld());
                            PaintMgr.PaintHightestBlock(position, p, false, false);
                            double damage = 10;
                            RayTrace rayTrace4 = new RayTrace(position.clone().add(sv.getX(), 0, sv.getZ()).toVector(), new Vector(0, -1, 0));
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(target.getWorld() == p.getWorld()){
                                    if(rayTrace4.intersects(new BoundingBox((Entity)target), 3, 0.5) && DataMgr.getPlayerData(target).getTeam() != DataMgr.getPlayerData(p).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)){
                                        if(target.getHealth() + DataMgr.getPlayerData(target).getArmor() > damage){
                                            DamageMgr.SclatGiveStrongDamage(target, damage, player);
                                            PaintMgr.Paint(target.getLocation(), player, true);
                                        }else{
                                            target.setGameMode(GameMode.SPECTATOR);
                                            DeathMgr.PlayerDeathRunnable(target, player, "subWeapon");
                                            PaintMgr.Paint(target.getLocation(), player, true);
                                        }

                                        //AntiNoDamageTime
                                        BukkitRunnable task = new BukkitRunnable(){
                                            Player p = target;
                                            @Override
                                            public void run(){
                                                target.setNoDamageTicks(0);
                                            }
                                        };
                                        task.runTaskLater(Main.getPlugin(), 1);
                                    }
                                }
                            }
                            for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(i == 0 || i == 1 || i == 2)
                                    o_player.spawnParticle(org.bukkit.Particle.FALLING_DUST, position.add(sv.getX(), -0.2, sv.getZ()), 1, 0, 0, 0, 200, bd);
                                else
                                    o_player.spawnParticle(org.bukkit.Particle.FALLING_DUST, position.add(sv.getX(), 0, sv.getZ()), 1, 0, 0, 0, 200, bd);
                            }
                        }
                    }

                    if(c == 110){
                        int i = 1;
                        for(ArmorStand a : list){
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(i == 12)
                                    ((CraftPlayer)target).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(a.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.WHITE_STAINED_GLASS))));
                            }
                            i++;
                        }
                    }


                    if(c > 200 || !DataMgr.getPlayerData(p).isInMatch() || ssdata.getDamage() > 40){
                        for(ArmorStand a : list)
                            a.remove();
                        list.get(0).getWorld().playSound(list.get(0).getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8F, 0.8F);
                        cancel();
                        return;
                    }
                    c++;
                }catch(Exception e){
                    cancel();
                    for(ArmorStand a : list)
                        a.remove();
                }
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
        ssdata.setTask(task);
        DataMgr.setSplashShieldDataWithPlayer(player, ssdata);
    }
}