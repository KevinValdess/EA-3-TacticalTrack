package cl.duoc.tacticaltrack
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cl.duoc.tacticaltrack.data.local.*
import cl.duoc.tacticaltrack.data.remote.AndroidEvidenceStore
import cl.duoc.tacticaltrack.data.repository.UserPreferencesRepository
import cl.duoc.tacticaltrack.util.*
import cl.duoc.tacticaltrack.viewmodel.SessionViewModelStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import java.io.File
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28], application=Application::class)
class PersistenceAndroidTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 @Test fun roomPreservesFieldsOrdersVisitsAndReplacesSnapshot()=runBlocking {
  val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).allowMainThreadQueries().build()
  try {
   val dao=db.visitDao();val old=sampleVisit().copy(id=10,timestamp=100,locationAccuracyMeters=null)
   val recent=sampleVisit().copy(id=11,timestamp=200,observations="Motor revisado",synced=true)
   assertEquals(10L,dao.insert(old));dao.insertAll(listOf(recent))
   assertEquals(listOf(recent,old),dao.observeAll().first());assertEquals(recent,dao.observeById(11).first())
   assertNull(dao.observeById(999).first());dao.delete(11);assertNull(dao.observeById(11).first())
   dao.replaceSnapshot(listOf(recent.copy(operatorName="Victor")))
   assertEquals(listOf(recent.copy(operatorName="Victor")),dao.observeAll().first())
   dao.replaceSnapshot(emptyList());assertTrue(dao.observeAll().first().isEmpty())
  }finally{db.close()}
 }
 @Test fun fileDatabaseSurvivesReopening()=runBlocking {
  val name="persistence-test.db";context.deleteDatabase(name)
  fun open()=Room.databaseBuilder(context,AppDatabase::class.java,name).allowMainThreadQueries().build()
  val first=open();first.visitDao().insert(sampleVisit());first.close();val second=open()
  try{assertEquals(sampleVisit(),second.visitDao().observeById(1).first())}
  finally{second.close();context.deleteDatabase(name)}
 }
 @Test fun evidenceRoundTripWritesBytesAndCanBeRemovedTwice()=runBlocking {
  val bytes=byteArrayOf(1,2,3,4);val store=AndroidEvidenceStore(context)
  val file=File(Uri.parse(store.persist(42,Base64.encodeToString(bytes,Base64.NO_WRAP))).path!!)
  assertArrayEquals(bytes,file.readBytes());store.remove(42);assertFalse(file.exists());store.remove(42)
 }
 @Test fun photoEncodingRejectsInvalidContentAndCompressesLargeImage()=runBlocking {
  val store=AndroidEvidenceStore(context)
  val invalid=File(context.cacheDir,"invalid.txt").apply{writeText("not an image")}
  try{store.encode(Uri.fromFile(invalid).toString());fail("Debe rechazar datos no fotográficos")}catch(_:IllegalArgumentException){}
  val photo=File(context.cacheDir,"source.png")
  Bitmap.createBitmap(1600,1300,Bitmap.Config.ARGB_8888).let{bitmap->photo.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
  val encoded=store.encode(Uri.fromFile(photo).toString())
  assertTrue(Base64.decode(encoded,Base64.NO_WRAP).isNotEmpty());assertTrue(encoded.length<=3_000_000)
 }
 @Test fun galleryCopiesFileAndCameraReturnsProviderUri(){
  val source=File(context.cacheDir,"gallery.jpg").apply{writeBytes(byteArrayOf(7,8,9))}
  val copied=copyImageToInternalStorage(context,Uri.fromFile(source));assertNotNull(copied)
  assertArrayEquals(source.readBytes(),context.contentResolver.openInputStream(copied!!)!!.use{it.readBytes()})
  val uri=createVisitImageUri(context);assertEquals("content",uri.scheme);assertEquals(context.packageName+".fileprovider",uri.authority)
  assertNull(copyImageToInternalStorage(context,Uri.parse("content://missing/image")))
 }
 @Test fun deniedGpsReturnsErrorAndCoarsePermissionIsAccepted(){
  shadowOf(context as Application).denyPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION)
  assertFalse(hasLocationPermission(context));var error=""
  requestCurrentLocation(context,{fail("Sin permiso no debe entregar ubicación")},{error=it})
  assertTrue(error.contains("permiso"))
  shadowOf(context as Application).grantPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION);assertTrue(hasLocationPermission(context))
 }
 @Test fun corruptSessionIsDiscarded(){
  val store=EncryptedSessionTokenStore(context);store.clear();assertNull(store.read())
  context.getSharedPreferences("auth_session",Context.MODE_PRIVATE).edit().putString("token","invalid").commit()
  assertNull(store.read());assertFalse(context.getSharedPreferences("auth_session",Context.MODE_PRIVATE).contains("token"))
 }
 @Test fun logoutClearsScopedViewModels(){
  var cleared=false;val vm=object:ViewModel(){override fun onCleared(){cleared=true}}
  val owner=SessionViewModelStore();owner.viewModelStore.put("test",vm);owner.clearSession();assertTrue(cleared);owner.clearSession()
 }
 @Test fun rememberedEmailIsReadByNewRepository()=runBlocking {
  UserPreferencesRepository(context).saveLastUsername("op@duoc.cl")
  assertEquals("op@duoc.cl",UserPreferencesRepository(context).lastUsername.first())
 }
}
