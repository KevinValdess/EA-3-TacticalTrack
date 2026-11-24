package cl.duoc.tacticaltrack
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import cl.duoc.tacticaltrack.data.remote.*
import cl.duoc.tacticaltrack.push.*
import com.google.firebase.messaging.RemoteMessage
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.io.IOException
import java.util.concurrent.Executor
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=Application::class)
class PushAndroidTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private lateinit var api:DeviceApi
 @Before fun setup(){
  WorkManagerTestInitHelper.initializeTestWorkManager(context,Configuration.Builder().setExecutor(Executor{it.run()}).build())
  PushRegistration.preferences(context).edit().clear().commit()
  api=mockk(relaxed=true);mockkObject(ApiFactory);every{ApiFactory.devices}returns api
 }
 @After fun cleanup(){unmockkAll()}
 private fun worker()=TestListenableWorkerBuilder<DeviceRegistrationWorker>(context).build()
 @Test fun workerRequiresIdTokenAndSupervisor()=runBlocking {
  val prefs=PushRegistration.preferences(context);assertEquals(ListenableWorker.Result.failure(),worker().doWork())
  prefs.edit().putString("installationId","install").putBoolean("enabled",true).putBoolean("supervisor",true).commit()
  assertEquals(ListenableWorker.Result.retry(),worker().doWork());prefs.edit().putString("token","token").commit()
  assertEquals(ListenableWorker.Result.failure(),worker().doWork());coVerify(exactly=0){api.register(any(),any())}
 }
 @Test fun workerRegistersOrUnregistersAccordingToPreference()=runBlocking {
  val p=PushRegistration.preferences(context)
  p.edit().putString("installationId","install").putString("supervisorId","SUP-001").putString("token","token").putBoolean("enabled",true).putBoolean("supervisor",true).commit()
  assertEquals(ListenableWorker.Result.success(),worker().doWork());coVerify{api.register("install",any())}
  assertTrue(p.getString("status","")!!.contains("registrado"));p.edit().putBoolean("enabled",false).commit()
  assertEquals(ListenableWorker.Result.success(),worker().doWork());coVerify{api.unregister("install")}
 }
 @Test fun workerRetriesOnlyTransientFailures()=runBlocking {
  PushRegistration.preferences(context).edit().putString("installationId","install").commit()
  coEvery{api.unregister(any())}throws IOException("sin red");assertEquals(ListenableWorker.Result.retry(),worker().doWork())
  for(code in listOf(400,401,403,404,408,429,500)){
   coEvery{api.unregister(any())}throws httpError(code)
   assertEquals("HTTP $code",if(code in listOf(408,429,500))ListenableWorker.Result.retry() else ListenableWorker.Result.failure(),worker().doWork())
  }
 }
 @Test fun initializationCreatesChannelAndKeepsInstallationId(){
  PushRegistration.initialize(context);val p=PushRegistration.preferences(context);val id=p.getString("installationId",null)
  assertFalse(id.isNullOrBlank());PushRegistration.initialize(context);assertEquals(id,p.getString("installationId",null))
  assertNotNull(context.getSystemService(NotificationManager::class.java).getNotificationChannel(PushRegistration.CHANNEL))
  assertFalse(PushRegistration.configured(context));PushRegistration.refreshToken(context)
  PushRegistration.tokenChanged(context,"updated");assertEquals("updated",p.getString("token",null))
 }
 @Test fun rolesAndLogoutRemoveSupervisorSession()=runBlocking {
  PushRegistration.initialize(context);PushRegistration.setRole(context,"Supervisor","SUP-001");assertTrue(PushRegistration.supervisorSession.value)
  PushRegistration.setEnabled(context,false);assertFalse(PushRegistration.preferences(context).getBoolean("enabled",true))
  PushRegistration.setRole(context,"Operario");assertFalse(PushRegistration.supervisorSession.value)
  PushRegistration.endSession(context);assertFalse(PushRegistration.preferences(context).getBoolean("supervisor",true))
 }
 @Test fun messagesRequireEnabledSupervisorAndValidIdAndReplaceDuplicates(){
  PushRegistration.initialize(context)
  val controller=Robolectric.buildService(TacticalMessagingService::class.java).create();val service=controller.get()
  val manager=context.getSystemService(NotificationManager::class.java)
  fun send(id:String){service.onMessageReceived(RemoteMessage.Builder("test").addData("visitId",id).build())}
  send("42");assertEquals(0,shadowOf(manager).allNotifications.size)
  PushRegistration.preferences(context).edit().putBoolean("enabled",true).putBoolean("supervisor",true).commit()
  for(id in listOf("invalid","0","-1"))send(id);assertEquals(0,shadowOf(manager).allNotifications.size)
  send("42");send("42");assertEquals(1,shadowOf(manager).allNotifications.size)
  val notification=shadowOf(manager).allNotifications.single()
  assertEquals("Nueva visita registrada",notification.extras.getCharSequence("android.title"))
  assertEquals("42",shadowOf(notification.contentIntent).savedIntent.getStringExtra("visitId"))
  // El token se conserva aunque no esté configurado Firebase en esta prueba.
  PushRegistration.preferences(context).edit().putBoolean("enabled",false).commit()
  service.onNewToken("new-token");assertEquals("new-token",PushRegistration.preferences(context).getString("token",null))
  controller.destroy()
 }
}
