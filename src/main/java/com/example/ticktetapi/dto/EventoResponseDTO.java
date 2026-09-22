package com.example.ticktetapi.dto;

import com.example.ticktetapi.model.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private LocalDateTime dataHora;
    private String local;
    private BigDecimal preco;
    private Integer quantidadeTotal;
    private Integer quantidadeDisponivel;

    public static EventoResponseDTO fromEntity(Evento evento) {
        return EventoResponseDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .descricao(evento.getDescricao())
                .dataHora(evento.getDataHora())
                .local(evento.getLocal())
                .preco(evento.getPreco())
                .quantidadeTotal(evento.getQuantidadeTotal())
                .quantidadeDisponivel(evento.getQuantidadeDisponivel())
                .build();
    }
}
