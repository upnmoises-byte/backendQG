package com.qualitygroup.gestion_pedidos.repository;

import com.qualitygroup.gestion_pedidos.model.ConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigEntryRepository extends JpaRepository<ConfigEntry, String> {
}
