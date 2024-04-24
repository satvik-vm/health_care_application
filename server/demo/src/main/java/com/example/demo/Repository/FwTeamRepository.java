package com.example.demo.Repository;

import com.example.demo.Entity.FwTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FwTeamRepository extends JpaRepository<FwTeam, String> {
}
