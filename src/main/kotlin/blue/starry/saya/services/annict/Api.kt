package blue.starry.saya.services.annict

import blue.starry.saya.Env
import jp.annict.lib.impl.v1.client.AnnictClient

val annictClient by lazy {
    AnnictClient(requireNotNull(Env.ANNICT_TOKEN))
}
