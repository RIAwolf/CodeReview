package com.rscr.server.content.minigames.tournament;

import com.rscr.server.model.entity.player.Player;
import com.rscr.server.net.rsc.ActionSender;

/**
 * Created by tomassimkus on 13/02/2018.
 */
public class TournamentInterface {

    public static void show(Player player) {
        ActionSender.sendOnScreenTextReset(player);
        ActionSender.sendOnScreenText(player, "Tournament char build:", 5, 15, 0, 0xFFFFFF, true);//Line 3
        ActionSender.sendOnScreenText(player, "Joining time left: ", 5, 30, 0, 0xFFFFFF, true);//Line 0
        ActionSender.sendOnScreenText(player, "Places left:", 5, 45, 0, 0xFFFFFF, true);//Line 1
        ActionSender.sendOnScreenText(player, "Prize pot:", 5, 60, 0, 0xFFFFFF, true);//Line 2
        ActionSender.sendOnScreenTextVisible(player, true);
        player.setAttribute("tournament_interface_init", true);
    }

    public static void update(Player player, int minutesleft, int secondsLeft, int placesleft, String prizepot, String charBuild) {
        ActionSender.sendOnScreenTextUpdate(player, 0,"Tournament char build: " + charBuild);
        ActionSender.sendOnScreenTextUpdate(player, 1,"Joining time left: " + (minutesleft > 60 ? (minutesleft / 60 + ":" + ((secondsLeft > 9)
                ? (secondsLeft) : "0" + secondsLeft)) : (minutesleft + " seconds")));
        ActionSender.sendOnScreenTextUpdate(player, 2,"Places left: " + placesleft);
        ActionSender.sendOnScreenTextUpdate(player, 3,"Prize pot: " + prizepot);
    }

    public static void hide(Player player) {
        ActionSender.sendOnScreenTextReset(player);
        ActionSender.sendOnScreenTextVisible(player, false);
        player.setAttribute("tournament_interface_init", false);
    }

}
