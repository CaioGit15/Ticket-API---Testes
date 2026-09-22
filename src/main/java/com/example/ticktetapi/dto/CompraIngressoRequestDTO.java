package com.example.ticktetapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompraIngressoRequestDTO {

    @NotNull(message = "O id do evento é obrigatório")
    private Long eventoId;

    @NotNull(message = "O id do cliente é obrigatório")
    private Long clienteId;

    @Min(value = 1, message = "A quantidade deve ser de pelo menos 1")
    private Integer quantidade = 1;
}
