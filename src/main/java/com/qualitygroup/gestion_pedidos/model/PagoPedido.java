package com.qualitygroup.gestion_pedidos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "pago_pedido")
public class PagoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    /** BCP, YAPE, BBVA, EFECTIVO, VISA */
    @Column(nullable = false, length = 32)
    private String metodoPago;

    @Column(columnDefinition = "TEXT")
    private String nota;

    private LocalDate fechaRegistro;
    private LocalTime horaRegistro;

    private String usuarioNombre;
    private String usuarioCorreo;
    private String usuarioRol;
}
