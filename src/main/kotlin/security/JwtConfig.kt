package security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    val SECRET = System.getenv("JWT_SECRET") ?: error("JWT_SECRET не задан")
    val ISSUER = System.getenv("JWT_ISSUER") ?: error("JWT_ISSUER не задан")
    val AUDIENCE = System.getenv("JWT_AUDIENCE") ?: error("JWT_AUDIENCE не задан")
    private const val VALIDITY = 14L * 24 * 60 * 60 * 1000 // 14

    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(SECRET))
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(email: String, role: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY))
            .sign(Algorithm.HMAC256(SECRET))
    }
}