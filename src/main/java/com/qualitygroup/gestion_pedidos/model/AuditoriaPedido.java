package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "auditoria_pedidos")
public class AuditoriaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;

    private String numeroOrden;

    private String estadoAnterior;

    private String estadoNuevo;

    private String usuarioNombre;

    private String usuarioCorreo;

    private String usuarioRol;

    private LocalDate fechaCambio;

    private LocalTime horaCambio;
}