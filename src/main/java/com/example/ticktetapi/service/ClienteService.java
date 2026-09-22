package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.ClienteRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Cliente;
import com.example.ticktetapi.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente criar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("Já existe um cliente cadastrado com este e-mail");
        }
        if (clienteRepository.existsByCpf(dto.getCpf())) {
            throw new RegraNegocioException("Já existe um cliente cadastrado com este CPF");
        }

        Cliente cliente = Cliente.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .build();
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com id: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }
}
