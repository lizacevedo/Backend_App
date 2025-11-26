package Ucentral.Arquitecturall_monitoreo.controlador.mascotas;


import Ucentral.Arquitecturall_monitoreo.dto.mascotas.MascotaDTO;
import Ucentral.Arquitecturall_monitoreo.servicio.mascotas.MascotaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaControlador {

    private final MascotaServicio mascotaServicio;

    @PostMapping("/{usuarioId}")
    public ResponseEntity<MascotaDTO> crearMascota(
            @RequestBody MascotaDTO mascotaDTO,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(mascotaServicio.crearMascota(mascotaDTO, usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MascotaDTO>> obtenerMascotasPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(mascotaServicio.obtenerMascotasPorUsuario(usuarioId));
    }

    @GetMapping("/{mascotaId}/usuario/{usuarioId}")
    public ResponseEntity<MascotaDTO> obtenerMascota(
            @PathVariable Long mascotaId,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(mascotaServicio.obtenerMascotaPorIdYUsuario(mascotaId, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Long id) {
        mascotaServicio.eliminarMascota(id);
        return ResponseEntity.ok().build();
    }
}