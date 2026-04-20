package uk.ac.tees.mad.e4615842.model

import com.google.gson.annotations.SerializedName

data class HiveResponse(
    val status: String,
    val output: List<Output>
)

data class Output(
    val time: Double,
    val classes: List<ClassResult>
)

data class ClassResult(
    @SerializedName("class_name") val className: String,
    val score: Double
)