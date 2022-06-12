/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp.objects;

import com.nsgwick.personalpvp.PPVPPlugin;
import com.nsgwick.personalpvp.config.GeneralConfig;

import java.util.UUID;

public class PvpUser {

    /**
    The player's UUID (unique ID) which never changes.
     */
    private final UUID uuid;

    /**
    Whether their PVP status has been altered.
     */
    private boolean altered;

    /*
    Constructor.
     */
    public PvpUser(final UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the player's UUID (unique ID).
     * @return The player's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Check if the player's PVP status is altered.
     * @return returns true if opposite to the default status; false if default.
     */
    public boolean isAltered() {
        return this.altered;
    }

    /**
     * Set the player's PVP status in relation to the default PVP status.
     * @param diff enter true if opposite to the default status; or false if default.
     */
    public void setAltered(boolean diff) {
        this.altered = diff;
    }

    /**
     * Check whether the player has PVP enabled.
     * @return true if pvp is enabled; false otherwise.
     */
    public boolean isPvpOn() {
        if(this.altered) {
            return !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
        } else {
            return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
        }
    }

    /**
     * Toggle PVP for the player.
     */
    public void toggle() {
        this.altered = !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
    }
}
