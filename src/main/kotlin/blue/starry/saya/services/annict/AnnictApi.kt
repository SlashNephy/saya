package blue.starry.saya.services.annict

import blue.starry.saya.services.annictClient
import jp.annict.lib.impl.v1.services.WorksGetResponseDataImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AnnictApi {
    suspend fun searchWorksByTitle(title: String) = withContext(Dispatchers.Default) {
        annictClient.getWorks(filter_title = title) as WorksGetResponseDataImpl
    }
}
