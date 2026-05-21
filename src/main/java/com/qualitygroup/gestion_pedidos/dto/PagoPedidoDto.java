package com.qualitygroup.gestion_pedidos.dto;

import com.qualitygroup.gestion_pedidos.model.PagoPedido;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Value
@Builder
public class PagoPedidoDto {

    Long id;
    Long pedidoId;
    BigDecimal monto;
    String metodoPago;
    String codigoPago;
    String nota;
    LocalDate fechaRegistro;
    LocalTime horaRegistro;
    String usuarioNombre;
    String usuarioCorreo;
    String usuarioRol;

    public static PagoPedidoDto fromEntity(PagoPedido p) {
        Long pid = p.getPedido() != null ? p.getPedido().getId() : null;
        return PagoPedidoDto.builder()
                .id(p.getId())
                .pedidoId(pid)
                .monto(p.getMonto())
                .metodoPago(p.getMetodoPago())
                .codigoPago(p.getCodigoPago())
                .nota(p.getNota())
                .fechaRegistro(p.getFechaRegistro())
                .horaRegistro(p.getHoraRegistro())
                .usuarioNombre(p.getUsuarioNombre())
                .usuarioCorreo(p.getUsuarioCorreo())
                .usuarioRol(p.getUsuarioRol())
                .build();
    }
}
