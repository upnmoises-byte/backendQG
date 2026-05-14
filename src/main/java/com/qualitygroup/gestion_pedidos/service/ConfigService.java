package com.qualitygroup.gestion_pedidos.service;

import com.qualitygroup.gestion_pedidos.model.ConfigEntry;
import com.qualitygroup.gestion_pedidos.repository.ConfigEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("empresa.nombre", "Q & G Quality Group"),
            Map.entry("empresa.ruc", ""),
            Map.entry("empresa.direccion", ""),
            Map.entry("empresa.telefono", ""),
            Map.entry("empresa.correo", ""),
            Map.entry("moneda.simbolo", "S/"),
            Map.entry("documento.pie", "Gracias por su preferencia."),
            Map.entry("sistema.zonaHoraria", "America/Lima"),
            Map.entry("notificaciones.correoOrigen", ""),
            Map.entry("sesion.minutosInactividad", "120")
    );

    private final ConfigEntryRepository configEntryRepository;

    public ConfigService(ConfigEntryRepository configEntryRepository) {
        this.configEntryRepository = configEntryRepository;
    }

    public Map<String, String> obtenerMapa() {
        Map<String, String> db = configEntryRepository.findAll().stream()
                .collect(Collectors.toMap(ConfigEntry::getClave, e -> e.getValor() != null ? e.getValor() : ""));
        Map<String, String> out = new LinkedHashMap<>();
        for (String clave : DEFAULTS.keySet()) {
            out.put(clave, db.getOrDefault(clave, DEFAULTS.get(clave)));
        }
        for (Map.Entry<String, String> e : db.entrySet()) {
            if (!out.containsKey(e.getKey())) {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    @Transactional
    public void guardarMapa(Map<String, String> valores) {
        if (valores == null) {
            return;
        }
        for (Map.Entry<String, String> e : valores.entrySet()) {
            if (e.getKey() == null || e.getKey().isBlank()) {
                continue;
            }
            ConfigEntry ce = configEntryRepository.findById(e.getKey().trim()).orElse(new ConfigEntry());
            ce.setClave(e.getKey().trim());
            ce.setValor(e.getValue() != null ? e.getValue() : "");
            configEntryRepository.save(ce);
        }
    }
}
