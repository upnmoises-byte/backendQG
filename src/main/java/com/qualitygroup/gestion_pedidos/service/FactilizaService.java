package com.qualitygroup.gestion_pedidos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qualitygroup.gestion_pedidos.dto.FactilizaDocumentoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class FactilizaService {

    private static final Logger log = LoggerFactory.getLogger(FactilizaService.class);

    private final RestClient restClient;
    private final String token;

    public FactilizaService(
            RestClient.Builder restClientBuilder,
            @Value("${factiliza.base-url:https://api.factiliza.com}") String baseUrl,
            @Value("${factiliza.token:}") String token
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.token = token;
    }

    public FactilizaDocumentoResponse consultarDocumento(String numeroRaw) {
        String numero = numeroRaw != null ? numeroRaw.trim() : "";
        if (!numero.matches("\\d+")) {
            throw new IllegalArgumentException("El documento solo debe contener números.");
        }
        if (numero.length() == 8) {
            return consultarDni(numero);
        }
        if (numero.length() == 11) {
            return consultarRuc(numero);
        }
        throw new IllegalArgumentException("Documento inválido. Ingrese DNI de 8 dígitos o RUC de 11 dígitos.");
    }

    private FactilizaDocumentoResponse consultarDni(String dni) {
        log.info("Consultando DNI en Factiliza: {}", dni);
        JsonNode data;
        try {
            data = consultarFactiliza("/dni/info/{numero}", dni);
        } catch (FactilizaUnavailableException ex) {
            return errorFactiliza("DNI");
        }
        if (data == null || data.isMissingNode() || data.isNull()) {
            log.info("DNI no encontrado en Factiliza: {}", dni);
            return noEncontrado("DNI");
        }

        String nombres = text(data, "nombres");
        String apellidoPaterno = text(data, "apellido_paterno", "apellidoPaterno");
        String apellidoMaterno = text(data, "apellido_materno", "apellidoMaterno");
        String nombreCompleto = firstNonBlank(
                text(data, "nombre_completo", "nombreCompleto", "nombre"),
                join(" ", apellidoPaterno, apellidoMaterno, nombres)
        );

        return FactilizaDocumentoResponse.builder()
                .success(true)
                .mensaje("Datos encontrados")
                .tipoDocumento("DNI")
                .numeroDocumento(firstNonBlank(text(data, "numero", "dni", "numero_documento"), dni))
                .nombres(nombres)
                .apellidoPaterno(apellidoPaterno)
                .apellidoMaterno(apellidoMaterno)
                .nombreCompleto(nombreCompleto)
                .direccion(text(data, "direccion", "direccion_completa"))
                .departamento(text(data, "departamento"))
                .provincia(text(data, "provincia"))
                .distrito(text(data, "distrito"))
                .build();
    }

    private FactilizaDocumentoResponse consultarRuc(String ruc) {
        log.info("Consultando RUC en Factiliza: {}", ruc);
        JsonNode data;
        try {
            data = consultarFactiliza("/ruc/info/{numero}", ruc);
        } catch (FactilizaUnavailableException ex) {
            return errorFactiliza("RUC");
        }
        if (data == null || data.isMissingNode() || data.isNull()) {
            log.info("RUC no encontrado en Factiliza: {}", ruc);
            return noEncontrado("RUC");
        }

        return FactilizaDocumentoResponse.builder()
                .success(true)
                .mensaje("Datos encontrados")
                .tipoDocumento("RUC")
                .ruc(firstNonBlank(text(data, "numero", "ruc"), ruc))
                .razonSocial(text(data, "nombre_o_razon_social", "razon_social", "razonSocial", "nombre"))
                .direccion(text(data, "direccion", "direccion_completa"))
                .departamento(text(data, "departamento"))
                .provincia(text(data, "provincia"))
                .distrito(text(data, "distrito"))
                .estado(text(data, "estado"))
                .condicion(text(data, "condicion"))
                .build();
    }

    private JsonNode consultarFactiliza(String path, String numero) {
        if (token == null || token.isBlank()) {
            log.warn("FACTILIZA_TOKEN no está configurado; no se consultará el documento {}", numero);
            throw new FactilizaUnavailableException();
        }
        try {
            JsonNode body = restClient.get()
                    .uri(path, numero)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.is5xxServerError()
                                    || status.value() == 401
                                    || status.value() == 403
                                    || status.value() == 429,
                            (request, response) -> {
                        log.warn("Factiliza respondió HTTP {} para documento {}", response.getStatusCode(), numero);
                        throw new FactilizaUnavailableException();
                    })
                    .body(JsonNode.class);

            if (body == null || !body.path("success").asBoolean(false)) {
                log.info("Factiliza no devolvió datos exitosos para documento {}", numero);
                return null;
            }
            return body.path("data");
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound ex) {
            log.info("Factiliza no encontró el documento {}", numero);
            return null;
        } catch (RestClientException ex) {
            log.warn("No fue posible consultar Factiliza para documento {}: {}", numero, ex.toString());
            throw new FactilizaUnavailableException();
        }
    }

    private static FactilizaDocumentoResponse noEncontrado(String tipo) {
        return FactilizaDocumentoResponse.builder()
                .success(false)
                .mensaje("Documento no encontrado")
                .tipoDocumento(tipo)
                .build();
    }

    private static FactilizaDocumentoResponse errorFactiliza(String tipo) {
        return FactilizaDocumentoResponse.builder()
                .success(false)
                .mensaje("No fue posible consultar Factiliza")
                .tipoDocumento(tipo)
                .build();
    }

    private static class FactilizaUnavailableException extends RuntimeException {
    }

    private static String text(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
                return value.asText().trim().toUpperCase();
            }
        }
        return "";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim().toUpperCase();
            }
        }
        return "";
    }

    private static String join(String separator, String... values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(value.trim());
        }
        return sb.toString().toUpperCase();
    }
}
