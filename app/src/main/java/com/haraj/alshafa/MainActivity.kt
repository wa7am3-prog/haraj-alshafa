package com.haraj.alshafa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Ad(val id: Int, val title: String, val price: String, val city: String, val category: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { HarajApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarajApp() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCat by remember { mutableStateOf("الكل") }
    val cats = listOf("الكل","أغنام","سيارات","عقار","دراجات")
    val ads = listOf(
        Ad(1,"غنم نعيمي 20 راس","150$ للراس","الشعفة","أغنام"),
        Ad(2,"كيا ريو 2015 نظيفة","5000$","دير الزور","سيارات"),
        Ad(3,"بيت عربي للايجار","100$ شهري","الشعفة","عقار"),
        Ad(4,"دراجة هوندا","800$","البوكمال","دراجات")
    )
    val filtered = if(selectedCat=="الكل") ads else ads.filter{it.category==selectedCat}

    Scaffold(
        topBar = { TopAppBar(title = { Text("حراج الشحفة", fontWeight=FontWeight.Bold) }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected=selectedTab==0, onClick={selectedTab=0}, label={Text("الرئيسية")}, icon={Text("🏠")})
                NavigationBarItem(selected=selectedTab==1, onClick={selectedTab=1}, label={Text("أقسام")}, icon={Text("📂")})
                NavigationBarItem(selected=selectedTab==2, onClick={selectedTab=2}, label={Text("أضف")}, icon={Text("➕")})
            }
        }
    ) { p ->
        Column(Modifier.padding(p).fillMaxSize()) {
            if(selectedTab==0){
                LazyRow(Modifier.padding(8.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    items(cats.size){ i ->
                        FilterChip(selected=cats[i]==selectedCat, onClick={selectedCat=cats[i]}, label={Text(cats[i])})
                    }
                }
                LazyColumn(Modifier.padding(12.dp)){
                    items(filtered){ ad ->
                        Card(Modifier.fillMaxWidth().padding(vertical=6.dp), shape=RoundedCornerShape(12.dp), elevation=CardDefaults.cardElevation(3.dp)){
                            Column(Modifier.padding(14.dp)){
                                Text(ad.title, fontWeight=FontWeight.Bold, fontSize=16.sp)
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween){
                                    Text(ad.price, color=MaterialTheme.colorScheme.primary, fontWeight=FontWeight.Bold)
                                    Text(ad.city, fontSize=12.sp)
                                }
                            }
                        }
                    }
                }
            } else if(selectedTab==1){
                LazyColumn(Modifier.padding(16.dp)){ items(cats){ c -> Card(Modifier.fillMaxWidth().padding(4.dp), onClick={selectedCat=c; selectedTab=0}){ Text(c, Modifier.padding(16.dp), fontWeight=FontWeight.Bold) } } }
            } else {
                Column(Modifier.padding(16.dp)){ Text("إضافة إعلان - الجزء الثاني مع Firebase", fontWeight=FontWeight.Bold); Spacer(Modifier.height(10.dp)); OutlinedTextField(value="", onValueChange={}, label={Text("عنوان الإعلان")}, modifier=Modifier.fillMaxWidth()); Button(onClick={}, Modifier.fillMaxWidth()){ Text("نشر قريباً") } }
            }
        }
    }
}
