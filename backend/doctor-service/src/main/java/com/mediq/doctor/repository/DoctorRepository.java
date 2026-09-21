package com.mediq.doctor.repository;

import com.mediq.doctor.entity.Doctor;
import com.mediq.doctor.entity.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByUserId(UUID userId);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByUserId(UUID userId);

    List<Doctor> findByStatus(DoctorStatus status);
}
