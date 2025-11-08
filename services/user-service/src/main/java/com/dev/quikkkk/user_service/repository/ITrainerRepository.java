package com.dev.quikkkk.user_service.repository;

import com.dev.quikkkk.user_service.entity.TrainerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITrainerRepository extends JpaRepository<TrainerProfile, String> {
    Optional<TrainerProfile> findByUserId(String userId);

    @Query("""
            SELECT t FROM TrainerProfile t
            JOIN t.user u
            WHERE t.isAvailable = true
            AND (:specialization IS NULL OR :specialization = ANY(t.specialization))
            """)
    Page<TrainerProfile> findAvailablyTrainers(@Param("Specialization") String specialization, Pageable pageable);

    boolean existsByUserId(String userId);
}
