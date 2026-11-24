package cl.duoc.tacticaltrack
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import cl.duoc.tacticaltrack.data.repository.*
import cl.duoc.tacticaltrack.model.*
import cl.duoc.tacticaltrack.navigation.*
import cl.duoc.tacticaltrack.push.PushRegistration
import cl.duoc.tacticaltrack.ui.theme.TacticalTrackTheme
import io.mockk.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=TacticalTrackApplication::class)
class NavigationAndroidTest {
 @get:Rule val compose=createComposeRule()
 private val user=MutableStateFlow<AuthUser?>(AuthUser(1,"ad@duoc.cl","Kevin","SUPERVISOR",true,"SUP-001"))
 private lateinit var auth:AuthRepository
 @Before fun setup(){
  val app=ApplicationProvider.getApplicationContext<TacticalTrackApplication>();mockkObject(app)
  auth=mockk(relaxed=true);every{auth.user}returns user;coEvery{auth.restore()}answers{user.value}
  coEvery{auth.logout()}coAnswers{user.value=null}
  val preferences=mockk<UserPreferencesRepository>(relaxed=true);every{preferences.lastUsername}returns flowOf("")
  val repo=mockk<VisitRepository>(relaxed=true);every{repo.visits}returns flowOf(listOf(sampleVisit()))
  every{repo.observeVisit(any())}returns flowOf(sampleVisit())
  val catalog=mockk<CatalogRepository>();coEvery{catalog.load()}returns CatalogSnapshot(TestCatalog.points,TestCatalog.assets)
  every{app.authRepository}returns auth;every{app.preferencesRepository}returns preferences
  every{app.visitRepository}returns repo;every{app.catalogRepository}returns catalog
  mockkObject(PushRegistration);every{PushRegistration.setRole(any(),any(),any())}just Runs
  every{PushRegistration.supervisorSession}returns MutableStateFlow(true)
  coEvery{PushRegistration.endSession(any())}just Runs
 }
 @After fun cleanup(){unmockkAll()}
 @Test fun supervisorCanNavigateTabsOpenNewVisitAndLogOut(){
  compose.setContent{TacticalTrackTheme{TacticalTrackApp(false)}}
  compose.onNodeWithText("Hola, Kevin · Supervisor").assertExists()
  compose.onNodeWithText("Historial",useUnmergedTree=true).performClick()
  compose.onNodeWithText("Historial de visitas").assertIsDisplayed()
  compose.onNodeWithText("Supervisor",useUnmergedTree=true).performClick()
  compose.onNodeWithText("Validación de supervisor").assertIsDisplayed()
  compose.onNodeWithText("Inicio",useUnmergedTree=true).performClick()
  compose.onNodeWithText("Registrar nueva visita",substring=true).performScrollTo().performClick()
  compose.onNodeWithText("Nueva visita").assertIsDisplayed()
  compose.onNodeWithContentDescription("Volver").performClick()
  compose.onNodeWithText("Cerrar sesión").performClick()
  compose.onNodeWithText("Correo electrónico").assertExists();coVerify{auth.logout()}
 }
 @Test fun operatorDoesNotSeeSupervisorTab(){
  user.value=AuthUser(2,"op@duoc.cl","Victor","OPERARIO",true,null)
  compose.setContent{TacticalTrackTheme{TacticalTrackApp(true)}}
  compose.onNodeWithText("Hola, Victor · Operario").assertExists()
  compose.onNodeWithText("Supervisor",substring=false).assertDoesNotExist()
 }
 @Test fun pendingPushIsConsumedAndRoutesToDetail(){
  // Evita llamadas a terceros mientras se comprueba la navegación de un aviso.
  val api=mockk<cl.duoc.tacticaltrack.data.remote.WeatherApi>()
  mockkObject(cl.duoc.tacticaltrack.data.remote.ApiFactory)
  every{cl.duoc.tacticaltrack.data.remote.ApiFactory.weather}returns api
  coEvery{api.current(any(),any(),any(),any())}throws java.io.IOException()
  var consumed=false
  compose.setContent{TacticalTrackTheme{TacticalTrackApp(false,1){consumed=true}}}
  compose.waitUntil(5000){consumed}
  compose.onNodeWithText("Detalle de visita").assertIsDisplayed()
 }
 @Test fun routesKeepVisitIdentifier(){
  assertEquals("detail/42",Screen.Detail.createRoute(42))
  assertEquals(7,setOf(Screen.Login.route,Screen.Home.route,Screen.History.route,Screen.Supervisor.route,Screen.NewVisit.route,Screen.Evidence.route,Screen.Detail.route).size)
 }
}
