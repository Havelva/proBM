package cz.uhk.kpro2.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.repository.PlayerRepository;
@Service
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    @Autowired public PlayerServiceImpl(PlayerRepository playerRepository) { this.playerRepository = playerRepository; }
    @Override @Transactional(readOnly = true)
    public List<Player> getAllPlayers() { return playerRepository.findAll(); }
    @Override @Transactional(readOnly = true)
    public Player getPlayer(long id) { return playerRepository.findById(id).orElse(null); }
    @Override @Transactional
    public Player savePlayer(Player player) { return playerRepository.save(player); }
    @Override @Transactional
    public void deletePlayer(long id) { playerRepository.deleteById(id); }
    @Override @Transactional(readOnly = true)
    public List<Player> getPlayersByTeamId(Long teamId) { return playerRepository.findByTeamId(teamId); }
}