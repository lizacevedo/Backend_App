package Ucentral.Arquitecturall_monitoreo.controlador.roboflow;

import Ucentral.Arquitecturall_monitoreo.entidad.Mascotas.Mascota;
import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import Ucentral.Arquitecturall_monitoreo.repositorio.mascotas.MascotaRepositorio;
import Ucentral.Arquitecturall_monitoreo.repositorio.usuarios.UsuarioRepositorio;
import Ucentral.Arquitecturall_monitoreo.servicio.notificaciones.NotificacionServicio;
import Ucentral.Arquitecturall_monitoreo.servicio.notificaciones.TelegramService;
import Ucentral.Arquitecturall_monitoreo.servicio.roboflow.RoboflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/detecciones")
@RequiredArgsConstructor
public class DeteccionController {

    private final RoboflowService roboflowService;
    private final UsuarioRepositorio usuarioRepositorio;
    private final MascotaRepositorio mascotaRepositorio;
    private final NotificacionServicio notificacionServicio;
    private final TelegramService telegramService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(
            value = "/imagen",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> detectarEnImagen(@RequestParam("file") MultipartFile file) {

        System.out.println(">>> LLEGÓ AL ENDPOINT /api/detecciones/imagen <<<");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El archivo está vacío"));
        }

        // 1) Llamamos a Roboflow (igual que antes)
        String resultadoJson = roboflowService.detectarEnImagen(file);

        // 2) Intentamos generar notificación + Telegram (no afecta la respuesta)
        try {
            procesarNotificacion(resultadoJson);
        } catch (Exception e) {
            System.out.println("⚠️ Error procesando notificación: " + e.getMessage());
        }

        // 3) Respondemos al cliente igual que antes
        return ResponseEntity.ok(resultadoJson);
    }

    private void procesarNotificacion(String resultadoJson) throws Exception {
        // A) Usuario autenticado (por JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            System.out.println("⚠️ No hay usuario autenticado, no se registra notificación.");
            return;
        }

        String correo = auth.getName();
        Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                .orElse(null);

        if (usuario == null) {
            System.out.println("⚠️ Usuario no encontrado para correo: " + correo);
            return;
        }

        Long usuarioId = usuario.getId();

        // B) Parsear JSON para extraer clases
        JsonNode root = objectMapper.readTree(resultadoJson);
        JsonNode predictionsNode = root.get("predictions");
        if (predictionsNode == null || !predictionsNode.isArray() || predictionsNode.isEmpty()) {
            System.out.println("ℹ️ Sin predicciones relevantes, no se genera evento.");
            return;
        }

        Set<String> clases = new HashSet<>();
        for (JsonNode pred : predictionsNode) {
            JsonNode classNode = pred.get("class");
            if (classNode != null && !classNode.isNull()) {
                clases.add(classNode.asText().toLowerCase());
            }
        }

        if (clases.isEmpty()) {
            System.out.println("ℹ️ No se encontraron clases en las predicciones.");
            return;
        }

        // C) Sacar nombres de mascotas del usuario
        List<Mascota> mascotasUsuario = mascotaRepositorio.findByUsuarioId(usuarioId);
        List<String> nombresMascotas = mascotasUsuario.stream()
                .map(Mascota::getNombre)
                .filter(Objects::nonNull)
                .filter(n -> !n.isBlank())
                .collect(Collectors.toList());

        // D) Aplicar reglas → tipo evento + mensajes singular/plural
        Evento evento = construirEventoDesdeClases(clases);
        if (evento == null) {
            System.out.println("ℹ️ No hay evento definido para estas clases: " + clases);
            return;
        }

        // E) Construir sujeto con nombres reales (Tu mascota Nala / Tus mascotas Nala y Loki)
        boolean plural = nombresMascotas.size() > 1;
        String sujeto = construirSujetoDesdeNombres(nombresMascotas, plural);

        // Escoger el texto base según sea singular o plural
        String mensajeBase = plural ? evento.mensajePlural() : evento.mensajeSingular();

        String mensajeFinal = sujeto + " " + mensajeBase;

        // F) Guardar en Mongo (con nombres)
        notificacionServicio.registrarNotificacion(
                usuarioId,
                nombresMascotas,
                evento.tipo(),
                mensajeFinal
        );

        // G) Enviar a Telegram
        telegramService.enviarNotificacionAlUsuario(usuarioId, mensajeFinal);
    }

    /**
     * Reglas de negocio:
     * Devuelve tipo de evento + texto base singular/plural.
     * El sujeto ("Tu mascota Nala", "Tus mascotas Nala y Loki") se construye aparte.
     */
    private Evento construirEventoDesdeClases(Set<String> clases) {
        boolean hayCat = clases.contains("cat") || clases.contains("gato");
        boolean hayDog = clases.contains("dog") || clases.contains("perro");
        boolean hayMascota = hayCat || hayDog;

        boolean hayPlant = clases.contains("plant") || clases.contains("planta");
        boolean hayLand = clases.contains("land") || clases.contains("tierra") ||
                clases.contains("soil") || clases.contains("dirt");

        boolean foodEmpty = clases.contains("food_empty") || clases.contains("plato_vacio");
        boolean foodFull = clases.contains("food_full") || clases.contains("plato_lleno");

        boolean haySofa = clases.contains("sofa") || clases.contains("couch");
        boolean hayZapatos = clases.contains("shoe") || clases.contains("shoes") ||
                clases.contains("zapato") || clases.contains("zapatos");

        // 1) Planta + tierra + mascota
        if (hayMascota && hayPlant && hayLand) {
            return new Evento(
                    "PLANTA_DANO",
                    "está haciendo daños a la planta y jugando con la tierra.",
                    "están haciendo daños a la planta y jugando con la tierra."
            );
        }

        // 2) Plato sin comida
        if (foodEmpty && !foodFull) {
            return new Evento(
                    "COMIDA_VACIA",
                    "no tiene comida en su plato.",
                    "no tienen comida en su plato."
            );
        }

        // 3) Plato lleno
        if (foodFull && !foodEmpty) {
            return new Evento(
                    "COMIDA_LLENA",
                    "está bien de comida.",
                    "están bien de comida."
            );
        }

        // 4) Mascota en el sofá
        if (hayMascota && haySofa) {
            return new Evento(
                    "SOFA",
                    "está en el sofá.",
                    "están en el sofá."
            );
        }

        // 5) Mascota con zapatos
        if (hayMascota && hayZapatos) {
            return new Evento(
                    "ZAPATOS",
                    "está mordiendo tus zapatos.",
                    "están mordiendo tus zapatos."
            );
        }

        // 6) Mascota activa
        if (hayMascota) {
            return new Evento(
                    "MASCOTA_ACTIVA",
                    "está activa en la cámara.",
                    "están activas en la cámara."
            );
        }

        return null;
    }


    private String construirSujetoDesdeNombres(List<String> nombres, boolean plural) {
        if (nombres == null || nombres.isEmpty()) {
            // fallback por si no tiene mascotas registradas
            return plural ? "Tus mascotas" : "Tu mascota";
        }

        if (nombres.size() == 1) {
            return nombres.get(0);
        }


        String listaNombres = formatearListaNombres(nombres);
        return listaNombres;
    }


    private String formatearListaNombres(List<String> nombres) {
        if (nombres.size() == 2) {
            return nombres.get(0) + " y " + nombres.get(1);
        }

        // 3 o más
        String todosMenosUltimo = String.join(", ", nombres.subList(0, nombres.size() - 1));
        String ultimo = nombres.get(nombres.size() - 1);
        return todosMenosUltimo + " y " + ultimo;
    }

    // tipo + mensaje singular + mensaje plural
    private record Evento(String tipo, String mensajeSingular, String mensajePlural) {}
}
