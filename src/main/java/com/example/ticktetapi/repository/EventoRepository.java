package com.example.ticktetapi.repository;

import com.example.ticktetapi.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
}
