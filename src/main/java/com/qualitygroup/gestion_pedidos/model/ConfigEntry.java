package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "app_config")
public class ConfigEntry {

    @Id
    @Column(length = 120)
    private String clave;

    @Column(columnDefinition = "TEXT")
    private String valor;
}
