package connectors

import play.api.Configuration
import redis.RedisClient
import javax.inject.Inject
import akka.actor.ActorSystem

// Import necessary libraries and modules

class RedisConnector @Inject() (config: Configuration)(implicit
    val actorSystem: ActorSystem
) {
  // Inject the Configuration and ActorSystem dependencies

  // Get the Redis server host from the environment variables, default to "defaultHost" if not found
  private val redisHost = sys.env.getOrElse("REDIS_HOST", "defaultHost")

  // Get the Redis server port from the environment variables, default to "defaultPort" if not found,
  // and convert it to an integer
  private val redisPort = sys.env.getOrElse("REDIS_PORT", "defaultPort").toInt

  // Get the Redis server password from the environment variables, default to "defaultPassword" if not found
  private val redisPassword =
    sys.env.getOrElse("REDIS_PASSWORD", "defaultPassword")

  // Create a RedisClient instance with the configured host, port, and password
  val client: RedisClient = RedisClient(
    host = redisHost,
    port = redisPort,
    password = Some(redisPassword)
  )
}
