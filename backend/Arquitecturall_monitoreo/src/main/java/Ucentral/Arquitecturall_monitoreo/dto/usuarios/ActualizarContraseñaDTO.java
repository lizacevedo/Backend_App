package Ucentral.Arquitecturall_monitoreo.dto.usuarios;

import lombok.Data;

@Data
public class ActualizarContraseñaDTO {
    private String correo;
    private String nuevaContraseña;
}