package blue.starry.saya.services.mirakurun

import blue.starry.saya.services.mirakurun.models.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object MirakurunServiceManager {
    private val services = mutableListOf<Service>()

    init {
        GlobalScope.launch {
            val new = MirakurunApi.getServices()
            services.addAll(new)
        }
    }

    fun findByServiceId(id: Int): Service? {
        return services.find { it.serviceId == id }
    }
}
