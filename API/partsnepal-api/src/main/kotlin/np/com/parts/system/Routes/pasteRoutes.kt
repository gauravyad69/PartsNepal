package np.com.parts.system.Routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Services.PasteReq
import np.com.parts.system.Services.PasteService

fun Route.pastebinApi(pasteService: PasteService) {
    route("paste") {
        // Create a new paste
        post {
            val content = call.receive<PasteReq>()
            val paste = pasteService.createPaste(content.title!!, content.content!!)
            call.respond(HttpStatusCode.Created, paste)
        }

        // Retrieve a paste by ID
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Id is required")

            val paste = pasteService.getPaste(id)
            if (paste != null) {
                call.respond(paste)
            } else {
                call.respond(HttpStatusCode.NotFound, "Paste not found")
            }
        }

        // Edit an existing paste
        put("/{id}") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Id is required")
            val newContent = call.receive<PasteReq>()
            val updatedPaste = pasteService.updatePaste(id, newContent=newContent.content!!, newTitle = newContent.title!!)
            if (updatedPaste != null) {
                call.respond(updatedPaste)
            } else {
                call.respond(HttpStatusCode.NotFound, "Paste not found")
            }
        }

        // Edit an existing paste
        patch("/{id}") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Id is required")
            val newContent = call.receive<PasteReq>()
            if (newContent.title!=null){
                val updatedPaste = pasteService.updateTitle(id,newTitle = newContent.title!!)
                if (updatedPaste != null) {
                    call.respond(updatedPaste)
                }
            }
            if (newContent.content!=null){
                val updatedPaste = pasteService.updatePasteCContent(id,newContent = newContent.content!!)
                if (updatedPaste != null) {
                    call.respond(updatedPaste)
                }

            } else {
                call.respond(HttpStatusCode.NotFound, "Paste not found")
            }
        }

        // Delete a paste by ID
        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id is required")

            val isDeleted = pasteService.deletePaste(id)
            if (isDeleted) {
                call.respond(HttpStatusCode.OK, "Paste deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Paste not found")
            }
        }
    }
}
