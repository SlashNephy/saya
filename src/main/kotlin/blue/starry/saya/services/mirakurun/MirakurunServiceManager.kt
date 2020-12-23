package blue.starry.saya.services.mirakurun

import blue.starry.saya.services.mirakurun.models.Service
import kotlinx.coroutines.runBlocking

object MirakurunServiceManager {
    private val services by lazy {
        runBlocking {
            MirakurunApi.getServices()
        }
    }

    fun findByServiceId(id: Int): Service? {
        return services.find { it.serviceId == id }
    }
}
