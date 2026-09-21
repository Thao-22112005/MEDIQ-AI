package com.mediq.doctor.service;

import com.mediq.doctor.dto.request.AddDoctorSpecialtyRequest;
import com.mediq.doctor.dto.request.CreateDoctorRequest;
import com.mediq.doctor.dto.request.UpdateDoctorRequest;
import com.mediq.doctor.dto.response.DoctorResponse;
import com.mediq.doctor.dto.response.DoctorSpecialtyResponse;
import com.mediq.doctor.entity.Doctor;
import com.mediq.doctor.entity.DoctorSpecialty;
import com.mediq.doctor.entity.DoctorStatus;
import com.mediq.doctor.exception.BusinessException;
import com.mediq.doctor.exception.ErrorCode;
import com.mediq.doctor.mapper.DoctorMapper;
import com.mediq.doctor.repository.DoctorRepository;
import com.mediq.doctor.repository.DoctorSpecialtyRepository;
import com.mediq.doctor.validation.DoctorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorValidator doctorValidator;

    @Override
    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        doctorValidator.validateCreate(request);

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BusinessException(ErrorCode.LICENSE_NUMBER_ALREADY_EXISTS,
                    "Doctor with license number " + request.getLicenseNumber() + " already exists");
        }

        if (doctorRepository.existsByUserId(request.getUserId())) {
            throw new BusinessException(ErrorCode.USER_ID_ALREADY_EXISTS,
                    "Doctor with user ID " + request.getUserId() + " already exists");
        }

        Doctor doctor = Doctor.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName().trim())
                .licenseNumber(request.getLicenseNumber().trim())
                .status(DoctorStatus.ACTIVE)
                .specialties(new ArrayList<>())
                .build();

        if (request.getSpecialties() != null && !request.getSpecialties().isEmpty()) {
            for (CreateDoctorRequest.InitialSpecialtyRequest specReq : request.getSpecialties()) {
                DoctorSpecialty specialty = DoctorSpecialty.builder()
                        .doctorSpecialtyId(UUID.randomUUID())
                        .specialtyId(specReq.getSpecialtyId())
                        .isPrimary(specReq.isPrimary())
                        .createdAt(Instant.now())
                        .build();
                doctor.addSpecialty(specialty);
            }
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(savedDoctor);
    }

    @Override
    public DoctorResponse getDoctorById(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND, 
                        "Doctor not found with ID: " + doctorId));
        return doctorMapper.toDoctorResponse(doctor);
    }

    @Override
    public List<DoctorResponse> getAllDoctors(DoctorStatus status) {
        List<Doctor> doctors = (status != null) 
                ? doctorRepository.findByStatus(status) 
                : doctorRepository.findAll();
        return doctorMapper.toDoctorResponseList(doctors);
    }

    @Override
    @Transactional
    public DoctorResponse updateDoctor(UUID doctorId, UpdateDoctorRequest request) {
        doctorValidator.validateUpdate(request);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND, 
                        "Doctor not found with ID: " + doctorId));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            doctor.setFullName(request.getFullName().trim());
        }

        if (request.getStatus() != null) {
            doctor.setStatus(request.getStatus());
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(updatedDoctor);
    }

    @Override
    @Transactional
    public DoctorSpecialtyResponse addSpecialty(UUID doctorId, AddDoctorSpecialtyRequest request) {
        doctorValidator.validateAddSpecialty(request);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND, 
                        "Doctor not found with ID: " + doctorId));

        if (doctorSpecialtyRepository.existsByDoctor_DoctorIdAndSpecialtyId(doctorId, request.getSpecialtyId())) {
            throw new BusinessException(ErrorCode.SPECIALTY_ALREADY_ASSIGNED, 
                    "Specialty already assigned to doctor");
        }

        if (request.isPrimary() && doctorSpecialtyRepository.findByDoctor_DoctorIdAndIsPrimaryTrue(doctorId).isPresent()) {
            throw new BusinessException(ErrorCode.PRIMARY_SPECIALTY_ALREADY_EXISTS, 
                    "Doctor already has a primary specialty");
        }

        DoctorSpecialty specialty = DoctorSpecialty.builder()
                .doctorSpecialtyId(UUID.randomUUID())
                .doctor(doctor)
                .specialtyId(request.getSpecialtyId())
                .isPrimary(request.isPrimary())
                .createdAt(Instant.now())
                .build();

        DoctorSpecialty savedSpecialty = doctorSpecialtyRepository.save(specialty);
        return doctorMapper.toSpecialtyResponse(savedSpecialty);
    }

    @Override
    @Transactional
    public void removeSpecialty(UUID doctorId, UUID specialtyId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new BusinessException(ErrorCode.DOCTOR_NOT_FOUND, 
                    "Doctor not found with ID: " + doctorId);
        }

        if (!doctorSpecialtyRepository.existsByDoctor_DoctorIdAndSpecialtyId(doctorId, specialtyId)) {
            throw new BusinessException(ErrorCode.SPECIALTY_NOT_FOUND, 
                    "Specialty not found for this doctor");
        }

        doctorSpecialtyRepository.deleteByDoctor_DoctorIdAndSpecialtyId(doctorId, specialtyId);
    }

    @Override
    public List<DoctorSpecialtyResponse> getDoctorSpecialties(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new BusinessException(ErrorCode.DOCTOR_NOT_FOUND, 
                    "Doctor not found with ID: " + doctorId);
        }

        List<DoctorSpecialty> specialties = doctorSpecialtyRepository.findByDoctor_DoctorId(doctorId);
        return doctorMapper.toSpecialtyResponseList(specialties);
    }
}
