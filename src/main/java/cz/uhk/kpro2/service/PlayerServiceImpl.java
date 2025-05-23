package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Player getPlayer(long id) {
        Optional<Player> player = playerRepository.findById(id);
        return player.orElse(null);
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public void deletePlayer(long id) {
        playerRepository.deleteById(id);
    }
}
