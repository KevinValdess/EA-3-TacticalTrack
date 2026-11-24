package cl.duoc.tacticaltrack
import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cl.duoc.tacticaltrack.data.local.VisitEntity
import cl.duoc.tacticaltrack.data.remote.*
import cl.duoc.tacticaltrack.model.TestCatalog
import cl.duoc.tacticaltrack.ui.components.*
import cl.duoc.tacticaltrack.ui.screens.*
import cl.duoc.tacticaltrack.ui.theme.TacticalTrackTheme
import cl.duoc.tacticaltrack.viewmodel.*
import io.mockk.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=Application::class)
class ScreensAndroidTest {
 @get:Rule val compose=createComposeRule()
 private val visits=MutableStateFlow<List<VisitEntity>>(emptyList())
 private val form=MutableStateFlow(VisitFormUiState())
 private val remote=MutableStateFlow(RemoteUiState())
 private val catalog=MutableStateFlow(CatalogUiState(TestCatalog.points,TestCatalog.assets,false))
 private lateinit var vm:VisitViewModel
 @Before fun setup(){
  vm=mockk(relaxed=true);every{vm.visits}returns visits;every{vm.form}returns form
  every{vm.remote}returns remote;every{vm.catalogState}returns catalog
  every{vm.operatorName}returns MutableStateFlow("Victor");every{vm.operatorRole}returns MutableStateFlow("Operario")
  every{vm.visitTypes}returns listOf("Mantenimiento","Venta","Transporte");every{vm.equipmentStatuses}returns listOf("Operativo","Con falla")
 }
 @After fun cleanup(){unmockkAll()}
 @Test fun loginForwardsInputsDisplaysErrorsAndRetries(){
  val state=MutableStateFlow(LoginUiState(busy=false));val login=mockk<LoginViewModel>(relaxed=true);every{login.uiState}returns state
  compose.setContent{TacticalTrackTheme{LoginScreen(login)}}
  compose.onNodeWithText("Correo electrónico").performTextInput("ad@duoc.cl");compose.onNodeWithText("Contraseña").performTextInput("Prueba")
  compose.onNodeWithText("Ingresar").performClick()
  verify{login.onUsernameChange("ad@duoc.cl");login.onPasswordChange("Prueba");login.login()}
  compose.runOnIdle{state.value=LoginUiState(busy=false,usernameError="Correo requerido",passwordError="Clave requerida",error="Sin conexión")}
  compose.onNodeWithText("Correo requerido").assertIsDisplayed();compose.onNodeWithText("Clave requerida").assertIsDisplayed()
  compose.onNodeWithText("Reintentar conexión").performClick();verify{login.restoreSession()}
  compose.runOnIdle{state.value=LoginUiState(busy=true)};compose.onNodeWithText("Correo electrónico").assertIsNotEnabled()
 }
 @Test fun homeShowsNameCountsBothLayoutsAndDispatchesActions(){
  visits.value=listOf(sampleVisit().copy(synced=false),sampleVisit().copy(id=2,presenceVerified=false))
  var newVisit=false;var logout=false;val expanded=mutableStateOf(false)
  compose.setContent{TacticalTrackTheme{HomeScreen(vm,expanded.value,{newVisit=true},{logout=true})}}
  compose.onNodeWithText("Hola, Victor · Operario").assertIsDisplayed();compose.onNodeWithText("Visitas registradas").assertExists()
  compose.onNodeWithText("Registrar nueva visita",substring=true).performScrollTo().performClick();assertTrue(newVisit)
  compose.onNodeWithText("Actualizar desde servidor",substring=true).performScrollTo().performClick();verify{vm.refreshRemote()}
  compose.onNodeWithText("Cerrar sesión").performClick();assertTrue(logout)
  compose.runOnIdle{expanded.value=true;remote.value=RemoteUiState(error="Error del servidor",message="Listo")}
  compose.onNodeWithText("Visitas").assertExists();compose.onNodeWithText("Error del servidor").assertExists()
  compose.runOnIdle{remote.value=RemoteUiState(busy=true)};compose.onNodeWithText("Actualizando…").assertIsNotEnabled()
 }
 @Test fun historyHandlesEmptyAndSelectsVisit(){
  var id=0L;compose.setContent{TacticalTrackTheme{HistoryScreen(vm){id=it}}}
  compose.onNodeWithText("Aún no hay visitas registradas.").assertIsDisplayed()
  compose.runOnIdle{visits.value=listOf(sampleVisit().copy(synced=true))}
  compose.onNodeWithText("FRZ-001 · Freezer").performClick();assertEquals(1L,id);compose.onNodeWithText("Sincronizada").assertExists()
 }
 @Test fun supervisorHandlesEmptyErrorAndBothDecisions(){
  compose.setContent{TacticalTrackTheme{SupervisorScreen(vm)}}
  compose.onNodeWithText("No existen registros para revisar.").assertIsDisplayed()
  compose.onNodeWithText("Actualizar").performClick();verify{vm.refreshRemote()}
  compose.runOnIdle{remote.value=RemoteUiState(error="Sin servidor")};compose.onNodeWithText("Sin servidor").assertIsDisplayed()
  compose.runOnIdle{remote.value=RemoteUiState();visits.value=listOf(sampleVisit())}
  compose.onNodeWithText("Aprobar",substring=true).performClick();verify{vm.approveVisit(1)}
  compose.onNodeWithText("Rechazar",substring=true).performClick();verify{vm.rejectVisit(1)}
  compose.runOnIdle{visits.value=listOf(sampleVisit().copy(presenceVerified=false))};compose.onNodeWithText("Ubicación fuera del radio de 150 m").assertExists()
 }
 @Test fun newVisitChecksFormAndForwardsSelection(){
  var back=false;var next=false;every{vm.validateBeforeEvidence()}returns false
  compose.setContent{TacticalTrackTheme{NewVisitScreen(vm,{back=true},{next=true})}}
  compose.onNodeWithContentDescription("Volver").performClick();assertTrue(back)
  compose.onAllNodesWithText("Seleccionar")[0].performClick();compose.onNodeWithText("Punto Central").performClick();verify{vm.onPointSelected("PV-001")}
  compose.runOnIdle{form.value=VisitFormUiState(pointId="PV-001",assetId="ACT-001",visitType="Mantenimiento",equipmentStatus="Operativo")}
  compose.onNodeWithText("Observaciones").performScrollTo().performTextInput("Motor revisado");verify{vm.onObservationsChange("Motor revisado")}
  compose.onNodeWithText("Continuar a evidencia").performScrollTo().performClick();assertFalse(next)
  every{vm.validateBeforeEvidence()}returns true;compose.onNodeWithText("Continuar a evidencia").performClick();assertTrue(next)
  compose.runOnIdle{catalog.value=CatalogUiState(busy=true,error="Catálogo no disponible");form.value=VisitFormUiState(errors=VisitErrors(observations="Explica la falla",point="Selecciona un punto"))}
  compose.onNodeWithText("Catálogo no disponible").assertExists();compose.onNodeWithText("Explica la falla").assertExists()
 }
 @Test fun editingValidatesSendsValuesAndDisablesWhileSaving(){
  var saved:Pair<String,String>?=null;var dismissed=false;val busy=mutableStateOf(false)
  compose.setContent{TacticalTrackTheme{EditVisitDialog("Con falla","",busy.value,"Error remoto",{dismissed=true},{s,o->saved=s to o})}}
  compose.onNodeWithText("Guardar").assertIsNotEnabled();compose.onNodeWithText("Observaciones").performTextInput("Motor detenido")
  compose.onNodeWithText("Guardar").performClick();assertEquals("Con falla" to "Motor detenido",saved)
  compose.onNodeWithText("Operativo").performClick();compose.onNodeWithText("Guardar").performClick();assertEquals("Operativo",saved!!.first)
  compose.runOnIdle{busy.value=true};compose.onNodeWithText("Guardar").assertIsNotEnabled()
  compose.runOnIdle{busy.value=false};compose.onNodeWithText("Cancelar").performClick();assertTrue(dismissed)
 }
 @Test fun selectorAndStatusChipsShowBothConditions(){
  val selected=mutableStateOf("");val verified=mutableStateOf(false)
  compose.setContent{TacticalTrackTheme{Column{
   SelectorField("Punto",selected.value,listOf("p1" to "Centro"),"Elige un punto",{selected.value=it})
   VisitStatusChips(verified.value,"Pendiente",verified.value)
  }}}
  compose.onNodeWithText("GPS fuera de radio").assertExists();compose.onNodeWithText("Local").assertExists()
  compose.onNodeWithText("Seleccionar").performClick();compose.onNodeWithText("Centro").performClick();assertEquals("p1",selected.value)
  compose.runOnIdle{verified.value=true};compose.onNodeWithText("GPS verificado").assertExists();compose.onNodeWithText("Sincronizada").assertExists()
 }
 @Test fun evidenceShowsPhotoCoordinatesErrorsAndProgress(){
  var back=false;compose.setContent{TacticalTrackTheme{EvidenceScreen(vm,{back=true},{})}}
  compose.onNodeWithContentDescription("Volver").performClick();assertTrue(back)
  compose.onNodeWithText("Guardar visita",substring=true).performScrollTo().performClick();verify{vm.saveVisit(any())}
  compose.runOnIdle{form.value=VisitFormUiState(photoUri="file:///missing.jpg",latitude=-33.5,longitude=-70.7,locationAccuracyMeters=5f,distanceToPointMeters=10f,presenceVerified=true,locationMessage="GPS válido",saveSuccess=true,errors=VisitErrors(photo="Foto requerida",location="GPS requerido"))}
  compose.onNodeWithContentDescription("Evidencia de la visita").assertExists();compose.onNodeWithText("Resultado: presencia compatible").assertExists()
  compose.onNodeWithText("Visita guardada correctamente en Room.").assertExists()
  compose.runOnIdle{form.value=form.value.copy(presenceVerified=false,isLocating=true,isSaving=true)}
  compose.onNodeWithText("Resultado: fuera del radio configurado").assertExists();compose.onNodeWithText("Guardando...",substring=true).assertIsNotEnabled()
 }
 @Test fun detailHandlesMissingVisitAndEditDeleteCallbacks(){
  val detail=MutableStateFlow<VisitDetailUiState>(VisitDetailUiState.Loading);every{vm.observeVisitDetail(1)}returns detail
  mockkObject(ApiFactory);val weather=mockk<WeatherApi>();every{ApiFactory.weather}returns weather
  coEvery{weather.current(any(),any(),any(),any())}returns WeatherResponse(CurrentWeather(15.0,3,"2026-11-24T12:00"))
  var back=false;compose.setContent{TacticalTrackTheme{VisitDetailScreen(1,vm,{back=true})}}
  compose.onNodeWithText("Cargando visita...").assertExists()
  compose.runOnIdle{detail.value=VisitDetailUiState.NotFound};compose.onNodeWithText("Visita no encontrada").assertExists()
  compose.runOnIdle{detail.value=VisitDetailUiState.Found(sampleVisit())};compose.onNodeWithText("FRZ-001 · Freezer").assertExists()
  compose.onNodeWithText("Editar visita").performClick();compose.onNodeWithText("Guardar").performClick();verify{vm.editVisit(1,any(),any(),any())}
  compose.onNodeWithText("Cancelar").performClick();compose.onNodeWithText("Eliminar visita").performClick();compose.onNodeWithText("¿Eliminar visita?").assertExists()
  compose.onNodeWithText("Cancelar").performClick();compose.onNodeWithText("Eliminar visita").performClick()
  compose.onNodeWithText("Eliminar",substring=false).performClick();verify{vm.deleteVisit(1,any())}
  compose.onNodeWithContentDescription("Volver").performClick();assertTrue(back)
 }
}
