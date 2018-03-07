package com.rscr.server.plugins.commands.eventcommands;

import com.rscr.server.Server;
import com.rscr.server.model.entity.player.Player;
import com.rscr.server.net.rsc.ActionSender;
import com.rscr.server.plugins.commands.UniversalCommandMethods;
import com.rscr.server.plugins.listeners.action.CommandListener;

import static com.rscr.server.plugins.Functions.showMenu;

/**
 * Class for commands that are universal between events we have.
 */
public class GeneralEventCommands implements CommandListener{

    @Override
    public void onCommand(String command, String[] args, Player player) {
        UniversalCommandMethods commandMethods = new UniversalCommandMethods();
        if (command.equals("donateforevent")) {
            if (commandMethods.isAnyEventRunning(player)) {
                return;
            }
            player.message("What event would you like to donate for?");
            int eventPickMenu = showMenu(player,"16 Player 1v1 Tournament","Survival event","Lottery event","I don't want to donate");
            if (player.isBusy() || eventPickMenu < 0 || eventPickMenu > 2) {
                return;
            }
            switch(eventPickMenu) {
                case 0:
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    player.message("How much would you like to donate?");
                    int donateMenu = showMenu(player,"I've changed my mind, forget it.","400k","600k","800k", "1000k");
                    if (player.isBusy() || donateMenu < 1 || donateMenu > 4 ) {
                        return;
                    }
                    if (donateMenu >= 1 || donateMenu <= 2) {
                        if (commandMethods.isAnyEventRunning(player)) {
                            return;
                        }
                        int donatedAmount = 0;
                        if (donateMenu == 0) {
                            return;
                        } else if (donateMenu == 1) {
                            donatedAmount = 400000;
                        } else if (donateMenu == 2) {
                            donatedAmount = 600000;
                        } else if (donateMenu == 3){
                            donatedAmount = 800000;
                        } else if (donateMenu == 4) {
                            donatedAmount = 1000000;
                        }
                        player.message("You've selected to donate " + donatedAmount / 1000 + "k, what type of character build");
                        player.message("should be used in your event?");
                        int buildPickMenu = showMenu(player,  "Flats", "Pure 90", "Pure 73", "Pure 33","I've changed my mind, forget it.");
                        if (player.isBusy() || buildPickMenu < 0 || buildPickMenu > 3) {
                            return;
                        }
                        player.message("You've chosen to donate " + donatedAmount / 1000 + "k for @yel@" + Server.getWorld().getPvPTournament().charBuildName(buildPickMenu) + ", is this correct?");
                        int confirmMenu1 = showMenu(player, "Yes", "No");
                        if (player.isBusy() || confirmMenu1 != 0) {
                            return;
                        }
                        if (confirmMenu1 == 0) {
                            if (commandMethods.isAnyEventRunning(player)) {
                                return;
                            }
                            if (player.getBank().remove(10, donatedAmount) > -1) {
                                player.message("" + donatedAmount / 1000 + "k gp has been taken from your bank account");
                                Server.getWorld().getPvPTournament().startDonationTournament(player,donatedAmount,false,null,buildPickMenu);
                            } else {
                                player.message("You don't have enough gp in your bank account");
                            }
                        }
                    }
                    break;
                case 1:
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    player.message("How much would you like to donate?");
                    int survivalDonateMenu = showMenu(player, "I've changed my mind, forget it.", "200k", "400k", "600k", "800k", "1000k");
                    if (player.isBusy() || survivalDonateMenu < 1 || survivalDonateMenu > 5) {
                        return;
                    }
                    if (survivalDonateMenu >= 1 || survivalDonateMenu <= 5) {
                        if (commandMethods.isAnyEventRunning(player)) {
                            return;
                        }
                        player.message("You've selected to donate " + (survivalDonateMenu * 200) + "k, are you sure?");
                        int confirmMenu1 = showMenu(player, "Yes, I want to donate " + (survivalDonateMenu * 200) + "k.", "I've changed my mind, forget it.");
                        if (player.isBusy() || confirmMenu1 != 0) {
                            return;
                        }
                        if (commandMethods.isAnyEventRunning(player)) {
                            return;
                        }
                        if (confirmMenu1 == 0) {
                            if (player.getBank().remove(10, (survivalDonateMenu * 200000)) > -1) {
                                player.message((survivalDonateMenu * 200) + "k gp has been taken from your bank account");
                                Server.getWorld().getSurvival().startDonationSurvival(player, (survivalDonateMenu * 5), false, null);
                            } else {
                                player.message("You don't have enough gp in your bank account");
                            }
                        }
                    }
                    break;
                case 2:
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    ActionSender.sendBox(player, "@lre@Information about donating for a lottery event%" + " %" +
                            "@whi@There are two options options on how you can start a lottery with a donation:%" + " %" +
                            "@yel@1)@whi@ You can donate @lre@400k@whi@ gp to start a lottery without needing for players to vote and choose is it a @yel@no limit@whi@ or @yel@one @yel@ticket limit@whi@ lottery, donated funds will go to the lottery pot and players will need to buy lottery tickets for 50k each.%" + " %" +
                            "@yel@2)@whi@ You can donate @lre@400k - 1000k@whi@ gp and start a one ticket limit@lre@ Free lottery@whi@, tickets for players are @yel@free@whi@ and they compete for the amount that you've donated.%" + " %" +
                            "Please choose the right option for you.", true);
                    int menuLottery1 = showMenu(player, "I would like to donate 400k to start a lottery", "I would like to donate gp for a free lottery", "I've changed my mind, I don't want to donate.");
                    if (menuLottery1 == 0) {
                        player.message("Should there be a ticket purchase limit?");
                        int purchaseMenu = showMenu(player, "Yes, start a lottery with one ticket limit.",
                                "No, start a lottery with unlimited ticket purchases.");
                        if (player.isBusy() || purchaseMenu == -1) {
                            return;
                        }
                        if (commandMethods.isAnyEventRunning(player)) {
                            return;
                        }
                        boolean noLimitLottery = purchaseMenu == 0;
                        if (player.getBank().remove(10, 400000) > -1) {
                            player.message("400k gp has been taken from your bank account");
                            Server.getWorld().getLottery().setLimitLottery(noLimitLottery);
                            Server.getWorld().getLottery().startDonationLottery(player, false, 10);
                        } else {
                            player.message("You don't have enough gp in your bank account");
                        }
                        return;
                    } else if (menuLottery1 == 1) {
                        player.message("How much would you like to donate for a free lottery?");
                        int purchaseMenu2 = showMenu(player, "I've changed my mind, I don't want to donate.", "400k", "600k", "800k", "1000k");
                        if (player.isBusy() || purchaseMenu2 == -1) {
                            return;
                        }
                        if (commandMethods.isAnyEventRunning(player)) {
                            return;
                        }
                        if (purchaseMenu2 == 0) {
                            return;
                        }
                        player.message("You've selected to donate " + ((purchaseMenu2 + 1) * 200) + "k, are you sure?");
                        int confirmMenu1 = showMenu(player, "Yes, I want to donate " + ((purchaseMenu2 + 1) * 200) + "k.", "I've changed my mind, forget it.");
                        if (player.isBusy() || confirmMenu1 != 0) {
                            return;
                        }
                        if (commandMethods.isAnyEventRunning(player)) {
                            return;
                        }
                        if (purchaseMenu2 >= 0 || purchaseMenu2 <= 4) {
                            if (player.getBank().remove(10, ((purchaseMenu2 + 1) * 200000)) > -1) {
                                player.message(((purchaseMenu2 + 1) * 200) + "k gp has been taken from your bank account");
                                Server.getWorld().getLottery().startDonationLottery(player, true, ((purchaseMenu2 + 1) * 5));
                            } else {
                                player.message("You don't have enough gp in your bank account");
                            }
                            return;
                        }
                    }
                    break;
            }
        }

        if (command.equals("starteventvote")) {
            if (commandMethods.isAnyEventRunning(player)) {
                return;
            }
            player.message("What event would you like started?");
            int eventPickMenu = showMenu(player,"16 Player 1v1 Tournament","Survival event","Lottery event","I don't want to start a vote");
            if (player.isBusy() || eventPickMenu < 0 || eventPickMenu > 2) {
                return;
            }
            switch (eventPickMenu) {
                case 0:
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    if (Server.getWorld().getPvPTournament().isVoteOnCooldown()) {
                        ActionSender.sendBox(player,"Starting a vote not allowed yet, please wait @red@" + ((Server.getWorld().getPvPTournament().getVoteCooldown() / 60) + 1) +
                                " minutes@whi@ or start a 1v1 Tournament immediately by donating 600k gp for the prize pot, you can do that by typing @red@::donatefortournament" +
                                "@whi@, gp will be taken from your bank account. For more information type @red@::abouttournament", false);
                        return;
                    }
                    Server.getWorld().getPvPTournament().startTournamentVote();
                    break;
                case 1:
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    if (Server.getWorld().getSurvival().isVoteOnCooldown()) {
                        ActionSender.sendBox(player, "Starting a vote not allowed yet, please wait @red@" + ((Server.getWorld().getSurvival().getVoteCooldown() / 60) + 1) +
                                " minutes@whi@ or start a survival immediately by donating 600k gp for the prize pot, you can do that by typing @red@::donateforsurvival" +
                                "@whi@, gp will be taken from your bank account. For more information type @red@::aboutsurvival", false);
                        return;
                    }
                    Server.getWorld().getSurvival().startSurvivalVote();
                    break;
                case 2:
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    if (Server.getWorld().getLottery().isVoteOnCooldown()) {
                        ActionSender.sendBox(player, "Starting a vote not allowed yet, please wait @red@" + ((Server.getWorld().getLottery().getVoteCooldown() / 60) + 1) +
                                " minutes@whi@ or start a lottery immediately by donating 600k gp for the prize pot, you can do that by typing @red@::donateforlottery" +
                                "@whi@, gp will be taken from your bank account. For more information type @red@::aboutlottery", false);
                        return;
                    }
                    player.message("Should there be a ticket purchase limit?");
                    int purchaseMenu = showMenu(player, "Yes, start a lottery with one ticket limit",
                            "No, start a lottery with unlimited ticket purchases");
                    if (player.isBusy() || purchaseMenu == -1) {
                        return;
                    }
                    if (commandMethods.isAnyEventRunning(player)) {
                        return;
                    }
                    boolean noLimitLottery = purchaseMenu == 0;
                    Server.getWorld().getLottery().setLimitLottery(noLimitLottery);
                    Server.getWorld().getLottery().startLotteryVote();
                    break;
            }
        }

        if (command.equals("vote")) {
            if (Server.getWorld().getSurvival().isVotingStarted()) {
                Server.getWorld().getSurvival().addVote(player);
                return;
            }
            if (Server.getWorld().getLottery().isVotingStarted()) {
                Server.getWorld().getLottery().addVote(player);
                return;
            }
            if (Server.getWorld().getPvPTournament().isVotingStarted()) {
                Server.getWorld().getPvPTournament().addVote(player);
                return;
            }
            player.message("There is nothing to vote for at the moment");
        }

        /* Messages to inform people about the changed commands */
        if (command.equals("startsurvivalvote") || command.equals("startlotteryvote") || command.equals("starttournamentvote") ||
                command.equals("donateforlottery") || command.equals("donateforsurvival") || command.equals("donatefortournament")) {
            player.message("Our commands have been updated, please type @yel@::commands,");
            player.message("To see the most up to date list of commands available.");
        }
    }
}
