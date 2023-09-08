package controllers

import javax.inject._
import play.api.mvc._
import connectors.RedisConnector
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json
import org.apache.commons.codec.digest.DigestUtils
import scala.util.matching.Regex

// Inject the required dependencies and set up the controller
class UrlController @Inject() (
    val controllerComponents: ControllerComponents,
    redisConnector: RedisConnector
)(implicit ec: ExecutionContext)
    extends BaseController {

  // Regular expression to validate URLs
  private val urlPattern: Regex =
    "^(https?://)?[a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,4})(:[0-9]+)?(/.*)?$".r

  // Function to check if a URL is valid
  private def isValidUrl(url: String): Boolean = {
    urlPattern.pattern.matcher(url).matches()
  }

  // Function to generate a short code from a long URL using SHA-256
  private def generateShortCode(longUrl: String): String = {
    val sha256Hash = DigestUtils.sha256Hex(longUrl)
    sha256Hash.substring(0, 6)
  }

  // Action to create a short URL from a long URL
  def create: Action[AnyContent] = Action.async { implicit request =>
    request.body.asText match {
      case Some(longUrl) if isValidUrl(longUrl) =>
        // Generate a short code for the long URL
        val shortCode = generateShortCode(longUrl)

        // Store the short code and long URL in Redis
        redisConnector.client.set(shortCode, longUrl).map { _ =>
          Ok(Json.obj("shortCode" -> shortCode))
        }
      case _ => Future.successful(BadRequest("Invalid URL"))
    }
  }

  // Action to redirect to the original URL using a short code
  def redirect(shortcode: String): Action[AnyContent] = Action.async {
    redisConnector.client.get[String](shortcode).map {
      case Some(url) =>
        // Check if the URL starts with "http://" or "https://", and add "http://" if not
        val transformedUrl =
          if (!url.startsWith("http://") && !url.startsWith("https://")) {
            s"http://$url"
          } else url

        // Redirect to the final URL
        Redirect(transformedUrl)
      case None =>
        NotFound("URL not found")
    }
  }

  // Action to update the original URL associated with a short code
  def update(shortcode: String): Action[AnyContent] = Action.async {
    implicit request =>
      request.body.asText match {
        case Some(newUrl) if isValidUrl(newUrl) =>
          // Update the long URL associated with the short code in Redis
          redisConnector.client.set(shortcode, newUrl).map { _ =>
            Ok("URL updated successfully")
          }
        case _ => Future.successful(BadRequest("Invalid URL"))
      }
  }

  // Action to delete the URL associated with a short code
  def delete(shortcode: String): Action[AnyContent] = Action.async {
    implicit request =>
      // Implement the logic to delete the URL associated with the given shortcode
      redisConnector.client.del(shortcode).map {
        case 1L => Ok("URL deleted successfully")
        case 0L => NotFound("URL not found")
      }
  }
}
