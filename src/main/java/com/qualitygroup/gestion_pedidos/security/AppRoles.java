package com.qualitygroup.gestion_pedidos.security;

/**
 * Roles vigentes del sistema. Usar en {@code @PreAuthorize} y migraciones.
 */
public final class AppRoles {

    public static final String ADMIN = "ADMIN";
    public static final String GERENCIA = "GERENCIA";
    public static final String PRODUCCION = "PRODUCCION";
    public static final String CAJA = "CAJA";
    public static final String VENDEDORA = "VENDEDORA";

    /** SpEL para endpoints operativos estándar (pedidos, clientes, catálogos lectura, etc.). */
    public static final String HAS_ANY_APP_ROLE =
            "hasAnyRole('ADMIN','GERENCIA','PRODUCCION','CAJA','VENDEDORA')";

    private AppRoles() {
    }
}
