package Ucentral.Arquitecturall_monitoreo.controlador.usuarios;


import Ucentral.Arquitecturall_monitoreo.dto.RespuestaAuth;
import Ucentral.Arquitecturall_monitoreo.dto.usuarios.ActualizarContraseñaDTO;
import Ucentral.Arquitecturall_monitoreo.dto.usuarios.LoginDTO;
import Ucentral.Arquitecturall_monitoreo.dto.usuarios.RegistroDTO;
import Ucentral.Arquitecturall_monitoreo.servicio.usuarios.UsuarioServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @PostMapping("/registrar")
    public RespuestaAuth registrar(@RequestBody RegistroDTO dto) {
        return usuarioServicio.registrar(dto);
    }

    @PostMapping("/login")
    public RespuestaAuth iniciarSesion(@RequestBody LoginDTO dto) {
        return usuarioServicio.iniciarSesion(dto);
    }
    @PostMapping("/actualizar-contraseña")
    public ResponseEntity<Map<String, String>> actualizarContraseña(@RequestBody ActualizarContraseñaDTO dto) {
        try {
            usuarioServicio.actualizarContraseña(dto);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Contraseña actualizada exitosamente",
                    "estado", "exito"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "estado", "error"
            ));
        }
    }
    @GetMapping("/me")
    public Map<String, Object> obtenerPerfil(org.springframework.security.core.Authentication auth) {
        return Map.of(
                "mensaje", "Acceso autorizado",
                "usuario", auth != null ? auth.getName() : "anónimo"
        );
    }
}
