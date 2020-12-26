package blue.starry.saya.services.annict

import blue.starry.saya.services.SayaAnnictClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AnnictApi {
    suspend fun searchWorksByTitle(title: String) = withContext(Dispatchers.Default) {
        SayaAnnictClient.getWorks(filter_title = title)
    }
}
