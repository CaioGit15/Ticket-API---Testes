package com.example.ticktetapi.controller;


import com.example.ticktetapi.dto.ClienteRequestDTO;
import com.example.ticktetapi.dto.ClienteResponseDTO;
import com.example.ticktetapi.model.Cliente;
import com.example.ticktetapi.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        Cliente cliente = clienteService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteResponseDTO.fromEntity(cliente));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> clientes = clienteService.listarTodos().stream()
                .map(ClienteResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponseDTO.fromEntity(clienteService.buscarPorId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
