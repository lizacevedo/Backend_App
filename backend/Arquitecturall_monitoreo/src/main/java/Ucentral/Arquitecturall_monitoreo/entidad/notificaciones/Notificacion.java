package Ucentral.Arquitecturall_monitoreo.entidad.notificaciones;



import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    private String id; // Mongo genera un ObjectId automáticamente

    private Long usuarioId;       // Usuario dueño de la mascota
    private List<String> mascotas; // Lista por si fueron varias mascotas detectadas

    private String tipo;         // ej: "PLANTA", "COMIDA", "SOFA", "ZAPATOS"
    private String mensaje;      // "Tu mascota Nala está haciendo daños en la planta"

    private LocalDateTime fecha; // Fecha de detección
}
