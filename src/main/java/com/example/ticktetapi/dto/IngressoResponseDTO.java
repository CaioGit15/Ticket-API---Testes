package com.example.ticktetapi.dto;

import com.example.ticktetapi.model.Ingresso;
import com.example.ticktetapi.model.StatusIngresso;
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
public class IngressoResponseDTO {

    private Long id;
    private String codigo;
    private Long eventoId;
    private String eventoNome;
    private Long clienteId;
    private String clienteNome;
    private LocalDateTime dataCompra;
    private BigDecimal valorPago;
    private StatusIngresso status;

    public static IngressoResponseDTO fromEntity(Ingresso ingresso) {
        return IngressoResponseDTO.builder()
                .id(ingresso.getId())
                .codigo(ingresso.getCodigo())
                .eventoId(ingresso.getEvento().getId())
                .eventoNome(ingresso.getEvento().getNome())
                .clienteId(ingresso.getCliente().getId())
                .clienteNome(ingresso.getCliente().getNome())
                .dataCompra(ingresso.getDataCompra())
                .valorPago(ingresso.getValorPago())
                .status(ingresso.getStatus())
                .build();
    }
}
