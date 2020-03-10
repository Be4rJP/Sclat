
package be4rjp.sclat.weapon;

import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.weapon.spweapon.BombRush;
import be4rjp.sclat.weapon.spweapon.SuperArmor;
import be4rjp.sclat.weapon.spweapon.SuperSensor;
import be4rjp.sclat.weapon.subweapon.QuickBomb;
import be4rjp.sclat.weapon.subweapon.SplashBomb;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Be4rJP
 */
public class SPWeapon implements Listener{
    //スペシャルウエポンのリスナー部分
    @EventHandler
    public void onClickSPWeapon(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Action action = event.getAction();
        PlayerData data = DataMgr.getPlayerData(player);
        
        if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
            switch (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName()) {
                case "スーパーアーマー":
                    SuperArmor.setArmor(player, 25, 160, true);
                    player.getInventory().setItem(4, new ItemStack(Material.AIR));
                    data.setSPGauge(0);
                    player.setExp(0.99F);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 2);
                    break;
                case "ボムラッシュ":
                    BombRush.BombRushRunnable(player);
                    player.getInventory().setItem(4, new ItemStack(Material.AIR));
                    data.setSPGauge(0);
                    player.setExp(0.99F);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 2);
                    break;
                case "スーパーセンサー":
                    SuperSensor.SuperSensorRunnable(player);
                    player.getInventory().setItem(4, new ItemStack(Material.AIR));
                    data.setSPGauge(0);
                    player.setExp(0.99F);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 2);
                    break;
            }
        }
    }
}
