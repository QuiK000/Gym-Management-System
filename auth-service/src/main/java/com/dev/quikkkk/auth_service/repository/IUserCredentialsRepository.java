package com.dev.quikkkk.auth_service.repository;

import com.dev.quikkkk.auth_service.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserCredentialsRepository extends JpaRepository<UserCredentials, String> {
}
