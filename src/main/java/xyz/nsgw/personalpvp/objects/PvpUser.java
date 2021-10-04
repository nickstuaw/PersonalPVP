/*
 * Copyright (c) 2021.
 */

package xyz.nsgw.personalpvp.objects;

import xyz.nsgw.personalpvp.PPVPPlugin;
import xyz.nsgw.personalpvp.config.GeneralConfig;

import java.util.UUID;

public class PvpUser {

    private final UUID uuid;

    private boolean difference;

    public PvpUser(final UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isDifference() {
        return this.difference;
    }

    public void setDifference(boolean diff) {
        this.difference = diff;
    }

    public boolean isPvpOn() {
        if(this.difference) {
            return !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
        } else {
            return PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
        }
    }

    public void enable() {
        this.difference = !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
    }

    public void disable() {
        this.difference = !PPVPPlugin.inst().conf().get().getProperty(GeneralConfig.DEFAULT_PVP_STATUS);
    }
}
