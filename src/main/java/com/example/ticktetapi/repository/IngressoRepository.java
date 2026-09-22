package com.example.ticktetapi.repository;

import com.example.ticktetapi.model.Ingresso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngressoRepository extends JpaRepository<Ingresso, Long> {

    List<Ingresso> findByEventoId(Long id);

    List<Ingresso> findByClienteId(Long id);

}
