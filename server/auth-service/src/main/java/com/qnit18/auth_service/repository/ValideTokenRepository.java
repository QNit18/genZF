package com.qnit18.auth_service.repository;

import com.qnit18.auth_service.entity.InvalidedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValideTokenRepository extends JpaRepository<InvalidedToken, String> {
}

