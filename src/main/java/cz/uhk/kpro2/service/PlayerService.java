package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Player;

import java.util.List;

public interface PlayerService {
    List<Player> getAllPlayers();
    Player getPlayer(long id);
    Player savePlayer(Player player); // Changed return type from void to Player
    void deletePlayer(long id);
}
