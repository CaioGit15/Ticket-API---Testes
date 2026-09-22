package com.example.ticktetapi.controller;


import com.example.ticktetapi.dto.CompraIngressoRequestDTO;
import com.example.ticktetapi.dto.IngressoResponseDTO;
import com.example.ticktetapi.model.Ingresso;
import com.example.ticktetapi.service.IngressoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingressos")
@RequiredArgsConstructor
public class IngressoController {

    private final IngressoService ingressoService;

    @PostMapping("/comprar")
    public ResponseEntity<List<IngressoResponseDTO>> comprar(@Valid @RequestBody CompraIngressoRequestDTO dto) {
        List<IngressoResponseDTO> ingressos = ingressoService.comprar(dto).stream()
                .map(IngressoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(ingressos);
    }

    @GetMapping
    public ResponseEntity<List<IngressoResponseDTO>> listarTodos() {
        List<IngressoResponseDTO> ingressos = ingressoService.listarTodos().stream()
                .map(IngressoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ingressos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngressoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(IngressoResponseDTO.fromEntity(ingressoService.buscarPorId(id)));
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<IngressoResponseDTO>> listarPorEvento(@PathVariable Long eventoId) {
        List<IngressoResponseDTO> ingressos = ingressoService.listarPorEvento(eventoId).stream()
                .map(IngressoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ingressos);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<IngressoResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        List<IngressoResponseDTO> ingressos = ingressoService.listarPorCliente(clienteId).stream()
                .map(IngressoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ingressos);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<IngressoResponseDTO> cancelar(@PathVariable Long id) {
        Ingresso ingresso = ingressoService.cancelar(id);
        return ResponseEntity.ok(IngressoResponseDTO.fromEntity(ingresso));
    }

    @PatchMapping("/{id}/utilizar")
    public ResponseEntity<IngressoResponseDTO> marcarComoUtilizado(@PathVariable Long id) {
        Ingresso ingresso = ingressoService.marcarComoUtilizado(id);
        return ResponseEntity.ok(IngressoResponseDTO.fromEntity(ingresso));
    }
}
