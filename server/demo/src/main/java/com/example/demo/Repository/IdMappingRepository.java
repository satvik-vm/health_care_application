package com.example.demo.Repository;

import com.example.demo.Entity.Hospital;
import com.example.demo.Entity.IdMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdMappingRepository extends JpaRepository<IdMapping, Integer> {
    IdMapping findByPrivateId(UUID uuid);
}
