package connectors

import play.api.Configuration
import redis.RedisClient
import javax.inject.Inject
import akka.actor.ActorSystem

class RedisConnector @Inject() (config: Configuration)(implicit
    val actorSystem: ActorSystem
) {
  private val redisHost = sys.env.getOrElse("REDIS_HOST", "defaultHost")
  private val redisPort = sys.env.getOrElse("REDIS_PORT", "defaultPort").toInt
  private val redisPassword =
    sys.env.getOrElse("REDIS_PASSWORD", "defaultPassword")

  val client: RedisClient = RedisClient(
    host = redisHost,
    port = redisPort,
    password = Some(redisPassword)
  )
}
