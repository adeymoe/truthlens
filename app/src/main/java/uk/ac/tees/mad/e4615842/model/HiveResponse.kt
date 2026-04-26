package uk.ac.tees.mad.e4615842.model

import com.google.gson.annotations.SerializedName

data class HiveResponse(
    val output: List<Output>
)

data class Output(
    val classes: List<ClassResult>
)

data class ClassResult(
    @SerializedName("class") val className: String,
    @SerializedName("value") val score: Double
)