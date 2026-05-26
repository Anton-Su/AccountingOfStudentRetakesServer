package domain.repository

//import domain.model.Country
import domain.model.User
import domain.model.NobelPrize

interface UserRepository {
    suspend fun findByUsername(username: String): User?
    suspend fun findById(id: Int): User?
    suspend fun getFavorites(userId: Int): List<NobelPrize>
    suspend fun addFavorite(userId: Int, prizeId: Int): Boolean
    suspend fun removeFavorite(userId: Int, prizeId: Int): Boolean
}