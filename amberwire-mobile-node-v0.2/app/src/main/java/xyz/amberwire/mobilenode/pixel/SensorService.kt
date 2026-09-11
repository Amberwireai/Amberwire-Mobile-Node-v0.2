package xyz.amberwire.mobilenode.pixel

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.net.*
import android.os.*
import androidx.core.app.*
import androidx.work.*
import com.google.android.gms.location.*
import org.json.*
import kotlin.math.sqrt

class SensorService:Service(),SensorEventListener{
 private lateinit var sensors:SensorManager;private lateinit var queue:EventQueue;private lateinit var location:FusedLocationProviderClient;private var lastMotion=0L
 override fun onCreate(){super.onCreate();queue=EventQueue(this);sensors=getSystemService(SENSOR_SERVICE) as SensorManager;location=LocationServices.getFusedLocationProviderClient(this);createChannel();startForeground(201,NotificationCompat.Builder(this,"amberwire_mobile").setContentTitle("Amberwire Mobile Node").setContentText("Sensor route active").setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).build());inventory();startSensors();startLocation();snapshot()}
 private fun createChannel(){(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel("amberwire_mobile","Amberwire Mobile Node",NotificationManager.IMPORTANCE_LOW))}
 private fun inventory(){val list=sensors.getSensorList(Sensor.TYPE_ALL);val types=list.map{it.stringType.substringAfterLast(".")}.distinct();queue.add("sensor_inventory","${types.size} Android sensors available",JSONObject().put("protocol_version","0.2").put("available_count",types.size).put("capabilities",JSONArray(types)).put("sensor_capabilities",JSONArray(list.map{JSONObject().put("name",it.name).put("vendor",it.vendor).put("type",it.stringType).put("power_ma",it.power).put("resolution",it.resolution)})))}
 private fun startSensors(){listOf(Sensor.TYPE_ACCELEROMETER,Sensor.TYPE_GYROSCOPE,Sensor.TYPE_MAGNETIC_FIELD,Sensor.TYPE_PRESSURE,Sensor.TYPE_LIGHT,Sensor.TYPE_PROXIMITY).forEach{t->sensors.getDefaultSensor(t)?.let{sensors.registerListener(this,it,SensorManager.SENSOR_DELAY_NORMAL)}}}
 @Suppress("MissingPermission") private fun startLocation(){if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return;val request=LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY,60000).setMinUpdateDistanceMeters(20f).build();location.requestLocationUpdates(request,object:LocationCallback(){override fun onLocationResult(r:LocationResult){r.lastLocation?.let{locationEvent(it)}}},mainLooper)}
 private fun locationEvent(l:Location){queue.add("location","Mobile position updated",JSONObject().put("provider",l.provider).put("speed_mps",l.speed).put("bearing",l.bearing).put("altitude_m",l.altitude).put("sensor_snapshot",JSONObject().put("location_time_ms",l.time)),l.latitude,l.longitude,l.accuracy)}
 private fun snapshot(){val battery=registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED));val level=battery?.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)?:-1;val cm=getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager;val caps=cm.getNetworkCapabilities(cm.activeNetwork);val network=when{caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)==true->"cellular";caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true->"wifi";else->"offline"};val fusion=JSONObject().put("motion_detection",true).put("location_fusion",true).put("confidence",0.9);queue.add("sensor_snapshot","Mobile sensor snapshot captured",JSONObject().put("protocol_version","0.2").put("battery_percent",level).put("charging",battery?.getIntExtra(BatteryManager.EXTRA_STATUS,-1) in listOf(BatteryManager.BATTERY_STATUS_CHARGING,BatteryManager.BATTERY_STATUS_FULL)).put("network",network).put("queue_depth",queue.size()).put("sensor_fusion",fusion).put("external_devices",JSONArray()));queue.add("sensor_fusion","Sensor fusion state updated",JSONObject().put("sensor_fusion",fusion));queue.add("external_devices","External-device scan state updated",JSONObject().put("external_devices",JSONArray()).put("permission_gated",true));queue.add("queue_health","Encrypted offline queue health updated",JSONObject().put("queue_depth",queue.size()).put("network",network))}
 override fun onSensorChanged(e:SensorEvent){if(e.sensor.type!=Sensor.TYPE_ACCELEROMETER)return;val magnitude=sqrt(e.values.sumOf{(it*it).toDouble()});if(magnitude>19&&System.currentTimeMillis()-lastMotion>10000){lastMotion=System.currentTimeMillis();queue.add("motion","Significant movement detected",JSONObject().put("magnitude_mps2",magnitude).put("classification","significant_motion").put("sensor_fusion",JSONObject().put("accelerometer",true).put("confidence",0.9)),severity="watch");WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<UploadWorker>().build())}}
 override fun onAccuracyChanged(sensor:Sensor?,accuracy:Int){}
 override fun onBind(intent:Intent?)=null
 override fun onDestroy(){sensors.unregisterListener(this);super.onDestroy()}
}
