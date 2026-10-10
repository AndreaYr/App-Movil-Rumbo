package com.app.rumbo.data.local

import com.app.rumbo.domain.model.Author

/**
 * Autor de las publicaciones creadas en este dispositivo. Es un marcador de
 * posición hasta que exista autenticación (features/login y features/register).
 */
object CurrentUser {
    val author = Author(id = "me", name = "Tú", levelName = "Explorador")
}

