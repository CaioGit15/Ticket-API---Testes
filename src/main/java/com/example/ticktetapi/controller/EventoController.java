package com.example.ticktetapi.controller;

import com.example.ticktetapi.dto.EventoRequestDTO;
import com.example.ticktetapi.dto.EventoResponseDTO;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    public ResponseEntity<EventoResponseDTO> criar(@Valid @RequestBody EventoRequestDTO dto) {
        Evento evento = eventoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventoResponseDTO.fromEntity(evento));
    }

    @GetMapping
    public ResponseEntity<List<EventoResponseDTO>> listarTodos() {
        List<EventoResponseDTO> eventos = eventoService.listarTodos().stream()
                .map(EventoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(EventoResponseDTO.fromEntity(eventoService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody EventoRequestDTO dto) {
        Evento evento = eventoService.atualizar(id, dto);
        return ResponseEntity.ok(EventoResponseDTO.fromEntity(evento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
