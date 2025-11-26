package Ucentral.Arquitecturall_monitoreo.repositorio.usuarios;


import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
}