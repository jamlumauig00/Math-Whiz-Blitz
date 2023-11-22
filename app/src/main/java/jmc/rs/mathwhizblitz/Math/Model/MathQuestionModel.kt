package jmc.rs.mathwhizblitz.Math.Model

data class MathQuestionModel(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = ""
)
