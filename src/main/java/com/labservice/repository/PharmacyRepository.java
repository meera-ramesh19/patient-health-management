package com.labservice.repository;

import com.labservice.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    // Search pharmacies by name (partial, case-insensitive)
    List<Pharmacy> findByNameContainingIgnoreCase(String name);

    // Find pharmacy by license number
    Pharmacy findByLicenseNumber(String licenseNumber);
}
