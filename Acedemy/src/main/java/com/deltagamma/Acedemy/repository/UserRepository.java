package com.deltagamma.Acedemy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
