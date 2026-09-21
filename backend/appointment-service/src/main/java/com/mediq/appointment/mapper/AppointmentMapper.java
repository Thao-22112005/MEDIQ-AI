package com.mediq.appointment.mapper;

import com.mediq.appointment.dto.response.AppointmentHistoryResponse;
import com.mediq.appointment.dto.response.AppointmentResponse;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentResponse toAppointmentResponse(Appointment appointment);

    List<AppointmentResponse> toAppointmentResponseList(List<Appointment> appointments);

    @Mapping(target = "appointmentId", source = "appointment.appointmentId")
    AppointmentHistoryResponse toHistoryResponse(AppointmentHistory history);

    List<AppointmentHistoryResponse> toHistoryResponseList(List<AppointmentHistory> histories);
}
