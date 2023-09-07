package controllers

import play.api.mvc._
import javax.inject._
import com.redis._

@Singleton
class UrlController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  // Initializing Redis client
  val redis = new RedisClient("localhost", 6379)

  def create = Action { implicit request: Request[AnyContent] =>
    request.body.asText
      .map { longUrl =>
        val shortCode =
          longUrl.hashCode.toString // Simple hash for the sake of this example
        redis.set(shortCode, longUrl) match {
          case true  => Ok(shortCode)
          case false => InternalServerError("Failed to create short URL")
        }
      }
      .getOrElse {
        BadRequest("Expected text data")
      }
  }

  def redirect(shortcode: String) = Action {
    redis.get(shortcode) match {
      case Some(url) => Redirect(url, MOVED_PERMANENTLY)
      case None      => NotFound("URL not found")
    }
  }

  def update(shortcode: String) = Action {
    implicit request: Request[AnyContent] =>
      request.body.asText
        .map { newUrl =>
          redis.set(shortcode, newUrl) match {
            case true  => Ok("URL updated")
            case false => InternalServerError("Failed to update URL")
          }
        }
        .getOrElse {
          BadRequest("Expected text data")
        }
  }
}
