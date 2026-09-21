package com.mediq.appointment.repository;

import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentId = :appointmentId")
    Optional<Appointment> findByIdWithLock(@Param("appointmentId") UUID appointmentId);

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByDoctorId(UUID doctorId);

    List<Appointment> findByClinicId(UUID clinicId);

    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

    List<Appointment> findByStatus(AppointmentStatus status);

    Optional<Appointment> findBySlotId(UUID slotId);
}
