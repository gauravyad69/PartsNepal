package np.com.parts.system.Routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Services.*

fun Route.categoryApi(categoryService: CategoryService) {
    route("/category") {
        // Create a new category
        post {
            val content = call.receive<CategoryModelReq>()
            val category = categoryService.createCategory(content.categoryName, content.subCategoryName)
            call.respond(HttpStatusCode.Created, category)
        }

        // Retrieve all categorys
        get {

            val category = categoryService.getAllCategory()
            if (category != null) {
                call.respond(category)
            } else {
                call.respond(HttpStatusCode.NotFound, "category not found")
            }
        }


        // Retrieve a category by ID
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Id is required")

            val category = categoryService.getCategory(id)
            if (category != null) {
                call.respond(category)
            } else {
                call.respond(HttpStatusCode.NotFound, "category not found")
            }
        }

        // Edit an existing category
        put("/{id}") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Id is required")
            val newContent = call.receive<CategoryModelReq>()
            val updatedCategory = categoryService.updateCategoryModel(id, categoryName=newContent.categoryName!!, subCategoryName = newContent.subCategoryName!!)
            if (updatedCategory != null) {
                call.respond(updatedCategory)
            } else {
                call.respond(HttpStatusCode.NotFound, "category not found")
            }
        }

        // Edit an existing category
        patch("/{id}") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Id is required")
            val newContent = call.receive<CategoryModelReq>()
            if (newContent.categoryName!=null){
                val updatedcategory = categoryService.updateCategoryName(id, newCategoryName = newContent.categoryName!!)
                if (updatedcategory != null) {
                    call.respond(updatedcategory)
                }
            }
            if (newContent.subCategoryName!=null){
                val updatedcategory = categoryService.updateSubCategoryName(id, newSubCategoryName = newContent.subCategoryName!!)
                if (updatedcategory != null) {
                    call.respond(updatedcategory)
                }

            } else {
                call.respond(HttpStatusCode.NotFound, "Category not not found")
            }
        }

        // Delete a category by ID
        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Id is required")

            val isDeleted = categoryService.deleteCategory(id)
            if (isDeleted) {
                call.respond(HttpStatusCode.OK, "category deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "category not found")
            }
        }
    }
}
