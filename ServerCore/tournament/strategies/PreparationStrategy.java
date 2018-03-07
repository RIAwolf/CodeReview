package com.rscr.server.content.minigames.tournament.strategies;

import com.rscr.server.content.minigames.tournament.PvPTournament;
import com.rscr.server.content.minigames.tournament.TournamentParticipant;
import com.rscr.server.model.Point;
import com.rscr.server.model.entity.player.Player;
import com.rscr.server.net.rsc.ActionSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by tomassimkus on 20/02/2018.
 */
public class PreparationStrategy implements ITournamentStrategy {

    private PvPTournament tournament;

    public PreparationStrategy(PvPTournament currentTournament) {
        tournament = currentTournament;
    }

    @Override
    public void run() {
        if (tournament.teleportToArenaTimer > 0 && tournament.teleportToArenaTimer <= 5) {

            tournament.messageParticipants("@ran@"+tournament.teleportToArenaTimer+"!");
        } else if (tournament.teleportToArenaTimer == 0) {
            teleportPlayers();
            tournament.setStatus(PvPTournament.TournamentStatus.RUNNING);
        }
        tournament.teleportToArenaTimer--;
    }

    private void teleportPlayers() { //Two teleport locations in each room
        ArrayList<TournamentParticipant> array = (ArrayList<TournamentParticipant>) tournament.getParticipants().clone();
        Collections.shuffle(array);
        Iterator<TournamentParticipant> iterator = array.iterator();
        for (int room = 0; room < 4; room++) {
            if (iterator.hasNext()) {
                TournamentParticipant pla = iterator.next();
                TournamentParticipant opp = iterator.next();
                pla.setCurrentRoom(room);
                opp.setCurrentRoom(room);
                tournament.teleportToCurrentRoom(pla, 0);
                tournament.teleportToCurrentRoom(opp, 1);
            }
        }
        for (int room = 11; room < 15; room++) {
            if (iterator.hasNext()) {
                TournamentParticipant pla = iterator.next();
                TournamentParticipant opp = iterator.next();
                pla.setCurrentRoom(room);
                opp.setCurrentRoom(room);
                tournament.teleportToCurrentRoom(pla, 0);
                tournament.teleportToCurrentRoom(opp, 1);
            }
        }
    }
}
