package xyz.amberwire.mobilenode.pixel

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class NodeStore(context:Context) {
    private val prefs=EncryptedSharedPreferences.create(context,"amberwire_secure",MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    var nodeId:String get()=prefs.getString("node_id","")!! set(v)=prefs.edit().putString("node_id",v).apply()
    var token:String get()=prefs.getString("device_token","")!! set(v)=prefs.edit().putString("device_token",v).apply()
    var displayName:String get()=prefs.getString("display_name","Pixel Mobile Node #001")!! set(v)=prefs.edit().putString("display_name",v).apply()
    var active:Boolean get()=prefs.getBoolean("active",false) set(v)=prefs.edit().putBoolean("active",v).apply()
    val enrolled:Boolean get()=nodeId.isNotBlank()&&token.isNotBlank()
    fun clear(){prefs.edit().clear().apply()}
}
