package com.qualitygroup.gestion_pedidos.util;

import java.util.Locale;
import java.util.Optional;

public final class VendedoraUtil {

    private VendedoraUtil() {
    }

    /**
     * Infiere código de vendedora (DIANA, ANABEL, etc.) desde el nombre del usuario.
     */
    public static Optional<String> inferirCodigoDesdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        String n = nombre.toUpperCase(Locale.ROOT);
        if (n.contains("ISAMAR")) {
            return Optional.of("ISAMAR");
        }
        if (n.contains("ANABEL")) {
            return Optional.of("ANABEL");
        }
        if (n.contains("DIANA")) {
            return Optional.of("DIANA");
        }
        if (n.contains("MELISSA")) {
            return Optional.of("MELISSA");
        }
        return Optional.empty();
    }
}
