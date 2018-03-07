package com.rscr.server.plugins.commands.eventcommands;

import com.rscr.server.Server;
import com.rscr.server.content.minigames.tournament.PvPTournament;
import com.rscr.server.model.entity.player.Player;
import com.rscr.server.net.rsc.ActionSender;
import com.rscr.server.plugins.commands.UniversalCommandMethods;
import com.rscr.server.plugins.listeners.action.CommandListener;

import static com.rscr.server.plugins.Functions.message;
import static com.rscr.server.plugins.Functions.showMenu;

/**
 * Created by tomassimkus on 20/02/2018.
 */
public class TournamentCommands implements CommandListener {

    @Override
    public void onCommand(String command, String[] args, Player player) {
        UniversalCommandMethods commandMethods = new UniversalCommandMethods();
        if (command.equals("replace")) {
            if (!commandMethods.isAllowedToJoinEvents(player)) {
                return;
            }
            if(player.getTournamentInstance() != null) {
                player.message("you're already participating");
                return;
            }
            if (Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS) {
                    Server.getWorld().getPvPTournament().replaceMissingParticipant(player);
            } else {
                player.message("No replacement is being looked for at the moment");
            }
        }

        if (command.equalsIgnoreCase("tournament")) {
            if (!commandMethods.isAllowedToJoinEvents(player)) {
                return;
            }
            if (Server.getWorld().getPvPTournament().isParticipating(player)) {
                player.message("You are already participating in this event on another character");
                return;
            }
            if (Server.getWorld().getPvPTournament().isIdle() ||
                    Server.getWorld().getPvPTournament().getStatus() != PvPTournament.TournamentStatus.COUNTDOWN) {
                player.message("There is no event to join at the moment");
                return;
            }
            if (Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.RUNNING ||
                    Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.PREPARATION ||
                    Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS) {
                player.message("It's too late to join the event");
                return;
            }
            ActionSender.sendBox(player,"@yel@16 player 1v1 " + Server.getWorld().getPvPTournament().charBuildName(Server.getWorld().getPvPTournament().characterBuildId) + " Tournament%" + " %" +
                    "This is a safe 1v1 player tournament that does not require any gear, supplies or stats. " +
                    "You will have 1v1 fights with random opponents in a controlled environment. " +
                    "If you get a kill you are able to move to the next room where you'll meet the next opponent. Last player remaining wins the grand prize.% " + " %" +
                    "When you buy a ticket, you will be teleported to a waiting area where you'll wait for 16 participants to be gathered. " +
                    "Upon arrival, your character stats will be changed, you'll receive full gear and supplies. " +
                    "When enough participants are gathered, players will be teleported to event arena where they will be randomly distributed into pairs to do 1v1 fights.%" + " %" +
                    "To enter the event you are required to have an empty inventory and equipment tab.% " + " %" +
                    (Server.getWorld().getPvPTournament().isTournamentDonated() ? "@yel@Entry is free" : "@yel@Event entry price: 50k"), true);
            if (!player.getEquipment().isEquipmentEmpty() || player.getInventory().getFreeSlots() != 30) {
                player.message("You need to have empty inventory and equipment tab to enter the event");
                return;
            }
            int confirmationMenu = showMenu(player, "I'm ready",
                    "No, I changed my mind");
            if (player.isBusy() || confirmationMenu == -1) {
                return;
            }
            if (confirmationMenu == 0) {
                if (!Server.getWorld().getPvPTournament().isTournamentDonated()) {
                    message(player, 600, "Entry fee is @red@50,000gp",
                            "It will be taken from your bank account");
                    int purchaseMenu = showMenu(player, "Let me join, I'll pay",
                            "Oh no, I won't go then");

                    if (player.isBusy() || purchaseMenu == -1) {
                        return;
                    }
                    if (purchaseMenu == 0) {
                        if (Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.RUNNING ||
                                Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.PREPARATION||
                                Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS ||
                                Server.getWorld().getPvPTournament().getStatus() != PvPTournament.TournamentStatus.COUNTDOWN) {
                            player.message("It's too late to join the event");
                            return;
                        }
                        if (Server.getWorld().getPvPTournament().isParticipating(player)) {
                            player.message("You are already participating in this event");
                            return;
                        }
                        if (player.getBank().remove(10, 50000) > -1) {
                            player.message("50,000gp has been taken from your bank account");
                            Server.getWorld().getPvPTournament().addWaitingAreaParticipant(player);
                        } else {
                            player.message("You don't have enough gp in bank to participate");
                            return;
                        }
                    } else if (purchaseMenu == 1) {
                        return;
                    }
                } else {
                    if (Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.RUNNING ||
                            Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.PREPARATION ||
                            Server.getWorld().getPvPTournament().getStatus() == PvPTournament.TournamentStatus.LOOKING_FOR_REPLACEMENT_PARTICIPANTS ||
                            Server.getWorld().getPvPTournament().getStatus() != PvPTournament.TournamentStatus.COUNTDOWN) {
                        player.message("It's too late to join now.");
                    } else {
                        Server.getWorld().getPvPTournament().addWaitingAreaParticipant(player);
                    }
                }
            }
        }

        if (command.equals("abouttournament")) {
            String text =
                            "This event does not require any gear, supplies or stats to\n" +
                            "participate, when entering, your character is set to stats that\n" +
                            "will be used in the event, you are given the required gear and\n" +
                            "supplies.\n\n" +

                            "Opponents in the event are randomly selected and the fights are\n" +
                            "only 1vs1. If you get a kill, you are able to progress in the\n" +
                            "event map and walk into the next room where you'll meet the next\n" +
                            "opponent. Last participant remaining wins the prize\n\n" +

                    "There are four possible character builds in the event:\n" +
                    "1) @lre@Flats @whi@Combat stats all 99;\n" +
                    "2) @lre@90's pure @whi@Stats: A:99, D:1, S:99, HP:99, R:1, P:46, M:85;\n" +
                    "3) @lre@73's pure @whi@Stats: A:40, D:1, S:99, HP:99, R:1, P:40, M:73;\n" +
                    "4) @lre@33's pure @whi@Stats: A:30, D:1, S:57, HP:99, R:58, P:1, M:1;\n" +
                    "All gear and supplies that are provided are f2p.\n\n" +

                    "Character build used in the event is determined by the person\n" +
                    "donating for the event, or it's randomly selected when event\n" +
                    "vote is successful. All players get identical stats\n\n" +

                                    "During event, when you get a kill you are automatically given fresh\n" +
                                    "supplies and get healed.\n\n" +

                    "Event can be started with a donation from 400k to 1000k by typing\n" +
                    "@lre@::donateforevent@whi@, entry to donated tournament is free.\n" +
                    "Event can also be started with a vote by typing @lre@::starteventvote@whi@,\n" +
                    "then event entry costs 50k and grand prize is 600k\n\n" +

                    "Event requires 16 participants to start, if 16 participants are\n" +
                    "not gathered event gets cancelled, donations and entry fees are\n" +
                    "returned.\n\n" +

                    "You are required to have empty inventory and equipment tab when\n" +
                    "entering the event.";
            ActionSender.sendScrollableInterface(player,"16 player PvP Tournament",text.split("\n"));
        }
    }
}
