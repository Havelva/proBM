package cz.uhk.kpro2.repository;

import cz.uhk.kpro2.model.Player; // Changed from FuelCell to Player
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> { // Changed from FuelCellRepository to PlayerRepository and FuelCell to Player
}
