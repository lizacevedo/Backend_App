package Ucentral.Arquitecturall_monitoreo.entidad.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    public enum Rol {
        USER,
        ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String correo;

    private String contraseña;

    private String fotoPerfil;

    private boolean registradoConGoogle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Rol rol = Rol.USER; // valor por defecto

    // ============================
    // NUEVO CAMPO PARA TELEGRAM
    // ============================
    @Column(name = "telegram_chat_id")
    private Long telegramChatId;
}
