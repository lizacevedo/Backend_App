package Ucentral.Arquitecturall_monitoreo.servicio.usuarios;

import Ucentral.Arquitecturall_monitoreo.dto.RespuestaAuth;
import Ucentral.Arquitecturall_monitoreo.dto.usuarios.ActualizarContraseñaDTO;
import Ucentral.Arquitecturall_monitoreo.dto.usuarios.LoginDTO;
import Ucentral.Arquitecturall_monitoreo.dto.usuarios.RegistroDTO;
import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import Ucentral.Arquitecturall_monitoreo.repositorio.usuarios.UsuarioRepositorio;
import Ucentral.Arquitecturall_monitoreo.seguridad.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 🔹 NUEVO: lista fija de chat IDs de Telegram
    // Aquí vas a poner TUS 3 chat_id reales cuando los tengas
    // Ejemplo cuando los tengas:
    // private static final Long[] TELEGRAM_CHAT_IDS = { 123456789L, 987654321L, 1122334455L };
    private static final Long[] TELEGRAM_CHAT_IDS = {
            5372625089L,  // primer usuario nuevo → aquí iría el primer chat_id
            6104199237L,  // segundo usuario nuevo → segundo chat_id
            5400446714L   // tercer usuario nuevo → tercer chat_id
    };

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, JwtService jwtService) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.jwtService = jwtService;
    }

    public RespuestaAuth registrar(RegistroDTO dto) {
        if (usuarioRepositorio.findByCorreo(dto.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        // 🔹 NUEVO: calcular (si aplica) qué chatId le tocaría a este usuario
        Long telegramChatId = asignarTelegramChatIdSecuencial();

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo().toLowerCase().trim())
                .contraseña(passwordEncoder.encode(dto.getContraseña()))
                .registradoConGoogle(false)
                .rol(Usuario.Rol.USER)
                // 🔹 NUEVO: asignar el chatId (puede ser null si aún no llenas el arreglo)
                .telegramChatId(telegramChatId)
                .build();

        usuarioRepositorio.save(usuario);

        String token = jwtService.generarToken(
                usuario.getCorreo(),
                Map.of("nombre", usuario.getNombre(), "rol", usuario.getRol().name())
        );

        return new RespuestaAuth(
                "Registro exitoso",
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                "LOCAL"
        );
    }

    // 🔹 NUEVO: lógica para asignar el siguiente telegramChatId en orden
    private Long asignarTelegramChatIdSecuencial() {
        // Si aún no has configurado la lista, no hacemos nada
        if (TELEGRAM_CHAT_IDS == null || TELEGRAM_CHAT_IDS.length == 0) {
            return null;
        }

        // Si todos son null (no has puesto tus IDs), también devolvemos null
        boolean todosNull = true;
        for (Long id : TELEGRAM_CHAT_IDS) {
            if (id != null) {
                todosNull = false;
                break;
            }
        }
        if (todosNull) {
            return null;
        }

        // Contar cuántos usuarios hay en total
        // (incluye antiguos y nuevos, pero es suficiente para repartir en orden)
        long totalUsuarios = usuarioRepositorio.count();

        // Elegir el índice según el total de usuarios (0,1,2,0,1,2,...)
        int index = (int) (totalUsuarios % TELEGRAM_CHAT_IDS.length);

        return TELEGRAM_CHAT_IDS[index];
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void actualizarContraseña(ActualizarContraseñaDTO dto) {
        Usuario usuario = usuarioRepositorio.findByCorreo(dto.getCorreo().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("No existe una cuenta con este correo."));

        // Validar que la nueva contraseña no esté vacía
        if (dto.getNuevaContraseña() == null || dto.getNuevaContraseña().trim().isEmpty()) {
            throw new RuntimeException("La nueva contraseña no puede estar vacía.");
        }

        // Actualizar contraseña
        usuario.setContraseña(passwordEncoder.encode(dto.getNuevaContraseña().trim()));
        usuarioRepositorio.save(usuario);

        System.out.println("✅ Contraseña actualizada para: " + dto.getCorreo());
    }

    public RespuestaAuth iniciarSesion(LoginDTO dto) {
        Usuario usuario = usuarioRepositorio.findByCorreo(dto.getCorreo().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!passwordEncoder.matches(dto.getContraseña(), usuario.getContraseña())) {
            throw new RuntimeException("Contraseña incorrecta.");
        }

        String token = jwtService.generarToken(
                usuario.getCorreo(),
                Map.of("nombre", usuario.getNombre(), "rol", usuario.getRol().name())
        );

        return new RespuestaAuth(
                "Inicio de sesión exitoso",
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                usuario.isRegistradoConGoogle() ? "GOOGLE" : "LOCAL"
        );
    }
}
