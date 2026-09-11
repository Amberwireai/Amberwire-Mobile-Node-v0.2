package xyz.amberwire.mobilenode.pixel

import android.content.Context
import androidx.work.*

class UploadWorker(ctx:Context,params:WorkerParameters):CoroutineWorker(ctx,params){
 override suspend fun doWork():Result{val store=NodeStore(applicationContext);if(!store.enrolled)return Result.success();val q=EventQueue(applicationContext);val rows=q.next();if(rows.isEmpty())return Result.success();return try{AmberwireApi.upload(store,rows);q.remove(rows.map{it.id});Result.success()}catch(e:Exception){Result.retry()}}
}
