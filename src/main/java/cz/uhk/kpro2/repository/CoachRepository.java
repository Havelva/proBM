package cz.uhk.kpro2.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cz.uhk.kpro2.model.Coach;
@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {}