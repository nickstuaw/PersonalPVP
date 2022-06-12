/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp;

import com.nsgwick.personalpvp.managers.TaskManager;
import org.bukkit.Bukkit;
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
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import com.nsgwick.personalpvp.config.GeneralConfig;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Listeners implements Listener {

    /*
    Constructor
     */
    public Listeners(final PPVPPlugin pl) {
        /*
        If player damage prevention is enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_PLAYERDAMAGE)) {
            /*
            Register the listener.
             */
            pl.getServer().getPluginManager().registerEvents(new DamageByEntityListener(), pl);
        }
        /*
        If fishing rod interception is enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_RODS)) {
            /*
            Register the listener.
             */
            pl.getServer().getPluginManager().registerEvents(new FishingListener(), pl);
        }
        /*
        If projectile protection is enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_PROJECTILES)) {
            /*
            Register the listener.
             */
            pl.getServer().getPluginManager().registerEvents(new ProjectileListener(), pl);
        }
        /*
        If potion protection is enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_POTS)) {
            /*
            Register the listener.
             */
            pl.getServer().getPluginManager().registerEvents(new PotionListener(), pl);
        }
        /*
        If fire protection is enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.PREVENT_FIRE)) {
            /*
            Register the listener.
             */
            pl.getServer().getPluginManager().registerEvents(new CombustionListener(), pl);
        }
        /*
        If actionbars are enabled,
         */
        if(pl.conf().get().getProperty(GeneralConfig.ABAR_ENABLE)) {
            /*
            Register the listener.
             */
            pl.getServer().getPluginManager().registerEvents(this, pl);
        }
        /*
        Register the death listener.
         */
        pl.getServer().getPluginManager().registerEvents(new DeathListener(), pl);
    }
    /*
    When a player joins,
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent e) {
        /*
        Get the player's uuid.
         */
        UUID uuid = e.getPlayer().getUniqueId();
        /*
        Add the player's uuid to the list of online players.
         */
        TaskManager.addOnlineUuid(uuid);
        /*
        If the post-login actionbar visibility time is smaller than 1 second, or it's shown to the player, return.
         */
        if(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.ABAR_LOGIN_VISIBILITY_DURATION) < 1
                || TaskManager.isPlayerActionbarShown(uuid)) return;
        /*
        Send the temporary actionbar to the player .
         */
        TaskManager.sendJoinDuration(uuid, PPVPPlugin.inst());
    }
    /*
    When a player quits,
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent e) {
        /*
        Remove the player's uuid from the list of online players.
         */
        TaskManager.removeOnlineUuid(e.getPlayer().getUniqueId());
    }
}
class DeathListener implements Listener {
    /*
    When a player dies,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(final PlayerDeathEvent e) {
        /*
        If the killer is null (offline), skip.
         */
        if(e.getEntity().getKiller() == null) return;
        /*
        Set whether or not to keep xp.
         */
        e.setKeepLevel(PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.KEEPXP_ON_PVP_DEATH));
        /*
        If keep inventory is disabled in the config, skip. Otherwise, continue.
         */
        if(!PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.KEEPINV_ON_PVP_DEATH)) return;
        /*
        Keep inventory is true so clear the drops
         */
        e.getDrops().clear();
        /*
        and enable keep inventory.
         */
        e.setKeepInventory(true);
    }
}
class DamageByEntityListener implements Listener {
    /*
    Constructor
     */
    public DamageByEntityListener() {}
    /*
    When an entity is damaged by another entity,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageByEntityEvent e) {
        /*
        Save the defender and the attacker.
         */
        Entity defender = e.getEntity(), attacker = e.getDamager();
        /*
        If a tamed animal is/tamed animals are involved, check the pvp status of its owner/their owners
         */
        if(Utils.Tameables.shouldTameablesCancel(attacker, defender)) {
            /*
            If pvp is off for at least 1 combatant, cancel the damage event and skip.
             */
            e.setCancelled(true);
            return;
        }
        /*
        If the defender and the attacker are both players, continue. Otherwise skip.
         */
        if(!(defender instanceof Player && attacker instanceof Player)) return;
        /*
        Save the UUID of each player that's involved.
         */
        UUID entityUuid = defender.getUniqueId(), damagerUuid = attacker.getUniqueId();
        /*
        If the attacking entity is a cloud of potion effects,
         */
        if(attacker instanceof AreaEffectCloud) {
            /*
            Get the attacker as an AreaEffectCloud entity to access the potion effects.
             */
            AreaEffectCloud cloud = (AreaEffectCloud) attacker;
            /*
            If any bad effects are inside the cloud, cancel the event.
             */
            if(cloud.getCustomEffects()
                    .stream().map(PotionEffect::getType)
                    .anyMatch(Arrays.asList(Utils.BAD_EFFECTS)::contains)) {
                e.setCancelled(true);
            }
            /*
            Skip.
             */
            return;
        }
        /*
        If a combatant has pvp off,
         */
        if(PPVPPlugin.inst().pvp().isEitherNegative(entityUuid,damagerUuid)) {
            /*
            Cancel the damage event.
             */
            e.setCancelled(true);
            /*
            Send a pvp alert if necessary.
             */
            TaskManager.blockedAttack(entityUuid,damagerUuid);
        }
    }
}
class PotionListener implements Listener {
    /*
    When a cloud of potion effects is applied on an entity,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCloud(final AreaEffectCloudApplyEvent e) {
        /*
        If there are no players involved, return.
         */
        if(e.getAffectedEntities().stream().noneMatch(livingEntity -> livingEntity instanceof Player)) return;
        /*
        Save the uuids of any players involved.
         */
        List<UUID> uuidsOfPlayersInvolved = e.getAffectedEntities().stream()
                .filter(livingEntity -> livingEntity instanceof Player)
                .map(LivingEntity::getUniqueId).collect(Collectors.toList());
        /*
        If the base effect of the effect cloud is a bad effect,
         */
        if(Arrays.asList(Utils.BAD_EFFECTS).contains(e.getEntity().getBasePotionData().getType().getEffectType())) {
            /*
            For every player's uuid,
             */
            uuidsOfPlayersInvolved.forEach(p -> {
                /*
                If the player has pvp disabled,
                 */
                if(PPVPPlugin.inst().pvp().pvpNegative(p)) {
                    /*
                    Remove the player from the list of affected entities (negate the effect).
                     */
                    e.getAffectedEntities().remove(Bukkit.getPlayer(p));
                }
            });
        }
        /*
        If the cloud has any harmful effects, continue. Otherwise, skip.
         */
        if(e.getEntity().getCustomEffects().stream().map(PotionEffect::getType).noneMatch(Arrays.asList(Utils.BAD_EFFECTS)::contains)) return;
        /*
        For each player uuid,
         */
        uuidsOfPlayersInvolved.forEach(p -> {
            /*
            If they have pvp disabled,
             */
            if(PPVPPlugin.inst().pvp().pvpNegative(p)) {
                    /*
                    Remove the player from the list of affected entities (negate the effect).
                     */
                e.getAffectedEntities().remove(Bukkit.getPlayer(p));
            }
        });
    }
    /*
    When a splash potion lands,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSplash(final PotionSplashEvent e){
        /*
        Save the potion thrower.
         */
        ProjectileSource shooter = e.getEntity().getShooter();
        /*
        If the potion thrower is not a player or there are no players affected, skip.
         */
        if((!(shooter instanceof Player) ||
                e.getAffectedEntities().stream()
                        .noneMatch(entity -> entity instanceof Player))) return;
        /*
        If there are no bad effects in the cloud made, skip,
         */
        if(e.getPotion().getEffects().stream().map(PotionEffect::getType)
                .noneMatch(Arrays.asList(Utils.BAD_EFFECTS)::contains)) return;
        /*
        Save a filtered stream of uuids of the players affected.
         */
        Stream<UUID> stream = e.getAffectedEntities().stream()
                .filter(livingEntity -> livingEntity instanceof Player).map(LivingEntity::getUniqueId);
        /*
        If the shooter has pvp disabled or none of the affected entities have it enabled,
         */
        if(PPVPPlugin.inst().pvp().pvpNegative((((Player) shooter).getUniqueId()))
                || stream.noneMatch(PPVPPlugin.inst().pvp()::pvpPositive)) {
            /*
            cancel the potion splash event.
             */
            e.setCancelled(true);
            /*
            Return the potion to the player's inventory.
             */
            ((Player) shooter).getInventory().addItem(e.getEntity().getItem());
            /*
            Send a pvp alert if necessary.
             */
            TaskManager.blockedAttack(stream.toArray(UUID[]::new));
        }
    }
    /*
    Old code which hasn't been tested properly.

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLing(final LingeringPotionSplashEvent e){
        e.getAreaEffectCloud().getBasePotionData().getType()
        Stream<UUID> stream = e.getAffectedEntities().stream().filter(livingEntity -> livingEntity instanceof Player).map(LivingEntity::getUniqueId);
        if(e.getEntity().getCustomEffects().stream().map(PotionEffect::getType).noneMatch(Arrays.asList(Utils.BAD_EFFECTS)::contains)) return;
        stream.forEach(p -> {
            if(PVPManager.pvpNegative(p)) {
                e.getAffectedEntities().remove(Bukkit.getPlayer(p));e.
            }
        });
    }*/
}
class ProjectileListener implements Listener {
    /*
    When a projectile hits an entity,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(final ProjectileHitEvent e){
        /*
        Save the projectile.
         */
        Projectile projectile = e.getEntity();
        /*
        If the defender exists and the shooter and defender are players, continue. Otherwise, skip.
         */
        if(e.getHitEntity()==null ||
                !(projectile.getShooter() instanceof Player) ||
                !(e.getHitEntity() instanceof Player)) return;
        /*
        Save the shooter (attacker).
         */
        Player shooter = (Player) projectile.getShooter();
        /*
        Save the uuids of the shooter and defender.
         */
        UUID shooterUuid = shooter.getUniqueId(), entityUuid = e.getHitEntity().getUniqueId();
        /*
        If either player has pvp disabled and the projectile is not a snowball or an egg,
         */
        if(PPVPPlugin.inst().pvp().isEitherNegative(shooterUuid,entityUuid) && !((projectile instanceof Snowball) || projectile instanceof Egg)) {
            /*
            Cancel the projectile hit event.
             */
            e.setCancelled(true);
            /*
            Send a pvp alert if necessary.
             */
            TaskManager.blockedAttack(shooterUuid,entityUuid);
            /*
            If the shooter is not in creative (is not immune to damage), continue. Otherwise, skip.
             */
            if((shooter).getGameMode().equals(GameMode.CREATIVE)) return;
            /*
            If the projectile is a trident,
             */
            if(projectile instanceof Trident) {
                /*
                Get the trident.
                 */
                ItemStack is = ((Trident) projectile).getItemStack();
                /*
                Remove the projectile from its location.
                 */
                projectile.remove();
                /*
                Return the projectile to the player's inventory.
                 */
                shooter.getInventory().addItem(is);
            }
            /*
            If the projectile is an arrow of any type,
             */
            else if(projectile instanceof AbstractArrow) {
                /*
                Remove it.
                 */
                projectile.remove();
                /*
                If the projectile is an arrow and the bow doesn't have infinity or isn't uncraftable, continue.
                Otherwise, skip.,
                 */
                if(projectile instanceof Arrow)
                    if ((((Arrow)projectile).hasCustomEffects() &&
                            ((Arrow)projectile).getBasePotionData().getType().equals(PotionType.UNCRAFTABLE))
                            &&
                            (shooter.getInventory().getItemInMainHand().containsEnchantment(Enchantment.ARROW_INFINITE)
                                    ||
                                    shooter.getInventory().getItemInOffHand()
                                            .containsEnchantment(Enchantment.ARROW_INFINITE)))
                        return;
                /*
                Return the arrow to the shooter's inventory.
                 */
                shooter.getInventory().addItem(((AbstractArrow) projectile).getItemStack());
            }
        }
    }
}
class FishingListener implements Listener {
    /*
    When a fishing rod hits an entity,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(@NotNull PlayerFishEvent e) {
        /*
        If the targeted entity is a player, continue. Otherwise, skip.
         */
        if(!(e.getCaught() instanceof Player)) return;
        /*
        Save the player uuids.
         */
        UUID caughtUuid = e.getCaught().getUniqueId(), playerUuid = e.getPlayer().getUniqueId();
        /*
        If either player has pvp disabled,
         */
        if(PPVPPlugin.inst().pvp().isEitherNegative(caughtUuid,playerUuid)) {
            /*
            Cancel the event so no damage is dealt.
             */
            e.setCancelled(true);
            /*
            Send a pvp alert if necessary.
             */
            TaskManager.blockedAttack(caughtUuid,playerUuid);
        }
    }
}
class CombustionListener implements Listener {
    /*
    When an entity combusts another entity,
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombust(final EntityCombustByEntityEvent e) {
        /*
        If both entities involved are players, continue. Otherwise, skip.
         */
        if(!(e.getCombuster() instanceof Player && e.getEntity() instanceof Player)) return;
        /*
        Save the players' uuids.
         */
        UUID combusterUuid = e.getCombuster().getUniqueId(), entityUuid = e.getEntity().getUniqueId();
        /*
        If either player has pvp disabled,
         */
        if(PPVPPlugin.inst().pvp().isEitherNegative(combusterUuid,entityUuid)) {
            /*
            Cancel the event.
             */
            e.setCancelled(true);
            /*
            Send a pvp alert if necessary.
             */
            TaskManager.blockedAttack(combusterUuid,entityUuid);
        }
    }
}