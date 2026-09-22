package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.ClienteRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Cliente;
import com.example.ticktetapi.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new ClienteRequestDTO();
        dtoValido.setNome("Maria Silva");
        dtoValido.setEmail("msilva@gmail.com");
        dtoValido.setCpf("11122233344");
    }

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByEmail(dtoValido.getEmail())).thenReturn(false);
        when(clienteRepository.existsByCpf(dtoValido.getCpf())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente c = invocation.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        Cliente criado = clienteService.criar(dtoValido);

        assertThat(criado.getId()).isEqualTo(1L);
        assertThat(criado.getEmail()).isEqualTo("msilva@gmail.com");
    }

    @Test
    void deveLancarExcecaoAoCriarClienteComEmailDuplicado() {
        when(clienteRepository.existsByEmail(dtoValido.getEmail())).thenReturn(true);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> clienteService.criar(dtoValido));

        assertThat(ex.getMessage()).contains("e-mail");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveLancarExececaoAoCriarClienteComCpfDuplicado() {
        when(clienteRepository.existsByCpf(dtoValido.getCpf())).thenReturn(true);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> clienteService.criar(dtoValido));

        assertThat(ex.getMessage().contains("CPF"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarClientePorIdComSucesso() {
        Cliente cliente = Cliente.builder().id(1L).nome("Maria Silva").build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente encontrado = clienteService.buscarPorId(1L);

        assertThat(encontrado.getNome()).isEqualTo("Maria Silva");
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.buscarPorId(999L));
    }

    @Test
    void deveDeletarClienteExistente() {

        Cliente cliente = Cliente.builder().id(1L).build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.deletar(1L);

        verify(clienteRepository).delete(cliente);

    }

}