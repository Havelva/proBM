package cz.uhk.kpro2.repository;

import cz.uhk.kpro2.model.Coach; // Changed from BOSMember to Coach
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> { // Changed from BOSRepository to CoachRepository and BOSMember to Coach

}
