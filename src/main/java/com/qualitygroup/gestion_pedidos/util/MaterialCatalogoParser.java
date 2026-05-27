package com.qualitygroup.gestion_pedidos.util;

import com.qualitygroup.gestion_pedidos.model.Material;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialCatalogoParser {

    private static final Pattern LINEA = Pattern.compile(
            "^MELAMINA\\s+(PELIKANO|HISPANOS)\\s+(.+?)\\s+(\\d+MM)\\s+(.+)$",
            Pattern.CASE_INSENSITIVE
    );

    private MaterialCatalogoParser() {
    }

    public static Material fromLinea(String linea) {
        String nombre = linea == null ? "" : linea.trim().toUpperCase(Locale.ROOT);
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Nombre de material vacío");
        }

        Material material = new Material();
        material.setNombre(nombre);
        material.setActivo(true);

        Matcher matcher = LINEA.matcher(nombre);
        if (matcher.matches()) {
            String marca = matcher.group(1).toUpperCase(Locale.ROOT);
            String resto = matcher.group(2).trim();
            String espesor = matcher.group(3).toUpperCase(Locale.ROOT);
            String medida = normalizarMedida(matcher.group(4));

            boolean esRh = resto.endsWith(" RH");
            String tipo = esRh ? "RH" : "NORMAL";
            String color = esRh ? resto.substring(0, resto.length() - 3).trim() : resto;

            material.setMarca(marca);
            material.setColor(color);
            material.setTipo(tipo);
            material.setEspesor(espesor);
            material.setMedida(medida);
        } else {
            material.setMarca(detectarMarca(nombre));
            material.setTipo(nombre.contains(" RH ") || nombre.endsWith(" RH") ? "RH" : "NORMAL");
            material.setEspesor("18MM");
            material.setMedida("");
            material.setColor("");
        }

        return material;
    }

    private static String normalizarMedida(String medida) {
        return medida.trim()
                .replace(" X ", " x ")
                .replace("X", "x");
    }

    private static String detectarMarca(String nombre) {
        String upper = nombre.toUpperCase(Locale.ROOT);
        if (upper.contains(" HISPANOS ")) {
            return "HISPANOS";
        }
        if (upper.contains(" PELIKANO ")) {
            return "PELIKANO";
        }
        return "";
    }
}
