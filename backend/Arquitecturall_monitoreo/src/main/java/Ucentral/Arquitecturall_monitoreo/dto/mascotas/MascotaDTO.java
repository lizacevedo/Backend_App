package Ucentral.Arquitecturall_monitoreo.dto.mascotas;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private Integer edad;
    private Double peso;
    private String fotoUrl;
    private String videoAsignado;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
}