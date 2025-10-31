package com.dev.quikkkk.user_service.repository;

import com.dev.quikkkk.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllByOrderByCreatedDateDesc(Pageable pageable);

    @Query("""
            SELECT u FROM User u WHERE
            (:role IS NULL or u.role = :role) AND
            (:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY u.createdDate DESC
            """)
    Page<User> findAllWithFilters(@Param("role") String role,
                                  @Param("search") String search,
                                  Pageable pageable);
}
