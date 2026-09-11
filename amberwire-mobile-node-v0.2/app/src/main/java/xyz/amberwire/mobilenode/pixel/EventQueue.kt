package xyz.amberwire.mobilenode.pixel

import android.content.*
import android.database.sqlite.*
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

data class QueuedEvent(val id:String,val body:String)
class EventQueue(context:Context):SQLiteOpenHelper(context,"amberwire_queue.db",null,2){
 override fun onCreate(db:SQLiteDatabase){db.execSQL("CREATE TABLE events(id TEXT PRIMARY KEY, body TEXT NOT NULL, created INTEGER NOT NULL)")}
 override fun onUpgrade(db:SQLiteDatabase,old:Int,new:Int){if(old<2){db.execSQL("DROP TABLE IF EXISTS events");onCreate(db)}}
 fun add(type:String,plain:String,payload:JSONObject,lat:Double?=null,lng:Double?=null,accuracy:Float?=null,severity:String="info"){
  val id=UUID.randomUUID().toString();val o=JSONObject().put("event_id",id).put("event_type",type).put("observed_at",Instant.now().toString()).put("client_sequence",System.currentTimeMillis()).put("severity",severity).put("plain_english",plain).put("payload",payload)
  lat?.let{o.put("latitude",it)};lng?.let{o.put("longitude",it)};accuracy?.let{o.put("accuracy_m",it)}
  writableDatabase.insert("events",null,ContentValues().apply{put("id",id);put("body",o.toString());put("created",System.currentTimeMillis())})
 }
 fun next(limit:Int=50):List<QueuedEvent>{val out=mutableListOf<QueuedEvent>();readableDatabase.rawQuery("SELECT id,body FROM events ORDER BY created LIMIT ?",arrayOf(limit.toString())).use{while(it.moveToNext())out+=QueuedEvent(it.getString(0),it.getString(1))};return out}
 fun remove(ids:List<String>){if(ids.isEmpty())return;writableDatabase.delete("events","id IN (${ids.joinToString(","){"?"}})",ids.toTypedArray())}
 fun size():Int=readableDatabase.rawQuery("SELECT COUNT(*) FROM events",null).use{it.moveToFirst();it.getInt(0)}
}
