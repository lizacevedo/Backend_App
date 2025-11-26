package Ucentral.Arquitecturall_monitoreo.servicio.roboflow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.util.Base64;

@Service
public class RoboflowService {

    private final WebClient webClient;
    private final String apiKey;
    private final String modelId;

    public RoboflowService(
            @Value("${roboflow.api.url}") String apiUrl,
            @Value("${roboflow.api.key}") String apiKey,
            @Value("${roboflow.model.id}") String modelId,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)   // https://detect.roboflow.com
                .build();
        this.apiKey = apiKey;
        this.modelId = modelId;   // monitor-am69z/9
    }

    public String detectarEnImagen(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();

            // base64 de la imagen
            String base64Image = Base64.getEncoder().encodeToString(bytes);

            // URL tipo: /monitor-am69z/9?api_key=...
            String url = String.format("/%s?api_key=%s", modelId, apiKey);

            return webClient.post()
                    .uri(url)
                    // Roboflow recomienda x-www-form-urlencoded
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(base64Image)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error llamando a Roboflow: " + e.getMessage(), e);
        }
    }
}
