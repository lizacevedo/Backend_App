package Ucentral.Arquitecturall_monitoreo.repositorio.notificaciones;


import Ucentral.Arquitecturall_monitoreo.entidad.notificaciones.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificacionRepositorio extends MongoRepository<Notificacion, String> {

    List<Notificacion> findByUsuarioId(Long usuarioId);
}
