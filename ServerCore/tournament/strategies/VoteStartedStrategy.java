package com.rscr.server.content.minigames.tournament.strategies;

import com.rscr.server.Server;
import com.rscr.server.content.minigames.EventVote;
import com.rscr.server.content.minigames.tournament.PvPTournament;

/**
 * Created by tomassimkus on 20/02/2018.
 */
public class VoteStartedStrategy implements ITournamentStrategy {

    private PvPTournament tournament;
    private EventVote voteSystem;//Initialized to the one at PvPTournament class

    public VoteStartedStrategy(PvPTournament pvPTournament, EventVote voteSystem) {
        this.tournament = pvPTournament;
        this.voteSystem = voteSystem;
    }

    @Override
    public void run() {//Should work now.
        tournament.processAnnouncements();//im pretty sure you're using different instance for voting.
        voteSystem.updateVoteInterfaces("1v1 Tournament Event");
        if(voteSystem.isVoteExpired()) {
            Server.getWorld().sendEventMessage("Vote for 1v1 Tournament event has gathered only " + voteSystem.voteCount() + " out of 12 required");
            Server.getWorld().sendEventMessage("For more ways to start a 1v1 Tournament event type: ::abouttournament");
            tournament.setStatus(PvPTournament.TournamentStatus.IDLE);
            voteSystem.resetVoteValues();
        }
    }
}
