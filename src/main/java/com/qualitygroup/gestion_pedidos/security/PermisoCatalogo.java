package com.qualitygroup.gestion_pedidos.security;

import com.qualitygroup.gestion_pedidos.model.Permiso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catálogo de permisos granulares y asignación por defecto por rol.
 */
public final class PermisoCatalogo {

    public static final String ROL_ADMIN = "ADMIN";

    private static final List<Permiso> CATALOGO = List.of(
            p("DASHBOARD_VER", "Dashboard", "Ver panel principal"),

            p("PEDIDOS_VER", "Pedidos", "Ver listado de pedidos"),
            p("PEDIDOS_CREAR", "Pedidos", "Crear pedidos"),
            p("PEDIDOS_EDITAR", "Pedidos", "Editar pedidos"),
            p("PEDIDOS_ELIMINAR", "Pedidos", "Eliminar pedidos"),
            p("PEDIDOS_CAMBIAR_ESTADO", "Pedidos", "Cambiar estado de pedidos"),
            p("PEDIDOS_VER_HISTORIAL", "Pedidos", "Ver historial de pedidos"),

            p("CLIENTES_VER", "Clientes", "Ver clientes"),
            p("CLIENTES_CREAR", "Clientes", "Crear clientes"),
            p("CLIENTES_EDITAR", "Clientes", "Editar clientes"),
            p("CLIENTES_ELIMINAR", "Clientes", "Eliminar clientes"),
            p("CLIENTES_REACTIVAR", "Clientes", "Reactivar clientes inactivos"),

            p("PAGOS_VER", "Pagos", "Ver pagos"),
            p("PAGOS_REGISTRAR", "Pagos", "Registrar pagos"),
            p("PAGOS_VER_HISTORIAL", "Pagos", "Ver historial de pagos"),
            p("PAGOS_ANULAR", "Pagos", "Anular pagos"),

            p("CORTE_VER", "Producción", "Ver módulo corte"),
            p("CANTO_VER", "Producción", "Ver módulo canto"),
            p("ESPECIALES_VER", "Producción", "Ver módulo especiales"),
            p("DESPACHO_VER", "Producción", "Ver despacho"),
            p("ENTREGADOS_VER", "Producción", "Ver entregados"),

            p("REGISTROS_VER", "Registros", "Ver catálogos de registros"),
            p("MATERIALES_CREAR", "Registros", "Crear materiales"),
            p("MATERIALES_EDITAR", "Registros", "Editar materiales"),
            p("MATERIALES_ELIMINAR", "Registros", "Eliminar materiales"),
            p("ESPECIALES_CREAR", "Registros", "Crear especiales de catálogo"),
            p("ESPECIALES_EDITAR", "Registros", "Editar especiales de catálogo"),
            p("ESPECIALES_ELIMINAR", "Registros", "Eliminar especiales de catálogo"),

            p("REPORTES_VER", "Reportes", "Ver reportes"),
            p("REPORTES_EXPORTAR_PDF", "Reportes", "Exportar reportes PDF"),
            p("REPORTES_EXPORTAR_EXCEL", "Reportes", "Exportar reportes Excel"),

            p("CONFIG_VER", "Configuración", "Ver configuración"),
            p("CONFIG_EDITAR", "Configuración", "Editar configuración"),

            p("ROLES_VER", "Roles", "Ver roles y permisos"),
            p("ROLES_CREAR", "Roles", "Crear roles"),
            p("ROLES_EDITAR", "Roles", "Editar roles"),
            p("ROLES_ELIMINAR", "Roles", "Eliminar roles"),
            p("ROLES_ASIGNAR_PERMISOS", "Roles", "Asignar permisos a roles")
    );

    private static final Set<String> ADMIN_CRITICOS = Set.of(
            "ROLES_VER",
            "ROLES_EDITAR",
            "ROLES_ASIGNAR_PERMISOS"
    );

    private PermisoCatalogo() {
    }

    public static List<Permiso> todos() {
        return CATALOGO;
    }

    public static Set<String> todosCodigos() {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (Permiso perm : CATALOGO) {
            out.add(perm.getCodigo());
        }
        return out;
    }

    public static Set<String> criticosAdmin() {
        return ADMIN_CRITICOS;
    }

    public static Map<String, Set<String>> permisosPorDefecto() {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        map.put(ROL_ADMIN, todosCodigos());

        map.put("GERENCIA", set(
                "DASHBOARD_VER",
                "PEDIDOS_VER", "PEDIDOS_CREAR", "PEDIDOS_EDITAR", "PEDIDOS_CAMBIAR_ESTADO", "PEDIDOS_VER_HISTORIAL",
                "CLIENTES_VER", "CLIENTES_CREAR", "CLIENTES_EDITAR",
                "PAGOS_VER", "PAGOS_VER_HISTORIAL",
                "CORTE_VER", "CANTO_VER", "ESPECIALES_VER", "DESPACHO_VER", "ENTREGADOS_VER",
                "REGISTROS_VER",
                "REPORTES_VER", "REPORTES_EXPORTAR_PDF", "REPORTES_EXPORTAR_EXCEL"
        ));

        map.put("PRODUCCION", set(
                "PEDIDOS_VER", "PEDIDOS_CAMBIAR_ESTADO", "PEDIDOS_VER_HISTORIAL",
                "CORTE_VER", "CANTO_VER", "ESPECIALES_VER", "DESPACHO_VER", "ENTREGADOS_VER"
        ));

        map.put("CAJA", set(
                "PEDIDOS_VER",
                "CLIENTES_VER",
                "PAGOS_VER", "PAGOS_REGISTRAR", "PAGOS_VER_HISTORIAL"
        ));

        map.put("VENDEDORA", set(
                "DASHBOARD_VER",
                "PEDIDOS_VER", "PEDIDOS_CREAR", "PEDIDOS_EDITAR",
                "CLIENTES_VER", "CLIENTES_CREAR"
        ));

        return map;
    }

    private static Permiso p(String codigo, String modulo, String descripcion) {
        return new Permiso(codigo, modulo, descripcion);
    }

    private static Set<String> set(String... codes) {
        return Set.of(codes);
    }
}
