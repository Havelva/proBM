package cz.uhk.kpro2.repository;

import cz.uhk.kpro2.model.Team; // Changed from Course to Team
import cz.uhk.kpro2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> { // Changed from CourseRepository to TeamRepository and Course to Team

}
