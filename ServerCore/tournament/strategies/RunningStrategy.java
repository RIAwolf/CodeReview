package com.rscr.server.content.minigames.tournament.strategies;

import com.rscr.server.Server;
import com.rscr.server.content.minigames.tournament.PvPTournament;
import com.rscr.server.content.minigames.tournament.TournamentParticipant;
import com.rscr.server.model.container.Item;
import com.rscr.server.model.entity.player.Player;
import com.rscr.server.net.rsc.ActionSender;

public class RunningStrategy implements ITournamentStrategy {

    private static final long TIME_LIMIT = 60;

    private PvPTournament tournament;

    public RunningStrategy(PvPTournament currentTournament) {
        tournament = currentTournament;
    }

    @Override
    public void run() {
        tournament.handleFightCountdown();
        for (TournamentParticipant player : tournament.getParticipants()) {
            if(player.getTimeOpponentKilled() > 0) {
                long now = System.currentTimeMillis();
                if ((now - player.getTimeOpponentKilled()) / 1000 == 40) {
                    player.getPlayer().message("@red@You will be teleported to the next room if you don't enter it");
                    player.getPlayer().message("@red@You have 20 seconds to pickup food and use altar.");
                }
                if (System.currentTimeMillis() - player.getTimeOpponentKilled() >= TIME_LIMIT * 1000) {
                    tournament.setNextRoom(player);
                    tournament.teleportToCurrentRoom(player,0);
                    if(!player.getPlayer().isAloneInArena()) {
                        TournamentParticipant participant = Server.getWorld().getPvPTournament().getOpponent(player);
                        player.setFightCountdown(5);
                        participant.setFightCountdown(5);
                        System.out.println("p: " + participant.getCurrentRoom() + ", " + player.getCurrentRoom());
                        System.out.println("p: " + participant.getPlayer().getUsername() + ", " + player.getPlayer().getUsername());
                    }
                    player.setTimeOpponentKilled(0);
                }
            }
        }
        for(TournamentParticipant tp : tournament.getParticipants()) {
            if(tp.isReplacementRequired())
                return;
        }
        if (tournament.getParticipants().size() == 1) {
            int prize = tournament.prizePot;
            TournamentParticipant winner= tournament.getParticipants().get(0);
            if(winner == null) {
                tournament.messageParticipants("Error while getting winner");
                tournament.resetTournament();
                return;
            }
            Player winnerPlayer =  winner.getPlayer();
            if (tournament.prizeIsAnItem) {
                ActionSender.sendBox(winnerPlayer, "@ran@Congratulations!%@whi@You have won the event!%" + "Prize won:@red@ " + tournament.donatedItemName + "@whi@!%" +
                        "Game moderator will reach you and trade you the prize.", false);
            } else {
                ActionSender.sendBox(winnerPlayer, "@ran@Congratulations!%@whi@You have won the event!%" + "Prize pot won:@red@ " + prize + " @whi@gp!%" +
                        "Prize has been added to your bank account.", false);
            }
            winnerPlayer.setTournamentInstance(null);
            winnerPlayer.getBank().add(new Item(10, prize));
            tournament.stopTournament();
            Server.getWorld().sendEventMessage("@ran@" +  winner.getCharacterName() + " has won the 1v1 Tournament! Congratulations!");

            winnerPlayer.getStatistics().increaseStat("tournaments_won");
            if(winnerPlayer.getStatistics().get("tournaments_won") == 1) {
                Server.getWorld().sendEventMessage("It's " + winner.getCharacterName() + " first 1v1 tournaments win!");
            } else {
                Server.getWorld().sendEventMessage(winner.getCharacterName() + " has won 1v1 tournament " + winnerPlayer.getStatistics().get("tournaments_won") + " times already!");
            }
            tournament.resetItemsAndStats(winnerPlayer);
            winnerPlayer.teleport(217,460,false);
        }
    }
}
