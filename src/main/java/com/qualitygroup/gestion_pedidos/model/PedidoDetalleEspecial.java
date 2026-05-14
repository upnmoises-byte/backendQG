package com.qualitygroup.gestion_pedidos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pedido_detalle_especiales")
public class PedidoDetalleEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cantidad;

    private String descripcion;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "detalle_id")
    private PedidoDetalle detalle;
}