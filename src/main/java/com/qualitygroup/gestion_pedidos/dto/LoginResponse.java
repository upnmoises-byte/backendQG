package com.qualitygroup.gestion_pedidos.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {

    String token;
    UsuarioDto usuario;
}
