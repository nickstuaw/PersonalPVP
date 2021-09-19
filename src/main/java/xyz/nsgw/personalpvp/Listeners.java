package xyz.nsgw.personalpvp;

import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import xyz.nsgw.personalpvp.config.GeneralConfig;
import xyz.nsgw.personalpvp.managers.PVPManager;
import xyz.nsgw.personalpvp.managers.TaskManager;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

public class Listeners implements Listener {

    public Listeners(final PPVPPlugin pl) {
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_PLAYERDAMAGE)) {
            pl.getServer().getPluginManager().registerEvents(new DamageByEntityListener(), pl);
        }
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_RODS)) {
            pl.getServer().getPluginManager().registerEvents(new FishingListener(), pl);
        }
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_PROJECTILES)) {
            pl.getServer().getPluginManager().registerEvents(new ProjectileListener(), pl);
        }
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_POTS)) {
            pl.getServer().getPluginManager().registerEvents(new PotionListener(), pl);
        }
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_FIRE)) {
            pl.getServer().getPluginManager().registerEvents(new CombustionListener(), pl);
        }
        if(pl.conf().get().getProperty(GeneralConfig.ABAR_ENABLE)) {
            pl.getServer().getPluginManager().registerEvents(this, pl);
        }
        pl.getServer().getPluginManager().registerEvents(new DeathListener(), pl);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        TaskManager.addUuid(uuid);
        if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_LOGIN_VISIBILITY_DURATION)<1 || TaskManager.ignoredNegative(uuid)) return;
        TaskManager.sendJoinDuration(uuid, PPVPPlugin.inst());
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent e) {
        TaskManager.remUuid(e.getPlayer().getUniqueId());
    }
}
class QuitListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(final PlayerQuitEvent e) {
        PVPManager.reset(e.getPlayer().getUniqueId());
    }
}
class DeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(final PlayerDeathEvent e) {
        if(e.getEntity().getKiller() == null) return;
        e.getDrops().clear();
        e.setKeepInventory(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.KEEPINV_ON_PVP_DEATH));
        e.setKeepLevel(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.KEEPXP_ON_PVP_DEATH));
    }
}
class DamageByEntityListener implements Listener {
    public DamageByEntityListener() {}
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageByEntityEvent e) {
        Entity defender = e.getEntity(), attacker = e.getDamager();
        if(shouldTameablesCancel(attacker, defender)) {
            e.setCancelled(true);
            return;
        }
        if(!(defender instanceof Player) || !(attacker instanceof Player)) return;
        UUID entityUuid = defender.getUniqueId(), damagerUuid = attacker.getUniqueId();
        if(PVPManager.isEitherNegative(entityUuid,damagerUuid)) {
            e.setCancelled(true);
            TaskManager.blockedAttack(entityUuid,damagerUuid);
        }
    }
    private boolean shouldTameablesCancel(final Entity attacker, final Entity defender) {
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.PREVENT_TAMEDDAMAGE)) return false;
        if(!(attacker instanceof Tameable || defender instanceof Tameable)) return false;
        Tameable animal;
        if(attacker instanceof Tameable) {
            animal = (Tameable) attacker;
            if(animal.getOwner() == null || !(animal.getOwner() instanceof Player)) return false;
            if(PVPManager.isPvpDisabled(animal.getOwner().getUniqueId())) return true;
            if(defender instanceof Player) {
                if(PVPManager.isPvpDisabled(defender.getUniqueId())) return true;
            }
        }
        if(defender instanceof Tameable) {
            animal = (Tameable) defender;
            if(animal.getOwner()==null) return false;
            if(!(animal.getOwner() instanceof Player)) return false;
            if(attacker instanceof Player) {
                if(animal.getOwner().equals(attacker)) return false;
            }
            return checkOwners((Tameable) defender);
        }
        return false;
    }
    private boolean checkOwners(Tameable animal) {
        if(animal.getOwner() == null || !(animal.getOwner() instanceof Player)) return false;
        if(PVPManager.isPvpDisabled(animal.getOwner().getUniqueId())) return true;
        return false;
    }
}
class PotionListener implements Listener {
    private final PotionEffectType[] BAD_EFFECTS = new PotionEffectType[]{
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.HARM,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.WEAKNESS
    };
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSplash(final PotionSplashEvent e){
        ProjectileSource shooter = e.getEntity().getShooter();
        if((!(shooter instanceof Player) ||
                e.getAffectedEntities().stream().noneMatch(entity -> entity instanceof Player))) return;
        if(e.getPotion().getEffects().stream().map(PotionEffect::getType).noneMatch(Arrays.asList(this.BAD_EFFECTS)::contains)) return;
        Stream<UUID> stream = e.getAffectedEntities().stream().filter(livingEntity -> livingEntity instanceof Player).map(LivingEntity::getUniqueId);
        if(PVPManager.pvpNegative((((Player) shooter).getUniqueId()))
                || stream.noneMatch(PVPManager::pvpPositive)) {
            e.setCancelled(true);
            ((Player) shooter).getInventory().addItem(e.getEntity().getItem());
            TaskManager.blockedAttack(stream.toArray(UUID[]::new));
        }
    }
}
class ProjectileListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(final ProjectileHitEvent e){
        Projectile projectile = e.getEntity();
        if(e.getHitEntity()==null ||
                !(projectile.getShooter() instanceof Player) ||
                !(e.getHitEntity() instanceof Player)) return;
        Player shooter = (Player) projectile.getShooter();
        UUID shooterUuid = shooter.getUniqueId(), entityUuid = e.getHitEntity().getUniqueId();
        if(PVPManager.isEitherNegative(shooterUuid,entityUuid) && !((projectile instanceof Snowball) || projectile instanceof Egg)) {
            e.setCancelled(true);
            TaskManager.blockedAttack(shooterUuid,entityUuid);
            if((shooter).getGameMode().equals(GameMode.CREATIVE)) return;
            if(projectile instanceof Trident) {
                ItemStack is = ((Trident) projectile).getItemStack();
                projectile.remove();
                shooter.getInventory().addItem(is);
            }
            else if(projectile instanceof AbstractArrow) {
                projectile.remove();
                if(projectile instanceof Arrow)
                    if ((((Arrow)projectile).hasCustomEffects()&&((Arrow)projectile).getBasePotionData().getType().equals(PotionType.UNCRAFTABLE)) &&
                            (shooter.getInventory().getItemInMainHand().containsEnchantment(Enchantment.ARROW_INFINITE)
                                    ||shooter.getInventory().getItemInOffHand().containsEnchantment(Enchantment.ARROW_INFINITE)))
                        return;
                shooter.getInventory().addItem(((AbstractArrow) projectile).getItemStack());
            }
        }
    }
}
class FishingListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(@NotNull PlayerFishEvent e) {
        if(!(e.getCaught() instanceof Player)) return;
        UUID caughtUuid = e.getCaught().getUniqueId(), playerUuid = e.getPlayer().getUniqueId();
        if(PVPManager.isEitherNegative(caughtUuid,playerUuid)) {
            e.setCancelled(true);
            TaskManager.blockedAttack(caughtUuid,playerUuid);
        }
    }
}
class CombustionListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombust(final EntityCombustByEntityEvent e) {
        if(!(e.getCombuster() instanceof Player) ||
                !(e.getEntity() instanceof Player)) return;
        UUID combusterUuid = e.getCombuster().getUniqueId(), entityUuid = e.getEntity().getUniqueId();
        if(PVPManager.isEitherNegative(combusterUuid,entityUuid)) {
            e.setCancelled(true);
            TaskManager.blockedAttack(combusterUuid,entityUuid);
        }
    }
}