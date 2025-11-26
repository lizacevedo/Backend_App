package Ucentral.Arquitecturall_monitoreo.repositorio.mascotas;


import Ucentral.Arquitecturall_monitoreo.entidad.Mascotas.Mascota;
import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepositorio extends JpaRepository<Mascota, Long> {
    List<Mascota> findByUsuario(Usuario usuario);
    List<Mascota> findByUsuarioId(Long usuarioId);
    Optional<Mascota> findByIdAndUsuarioId(Long id, Long usuarioId);
}