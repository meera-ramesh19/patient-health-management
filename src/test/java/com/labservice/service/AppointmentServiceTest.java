package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Appointment;
import com.labservice.model.Doctor;
import com.labservice.model.Patient;
import com.labservice.repository.AppointmentRepository;
import com.labservice.repository.DoctorRepository;
import com.labservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ─────────────────────────────────────────────────────────────
// UNIT TEST FOR AppointmentService
// ─────────────────────────────────────────────────────────────
// Tests the BUSINESS RULES in AppointmentService:
//   - Can't book in the past
//   - Can't double-book a doctor
//   - Patient and doctor must exist
//   - Cancel changes status to "CANCELLED"
// ─────────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("John");

        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setName("Dr. Smith");
    }

    @Test
    void bookAppointment_validData_createsAppointment() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorIdAndDateTime(1L, futureDate)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        Appointment result = appointmentService.bookAppointment(1L, 1L, futureDate, "Checkup");

        assertEquals("SCHEDULED", result.getStatus());
        assertEquals("Checkup", result.getReason());
        assertEquals(testPatient, result.getPatient());
        assertEquals(testDoctor, result.getDoctor());
    }

    @Test
    void bookAppointment_pastDate_throwsBadRequest() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> appointmentService.bookAppointment(1L, 1L, pastDate, "Checkup"));

        assertEquals("Cannot book appointments in the past", exception.getMessage());
    }

    @Test
    void bookAppointment_doctorDoubleBooked_throwsBadRequest() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorIdAndDateTime(1L, futureDate)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> appointmentService.bookAppointment(1L, 1L, futureDate, "Checkup"));

        assertEquals("Doctor already has an appointment at this time", exception.getMessage());
    }

    @Test
    void bookAppointment_patientNotFound_throwsNotFound() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(99L, 1L, futureDate, "Checkup"));
    }

    @Test
    void bookAppointment_doctorNotFound_throwsNotFound() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(1L, 99L, futureDate, "Checkup"));
    }

    @Test
    void cancelAppointment_setsStatusCancelled() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus("SCHEDULED");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Appointment result = appointmentService.cancelAppointment(1L);

        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    void getAppointmentById_notFound_throwsException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentById(99L));
    }
}
