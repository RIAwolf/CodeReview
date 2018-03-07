package com.rscr.server.content.minigames.tournament;

import com.rscr.server.Server;
import com.rscr.server.model.entity.player.Player;
import com.rscr.server.net.rsc.ActionSender;

import java.sql.Timestamp;

/**
 * Created by tomassimkus on 20/02/2018.
 */
public class TournamentParticipant {

    private String mac;

    public TournamentParticipant(int characterID, String characterName) {
        this.characterID = characterID;
        this.characterName = characterName;
    }

    private int characterID;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String ipAddress;

    private String characterName;
    private int currentRoom;
    private long timeOpponentKilled;

    public int getFightCountdown() {
        return fightCountdown;
    }

    public void setFightCountdown(int fightCountdown) {
        this.fightCountdown = fightCountdown;
    }

    private int fightCountdown;

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int currentRoom) {
        this.currentRoom = currentRoom;
    }

    public long getTimeOpponentKilled() {
        return timeOpponentKilled;
    }

    public void setTimeOpponentKilled(long timeOpponentKilled) {
        this.timeOpponentKilled = timeOpponentKilled;
    }

    public int getCharacterID() {
        return characterID;
    }

    public void setCharacterID(int characterID) {
        this.characterID = characterID;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }


    public Player getPlayer() {
        return Server.getWorld().getPlayerByID(characterID);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public boolean isReplacementRequired() {
        return characterID == -1;
    }

    public String getMACAddress() {
        return mac;
    }

    public void tickFightCountdown() {
        if(fightCountdown > 0) {
            fightCountdown--;

            ActionSender.sendObjectiveText(getPlayer(), "You can begin fight in " + fightCountdown + "!", 4, 100);
        }
    }

    public boolean onCooldown() {
        return fightCountdown > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TournamentParticipant) {
            TournamentParticipant tp = (TournamentParticipant) obj;
            if(tp.getCharacterID() == this.getCharacterID()) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
