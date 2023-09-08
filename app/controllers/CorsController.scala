package controllers

import javax.inject.Inject
import play.api.mvc._

class CorsController @Inject() (cc: ControllerComponents)
    extends AbstractController(cc) {
  def preFlight = Action {
    Ok.withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "OPTIONS, POST, GET, PUT, DELETE",
      "Access-Control-Allow-Headers" -> "*",
      "Access-Control-Max-Age" -> "3600"
    )
  }

  // Add a method to handle your API endpoint without authentication here
  def yourApiEndpoint = Action {
    // Your API logic here
    Ok("Response without authentication")
  }
}
