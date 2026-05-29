package com.qualitygroup.gestion_pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "rol_permisos")
@IdClass(RolPermiso.RolPermisoId.class)
@Getter
@Setter
@NoArgsConstructor
public class RolPermiso {

    @Id
    @Column(name = "rol_nombre", length = 50)
    private String rolNombre;

    @Id
    @Column(name = "permiso_codigo", length = 80)
    private String permisoCodigo;

    public RolPermiso(String rolNombre, String permisoCodigo) {
        this.rolNombre = rolNombre;
        this.permisoCodigo = permisoCodigo;
    }

    public static class RolPermisoId implements Serializable {
        private String rolNombre;
        private String permisoCodigo;

        public RolPermisoId() {
        }

        public RolPermisoId(String rolNombre, String permisoCodigo) {
            this.rolNombre = rolNombre;
            this.permisoCodigo = permisoCodigo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RolPermisoId that = (RolPermisoId) o;
            return Objects.equals(rolNombre, that.rolNombre)
                    && Objects.equals(permisoCodigo, that.permisoCodigo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rolNombre, permisoCodigo);
        }
    }
}
