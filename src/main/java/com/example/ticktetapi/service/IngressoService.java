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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngressoService {

    private final IngressoRepository ingressoRepository;
    private final EventoRepository eventoRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Compra 1 ou mais ingressos para um evento, de forma atômica:
     * ou reserva a quantidade total pedida, ou não reserva nada.
     */
    @Transactional
    public List<Ingresso> comprar(CompraIngressoRequestDTO dto) {
        Evento evento = eventoRepository.findById(dto.getEventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Evento não encontrado com id: " + dto.getEventoId()));

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado com id: " + dto.getClienteId()));

        if (evento.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível comprar ingressos para um evento que já ocorreu");
        }

        int quantidade = dto.getQuantidade() == null ? 1 : dto.getQuantidade();

        if (evento.getQuantidadeDisponivel() < quantidade) {
            throw new RegraNegocioException(
                    "Ingressos esgotados. Disponível: " + evento.getQuantidadeDisponivel()
                            + ", solicitado: " + quantidade);
        }

        List<Ingresso> ingressosComprados = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Ingresso ingresso = Ingresso.builder()
                    .codigo(UUID.randomUUID().toString())
                    .evento(evento)
                    .cliente(cliente)
                    .dataCompra(LocalDateTime.now())
                    .valorPago(evento.getPreco())
                    .status(StatusIngresso.ATIVO)
                    .build();
            ingressosComprados.add(ingressoRepository.save(ingresso));
        }

        evento.setQuantidadeDisponivel(evento.getQuantidadeDisponivel() - quantidade);
        eventoRepository.save(evento);

        return ingressosComprados;
    }

    @Transactional(readOnly = true)
    public List<Ingresso> listarTodos() {
        return ingressoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ingresso buscarPorId(Long id) {
        return ingressoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ingresso não encontrado com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Ingresso> listarPorEvento(Long eventoId) {
        return ingressoRepository.findByEventoId(eventoId);
    }

    @Transactional(readOnly = true)
    public List<Ingresso> listarPorCliente(Long clienteId) {
        return ingressoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Ingresso cancelar(Long id) {
        Ingresso ingresso = buscarPorId(id);

        if (ingresso.getStatus() == StatusIngresso.CANCELADO) {
            throw new RegraNegocioException("Este ingresso já está cancelado");
        }
        if (ingresso.getStatus() == StatusIngresso.UTILIZADO) {
            throw new RegraNegocioException("Não é possível cancelar um ingresso já utilizado");
        }

        ingresso.setStatus(StatusIngresso.CANCELADO);
        ingressoRepository.save(ingresso);

        Evento evento = ingresso.getEvento();
        evento.setQuantidadeDisponivel(evento.getQuantidadeDisponivel() + 1);
        eventoRepository.save(evento);

        return ingresso;
    }

    @Transactional
    public Ingresso marcarComoUtilizado(Long id) {
        Ingresso ingresso = buscarPorId(id);

        if (ingresso.getStatus() != StatusIngresso.ATIVO) {
            throw new RegraNegocioException("Somente ingressos ativos podem ser marcados como utilizados");
        }

        ingresso.setStatus(StatusIngresso.UTILIZADO);
        return ingressoRepository.save(ingresso);
    }
}
