package com.dev.quikkkk.user_service.repository;

import com.dev.quikkkk.user_service.entity.TrainerSchedule;
import com.dev.quikkkk.user_service.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITrainerScheduleRepository extends JpaRepository<TrainerSchedule, String> {
    List<TrainerSchedule> findByTrainerIdAndIsActiveTrue(String trainerId);

    List<TrainerSchedule> findByTrainerIdAndDayOfWeekAndIsActiveTrue(String trainerId, DayOfWeek dayOfWeek);

    void deleteByTrainerId(String trainerId);
}
