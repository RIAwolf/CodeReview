package com.rscr.server.content.minigames.tournament;

import com.rscr.server.Server;
import com.rscr.server.content.minigames.EventVote;
import com.rscr.server.content.minigames.tournament.strategies.*;
import com.rscr.server.event.DelayedEvent;
import com.rscr.server.external.EntityHandler;
import com.rscr.server.external.ObjectMiningDef;
import com.rscr.server.external.TournamentBuildDef;
import com.rscr.server.model.Point;
import com.rscr.server.model.Skills;
import com.rscr.server.model.container.Item;
import com.rscr.server.model.entity.player.Player;
import com.rscr.server.model.states.Action;
import com.rscr.server.model.world.Area;
import com.rscr.server.net.rsc.ActionSender;

import java.util.*;

import static com.rscr.server.plugins.Functions.showMenu;
import static com.rscr.server.plugins.Functions.*;

public class PvPTournament extends DelayedEvent {

    public static final int FLATS = 0;
    public static final int PURE_90 = 1;
    public static final int PURE_73 = 2;
    public static final int PURE_33 = 3;

    private static int ANNOUNCEMENT_DELAY = 120;
    private static int START_TIME = 500;
    private static int TELEPORT_TO_ARENA_TIMER = 15;

    private EventVote voteSystem = new EventVote();

    public TournamentParticipant getOpponent(TournamentParticipant tournamentInstance) {
       for(TournamentParticipant participant : participants) {
           if(participant.getCurrentRoom() == tournamentInstance.getCurrentRoom() && participant != tournamentInstance) {
               return participant;
           }
       }
       return null;
    }

    public enum TournamentStatus {
        IDLE,
        VOTE_STARTED,
        COUNTDOWN,
        PREPARATION,
        RUNNING,
        LOOKING_FOR_REPLACEMENT_PARTICIPANTS
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    private TournamentStatus status = TournamentStatus.IDLE;
    private ArrayList<TournamentParticipant> participants = new ArrayList<>();

    public int characterBuildId;
    public int prizePot;
    public int teleportToArenaTimer = TELEPORT_TO_ARENA_TIMER;
    public int timeTillStart = START_TIME;
    public int secondsTillStart = 60;
    public int timeTillNextAnnouncement = 0;

    public boolean tournamentDonated = false;
    public boolean prizeIsAnItem = false;
    public String donater = "";
    public String donatedItemName = "";
    public Player playerDonating;

    private Map<TournamentStatus, ITournamentStrategy> strategyMap;

    private HashMap<Integer, Point[]> roomCoordinates = new HashMap<>();
    private HashMap<Point, Integer> doorsToRoom = new HashMap<>();

    private TournamentBuildDef def;

    public PvPTournament() {
        super(null, 1000);

        strategyMap = new HashMap<>();
        strategyMap.put(TournamentStatus.IDLE, new IdleStrategy());
        strategyMap.put(TournamentStatus.VOTE_STARTED, new VoteStartedStrategy(this, voteSystem));
        strategyMap.put(TournamentStatus.COUNTDOWN, new CountdownStrategy(this));
        strategyMap.put(TournamentStatus.PREPARATION, new PreparationStrategy(this));
        strategyMap.put(TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS, new LookingForParticipantsStrategy(this));
        strategyMap.put(TournamentStatus.RUNNING, new RunningStrategy(this));

        roomCoordinates.put(0, new Point[]{new Point(247,28), new Point(250,28)});//South row of rooms, SE corner
        roomCoordinates.put(1, new Point[]{new Point(255,28), new Point(258,28)});//South row of rooms
        roomCoordinates.put(2, new Point[]{new Point(271,28), new Point(274,28)});//South row of rooms
        roomCoordinates.put(3, new Point[]{new Point(279,28), new Point(282,28)});//South row of rooms, SW corner
        roomCoordinates.put(4, new Point[]{new Point(254, 19), new Point(254, 24)});//Room after 0,1 rooms
        roomCoordinates.put(5, new Point[]{new Point(275, 19), new Point(275, 24)});//Room after 2,3 rooms
        roomCoordinates.put(6, new Point[]{new Point(254, 11), new Point(254, 16)});//Room after 11,12 rooms
        roomCoordinates.put(7, new Point[]{new Point(275, 11), new Point(275, 17)});//Room after 13,14 rooms
        roomCoordinates.put(8, new Point[]{new Point(263, 16), new Point(257, 16)});//Room after 4,6 rooms
        roomCoordinates.put(9, new Point[]{new Point(272, 16), new Point(266, 16)});//Room after 5,7 rooms
        roomCoordinates.put(10, new Point[]{new Point(267, 10), new Point(260, 31)});//Final room
        roomCoordinates.put(11, new Point[]{new Point(247, 7), new Point(250, 7)});//North row of rooms, NE corner
        roomCoordinates.put(12, new Point[]{new Point(255, 7), new Point(258, 7)});//North row of rooms
        roomCoordinates.put(13, new Point[]{new Point(271, 7), new Point(274, 7)});//North row of rooms
        roomCoordinates.put(14, new Point[]{new Point(279, 7), new Point(282, 7)});//North row of rooms, NW corner

        doorsToRoom.put(new Point(251,26), 4);
        doorsToRoom.put(new Point(254,26), 4);
        doorsToRoom.put(new Point(275,26), 5);
        doorsToRoom.put(new Point(278,26), 5);
        doorsToRoom.put(new Point(256,19), 8);
        doorsToRoom.put(new Point(274,19), 9);
        doorsToRoom.put(new Point(256,16), 8);
        doorsToRoom.put(new Point(274,16), 9);
        doorsToRoom.put(new Point(263,15), 10);
        doorsToRoom.put(new Point(266,15), 10);
        doorsToRoom.put(new Point(251,10), 6);
        doorsToRoom.put(new Point(254,10), 6);
        doorsToRoom.put(new Point(275,10), 7);
        doorsToRoom.put(new Point(278,10), 7);
    }

    public int getRoomDoor(Point location) {
        return doorsToRoom.get(location);
    }
    public void run() {
        ITournamentStrategy strategy = strategyMap.get(status);
        strategy.run();
    }

    public void setNextRoom(TournamentParticipant p) {
        int nextRoom = getNextRoom(p.getCurrentRoom());
        p.setCurrentRoom(nextRoom);
    }

    public void registerKill(Player killer) {
        if(killer.getTournamentInstance() == null) {
            System.out.println("Tournament instance is null");
            return;
        }
        if(!participants.contains(killer.getTournamentInstance())) {
            System.out.println("There is a bug somewhere that made this possible");
            return;
        }
        killer.getTournamentInstance().setTimeOpponentKilled(System.currentTimeMillis());
        if (characterBuildId == 3) {
            if (killer.getInventory().hasItemId(188)) {
                killer.getInventory().getItems().clear();
                addItem(killer,188,1);
                addItem(killer,221,1);
                addItem(killer,370,28);
            } else {
                killer.getInventory().getItems().clear();
                addItem(killer,80,1);
                addItem(killer,221,1);
                addItem(killer,370,28);
            }
        } else {
            refreshSupplies(killer);
        }
        killer.getSkills().normalize();
    }

    public int getNextRoom(int currentRoom) {
        if(currentRoom == 0 || currentRoom == 1) {
            return 4;
        }
        if(currentRoom == 2 || currentRoom == 3) {
            return 5;
        }
        if(currentRoom == 4 || currentRoom == 6) {
            return 8;
        }
        if(currentRoom == 5 || currentRoom == 7) {
            return 9;
        }
        if(currentRoom == 11 || currentRoom == 12) {
            return 6;
        }
        if(currentRoom == 13 || currentRoom == 14) {
            return 7;
        }
        if(currentRoom == 8 || currentRoom == 9) {
            return 10;
        }
        return -1;
    }

    public void addWaitingAreaParticipant(Player p) {
        if (p.isDeadMan() || p.getIronMan() > 0) {
            p.message("You can't participate in this tournament.");
            return;
        }
        TournamentParticipant newParticipant = new TournamentParticipant(p.getDatabaseIndex(), p.getUsername());
        newParticipant.setMac(p.getMACAddress());
        newParticipant.setIpAddress(p.getCurrentIP());
        newParticipant.setTimeOpponentKilled(0);
        p.setTournamentInstance(newParticipant);

        initializeParticipant(newParticipant);
        participants.add(newParticipant);

        p.teleport(265, 42, false);
        if (tournamentDonated) {
            ActionSender.sendBox(p, "If you logout after 16 participants are gathered, you will be automatically kicked from the event. If you wish to leave, please do it before event starts using command @red@::leave.", false);
        } else {
            ActionSender.sendBox(p, "If you logout after 16 participants are gathered, you will be automatically kicked from the event and your entry @red@fee won't be refunded@whi@. If you wish to leave the event and get your entry fee back, please do it before event starts using command @red@::leave.", false);
        }
    }

    public void replaceMissingParticipant(Player p) {
        for (TournamentParticipant tp : participants) {
            if (!tp.isReplacementRequired()) {
                continue;
            }
            tp.setCharacterName(p.getUsername());
            tp.setCharacterID(p.getDatabaseIndex());
            tp.setIpAddress(p.getCurrentIP());
            tp.setMac(p.getMACAddress());

            initializeParticipant(tp);
            teleportToCurrentRoom(tp, 0);
            p.setTournamentInstance(tp);
            break;
        }
    }

    public void teleportToCurrentRoom(TournamentParticipant tp, int coord) {
        tp.getPlayer().teleport(roomCoordinates.get(tp.getCurrentRoom())[coord].getX(), roomCoordinates.get(tp.getCurrentRoom())[coord].getY(), false);
        if (!tp.getPlayer().isSkulled()) {
            tp.getPlayer().addSkull(1200000);
        }
    }

    public void removeLeavingParticipant(Player p) {
        if (!isJoinOrLeaveAllowed()) {
            p.message("You can't leave now");
            return;
        }
        p.message("Are you sure you want to leave the event?");
        int menu1 = showMenu(p, "Yes",
                "No, I'll stay");
        if (menu1 == 0) {
            removeParticipant(p);
            if (!isTournamentDonated()) {
                p.getBank().add(new Item(10, 50000));
            }

            p.teleport(217, 460);
            p.message("You have left the event");
        }
    }

    public void removeParticipant(Player p) {
        TournamentParticipant participantInstance = p.getTournamentInstance();
        if (participantInstance == null) {
            System.out.println("No participant instance found for " + p.getUsername());
            return;
        }
        p.setTournamentInstance(null);
        participants.remove(participantInstance);
        resetItemsAndStats(p);
    }

    public void resetItemsAndStats(Player p) {
        p.setTempSkills(null);
        ActionSender.sendStats(p);
        removeGearAndItems(p);
        if (p.isSkulled()) {
            p.removeSkull();
        }
    }

    private void initializeParticipant(TournamentParticipant tp) {
        int[] stats = def.getStats();
        Skills skills = new Skills(tp.getPlayer());
        for (int i = 0; i < stats.length; i++) {
            skills.setLevelTo(i,stats[i]);
        }
        tp.getPlayer().setTempSkills(skills);
        ActionSender.sendStats(tp.getPlayer());
        giveGearAndSupplies(tp.getPlayer());
    }

    /*
    * Pretty sure this method is useless now but is gonna be needed if we add
    * reconnect timeouts so user can join back.
    * */
    public TournamentParticipant getParticipant(int characterID) {
        for (TournamentParticipant participant : participants) {
            if (participant.getCharacterID() == characterID)
                return participant;
        }
        return null;
    }

    public void processAnnouncements() {
        if (timeTillNextAnnouncement > 0) {
            timeTillNextAnnouncement--;
        } else if (timeTillNextAnnouncement <= 0) {
            if (status == TournamentStatus.VOTE_STARTED) {
                voteInProgressAnnouncement();
            } else if (status == TournamentStatus.COUNTDOWN) {
                tournamentGeneralInfoAnnouncement();
            }
            timeTillNextAnnouncement = ANNOUNCEMENT_DELAY;
        }
    }

    private void voteInProgressAnnouncement() {
        Server.getWorld().sendEventMessage("Vote to start a 16 player 1v1 Tournament event in progress!");
        Server.getWorld().sendEventMessage("Type ::vote if you would like a tournament event, for info ::abouttournament");
    }

    private void tournamentGeneralInfoAnnouncement() {
        if (timeTillStart <= 60) {
            Server.getWorld().sendEventMessage("Entry to 16 player @yel@" + charBuildName(characterBuildId) + "@whi@ 1v1 Tournament event is open! Last minute to join the event!");
        } else {
            Server.getWorld().sendEventMessage("Entry to 16 player @yel@" + charBuildName(characterBuildId) + "@whi@ 1v1 Tournament event is open!");
        }
        if (tournamentDonated) {
            if (prizeIsAnItem) {
                Server.getWorld().sendEventMessage("Thanks to " + donater + " there is @red@no entry fee@whi@! Prize is@yel@ " + donatedItemName + ".");
            } else {
                Server.getWorld().sendEventMessage("Thanks to " + donater + " there is @red@no entry fee@whi@! Prize is@yel@ " + prizePot / 1000 + "k gp.");
            }
            Server.getWorld().sendEventMessage("@yel@No gear, supplies or stats required! @whi@Information ::abouttournament");
        } else {
            Server.getWorld().sendEventMessage("@yel@Entry price: 50k, no gear, supplies or stats required! @whi@Information ::abouttournament");
        }
        Server.getWorld().sendEventMessage("Type ::tournament to participate, ::spectate to spectate the event.");
    }

    public void startTournamentVote() {
        Server.getWorld().sendEventMessage("Vote to start a 1v1 tournament event has been started!");
        Server.getWorld().sendEventMessage("If you would like a tournament event to be started type ::vote, 12 votes required");
        voteSystem.start();
        status = TournamentStatus.VOTE_STARTED;
    }

    public void addVote(Player player) {
        if (voteSystem.isVoteExpired()) {
            System.out.println("expired");
            return;
        }
        if (voteSystem.isVoteSuccesful()) {
            player.message("Event already got enough votes for start");
            return;
        }
        if (voteSystem.addVote(player)) {
            if (voteSystem.isVoteSuccesful()) {
                Server.getWorld().sendEventMessage("@ran@Vote for 1v1 tournament event has been successful!");
                characterBuildId = random(0,3);
                Server.getWorld().sendEventMessage("Randomly picked character build for the event is: @yel@" + charBuildName(characterBuildId) + "@whi@!");
                startTournament();
            }
        } else {
            player.message("You have already voted");
        }
    }

    public void resetTournament() {
        prizePot = 0;
        teleportToArenaTimer = TELEPORT_TO_ARENA_TIMER;
        participants.clear();
        donater = "";
        playerDonating = null;
        donatedItemName = "";
        tournamentDonated = false;
        prizeIsAnItem = false;
        timeTillNextAnnouncement = 0;
        characterBuildId = 0;
    }

    public void stopTournament() {
        hideTournamentInterface();
        status = TournamentStatus.IDLE;
        resetTournament();
    }

    public void cancelTournament() {
        hideTournamentInterface();
        for (TournamentParticipant p : participants) {
            if (!p.isOnline() || p.isReplacementRequired()) {
                continue;
            }
            p.getPlayer().teleport(217, 460);
            resetItemsAndStats(p.getPlayer());
            p.getPlayer().setTournamentInstance(null);
            if (!tournamentDonated) {
                p.getPlayer().getBank().add(new Item(10, 50000));
            }
        }

        participants.clear();
        if (tournamentDonated && !prizeIsAnItem) {
            if (playerDonating.isLoggedIn()) {
                ActionSender.sendBox(playerDonating, "Tournament did not gather enough participants to start, your donation has been returned to your bank account", false);
                playerDonating.getBank().add(new Item(10, prizePot));
            }
        }
        hideTournamentInterface();
        status = TournamentStatus.IDLE;
        resetTournament();
    }

    public void startTournament() {
        if (!tournamentDonated) {
            prizePot = 600000;
        }
        def = EntityHandler.getTournamentBuildDef(characterBuildId);
        voteSystem.resetVoteValues();
        status = TournamentStatus.COUNTDOWN;
        timeTillStart = START_TIME;
        timeTillNextAnnouncement = ANNOUNCEMENT_DELAY;
        Server.getWorld().sendEventMessage("Type ::tournament to participate, ::spectate to spectate, info ::abouttournament.");
    }

    public void startDonationTournament(Player p, int donatedPrizePot, boolean donatedItem, String itemName, int charBuildType) {
        voteSystem.resetVoteValues();
        timeTillStart = START_TIME;
        status = TournamentStatus.COUNTDOWN;
        donatedItemName = itemName;
        donater = p.getUsername();
        playerDonating = p;
        tournamentDonated = true;
        prizePot = donatedPrizePot;
        characterBuildId = charBuildType;
        def = EntityHandler.getTournamentBuildDef(characterBuildId);
        System.out.println(characterBuildId);
        if (donatedItem) {
            prizeIsAnItem = true;
            Server.getWorld().sendEventMessage("@gre@" + donater + " @whi@has just donated@yel@ " + donatedItemName + "@whi@ to start a 1v1 tournament event!");
        } else {
            Server.getWorld().sendEventMessage("@gre@" + donater + " @whi@has just donated " + prizePot / 1000 + "K to start a 1v1 tournament event!");
        }
    }

    public void messageParticipants(String string) {
        for (TournamentParticipant p : participants) {
            if (p.isOnline()) {
                ActionSender.sendObjectiveText(p.getPlayer(), string, 4, 100);
            }
        }
    }

    public boolean isParticipating(Player player) {
        if(player.getMACAddress().equals("failed")) {
            for (TournamentParticipant tp : participants) {
                if (tp.getIpAddress().equals(player.getCurrentIP())) {
                    return true;
                }
            }
            return false;
        }

        for (TournamentParticipant tp : participants) {
            if (tp.getMACAddress().length() > 0 && tp.getMACAddress().equals(player.getMACAddress())) {
                return true;
            }
        }
        return false;
    }

    public void handleParticipantLogout(Player p) {
        if (status == TournamentStatus.COUNTDOWN) {
            if (!tournamentDonated) {
                p.getCache().store("should_get_tournament_refund", true);
            }
            removeParticipant(p);
            p.setTournamentInstance(null);
            return;
        }

        if (status == TournamentStatus.RUNNING || 
                status == TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS ||
                status == TournamentStatus.PREPARATION) {
            TournamentParticipant participantInstance = p.getTournamentInstance();
            if (participantInstance == null) {
                System.out.println("No participant instance found for " + p.getUsername());
                return;
            }
            p.setTournamentInstance(null);
            resetItemsAndStats(p);
            p.teleport(216, 460);
            participantInstance.setCharacterID(-1);
            participantInstance.setCharacterName(null);
            participantInstance.setMac("");
            participantInstance.setIpAddress("");
            status = TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS;
        }
    }

    public void participantLoggedIn(Player p) {
        if (p.isMod() || p.isAdmin()) {
            return;
        }
        if (p.getLocation().inArea("tournamentWaitingArea")) {
            //Player logins when event is not looking for participants, should not be registered back into event;
            if (status != TournamentStatus.COUNTDOWN) {
                resetItemsAndStats(p);
                p.teleport(217, 460);
            }
            //Player logins while event is looking for participants
            if (status == TournamentStatus.COUNTDOWN) {
                if (p.getTournamentInstance() == null) {
                    removeGearAndItems(p);
                    addWaitingAreaParticipant(p);
                } else {
                    return;
                }
            }
            //Player should be added back to the event, but did not login in time.
            if (status != TournamentStatus.COUNTDOWN) {
                resetItemsAndStats(p);
                p.teleport(216, 460);
                if (p.getCache().hasKey("should_get_tournament_refund")) {
                    p.getCache().remove("should_get_tournament_refund");
                    p.getBank().add(new Item(10, 50000));
                    ActionSender.sendBox(p,"@red@You have logged out before 1v1 Tournament event%" + " %" +
                    "Entry fee of 50k that you've paid to enter the event has been refunded to your bank account.",false);
                } else {
                    ActionSender.sendBox(p, "@red@You have logged out during a 1v1 Tournament event%" +
                            "That is strictly forbidden in order to prevent cheating, if you've paid to enter the event, your entry fee won't be refunded." +
                            "Please do not logout during events", false);
                }
            }
        }

        if (p.inArenaRoom()) {//If player is not in the waiting and is not a spectator
            resetItemsAndStats(p);
            p.teleport(216, 460);
            ActionSender.sendBox(p, "@red@You have logged out during a 1v1 Tournament event%" +
                    "That is strictly forbidden in order to prevent cheating, if you've paid to enter the event, your entry fee won't be refunded." +
                    "Please do not logout during events", false);
        }
        if (!p.inArenaRoom() && !p.getLocation().inArea("tournamentWaitingArea")) {
            //If player used ::spectate to get into the area, he gets teleported back where he was.
            if (p.getCache().hasKey("after_spectating_return_x") && p.getCache().hasKey("after_spectating_return_y")) {
                int return_x = p.getCache().getInt("after_spectating_return_x");
                int return_y = p.getCache().getInt("after_spectating_return_y");
                p.teleport(return_x, return_y, false);
                p.getCache().remove("after_spectating_return_x");
                p.getCache().remove("after_spectating_return_y");
                return;
            } else {
                p.teleport(217,460, false);
            }
        }
    }

    private void hideTournamentInterface() {
        for (Player p : Server.getWorld().getPlayers()) {
            if (p.getIronMan() > 0 || p.isDeadMan())
                continue;
            TournamentInterface.hide(p);
        }
    }

    public boolean isVoteOnCooldown() {
        return voteSystem.isOnCooldown();
    }

    public boolean isIdle() {
        return status == TournamentStatus.IDLE;
    }

    public boolean isRunning() {
        return status == TournamentStatus.RUNNING;
    }

    public boolean isVotingStarted() {
        return status == TournamentStatus.VOTE_STARTED;
    }

    public boolean isTournamentDonated() {
        return tournamentDonated;
    }

    public boolean isJoinOrLeaveAllowed() {
        return status == TournamentStatus.COUNTDOWN && timeTillStart > 30;
    }

    public long getVoteCooldown() {
        return voteSystem.getCooldownTimeLeft();
    }

    public ArrayList<TournamentParticipant> getParticipants() {
        return participants;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void removeGearAndItems(Player p) {
        p.getInventory().getItems().clear();
        for (int slot = 0; slot < p.getEquipment().getEquippedItems().length; slot++) {
            Item item = p.getEquipment().getEquippedItem(slot);
            if (item != null) {
                p.getEquipment().getEquippedItems()[slot] = null;
                if (item.getDef().getWieldPosition() != -1) {
                    p.updateWornItems(item.getDef().getWieldPosition(),
                            p.getSettings().getAppearance().getSprite(item.getDef().getWieldPosition()));
                }
            }
        }
        ActionSender.sendInventory(p);
        ActionSender.sendEquipmentStats(p);
        ActionSender.sendEquipment(p);
    }

    static final int capeIDs[] = {183, 209, 229, 511, 512, 513};
    static final int gloveIDs[] = {2152, 2153, 2154, 2155, 2156};
    static final int bootsIDs[] = {2158, 2159, 2160, 2161, 2162};

    public void giveGearAndSupplies(Player p) {

        int random = random(0, gloveIDs.length - 1);
        Item[] equipment = def.getGear();
        Item[] randomItems = new Item[] {
                new Item(capeIDs[random(0, capeIDs.length - 1)], 1),//Random f2p cape
                new Item(gloveIDs[random], 1),//Random colored gloves
                new Item(bootsIDs[random], 1)};//Random colored boots
        for (Item i : equipment) {

            p.getEquipment().setEquippedFindSlot(new Item(i.getID(), i.getAmount()));
        }
        for (Item i : randomItems) {
            p.getEquipment().setEquippedFindSlot(new Item(i.getID(), i.getAmount()));
        }
        ActionSender.sendEquipmentStats(p);
        ActionSender.sendEquipment(p);
        refreshSupplies(p);
    }

    public void refreshSupplies(Player p) {
        Item[] supplies = def.getSupplies();
        p.getInventory().getItems().clear();
        for (int i = 0; i < supplies.length; i++) {
            Item item = supplies[i];
            addItem(p,item.getID(),item.getAmount());
        }
    }

    public void handleFightCountdown() {
        for (TournamentParticipant p : participants) {
            p.tickFightCountdown();
        }
    }

    public String charBuildName(int characterBuildId) {
        String string = "";
        if (characterBuildId == FLATS) {
            string = "Flats";
        } else if (characterBuildId == PURE_90) {
            string = "90's pures";
        } else if (characterBuildId == PURE_73) {
            string = "73's pures";
        } else if (characterBuildId == PURE_33) {
            string = "33's pures";
        }
        return string;
    }

    public void terminateTournament() {
        hideTournamentInterface();
        for (TournamentParticipant p : participants) {
            if (!p.isOnline() || p.isReplacementRequired()) {
                continue;
            }
            p.getPlayer().teleport(217, 460);
            resetItemsAndStats(p.getPlayer());
            p.getPlayer().setTournamentInstance(null);
            if (!tournamentDonated) {
                p.getPlayer().getBank().add(new Item(10, 50000));
            }
            ActionSender.sendBox(p.getPlayer(),"@red@Tournament terminated%" + " %" + "Tournament has been terminated by a moderator, if you've paid to enter the event, your payment was refunded to your bank account",false);
        }

        participants.clear();
        if (tournamentDonated && !prizeIsAnItem) {
            if (playerDonating.isLoggedIn()) {
                ActionSender.sendBox(playerDonating, "@red@Tournament terminated%" + " %" + "Tournament has been terminated by a moderator, your donation has been refunded", false);
                playerDonating.getBank().add(new Item(10, prizePot));
            }
        }
        hideTournamentInterface();
        status = TournamentStatus.IDLE;
        resetTournament();
    }

}