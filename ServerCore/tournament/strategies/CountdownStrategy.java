package com.rscr.server.content.minigames.tournament.strategies;

import com.rscr.server.Server;
import com.rscr.server.content.minigames.tournament.PvPTournament;
import com.rscr.server.content.minigames.tournament.TournamentInterface;
import com.rscr.server.model.entity.player.Player;

/**
 * Created by tomassimkus on 20/02/2018.
 */
public class CountdownStrategy implements ITournamentStrategy {

    private static int REQUIRED_PARTICIPANTS = 16;
    private PvPTournament tournament;

    public CountdownStrategy(PvPTournament currentTournament) {
        tournament = currentTournament;
    }

    @Override
    public void run() {
        tournament.processAnnouncements();
        if (tournament.timeTillStart > 0) {
            tournament.timeTillStart--;
            tournament.secondsTillStart--;
            updateInterfaces();
        }
        if (tournament.secondsTillStart == 0) {
            tournament.secondsTillStart = 60;
        }
        if(tournament.timeTillStart == 0) {
            if (tournament.getParticipants().size() < REQUIRED_PARTICIPANTS) {
                hideTournamentInterface();
                tournament.cancelTournament();
            }
        }
        if (tournament.getParticipants().size() == REQUIRED_PARTICIPANTS) {
            tournament.messageParticipants(tournament.getParticipants().size() + " fighters have been finally gathered! Entry to event closed!");
            tournament.messageParticipants("Get ready and pot up! Fights starting in 15 seconds!");
            hideTournamentInterface();
            tournament.setStatus(PvPTournament.TournamentStatus.PREPARATION);
        }
    }

    private void hideTournamentInterface() {
        for (Player p : Server.getWorld().getPlayers()) {
            if(p.getIronMan() > 0 || p.isDeadMan())
                continue;
            TournamentInterface.hide(p);
        }
    }

    private void updateInterfaces() {
        for (Player p : Server.getWorld().getPlayers()) {
            if(p.getIronMan() > 0 || p.isDeadMan())
                continue;
            if(!p.getAttribute("tournament_interface_init", false)) {
                TournamentInterface.show(p);
            }
            TournamentInterface.update(p,tournament.timeTillStart,tournament.secondsTillStart,REQUIRED_PARTICIPANTS - tournament.getParticipants().size(),
                    getPrize(),tournament.charBuildName(tournament.characterBuildId));
        }
    }

    private String getPrize() {
        String prize;
        if (tournament.prizeIsAnItem) {
            prize = tournament.donatedItemName;
        } else {
            prize = Integer.toString(tournament.prizePot / 1000) + "k" ;
        }
        return prize;
    }
}
