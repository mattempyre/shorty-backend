package controllers

import play.api.mvc._
import play.api.libs.json.Json // Import for the Json object
import javax.inject.Inject

class CsrfTokenController @Inject() (cc: ControllerComponents)
    extends AbstractController(cc) {

  def csrfToken: Action[AnyContent] = Action { implicit request =>
    // Generate a CSRF token
    val csrfToken =
      play.filters.csrf.CSRF.getToken(request).map(_.value).getOrElse("")

    // Return the CSRF token as a JSON response
    Ok(Json.obj("csrfToken" -> csrfToken))
  }
}
