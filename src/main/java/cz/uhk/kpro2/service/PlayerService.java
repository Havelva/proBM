package cz.uhk.kpro2.service;
import java.util.List;

import cz.uhk.kpro2.model.Player;
public interface PlayerService {
    List<Player> getAllPlayers();
    Player getPlayer(long id);
    Player savePlayer(Player player);
    void deletePlayer(long id);
    List<Player> getPlayersByTeamId(Long teamId);
}