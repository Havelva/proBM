package cz.uhk.kpro2.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cz.uhk.kpro2.model.Team;
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {}