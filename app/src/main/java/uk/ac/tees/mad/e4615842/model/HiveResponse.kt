package uk.ac.tees.mad.e4615842.model

data class HiveResponse(
    val status: String,
    val output: List<Output>
)

data class Output(
    val time: Double,
    val classes: List<ClassResult>
)

data class ClassResult(
    val className: String,
    val score: Double
)