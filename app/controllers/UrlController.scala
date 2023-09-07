package controllers

import javax.inject._
import play.api.mvc._
import connectors.RedisConnector
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json
import org.apache.commons.codec.digest.DigestUtils
import scala.util.matching.Regex

class UrlController @Inject() (
    val controllerComponents: ControllerComponents,
    redisConnector: RedisConnector
)(implicit ec: ExecutionContext)
    extends BaseController {

  private val urlPattern: Regex =
    "^(https?://)?[a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,4})(:[0-9]+)?(/.*)?$".r

  private def isValidUrl(url: String): Boolean = {
    urlPattern.pattern.matcher(url).matches()
  }

  private def generateShortCode(longUrl: String): String = {
    val sha256Hash = DigestUtils.sha256Hex(longUrl)
    sha256Hash.substring(0, 6)
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    request.body.asText match {
      case Some(longUrl) if isValidUrl(longUrl) =>
        val shortCode = generateShortCode(longUrl)
        redisConnector.client.set(shortCode, longUrl).map { _ =>
          Ok(Json.obj("shortCode" -> shortCode))
        }
      case _ => Future.successful(BadRequest("Invalid URL"))
    }
  }

  def redirect(shortcode: String): Action[AnyContent] = Action.async {
    redisConnector.client.get[String](shortcode).map {
      case Some(url) =>
        // Check if the URL starts with "http://" or "https://", and add "http://" if not
        val transformedUrl =
          if (!url.startsWith("http://") && !url.startsWith("https://")) {
            s"http://$url"
          } else url

        // Check if the URL starts with "http://www." or "https://www." and remove "www." if present
        val finalUrl =
          if (transformedUrl.startsWith("http://www.")) transformedUrl.drop(11)
          else if (transformedUrl.startsWith("https://www."))
            transformedUrl.drop(12)
          else transformedUrl

        Redirect(finalUrl)
      case None =>
        NotFound("URL not found")
    }
  }

  def update(shortcode: String): Action[AnyContent] = Action.async {
    implicit request =>
      request.body.asText match {
        case Some(newUrl) if isValidUrl(newUrl) =>
          redisConnector.client.set(shortcode, newUrl).map { _ =>
            Ok("URL updated successfully")
          }
        case _ => Future.successful(BadRequest("Invalid URL"))
      }
  }
}
