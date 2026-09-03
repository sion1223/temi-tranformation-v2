package kr.hs.gwangyang.temidelivery

import android.app.Application
import com.robotemi.sdk.Robot
import kr.hs.gwangyang.temidelivery.aiguide.SupplyGuideClientFactory
import kr.hs.gwangyang.temidelivery.data.AssetDeliveryRouteRepository
import kr.hs.gwangyang.temidelivery.data.TemiKioskGateway
import kr.hs.gwangyang.temidelivery.data.TemiRobotGateway
import kr.hs.gwangyang.temidelivery.domain.DeliveryCoordinator
import kr.hs.gwangyang.temidelivery.domain.HandleVoiceCommandUseCase
import kr.hs.gwangyang.temidelivery.domain.VoiceCommandInterpreter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TemiDeliveryApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val featureSettingsStore = FeatureSettingsStore(application)
    val robotGateway = TemiRobotGateway(Robot.getInstance()) {
        featureSettingsStore.current().speechOutputEnabled
    }
    val kioskGateway = TemiKioskGateway(Robot.getInstance())
    val routeRepository = AssetDeliveryRouteRepository(application)
    val deliveryCoordinator = DeliveryCoordinator(robotGateway, applicationScope)
    val handleVoiceCommand = HandleVoiceCommandUseCase(
        interpreter = VoiceCommandInterpreter(),
        deliveryCoordinator = deliveryCoordinator,
        robotGateway = robotGateway,
    )
    val supplyGuideClient = SupplyGuideClientFactory.fromAsset(application)
    val basketClient = RuntimeBasketClient(featureSettingsStore.current())

    init {
        robotGateway.start()
    }
}
