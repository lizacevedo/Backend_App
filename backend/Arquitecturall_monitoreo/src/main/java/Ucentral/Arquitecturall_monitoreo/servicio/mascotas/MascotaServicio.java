package Ucentral.Arquitecturall_monitoreo.servicio.mascotas;


import Ucentral.Arquitecturall_monitoreo.dto.mascotas.MascotaDTO;
import Ucentral.Arquitecturall_monitoreo.dto.mascotas.MascotaMapper;
import Ucentral.Arquitecturall_monitoreo.entidad.Mascotas.Mascota;
import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import Ucentral.Arquitecturall_monitoreo.repositorio.mascotas.MascotaRepositorio;
import Ucentral.Arquitecturall_monitoreo.servicio.usuarios.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MascotaServicio {

    private final MascotaRepositorio mascotaRepositorio;
    private final UsuarioServicio usuarioServicio;
    private final MascotaMapper mascotaMapper;

    public MascotaDTO crearMascota(MascotaDTO mascotaDTO, Long usuarioId) {
        Mascota mascota = mascotaMapper.toEntity(mascotaDTO);
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(usuarioId);
        mascota.setUsuario(usuario);

        String videoAsignado = asignarVideoPorUsuario(usuarioId);
        mascota.setVideoAsignado(videoAsignado);

        Mascota mascotaGuardada = mascotaRepositorio.save(mascota);
        return mascotaMapper.toDTO(mascotaGuardada);
    }

    public List<MascotaDTO> obtenerMascotasPorUsuario(Long usuarioId) {
        List<Mascota> mascotas = mascotaRepositorio.findByUsuarioId(usuarioId);
        return mascotas.stream()
                .map(mascotaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MascotaDTO obtenerMascotaPorId(Long id) {
        Mascota mascota = mascotaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        return mascotaMapper.toDTO(mascota);
    }

    public MascotaDTO obtenerMascotaPorIdYUsuario(Long mascotaId, Long usuarioId) {
        Mascota mascota = mascotaRepositorio.findByIdAndUsuarioId(mascotaId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada para este usuario"));
        return mascotaMapper.toDTO(mascota);
    }

    public void eliminarMascota(Long id) {
        mascotaRepositorio.deleteById(id);
    }

    // Campo estático que recuerda el último video asignado
    private static int ultimoVideo = 0;

    private String asignarVideoPorUsuario(Long usuarioId) {

        // Rotamos manualmente: 1 → 2 → 3 → 1 → 2 → 3 ...
        ultimoVideo = (ultimoVideo % 3) + 1;

        return "video" + ultimoVideo;
    }

}