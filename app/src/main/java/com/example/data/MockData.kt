package com.example.data

import com.example.model.AppNotification
import com.example.model.Professional
import com.example.model.Review
import com.example.model.ServiceCategory
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.model.UserProfile
import com.example.model.UserRole

object MockData {

    val defaultClientUser = UserProfile(
        id = "usr_client_laura",
        name = "Laura",
        lastName = "Gómez",
        email = "laura.gomez@example.com",
        phone = "+57 300 234 5678",
        role = UserRole.CLIENT,
        location = "Laureles, Medellín"
    )

    val defaultWorkerUser = UserProfile(
        id = "usr_worker_mateo",
        name = "Mateo",
        lastName = "Ramírez",
        email = "mateo.ramirez@example.com",
        phone = "+57 311 987 6543",
        role = UserRole.WORKER,
        location = "Laureles, Medellín"
    )

    val categories = listOf(
        ServiceCategory(
            id = "cat_plomeria",
            name = "Plomería",
            iconName = "plumbing",
            description = "Fugas, grifería, tuberías y desagües",
            suggestedKeywords = listOf("Reparación de fugas", "Instalación de grifería", "Destape de cañerías")
        ),
        ServiceCategory(
            id = "cat_electricidad",
            name = "Electricidad",
            iconName = "bolt",
            description = "Instalaciones, cableado, cortos y tomas",
            suggestedKeywords = listOf("Instalación eléctrica", "Cambio de breakers", "Iluminación LED")
        ),
        ServiceCategory(
            id = "cat_carpinteria",
            name = "Carpintería",
            iconName = "carpenter",
            description = "Muebles, puertas, cerraduras de madera y repisas",
            suggestedKeywords = listOf("Arreglo de puertas", "Muebles a medida", "Instalación de repisas")
        ),
        ServiceCategory(
            id = "cat_pintura",
            name = "Pintura",
            iconName = "format_paint",
            description = "Interiores, exteriores, acabados y resanes",
            suggestedKeywords = listOf("Pintura de paredes", "Resane de techos", "Impermeabilización")
        ),
        ServiceCategory(
            id = "cat_cerrajeria",
            name = "Cerrajería",
            iconName = "vpn_key",
            description = "Apertura de chapas, cambio de cilindros y copias",
            suggestedKeywords = listOf("Cambio de cerradura", "Apertura de urgencia", "Instalación de cerrojos")
        ),
        ServiceCategory(
            id = "cat_electrodomesticos",
            name = "Electrodomésticos",
            iconName = "kitchen",
            description = "Lavadoras, neveras, estufas y calentadores",
            suggestedKeywords = listOf("Reparación de electrodomésticos", "Mantenimiento lavadora", "Revisión calentador")
        )
    )

    val professionals = listOf(
        Professional(
            id = "pro_mateo",
            userId = "usr_worker_mateo",
            name = "Mateo Ramírez",
            profession = "Plomero certificado",
            description = "Técnico con más de 8 años de experiencia en soluciones hidrosanitarias, detección de filtraciones y emergencias residenciales en Medellín.",
            experienceYears = 8,
            rating = 4.9,
            servicesCompleted = 128,
            distanceKm = 1.2,
            available = true,
            categoryIds = listOf("cat_plomeria"),
            servicesOffered = listOf(
                "Reparación de fugas",
                "Instalación de tuberías",
                "Mantenimiento de sistemas de agua",
                "Reparaciones generales"
            ),
            locationName = "Laureles, Medellín",
            latitude = 6.2442,
            longitude = -75.5925,
            reviews = listOf(
                Review(
                    id = "rev_1",
                    serviceRequestId = "req_hist_1",
                    clientName = "Carlos M.",
                    rating = 5.0,
                    comment = "Excelente trabajo, llegó en 20 minutos y solucionó la fuga de inmediato. Muy pulcro y respetuoso.",
                    date = "hace 3 días"
                ),
                Review(
                    id = "rev_2",
                    serviceRequestId = "req_hist_2",
                    clientName = "María G.",
                    rating = 5.0,
                    comment = "Recomendadísimo. Trajo todos los repuestos necesarios y dejó el lavaplatos impecable.",
                    date = "hace 1 semana"
                )
            )
        ),
        Professional(
            id = "pro_julian",
            userId = "usr_julian",
            name = "Julián Castro",
            profession = "Electricista",
            description = "Especialista certificado CONTE en instalaciones eléctricas seguras residenciales y comerciales. Diagnósticos rápidos y seguros.",
            experienceYears = 6,
            rating = 4.8,
            servicesCompleted = 96,
            distanceKm = 2.4,
            available = true,
            categoryIds = listOf("cat_electricidad"),
            servicesOffered = listOf(
                "Instalación eléctrica",
                "Revisión de breakers y tableros",
                "Puntos de luz y tomacorrientes",
                "Cableado estructurado"
            ),
            locationName = "Belén, Medellín",
            latitude = 6.2310,
            longitude = -75.5990,
            reviews = listOf(
                Review(
                    id = "rev_3",
                    serviceRequestId = "req_hist_3",
                    clientName = "Andrea P.",
                    rating = 4.8,
                    comment = "Gran profesionalismo, encontró un corto que nadie más había podido resolver.",
                    date = "hace 2 semanas"
                )
            )
        ),
        Professional(
            id = "pro_andres",
            userId = "usr_andres",
            name = "Andrés Molina",
            profession = "Carpintero",
            description = "Maestro carpintero y ebanista. Restauración, fabricación de closets a medida, ajuste de puertas y muebles modulares.",
            experienceYears = 10,
            rating = 4.7,
            servicesCompleted = 74,
            distanceKm = 3.1,
            available = false,
            categoryIds = listOf("cat_carpinteria"),
            servicesOffered = listOf(
                "Arreglo de puertas y bisagras",
                "Muebles a medida",
                "Instalación de repisas y cocinas",
                "Lijado y barnizado"
            ),
            locationName = "Poblado, Medellín",
            latitude = 6.2100,
            longitude = -75.5700,
            reviews = listOf(
                Review(
                    id = "rev_4",
                    serviceRequestId = "req_hist_4",
                    clientName = "Esteban R.",
                    rating = 4.7,
                    comment = "Mueble de baño perfecto, madera de primera calidad.",
                    date = "hace 1 mes"
                )
            )
        ),
        Professional(
            id = "pro_sofia",
            userId = "usr_sofia",
            name = "Sofía Herrera",
            profession = "Pintora profesional",
            description = "Pintura decorativa e impermeabilización con acabados finos y garantía de durabilidad sin ensuciar pisos ni muebles.",
            experienceYears = 5,
            rating = 4.9,
            servicesCompleted = 82,
            distanceKm = 1.8,
            available = true,
            categoryIds = listOf("cat_pintura"),
            servicesOffered = listOf(
                "Pintura de paredes y techos",
                "Resane de grietas y humedad",
                "Acabados decorativos",
                "Impermeabilización de fachadas"
            ),
            locationName = "Estadio, Medellín",
            latitude = 6.2550,
            longitude = -75.5900,
            reviews = listOf(
                Review(
                    id = "rev_5",
                    serviceRequestId = "req_hist_5",
                    clientName = "Patricia L.",
                    rating = 5.0,
                    comment = "Dejó el apartamento como nuevo, super detallista con las esquinas.",
                    date = "hace 4 días"
                )
            )
        ),
        Professional(
            id = "pro_carlos",
            userId = "usr_carlos",
            name = "Carlos Mario Restrepo",
            profession = "Cerrajero de emergencia",
            description = "Servicio rápido 24/7. Apertura de cerraduras de alta seguridad sin dañar la puerta, cambio de claves y guardas.",
            experienceYears = 12,
            rating = 4.9,
            servicesCompleted = 160,
            distanceKm = 0.9,
            available = true,
            categoryIds = listOf("cat_cerrajeria"),
            servicesOffered = listOf(
                "Apertura de chapas residenciales",
                "Instalación de cerraduras digitales",
                "Cambio de cilindros",
                "Mantenimiento de cerrojos"
            ),
            locationName = "Laureles, Medellín",
            latitude = 6.2410,
            longitude = -75.5940,
            reviews = listOf(
                Review(
                    id = "rev_6",
                    serviceRequestId = "req_hist_6",
                    clientName = "Jorge V.",
                    rating = 5.0,
                    comment = "Me quedé afuera a las 8pm y en 15 minutos ya me había abierto sin romper nada.",
                    date = "ayer"
                )
            )
        ),
        Professional(
            id = "pro_daniel",
            userId = "usr_daniel",
            name = "Daniel Ortiz",
            profession = "Técnico en electrodomésticos",
            description = "Certificado en marcas Whirlpool, Haceb, LG y Samsung. Diagnóstico computarizado de tarjetas y compresores.",
            experienceYears = 7,
            rating = 4.6,
            servicesCompleted = 110,
            distanceKm = 2.8,
            available = true,
            categoryIds = listOf("cat_electrodomesticos"),
            servicesOffered = listOf(
                "Reparación de lavadoras y secadoras",
                "Mantenimiento de neveras",
                "Arreglo de calentadores a gas",
                "Revisión de estufas"
            ),
            locationName = "Robledo, Medellín",
            latitude = 6.2700,
            longitude = -75.5800,
            reviews = listOf(
                Review(
                    id = "rev_7",
                    serviceRequestId = "req_hist_7",
                    clientName = "Diana C.",
                    rating = 4.6,
                    comment = "Diagnosticó rápidamente el sensor de la nevera. Repuesto original.",
                    date = "hace 2 semanas"
                )
            )
        )
    )

    val initialRequests = listOf(
        ServiceRequest(
            id = "req_hist_1",
            clientId = "usr_client_laura",
            clientName = "Laura Gómez",
            clientPhone = "+57 300 234 5678",
            professionalId = "pro_mateo",
            professionalName = "Mateo Ramírez",
            professionalProfession = "Plomero certificado",
            categoryId = "cat_plomeria",
            categoryName = "Plomería",
            description = "Fuga de agua debajo del lavaplatos.",
            location = "Laureles, Medellín",
            scheduledDate = "7 de octubre de 2026",
            scheduledTime = "10:30 AM",
            status = ServiceStatus.COMPLETED,
            ratingGiven = 5.0,
            reviewComment = "Excelente servicio y rapidez.",
            createdAt = "2026-10-07T09:00:00"
        ),
        ServiceRequest(
            id = "req_hist_2",
            clientId = "usr_client_laura",
            clientName = "Laura Gómez",
            clientPhone = "+57 300 234 5678",
            professionalId = "pro_julian",
            professionalName = "Julián Castro",
            professionalProfession = "Electricista",
            categoryId = "cat_electricidad",
            categoryName = "Electricidad",
            description = "Revisión de instalación eléctrica",
            location = "Laureles, Medellín",
            scheduledDate = "28 de septiembre de 2026",
            scheduledTime = "02:00 PM",
            status = ServiceStatus.COMPLETED,
            ratingGiven = 4.8,
            reviewComment = "Puntual y muy ordenado.",
            createdAt = "2026-09-28T11:00:00"
        ),
        ServiceRequest(
            id = "req_active_1",
            clientId = "usr_client_laura",
            clientName = "Laura Gómez",
            clientPhone = "+57 300 234 5678",
            professionalId = "pro_mateo",
            professionalName = "Mateo Ramírez",
            professionalProfession = "Plomero certificado",
            categoryId = "cat_plomeria",
            categoryName = "Plomería",
            description = "Fuga de agua persistente debajo del lavaplatos con goteo constante.",
            location = "Laureles, Medellín",
            scheduledDate = "Hoy, 8 de octubre",
            scheduledTime = "03:00 PM",
            status = ServiceStatus.PENDING,
            createdAt = "2026-10-08T08:30:00"
        )
    )

    val initialNotifications = listOf(
        AppNotification(
            id = "notif_1",
            title = "¡Bienvenida a OficiosCerca!",
            message = "Encuentra personas confiables para resolver los oficios de tu hogar.",
            read = true,
            date = "Hoy 08:00 AM"
        ),
        AppNotification(
            id = "notif_2",
            title = "Mateo Ramírez está revisando tu solicitud",
            message = "Solicitud de plomería enviada con éxito.",
            read = false,
            date = "Hoy 08:31 AM",
            relatedRequestId = "req_active_1"
        ),
        AppNotification(
            id = "notif_3",
            title = "Recuerda calificar tu servicio anterior",
            message = "Tu opinión ayuda a mantener la comunidad segura y confiable.",
            read = true,
            date = "Ayer 05:00 PM"
        )
    )
}
