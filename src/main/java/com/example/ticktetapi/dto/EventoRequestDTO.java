package com.example.ticktetapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventoRequestDTO {

    @NotBlank(message = "O nome do evento é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "A data e hora do evento são obrigatórias")
    @Future(message = "A data do evento deve estar no futuro")
    private LocalDateTime dataHora;

    @NotBlank(message = "O local do evento é obrigatório")
    private String local;

    @NotNull(message = "O preço do ingresso é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "A quantidade total de ingressos é obrigatória")
    @Min(value = 1, message = "A quantidade total deve ser de pelo menos 1")
    private Integer quantidadeTotal;
}

