package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "pedido_detalles")
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal cantidad;
    private String material;
    private String maquina;

    private Integer cortes;
    private Integer ranuras;
    private Integer perforaciones;

    private BigDecimal cantoDelgado;
    private BigDecimal cantoGrueso;
    private BigDecimal cantoDelgado36mm;
    private BigDecimal cantoGrueso36mm;

    private String observaciones;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @OneToMany(mappedBy = "detalle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalleEspecial> especiales;
}