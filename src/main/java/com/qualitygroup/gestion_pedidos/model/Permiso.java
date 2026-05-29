package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
public class Permiso {

    @Id
    @Column(length = 80)
    private String codigo;

    @Column(nullable = false, length = 40)
    private String modulo;

    @Column(nullable = false, length = 200)
    private String descripcion;

    public Permiso(String codigo, String modulo, String descripcion) {
        this.codigo = codigo;
        this.modulo = modulo;
        this.descripcion = descripcion;
    }
}
