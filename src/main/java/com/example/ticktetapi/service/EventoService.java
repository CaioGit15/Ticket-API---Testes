package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.EventoRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional
    public Evento criar(EventoRequestDTO dto) {
        Evento evento = Evento.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .dataHora(dto.getDataHora())
                .local(dto.getLocal())
                .preco(dto.getPreco())
                .quantidadeTotal(dto.getQuantidadeTotal())
                .quantidadeDisponivel(dto.getQuantidadeTotal())
                .build();
        return eventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com id: " + id));
    }

    @Transactional
    public Evento atualizar(Long id, EventoRequestDTO dto) {
        Evento evento = buscarPorId(id);

        int ingressosVendidos = evento.getQuantidadeTotal() - evento.getQuantidadeDisponivel();
        if (dto.getQuantidadeTotal() < ingressosVendidos) {
            throw new RegraNegocioException(
                    "A nova quantidade total não pode ser menor que a quantidade já vendida (" + ingressosVendidos + ")");
        }

        evento.setNome(dto.getNome());
        evento.setDescricao(dto.getDescricao());
        evento.setDataHora(dto.getDataHora());
        evento.setLocal(dto.getLocal());
        evento.setPreco(dto.getPreco());
        evento.setQuantidadeDisponivel(dto.getQuantidadeTotal() - ingressosVendidos);
        evento.setQuantidadeTotal(dto.getQuantidadeTotal());

        return eventoRepository.save(evento);
    }

    @Transactional
    public void deletar(Long id) {
        Evento evento = buscarPorId(id);
        eventoRepository.delete(evento);
    }
}
