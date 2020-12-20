package blue.starry.saya.services.annict

import blue.starry.saya.Env
import jp.annict.lib.impl.v1.client.AnnictClient
import jp.annict.lib.impl.v1.services.WorksGetResponseDataImpl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

val annictClient by lazy {
    AnnictClient(Env.ANNICT_TOKEN)
}

fun searchWorksByTitle(title: String): Deferred<WorksGetResponseDataImpl> {
    return GlobalScope.async {
        annictClient.getWorks(filter_title = title) as WorksGetResponseDataImpl
    }
}
