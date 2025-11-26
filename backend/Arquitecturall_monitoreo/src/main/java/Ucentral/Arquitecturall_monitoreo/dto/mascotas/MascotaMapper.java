package Ucentral.Arquitecturall_monitoreo.dto.mascotas;



import Ucentral.Arquitecturall_monitoreo.entidad.Mascotas.Mascota;
import org.springframework.stereotype.Component;

@Component
public class MascotaMapper {

    public MascotaDTO toDTO(Mascota mascota) {
        return MascotaDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .edad(mascota.getEdad())
                .peso(mascota.getPeso())
                .fotoUrl(mascota.getFotoUrl())
                .videoAsignado(mascota.getVideoAsignado())
                .usuarioId(mascota.getUsuario().getId())
                .fechaCreacion(mascota.getFechaCreacion())
                .build();
    }

    public Mascota toEntity(MascotaDTO dto) {
        return Mascota.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .especie(dto.getEspecie())
                .raza(dto.getRaza())
                .edad(dto.getEdad())
                .peso(dto.getPeso())
                .fotoUrl(dto.getFotoUrl())
                .videoAsignado(dto.getVideoAsignado())
                .fechaCreacion(dto.getFechaCreacion())
                .build();
    }
}