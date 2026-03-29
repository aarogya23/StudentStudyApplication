package com.deltagamma.Acedemy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.LiveClassSession;

public interface LiveClassSessionRepository extends JpaRepository<LiveClassSession, Long> {
    List<LiveClassSession> findAllByOrderByScheduledForAsc();
}
