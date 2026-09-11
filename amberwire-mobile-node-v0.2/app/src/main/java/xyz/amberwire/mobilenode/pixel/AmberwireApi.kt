package xyz.amberwire.mobilenode.pixel

import android.os.Build
import org.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object AmberwireApi {
 private fun post(path:String,body:JSONObject):JSONObject{val c=(URL(BuildConfig.AMBERWIRE_URL+path).openConnection() as HttpURLConnection).apply{requestMethod="POST";connectTimeout=15000;readTimeout=20000;doOutput=true;setRequestProperty("Content-Type","application/json");setRequestProperty("Accept","application/json")};c.outputStream.use{it.write(body.toString().toByteArray())};val stream=if(c.responseCode in 200..299)c.inputStream else c.errorStream;val text=stream?.bufferedReader()?.readText().orEmpty();if(c.responseCode !in 200..299)throw IllegalStateException("HTTP ${c.responseCode}: $text");return JSONObject(text)}
 fun enroll(code:String,name:String):JSONObject=post("/functions/node-enroll",JSONObject().put("enrollment_code",code).put("display_name",name).put("platform","Android/${Build.VERSION.SDK_INT}").put("architecture",Build.SUPPORTED_ABIS.firstOrNull()?:"unknown").put("agent_version","0.2.0").put("hostname_hash",sha(Build.MODEL+Build.FINGERPRINT)))
 fun upload(store:NodeStore,events:List<QueuedEvent>):JSONObject=post("/functions/mobile-node-ingest",JSONObject().put("protocol_version","0.2").put("node_id",store.nodeId).put("device_token",store.token).put("events",JSONArray(events.map{JSONObject(it.body)})))
 private fun sha(v:String)=MessageDigest.getInstance("SHA-256").digest(v.toByteArray()).joinToString(""){"%02x".format(it)}
}
