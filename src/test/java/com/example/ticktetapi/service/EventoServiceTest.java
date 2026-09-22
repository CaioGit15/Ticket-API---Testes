package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.EventoRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

	@Mock
	private EventoRepository eventoRepository;

	@InjectMocks
	private EventoService eventoService;

	private EventoRequestDTO dtoValido;

	@BeforeEach
	void setUp() {
		dtoValido = new EventoRequestDTO();
		dtoValido.setNome("Show de Rock");
		dtoValido.setDescricao("Evento musical");
		dtoValido.setDataHora(LocalDateTime.now().plusDays(10));
		dtoValido.setLocal("Arena Central");
		dtoValido.setPreco(new BigDecimal("75.00"));
		dtoValido.setQuantidadeTotal(100);
	}

	@Test
	void deveCriarEventoComQuantidadeDisponivelIgualAoTotal() {
		when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
			Evento evento = invocation.getArgument(0);
			evento.setId(1L);
			return evento;
		});

		Evento criado = eventoService.criar(dtoValido);

		assertThat(criado.getId()).isEqualTo(1L);
		assertThat(criado.getNome()).isEqualTo(dtoValido.getNome());
		assertThat(criado.getQuantidadeDisponivel()).isEqualTo(100);
		verify(eventoRepository).save(any(Evento.class));
	}

	@Test
	void deveListarTodosOsEventos() {
		List<Evento> eventos = List.of(Evento.builder().id(1L).nome("Show").build());
		when(eventoRepository.findAll()).thenReturn(eventos);

		assertThat(eventoService.listarTodos()).isSameAs(eventos);
		verify(eventoRepository).findAll();
	}

	@Test
	void deveBuscarEventoPorId() {
		Evento evento = Evento.builder().id(1L).nome("Show").build();
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

		assertThat(eventoService.buscarPorId(1L)).isSameAs(evento);
	}

	@Test
	void deveLancarExcecaoQuandoEventoNaoForEncontrado() {
		when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> eventoService.buscarPorId(999L));
	}

	@Test
	void deveAtualizarEventoMantendoIngressosJaVendidos() {
		Evento evento = Evento.builder()
				.id(1L)
				.nome("Nome antigo")
				.dataHora(dtoValido.getDataHora())
				.local("Local antigo")
				.preco(new BigDecimal("50.00"))
				.quantidadeTotal(100)
				.quantidadeDisponivel(70)
				.build();
		dtoValido.setQuantidadeTotal(120);
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
		when(eventoRepository.save(evento)).thenReturn(evento);

		Evento atualizado = eventoService.atualizar(1L, dtoValido);

		assertThat(atualizado.getNome()).isEqualTo(dtoValido.getNome());
		assertThat(atualizado.getQuantidadeTotal()).isEqualTo(120);
		assertThat(atualizado.getQuantidadeDisponivel()).isEqualTo(90);
		verify(eventoRepository).save(evento);
	}

	@Test
	void deveImpedirReducaoAbaixoDaQuantidadeVendida() {
		Evento evento = Evento.builder().quantidadeTotal(100).quantidadeDisponivel(70).build();
		dtoValido.setQuantidadeTotal(29);
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

		assertThrows(RegraNegocioException.class, () -> eventoService.atualizar(1L, dtoValido));
		verify(eventoRepository, never()).save(any(Evento.class));
	}

	@Test
	void deveDeletarEventoExistente() {
		Evento evento = Evento.builder().id(1L).build();
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

		eventoService.deletar(1L);

		verify(eventoRepository).delete(evento);
	}
}
