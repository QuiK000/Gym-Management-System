package com.dev.quikkkk.auth_service.repository;

import com.dev.quikkkk.auth_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
