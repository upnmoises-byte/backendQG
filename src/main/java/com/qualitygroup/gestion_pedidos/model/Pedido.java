package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.FetchType;

@Data
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String numeroOrden;
    private BigDecimal cantidad;
    private String colorPrincipal;
    private String colorSecundario;
    private String colorTercero;

    private Integer cortes;
    private Integer ranuras;
    private Integer perforaciones;

    private String maquina;
    private String estado;
    private String vendedora;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private Integer cantoDelgado;
    private Integer cantoGrueso;
    private Integer cantoDelgado36mm;
    private Integer cantoGrueso36mm;
    private Integer cantidadEspeciales;

    private LocalDate fechaIngreso;
    private LocalTime horaIngreso;

    private LocalDate fechaEntrega;
    private LocalTime horaEntrega;

    private Integer prioridad;

    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal adelanto = BigDecimal.ZERO;

    @OneToMany(
    mappedBy = "pedido",
    cascade = CascadeType.ALL,
    fetch = FetchType.EAGER
    )
    private List<PedidoDetalle> detalles;
}

