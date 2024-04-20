package com.example.demo.services;

import com.example.demo.Entity.IdMapping;
import com.example.demo.Entity.Role;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Transactional
    public Role getOrCreateRole(String roleName) {
        // Search for the role by its name
        Optional<Role> optionalRole = roleRepository.findByName(roleName);

        // If the role exists, return it
        if (optionalRole.isPresent()) {
            return optionalRole.get();
        } else {
            // If the role does not exist, create a new role with the given name
            Role newRole = new Role();

            // Create a new IdMapping object and set its privateId to the Answer's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(newRole.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            newRole.setName(roleName);
            return roleRepository.save(newRole);
        }
    }
}
