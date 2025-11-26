package Ucentral.Arquitecturall_monitoreo.servicio.notificaciones;


import Ucentral.Arquitecturall_monitoreo.entidad.notificaciones.Notificacion;
import Ucentral.Arquitecturall_monitoreo.repositorio.notificaciones.NotificacionRepositorio;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class NotificacionServicio {

    private final NotificacionRepositorio notificacionRepositorio;

    public NotificacionServicio(NotificacionRepositorio notificacionRepositorio) {
        this.notificacionRepositorio = notificacionRepositorio;
    }

    /**
     * Registra una notificación completa en Mongo.
     *
     * @param usuarioId       Id del usuario dueño de la mascota
     * @param nombresMascotas Lista de nombres de las mascotas involucradas
     * @param tipoEvento      Tipo de evento (PLANTA, COMIDA_VACIA, COMIDA_LLENA, SOFA, ZAPATOS, etc.)
     * @param mensaje         Mensaje ya formateado que se envía por Telegram
     */
    public Notificacion registrarNotificacion(
            Long usuarioId,
            List<String> nombresMascotas,
            String tipoEvento,
            String mensaje
    ) {
        if (nombresMascotas == null) {
            nombresMascotas = Collections.emptyList();
        }

        Notificacion notificacion = Notificacion.builder()
                .usuarioId(usuarioId)
                .mascotas(nombresMascotas)
                .tipo(tipoEvento)
                .mensaje(mensaje)
                .fecha(LocalDateTime.now())
                .build();

        return notificacionRepositorio.save(notificacion);
    }

    /**
     * Versión simplificada cuando no quieres enviar lista de mascotas.
     */
    public Notificacion registrarNotificacionSimple(
            Long usuarioId,
            String tipoEvento,
            String mensaje
    ) {
        return registrarNotificacion(usuarioId, Collections.emptyList(), tipoEvento, mensaje);
    }

    /**
     * Obtiene el historial de notificaciones de un usuario.
     */
    public List<Notificacion> obtenerNotificacionesDeUsuario(Long usuarioId) {
        return notificacionRepositorio.findByUsuarioId(usuarioId);
    }
}
