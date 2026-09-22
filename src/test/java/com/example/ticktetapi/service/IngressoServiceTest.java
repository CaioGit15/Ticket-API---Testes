package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.CompraIngressoRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Cliente;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.model.Ingresso;
import com.example.ticktetapi.model.StatusIngresso;
import com.example.ticktetapi.repository.ClienteRepository;
import com.example.ticktetapi.repository.EventoRepository;
import com.example.ticktetapi.repository.IngressoRepository;
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
class IngressoServiceTest {

	@Mock
	private IngressoRepository ingressoRepository;

	@Mock
	private EventoRepository eventoRepository;

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private IngressoService ingressoService;

	private Evento evento;
	private Cliente cliente;
	private CompraIngressoRequestDTO compra;

	@BeforeEach
	void setUp() {
		evento = Evento.builder()
				.id(1L)
				.dataHora(LocalDateTime.now().plusDays(1))
				.preco(new BigDecimal("50.00"))
				.quantidadeDisponivel(10)
				.build();
		cliente = Cliente.builder().id(2L).nome("Maria").build();
		compra = new CompraIngressoRequestDTO();
		compra.setEventoId(1L);
		compra.setClienteId(2L);
		compra.setQuantidade(2);
	}

	@Test
	void deveComprarQuantidadeSolicitadaEAtualizarDisponibilidade() {
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
		when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
		when(ingressoRepository.save(any(Ingresso.class))).thenAnswer(invocation -> invocation.getArgument(0));

		List<Ingresso> ingressos = ingressoService.comprar(compra);

		assertThat(ingressos).hasSize(2).allSatisfy(ingresso -> {
			assertThat(ingresso.getEvento()).isSameAs(evento);
			assertThat(ingresso.getCliente()).isSameAs(cliente);
			assertThat(ingresso.getStatus()).isEqualTo(StatusIngresso.ATIVO);
			assertThat(ingresso.getValorPago()).isEqualByComparingTo("50.00");
		});
		assertThat(evento.getQuantidadeDisponivel()).isEqualTo(8);
		verify(ingressoRepository, times(2)).save(any(Ingresso.class));
		verify(eventoRepository).save(evento);
	}

	@Test
	void deveUsarUmIngressoQuandoQuantidadeNaoForInformada() {
		compra.setQuantidade(null);
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
		when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
		when(ingressoRepository.save(any(Ingresso.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(ingressoService.comprar(compra)).hasSize(1);
		assertThat(evento.getQuantidadeDisponivel()).isEqualTo(9);
	}

	@Test
	void deveImpedirCompraDeEventoJaOcorrido() {
		evento.setDataHora(LocalDateTime.now().minusMinutes(1));
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
		when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));

		assertThrows(RegraNegocioException.class, () -> ingressoService.comprar(compra));
		verify(ingressoRepository, never()).save(any(Ingresso.class));
	}

	@Test
	void deveImpedirCompraAcimaDaDisponibilidade() {
		compra.setQuantidade(11);
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
		when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));

		assertThrows(RegraNegocioException.class, () -> ingressoService.comprar(compra));
		verify(ingressoRepository, never()).save(any(Ingresso.class));
	}

	@Test
	void deveLancarExcecaoQuandoEventoNaoForEncontradoNaCompra() {
		when(eventoRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> ingressoService.comprar(compra));
		verifyNoInteractions(clienteRepository, ingressoRepository);
	}

	@Test
	void deveLancarExcecaoQuandoClienteNaoForEncontradoNaCompra() {
		when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
		when(clienteRepository.findById(2L)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> ingressoService.comprar(compra));
		verifyNoInteractions(ingressoRepository);
	}

	@Test
	void deveListarIngressosPorEventoECliente() {
		List<Ingresso> ingressos = List.of(Ingresso.builder().id(1L).build());
		when(ingressoRepository.findByEventoId(1L)).thenReturn(ingressos);
		when(ingressoRepository.findByClienteId(2L)).thenReturn(ingressos);

		assertThat(ingressoService.listarPorEvento(1L)).isSameAs(ingressos);
		assertThat(ingressoService.listarPorCliente(2L)).isSameAs(ingressos);
		verify(ingressoRepository).findByEventoId(1L);
		verify(ingressoRepository).findByClienteId(2L);
	}

	@Test
	void deveBuscarIngressoPorId() {
		Ingresso ingresso = Ingresso.builder().id(1L).build();
		when(ingressoRepository.findById(1L)).thenReturn(Optional.of(ingresso));

		assertThat(ingressoService.buscarPorId(1L)).isSameAs(ingresso);
	}

	@Test
	void deveCancelarIngressoEDevolverDisponibilidade() {
		Ingresso ingresso = Ingresso.builder().id(1L).evento(evento).status(StatusIngresso.ATIVO).build();
		when(ingressoRepository.findById(1L)).thenReturn(Optional.of(ingresso));
		when(ingressoRepository.save(ingresso)).thenReturn(ingresso);

		Ingresso cancelado = ingressoService.cancelar(1L);

		assertThat(cancelado.getStatus()).isEqualTo(StatusIngresso.CANCELADO);
		assertThat(evento.getQuantidadeDisponivel()).isEqualTo(11);
		verify(ingressoRepository).save(ingresso);
		verify(eventoRepository).save(evento);
	}

	@Test
	void deveImpedirCancelamentoDeIngressoCanceladoOuUtilizado() {
		for (StatusIngresso status : List.of(StatusIngresso.CANCELADO, StatusIngresso.UTILIZADO)) {
			Ingresso ingresso = Ingresso.builder().id(1L).evento(evento).status(status).build();
			when(ingressoRepository.findById(1L)).thenReturn(Optional.of(ingresso));

			assertThrows(RegraNegocioException.class, () -> ingressoService.cancelar(1L));
		}
		verify(ingressoRepository, never()).save(any(Ingresso.class));
	}

	@Test
	void deveMarcarIngressoAtivoComoUtilizado() {
		Ingresso ingresso = Ingresso.builder().id(1L).status(StatusIngresso.ATIVO).build();
		when(ingressoRepository.findById(1L)).thenReturn(Optional.of(ingresso));
		when(ingressoRepository.save(ingresso)).thenReturn(ingresso);

		assertThat(ingressoService.marcarComoUtilizado(1L).getStatus()).isEqualTo(StatusIngresso.UTILIZADO);
		verify(ingressoRepository).save(ingresso);
	}

	@Test
	void deveImpedirMarcarIngressoNaoAtivoComoUtilizado() {
		Ingresso ingresso = Ingresso.builder().id(1L).status(StatusIngresso.CANCELADO).build();
		when(ingressoRepository.findById(1L)).thenReturn(Optional.of(ingresso));

		assertThrows(RegraNegocioException.class, () -> ingressoService.marcarComoUtilizado(1L));
		verify(ingressoRepository, never()).save(any(Ingresso.class));
	}
}
