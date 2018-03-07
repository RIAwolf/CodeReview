package com.rscr.server.content.minigames.tournament.strategies;

import com.rscr.server.Server;
import com.rscr.server.content.minigames.tournament.PvPTournament;
import com.rscr.server.content.minigames.tournament.TournamentParticipant;

import java.util.Map;

/**
 * Created by tomassimkus on 20/02/2018.
 */
public class LookingForParticipantsStrategy implements ITournamentStrategy {

    private PvPTournament tournament;
    private int lookingForReplacementTimer;

    public LookingForParticipantsStrategy(PvPTournament currentTournament) {
        tournament = currentTournament;
    }

    @Override
    public void run() {
        tournament.handleFightCountdown();
        if (lookingForReplacementTimer == 0) {
            announceLookingForReplacement();
            lookingForReplacementTimer = 20;
        }
        lookingForReplacementTimer--;

        for(TournamentParticipant tp : tournament.getParticipants()) {
            if(tp.isReplacementRequired()) {
                return;
            }
        }
        tournament.setStatus(PvPTournament.TournamentStatus.RUNNING);
    }

    private void announceLookingForReplacement() {
        Server.getWorld().sendEventMessage("A player has disconnected during 16 player 1v1 Tournament.");
        Server.getWorld().sendEventMessage("If you would like to replace him in the event, type ::replace, entry is free.");
    }
}
