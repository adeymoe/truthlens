package uk.ac.tees.mad.e4615842.model

import com.google.gson.annotations.SerializedName

data class HiveRequest(
    val input: List<HiveInput>
)

data class HiveInput(
    @SerializedName("media_base64") val mediaBase64: String
)