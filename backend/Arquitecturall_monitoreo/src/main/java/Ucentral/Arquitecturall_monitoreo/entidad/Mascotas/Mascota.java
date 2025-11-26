package Ucentral.Arquitecturall_monitoreo.entidad.Mascotas;


import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String especie;
    private String raza;
    private Integer edad;
    private Double peso;
    private String fotoUrl;

    // NUEVO CAMPO: Asignación de video
    @Column(name = "video_asignado")
    private String videoAsignado; // "video1", "video2", "video3"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}