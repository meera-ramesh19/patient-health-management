package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.*;
import com.labservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment bookAppointment(Long patientId, Long doctorId,
                                        LocalDateTime dateTime, String reason) {

        // RULE 1: Patient must exist
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // RULE 2: Doctor must exist
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // RULE 3: Can't book in the past
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book appointments in the past");
        }

        // RULE 4: Doctor can't be double-booked
        boolean conflict = appointmentRepository
            .existsByDoctorIdAndDateTime(doctorId, dateTime);
        if (conflict) {
            throw new BadRequestException("Doctor already has an appointment at this time");
        }

        // ALL RULES PASSED — create the appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDateTime(dateTime);
        appointment.setReason(reason);
        appointment.setStatus("SCHEDULED");

        return appointmentRepository.save(appointment);
    }

    public Appointment cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus("CANCELLED");
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
}
