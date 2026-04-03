package com.labservice.service;

import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Pharmacy;
import com.labservice.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PharmacyService {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    // Get all pharmacies
    public List<Pharmacy> getAllPharmacies() {
        return pharmacyRepository.findAll();
    }

    // Get one pharmacy by ID
    public Pharmacy getPharmacyById(Long id) {
        return pharmacyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id: " + id));
    }

    // Search pharmacies by name
    public List<Pharmacy> searchByName(String name) {
        return pharmacyRepository.findByNameContainingIgnoreCase(name);
    }

    // Create a new pharmacy (Admin Portal)
    public Pharmacy createPharmacy(Pharmacy pharmacy) {
        return pharmacyRepository.save(pharmacy);
    }

    // Update pharmacy info
    public Pharmacy updatePharmacy(Long id, Pharmacy updatedData) {
        Pharmacy pharmacy = getPharmacyById(id);
        pharmacy.setName(updatedData.getName());
        pharmacy.setPhoneNumber(updatedData.getPhoneNumber());
        pharmacy.setEmail(updatedData.getEmail());
        pharmacy.setLicenseNumber(updatedData.getLicenseNumber());
        pharmacy.setAddress(updatedData.getAddress());
        pharmacy.setHoursOfOperation(updatedData.getHoursOfOperation());
        pharmacy.setServicesOffered(updatedData.getServicesOffered());
        return pharmacyRepository.save(pharmacy);
    }

    // Delete a pharmacy (Admin Portal)
    public void deletePharmacy(Long id) {
        pharmacyRepository.deleteById(id);
    }
}
