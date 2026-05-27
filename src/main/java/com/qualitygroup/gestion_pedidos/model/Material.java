package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "materiales")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String nombre;

    @Column(length = 64)
    private String marca;

    @Column(length = 128)
    private String color;

    @Column(nullable = false, length = 16)
    private String tipo = "NORMAL";

    @Column(length = 32)
    private String espesor;

    @Column(length = 64)
    private String medida;

    @Column(nullable = false)
    private Boolean activo = true;
}
