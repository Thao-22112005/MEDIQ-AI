package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.WorkScheduleResponse;
import com.mediq.scheduling.entity.WorkSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {

    @Mapping(target = "clinicId", source = "clinic.clinicId")
    @Mapping(target = "specialtyId", source = "specialty.specialtyId")
    @Mapping(target = "roomId", source = "room.roomId")
    WorkScheduleResponse toWorkScheduleResponse(WorkSchedule schedule);

    List<WorkScheduleResponse> toWorkScheduleResponseList(List<WorkSchedule> schedules);
}
