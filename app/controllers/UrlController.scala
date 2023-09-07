package controllers

import javax.inject._
import play.api.mvc._
import connectors.RedisConnector
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json
import java.security.MessageDigest

class UrlController @Inject() (
    val controllerComponents: ControllerComponents,
    redisConnector: RedisConnector
)(implicit ec: ExecutionContext)
    extends BaseController {

  // Function to generate a short code from a long URL using SHA-256
  private def generateShortCode(longUrl: String): String = {
    val sha256Digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = sha256Digest.digest(longUrl.getBytes("UTF-8"))
    val hexString = hashBytes.map("%02x".format(_)).mkString
    hexString.substring(0, 6) // Limit to 6 characters
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    request.body.asText match {
      case Some(longUrl) =>
        val shortCode = generateShortCode(longUrl)
        redisConnector.client.set(shortCode, longUrl).map { _ =>
          Ok(Json.obj("shortCode" -> shortCode))
        }
      case None => Future.successful(BadRequest("Invalid URL"))
    }
  }

  def redirect(shortcode: String): Action[AnyContent] = Action.async {
    redisConnector.client.get[String](shortcode).map {
      case Some(url) => Redirect(url)
      case None      => NotFound("URL not found")
    }
  }

  def update(shortcode: String): Action[AnyContent] = Action.async {
    implicit request =>
      request.body.asText match {
        case Some(newUrl) =>
          redisConnector.client.set(shortcode, newUrl).map { _ =>
            Ok("URL updated successfully")
          }
        case None => Future.successful(BadRequest("Invalid URL"))
      }
  }
}
