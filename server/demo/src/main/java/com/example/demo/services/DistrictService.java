package com.example.demo.services;

import com.example.demo.Entity.District;
import com.example.demo.Repository.DistrictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DistrictService {
    @Autowired
    private DistrictRepository districtRepository;

    @Transactional
    public District getOrCreateDistrict(String name) {
        // Search for the role by its name
        Optional<District> optionalDistrict = districtRepository.findByName(name);

        // If the role exists, return it
        if (optionalDistrict.isPresent()) {
            return optionalDistrict.get();
        } else {
            // If the role does not exist, create a new role with the given name
            District newDistrict = new District();
            newDistrict.setName(name);
            return districtRepository.save(newDistrict);
        }
    }
}
