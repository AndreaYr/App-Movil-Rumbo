package com.app.rumbo.data.local

import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus

/**
 * Publicaciones de ejemplo tomadas del prototipo. Se reemplazarán por la fuente
 * de datos real (Room / backend) cuando se defina.
 */
object SamplePublications {

    private const val HOUR = 60 * 60 * 1000L
    private const val DAY = 24 * HOUR

    // Fotos libres de Wikimedia Commons (licencias CC BY-SA: hay que mostrar el crédito)
    private const val COMMONS = "https://upload.wikimedia.org/wikipedia/commons/thumb"
    private const val GAGNON = "Bernard Gagnon · CC BY-SA 4.0"

    private fun commons(path: String, description: String, credit: String) =
        Photo("$COMMONS/$path/1280px-${path.substringAfterLast('/')}", description, credit)

    private val camila = Author("u1", "Camila Restrepo", "Embajadora Local")
    private val julian = Author("u2", "Julián Ramírez", "Aventurero")
    private val ana = Author("u3", "Ana María Soto", "Exploradora")
    private val daniel = Author("u4", "Daniel Pineda", "Turista")

    fun create(now: Long = System.currentTimeMillis()): List<Publication> = listOf(
        Publication(
            id = "valle-cocora",
            title = "Valle de Cocora",
            description = "Sendero entre las palmas de cera más altas del mundo, el árbol nacional de Colombia. " +
                "El circuito completo pasa por un puente colgante, bosque de niebla y la casa de los colibríes.",
            category = Category.NATURE,
            town = "Salento",
            address = "Vereda Cocora, 11 km al oriente de Salento",
            schedule = "Todos los días · 6:00 a. m. – 5:00 p. m.",
            distanceKm = 12.4,
            price = PriceLevel.FREE,
            status = PublicationStatus.VERIFIED,
            author = camila,
            publishedAt = now - 3 * HOUR,
            votes = 1284,
            interested = false,
            visited = true,
            latitude = 4.6375,
            longitude = -75.4870,
            photos = listOf(
                commons("a/af/Valle_de_Cocora%2C_Colombia_02.jpg", "palmas de cera en el valle", GAGNON),
                commons("4/40/Valle_de_Cocora%2C_Colombia_03.jpg", "sendero entre las palmas", GAGNON),
                commons("e/e6/Valle_de_Cocora%2C_Colombia_05.jpg", "montañas del valle", GAGNON)
            ),
            comments = listOf(
                Comment("c1", julian, "Llegando a las 6 a. m. no hay filas y se ve la niebla entre las palmas. Vale cada paso.", now - 2 * DAY, 18),
                Comment("c2", ana, "Lleven botas: el tramo del bosque estaba muy barroso. El café de la entrada es excelente.", now - 5 * DAY, 9),
                Comment("c3", daniel, "Hicimos el circuito corto con niños de 7 años y fue perfecto, unas 2 horas.", now - 7 * DAY, 4)
            )
        ),
        Publication(
            id = "trucha-cocora",
            title = "Trucha del Cocora",
            description = "Restaurante tradicional a la entrada del valle. La trucha al ajillo con patacón es la especialidad.",
            category = Category.GASTRONOMY,
            town = "Salento",
            address = "Entrada al Valle de Cocora",
            schedule = "Martes a domingo · 11:00 a. m. – 6:00 p. m.",
            distanceKm = 12.1,
            price = PriceLevel.MODERATE,
            status = PublicationStatus.VERIFIED,
            author = julian,
            publishedAt = now - 6 * HOUR,
            votes = 418,
            interested = false,
            visited = false,
            latitude = 4.6370,
            longitude = -75.4905,
            photos = emptyList(),
            comments = listOf(
                Comment("c4", camila, "Porciones grandes y buen precio. Llegar antes de la 1 p. m.", now - 1 * DAY, 6)
            )
        ),
        Publication(
            id = "murales-filandia",
            title = "Murales de Filandia",
            description = "Recorrido de arte urbano por las fachadas del pueblo, con obras de artistas locales sobre la cultura cafetera.",
            category = Category.CULTURE,
            town = "Filandia",
            address = "Calle 7 y alrededores del parque principal",
            schedule = "Abierto todo el día",
            distanceKm = 23.6,
            price = PriceLevel.FREE,
            status = PublicationStatus.PENDING,
            author = ana,
            publishedAt = now - 1 * DAY,
            votes = 274,
            interested = false,
            visited = false,
            latitude = 4.6736,
            longitude = -75.6577,
            photos = listOf(
                commons("9/96/Calle_7%2C_Filandia_02.jpg", "fachadas de la calle 7", GAGNON),
                commons("a/aa/Calle_del_Convento%2C_Filandia.jpg", "calle del Convento", GAGNON)
            ),
            comments = emptyList()
        ),
        Publication(
            id = "calle-real",
            title = "Calle Real de Salento",
            description = "Calle peatonal de balcones de colores, tiendas de artesanías y escaleras que suben al mirador Alto de la Cruz.",
            category = Category.HISTORY,
            town = "Salento",
            address = "Carrera 6, centro de Salento",
            schedule = "Abierto todo el día",
            distanceKm = 11.8,
            price = PriceLevel.FREE,
            status = PublicationStatus.VERIFIED,
            author = julian,
            publishedAt = now - 2 * DAY,
            votes = 942,
            interested = true,
            visited = true,
            latitude = 4.6372,
            longitude = -75.5708,
            photos = listOf(
                commons("4/48/Callerealsalento.JPG", "balcones de colores", "Sebasmrodriguez · CC BY-SA 3.0"),
                commons("1/11/Mirador_Alto_de_la_Cruz%2C_Salento.jpg", "mirador Alto de la Cruz", GAGNON)
            ),
            comments = listOf(
                Comment("c5", daniel, "Los fines de semana se llena; entre semana es mucho más tranquilo.", now - 3 * DAY, 3)
            )
        ),
        Publication(
            id = "museo-oro-quimbaya",
            title = "Museo del Oro Quimbaya",
            description = "Colección de orfebrería y cerámica de la cultura Quimbaya en un edificio de Rogelio Salmona.",
            category = Category.CULTURE,
            town = "Armenia",
            address = "Av. Bolívar #40N-80, Armenia",
            schedule = "Martes a domingo · 9:00 a. m. – 5:00 p. m.",
            distanceKm = 2.3,
            price = PriceLevel.CHEAP,
            status = PublicationStatus.VERIFIED,
            author = daniel,
            publishedAt = now - 4 * DAY,
            votes = 638,
            interested = false,
            visited = false,
            latitude = 4.5530,
            longitude = -75.6584,
            photos = listOf(
                commons("7/79/Museo_Quimbaya_Armenia_Quindio.JPG", "edificio del museo", "Jhon James Gutierrez Gil · CC BY-SA 3.0"),
                commons("6/62/Museo_del_Oro_Quimbaya.jpg", "jardines del museo", "CamiloBer04 · CC BY-SA 4.0")
            ),
            comments = emptyList()
        ),
        Publication(
            id = "parque-cafe",
            title = "Parque del Café",
            description = "Parque temático con atracciones mecánicas, teleférico y un recorrido por la historia de la cultura cafetera.",
            category = Category.ENTERTAINMENT,
            town = "Montenegro",
            address = "Km 6 vía Montenegro – Pueblo Tapao",
            schedule = "Miércoles a domingo · 9:00 a. m. – 6:00 p. m.",
            distanceKm = 14.7,
            price = PriceLevel.EXPENSIVE,
            status = PublicationStatus.VERIFIED,
            author = camila,
            publishedAt = now - 6 * DAY,
            votes = 1512,
            interested = false,
            visited = false,
            latitude = 4.5397,
            longitude = -75.7714,
            photos = listOf(
                commons("0/03/PLAZA_CENTRAL_DEL_PARQUE_DEL_CAF%C3%89_en_Montenegro%2C_Quind%C3%ADo.JPG", "plaza central del parque", "ACHETORRES · CC BY-SA 4.0"),
                commons("b/b7/Parque_del_Cafe_Entrada.jpg", "entrada del parque", "Petruss · CC BY-SA 4.0")
            ),
            comments = emptyList()
        ),
        Publication(
            id = "cafe-plaza",
            title = "Café de la Plaza",
            description = "Cafetería en la plaza principal.",
            category = Category.GASTRONOMY,
            town = "Circasia",
            address = "Parque principal de Circasia",
            schedule = "Sin horario",
            distanceKm = 9.0,
            price = PriceLevel.CHEAP,
            status = PublicationStatus.REJECTED,
            author = camila,
            publishedAt = now - 6 * HOUR,
            votes = 0,
            interested = false,
            visited = false,
            latitude = 4.6190,
            longitude = -75.6360,
            photos = emptyList(),
            comments = emptyList()
        )
    )
}
