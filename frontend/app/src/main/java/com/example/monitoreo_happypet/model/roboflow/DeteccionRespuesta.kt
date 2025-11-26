package com.example.monitoreo_happypet.model.roboflow



import com.google.gson.annotations.SerializedName

data class DeteccionRespuesta(
    val inference_id: String,
    val time: Double,
    val image: ImagenInfo,
    val predictions: List<Prediccion>
)

data class ImagenInfo(
    val width: Int,
    val height: Int
)

data class Prediccion(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    @SerializedName("class") val clase: String,
    @SerializedName("class_id") val classId: Int,
    @SerializedName("detection_id") val detectionId: String
)
