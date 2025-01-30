package np.com.parts.system.Routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Services.Carrousel
import np.com.parts.system.Services.CarrouselService
import np.com.parts.system.Services.CarrouselReq

fun Route.unauthenticatedCarrouselRoutes(carrouselService: CarrouselService) {
    route("carrousel") {
        // Retrieve all carrousels
        get {

            val carrousel = carrouselService.getAllCarrousel()
            if (carrousel != null) {
                call.respond(carrousel)
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }


        // Retrieve a carrousel by ID
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Id is required")

            val carrousel = carrouselService.getCarrousel(id)
            if (carrousel != null) {
                call.respond(carrousel)
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }


    }
}
fun Route.adminCarrouselRoutes(carrouselService: CarrouselService) {
    route("admin/carrousel") {
        // Create a new carrousel
        post {
            val content = call.receive<Carrousel>()
            val carrousel = carrouselService.createCarrousel(content.carrouselId!!, content.imageUrl!!)
            call.respond(HttpStatusCode.Created, carrousel)
        }

        // Retrieve all carrousels
        get {

            val carrousel = carrouselService.getAllCarrousel()
            if (carrousel != null) {
                call.respond(carrousel)
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }


        // Retrieve a carrousel by ID
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Id is required")

            val carrousel = carrouselService.getCarrousel(id)
            if (carrousel != null) {
                call.respond(carrousel)
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }

        // Edit an existing carrousel
        put("/{id}") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Id is required")
            val newContent = call.receive<CarrouselReq>()
            val updatedCarrousel = carrouselService.updateCarrousel(id, newContent=newContent.imageUrl!!, newTitle = newContent.carrouselId!!)
            if (updatedCarrousel != null) {
                call.respond(updatedCarrousel)
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }

        // Edit an existing carrousel
        patch("/{id}") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Id is required")
            val newContent = call.receive<CarrouselReq>()
            if (newContent.carrouselId!=null){
                val updatedCarrousel = carrouselService.updateTitle(id,newTitle = newContent.carrouselId!!)
                if (updatedCarrousel != null) {
                    call.respond(updatedCarrousel)
                }
            }
            if (newContent.imageUrl!=null){
                val updatedCarrousel = carrouselService.updateCarrouselCContent(id,newContent = newContent.imageUrl!!)
                if (updatedCarrousel != null) {
                    call.respond(updatedCarrousel)
                }

            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }

        // Delete a carrousel by ID
        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id is required")

            val isDeleted = carrouselService.deleteCarrousel(id)
            if (isDeleted) {
                call.respond(HttpStatusCode.OK, "Carrousel deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Carrousel not found")
            }
        }
    }
}
