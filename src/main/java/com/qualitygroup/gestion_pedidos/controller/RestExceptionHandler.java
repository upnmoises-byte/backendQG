package com.qualitygroup.gestion_pedidos.controller;

import com.qualitygroup.gestion_pedidos.exception.CorrelativoPedidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "FORBIDDEN",
                "mensaje", "No tiene permiso para realizar esta acción."
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> notReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "BAD_REQUEST",
                "mensaje", "Cuerpo de la petición inválido. Revise montos y campos obligatorios."
        ));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> security(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "FORBIDDEN",
                "mensaje", ex.getMessage() != null ? ex.getMessage() : "Acceso denegado"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "BAD_REQUEST",
                "mensaje", ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida"
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException ex) {
        String detalle = ex.getMostSpecificCause() != null
                ? String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase()
                : "";
        String mensaje = detalle.contains("documento") || detalle.contains("clientes")
                ? "Ya existe un cliente registrado con ese documento"
                : "No se pudo eliminar el pedido porque tiene registros relacionados.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "CONFLICT",
                "mensaje", mensaje
        ));
    }

    @ExceptionHandler(CorrelativoPedidoException.class)
    public ResponseEntity<Map<String, Object>> correlativoConflict(CorrelativoPedidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "CONFLICT",
                "mensaje", ex.getMessage() != null ? ex.getMessage() : "Conflicto de correlativo en N° de orden."
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> runtime(RuntimeException ex) {
        log.warn("Error de ejecución: {}", ex.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "mensaje", ex.getMessage() != null && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "Error interno. Intente de nuevo o contacte a soporte."
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "mensaje", ex.getMessage() != null && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "Error interno no controlado. Revise los logs del backend."
        ));
    }
}
