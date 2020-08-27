package ru.ijo42.uplink.api;

import club.minnced.discord.rpc.DiscordRPC;

public abstract class PresenceListener {

    protected PresenceManager presenceManager;
    private DiscordRPC rpc;
    private int curTick = 0;
    private int curPlayerCount = 0;

    protected void init(DiscordRPC rpc, PresenceManager presenceManager) {
        this.rpc = rpc;
        this.presenceManager = presenceManager;
    }

    public void onTick() {
        if (presenceManager.getCurState() != PresenceState.INGAME) {
            curTick = 0;
            return;
        }

        if (curTick >= 1000) {
            curTick = 0;

            try {
                int playerCount = UplinkAPI.forgeImpl.getPlayerCount();
                int maxPlayers = UplinkAPI.forgeImpl.getMaxPlayers();

                if (this.curPlayerCount != playerCount) {
                    rpc.Discord_UpdatePresence(presenceManager.updatePlayerCount(playerCount, maxPlayers));
                    this.curPlayerCount = playerCount;
                }
            } catch (NullPointerException ignored) {
            }
        } else {
            curTick++;
        }
    }

    public void onMainMenu() {
        presenceManager.setCurState(PresenceState.MENU_MAIN);
        rpc.Discord_UpdatePresence(presenceManager.initMenu());
    }

    public void onJoin() {
        if (UplinkAPI.forgeImpl.isMP()) {
            if (presenceManager.getCurState() == PresenceState.INGAME) {
                // Player is already in a server.
                return;
            }

            rpc.Discord_UpdatePresence(presenceManager.initMP(UplinkAPI.forgeImpl.getServerIP()));
        } else {
            rpc.Discord_UpdatePresence(presenceManager.initSP(UplinkAPI.forgeImpl.getWorldName()));
        }

        presenceManager.setCurState(PresenceState.INGAME);
    }

    public void onClientDisconnect() {
        rpc.Discord_UpdatePresence(presenceManager.initMenu());
        presenceManager.setCurState(PresenceState.MENU_MAIN);
    }
}