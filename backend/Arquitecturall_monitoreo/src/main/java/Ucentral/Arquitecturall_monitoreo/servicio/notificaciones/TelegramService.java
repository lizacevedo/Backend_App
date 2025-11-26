package Ucentral.Arquitecturall_monitoreo.servicio.notificaciones;


import Ucentral.Arquitecturall_monitoreo.entidad.usuarios.Usuario;
import Ucentral.Arquitecturall_monitoreo.repositorio.usuarios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UsuarioRepositorio usuarioRepositorio;
    private final RestTemplate restTemplate;

    public TelegramService(UsuarioRepositorio usuarioRepositorio,
                           RestTemplateBuilder restTemplateBuilder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Envía una notificación al usuario usando su telegramChatId.
     * Si el usuario no existe o no tiene chat configurado, no hace nada.
     */
    public void enviarNotificacionAlUsuario(Long usuarioId, String mensaje) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
        if (usuario == null) {
            System.out.println("⚠️ Usuario no encontrado para enviar Telegram: " + usuarioId);
            return;
        }

        Long chatId = usuario.getTelegramChatId();
        if (chatId == null) {
            System.out.println("ℹ️ Usuario " + usuarioId + " no tiene telegramChatId configurado.");
            return;
        }

        enviarMensajeChatId(chatId, mensaje);
    }

    /**
     * Envía un mensaje directo a un chatId específico.
     */
    public void enviarMensajeChatId(Long chatId, String mensaje) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        // Enviar como application/x-www-form-urlencoded
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId.toString());
        body.add("text", mensaje);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Telegram enviado a chatId " + chatId);
        } catch (Exception e) {
            System.out.println("❌ Error enviando mensaje a Telegram: " + e.getMessage());
        }
    }
}

