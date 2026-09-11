package xyz.amberwire.mobilenode.pixel

import android.Manifest
import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{AmberwireTheme{NodeScreen()}}}
 @Composable private fun NodeScreen(){val store=remember{NodeStore(this)};var code by remember{mutableStateOf("")};var name by remember{mutableStateOf(store.displayName)};var status by remember{mutableStateOf(if(store.enrolled)"Enrolled as ${store.nodeId}" else "Enrollment required")};var active by remember{mutableStateOf(store.active)};var queue by remember{mutableIntStateOf(EventQueue(this).size())};val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){grants->if(grants[Manifest.permission.ACCESS_FINE_LOCATION]==true){startNode();active=true;status="Sensor route active"}}
  Surface(Modifier.fillMaxSize(),color=Color(0xFF09090B)){Column(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Text("AMBERWIRE MOBILITY PLANE",color=Color(0xFF22D3EE),style=MaterialTheme.typography.labelSmall);Text("Mobile Node",style=MaterialTheme.typography.headlineLarge);Text("Sensor Protocol v0.2",color=Color.Gray);Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF151518)),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(18.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(status);Text("Encrypted queue: $queue events",color=Color.LightGray);if(!store.enrolled){OutlinedTextField(code,{code=it},label={Text("Enrollment code")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(name,{name=it},label={Text("Node name")},singleLine=true,modifier=Modifier.fillMaxWidth());Button(onClick={lifecycleScope.launch{status="Enrolling…";try{val result=withContext(Dispatchers.IO){AmberwireApi.enroll(code,name)};store.nodeId=result.getString("node_id");store.token=result.getString("device_token");store.displayName=name;status="Enrolled as ${store.nodeId}"}catch(e:Exception){status=e.message?:"Enrollment failed"}}},modifier=Modifier.fillMaxWidth()){Text("Enroll Android Node")}}else{Button(onClick={if(active){{stopService(Intent(this@MainActivity,SensorService::class.java));store.active=false;active=false;status="Sensor route paused"}}else{{permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.POST_NOTIFICATIONS))}}},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=if(active)Color(0xFF7F1D1D) else Color(0xFF06B6D4))){Text(if(active)"Pause Sensor Route" else "Start Sensor Route")};OutlinedButton(onClick={WorkManager.getInstance(this@MainActivity).enqueue(OneTimeWorkRequestBuilder<UploadWorker>().build());queue=EventQueue(this@MainActivity).size();status="Sync requested"},modifier=Modifier.fillMaxWidth()){Text("Sync Now")}}}}
   Spacer(Modifier.weight(1f));Text("Raw high-frequency readings remain on-device. Amberwire receives inventories, snapshots and material events.",color=Color.Gray,style=MaterialTheme.typography.bodySmall)}}
 }
 private fun startNode(){NodeStore(this).active=true;ContextCompat.startForegroundService(this,Intent(this,SensorService::class.java))}
}
@Composable fun AmberwireTheme(content:@Composable()->Unit){MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF22D3EE),background=Color(0xFF09090B),surface=Color(0xFF151518)),content=content)}
