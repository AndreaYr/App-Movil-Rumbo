package com.app.rumbo.domain.model

data class Author(
    val id: String,
    val name: String,
    val levelName: String
) {
    /** "Camila Restrepo" -> "CR" */
    val initials: String
        get() = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
}
