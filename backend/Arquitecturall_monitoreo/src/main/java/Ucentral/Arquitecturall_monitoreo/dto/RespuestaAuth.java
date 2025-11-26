package Ucentral.Arquitecturall_monitoreo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RespuestaAuth {
    private String mensaje;
    private String token;     // JWT aquí
    private Long id;
    private String nombre;
    private String correo;
    private String rol;       // "USER" | "ADMIN"
    private String provider;  // "LOCAL" | "GOOGLE"
}