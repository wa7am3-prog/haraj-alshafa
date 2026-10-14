package com.haraj.alshafa

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Ad(var id:String="", val title:String="", val price:String="", val city:String="", val phone:String="", val cat:String="", val desc:String="", val uid:String="", val userName:String="", val time:Long=System.currentTimeMillis())
data class Comment(val text:String="", val userName:String="", val uid:String="", val time:Long=System.currentTimeMillis())
data class ChatMsg(val text:String="", val fromUid:String="", val time:Long=System.currentTimeMillis())

class MainActivity : Activity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentScreen = "home"
    private lateinit var contentFrame: FrameLayout
    private lateinit var adapter: AdAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (auth.currentUser == null) showAuth() else showMain()
    }

    private fun showAuth(){
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(60,0,60,0); background=GradientDrawable().apply { colors=intArrayOf(Color.parseColor("#050A14"), Color.parseColor("#0D1B2A")); orientation=GradientDrawable.Orientation.TOP_BOTTOM } }
        root.addView(TextView(this).apply { text="⚜️ حراج الشعفة ⚜️\nالأسطورة"; textSize=28f; setTextColor(Color.parseColor("#FFD700")); typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(0,0,0,60) })
        val card = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(50,50,50,50); background=GradientDrawable().apply { cornerRadius=40f; setColor(Color.parseColor("#162032")); setStroke(2, Color.parseColor("#FFD700")) } }
        val eName = inp("اسمك (للتسجيل)"); val eEmail = inp("البريد"); val ePass = inp("كلمة السر").apply { inputType=129 }
        val btnLogin = btn("✨ دخول أسطوري","#FFD700","#000000") {
            if(eEmail.text.isEmpty()||ePass.text.isEmpty()){ toast("عبي البيانات"); return@btn }
            auth.signInWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener { showMain() }.addOnFailureListener { toast(it.message?:"خطأ") }
        }
        val btnReg = btn("👑 إنشاء حساب جديد","#FF6B00","#FFFFFF") {
            if(eEmail.text.isEmpty()||ePass.text.isEmpty()||eName.text.isEmpty()){ toast("عبي الكل"); return@btn }
            auth.createUserWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener {
                val m = mapOf("name" to eName.text.toString(), "email" to eEmail.text.toString(), "phone" to "", "time" to System.currentTimeMillis())
                db.collection("users").document(it.user!!.uid).set(m); showMain()
            }.addOnFailureListener { toast(it.message?:"خطأ") }
        }
        card.addView(eName); card.addView(eEmail); card.addView(ePass); card.addView(btnLogin); card.addView(btnReg)
        root.addView(card); setContentView(root)
    }

    private fun showMain(){
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#070D17")) }
        contentFrame = FrameLayout(this).apply { layoutParams=LinearLayout.LayoutParams(-1,-1).apply { weight=1f } }
        root.addView(contentFrame)
        root.addView(createBottomBar(root))
        setContentView(root)
        showHomeScreen()
    }

    private fun createBottomBar(root: LinearLayout): LinearLayout {
        val bottom = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(10,12,10,25); gravity=Gravity.CENTER; background=GradientDrawable().apply { setColor(Color.parseColor("#0D1B2A")) } }
        fun item(icon:String, label:String, id:String): LinearLayout {
            val l = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; layoutParams=LinearLayout.LayoutParams(0,-2).apply { weight=1f } }
            l.addView(TextView(this).apply { text=icon; textSize=22f; gravity=Gravity.CENTER; setTextColor(if(currentScreen==id) Color.parseColor("#FFD700") else Color.parseColor("#6A7A8A")) })
            l.addView(TextView(this).apply { text=label; textSize=10f; gravity=Gravity.CENTER; setTextColor(if(currentScreen==id) Color.parseColor("#FFD700") else Color.parseColor("#6A7A8A")); typeface=Typeface.DEFAULT_BOLD })
            l.setOnClickListener { currentScreen=id; when(id){ "home"->showHomeScreen(); "my"->showMyAds(); "msg"->showMessages(); "profile"->showProfile() }; root.removeView(bottom); root.addView(createBottomBar(root)) }
            return l
        }
        bottom.addView(item("🏠","الرئيسية","home")); bottom.addView(item("📦","إعلاناتي","my"))
        bottom.addView(TextView(this).apply { text="＋"; textSize=26f; gravity=Gravity.CENTER; setTextColor(Color.BLACK); setPadding(0,15,0,15); background=GradientDrawable().apply { shape=GradientDrawable.OVAL; colors=intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FF6B00")) }; layoutParams=LinearLayout.LayoutParams(120,120).apply { setMargins(10,0,10,0) }; setOnClickListener { showAddDialog() } })
        bottom.addView(item("💬","رسائلي","msg")); bottom.addView(item("👤","حسابي","profile"))
        return bottom
    }

    private fun showHomeScreen(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        lay.addView(TextView(this).apply { text="👑 حراج الشعفة الأسطوري"; textSize=18f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(40,80,40,30); background=GradientDrawable().apply { colors=intArrayOf(Color.parseColor("#0D1B2A"), Color.parseColor("#1B2A41")) } })
        val filterScroll = HorizontalScrollView(this).apply { setPadding(0,15,0,5) }
        val filterRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(20,0,20,0) }
        listOf("الكل","سيارات","عقار","مواشي","أجهزة","أثاث","عام").forEach { cat ->
            val chip = TextView(this).apply { text=cat; setPadding(38,14,38,14); textSize=12f; typeface=Typeface.DEFAULT_BOLD; setTextColor(if(cat=="الكل") Color.BLACK else Color.WHITE); background=GradientDrawable().apply { cornerRadius=50f; setColor(if(cat=="الكل") Color.parseColor("#FFD700") else Color.parseColor("#162032")) }; layoutParams=LinearLayout.LayoutParams(-2,-2).apply { setMargins(10,0,10,0) }; setOnClickListener { loadAds(cat) } }
            filterRow.addView(chip)
        }
        filterScroll.addView(filterRow); lay.addView(filterScroll)
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        adapter = AdAdapter(mutableListOf()) { ad -> showAdDetail(ad) }; rv.adapter = adapter
        lay.addView(rv, LinearLayout.LayoutParams(-1,-1).apply { weight=1f })
        contentFrame.removeAllViews(); contentFrame.addView(lay); loadAds("الكل")
    }

    private fun loadAds(cat:String){
        val q = if(cat=="الكل") db.collection("ads").orderBy("time", Query.Direction.DESCENDING) else db.collection("ads").whereEqualTo("cat",cat).orderBy("time", Query.Direction.DESCENDING)
        q.addSnapshotListener { snap,_ -> val list = snap?.documents?.map { d-> val a=d.toObject(Ad::class.java)!!; a.id=d.id; a }?.toMutableList()?: mutableListOf(); adapter.update(list) }
    }

    private fun showAdDetail(ad:Ad){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,20,30,20) }
        lay.addView(TextView(this).apply { text="⚜️ ${ad.cat} • ${ad.city}"; textSize=12f; setTextColor(Color.parseColor("#FFD700")) })
        lay.addView(TextView(this).apply { text=ad.title; textSize=20f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(0,15,0,10) })
        lay.addView(TextView(this).apply { text="💰 ${ad.price}\n${ad.desc}\n\n👤 ${ad.userName}"; textSize=14f; setTextColor(Color.parseColor("#CCCCCC")) })
        lay.addView(btn("💬 مراسلة داخل التطبيق","#162032","#FFD700") { openChat(ad) })
        lay.addView(btn("💚 واتساب فوري","#25D366","#FFF") { try{ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${ad.phone}")))}catch(_:Exception){} })
        lay.addView(btn("📞 اتصال","#FF6B00","#FFF") { try{ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.phone}")))}catch(_:Exception){} })
        lay.addView(TextView(this).apply { text="\n💬 التعليقات:"; textSize=16f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD })
        val rvC = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity); layoutParams=LinearLayout.LayoutParams(-1,400) }
        val cList = mutableListOf<Comment>(); val cAdapter = CommentAdapter(cList); rvC.adapter=cAdapter; lay.addView(rvC)
        db.collection("ads").document(ad.id).collection("comments").orderBy("time").addSnapshotListener { snap,_ -> cList.clear(); snap?.forEach { cList.add(it.toObject(Comment::class.java)) }; cAdapter.notifyDataSetChanged() }
        val eComment = inp("اكتب تعليق فخم..."); val btnSend = btn("إرسال التعليق ✨","#FFD700","#000") {
            if(eComment.text.isEmpty()) return@btn
            val userName = auth.currentUser?.email?.substringBefore("@")?:"مستخدم"
            val c = Comment(eComment.text.toString(), userName, auth.currentUser!!.uid)
            db.collection("ads").document(ad.id).collection("comments").add(c); eComment.setText("")
        }
        lay.addView(eComment); lay.addView(btnSend)
        val scroll = ScrollView(this).apply { addView(lay) }
        AlertDialog.Builder(this).setView(scroll).setPositiveButton("إغلاق",null).show()
    }

    private fun openChat(ad:Ad){
        val myUid = auth.currentUser!!.uid; val otherUid = ad.uid; if(myUid==otherUid){ toast("هذا إعلانك!"); return }
        val chatId = if(myUid < otherUid) "${myUid}_${otherUid}_${ad.id}" else "${otherUid}_${myUid}_${ad.id}"
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(20,10,20,10) }
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity); layoutParams=LinearLayout.LayoutParams(-1,500) }
        val msgs = mutableListOf<ChatMsg>(); val adp = ChatAdapter(msgs, myUid); rv.adapter=adp
        db.collection("chats").document(chatId).collection("msgs").orderBy("time").addSnapshotListener { snap,_ -> msgs.clear(); snap?.forEach { msgs.add(it.toObject(ChatMsg::class.java)) }; adp.notifyDataSetChanged() }
        val eMsg = inp("رسالتك..."); val btnS = btn("إرسال 🚀","#FFD700","#000") {
            if(eMsg.text.isEmpty()) return@btn
            db.collection("chats").document(chatId).set(mapOf("adId" to ad.id, "adTitle" to ad.title, "user1" to myUid, "user2" to otherUid, "last" to eMsg.text.toString(), "time" to System.currentTimeMillis()))
            db.collection("chats").document(chatId).collection("msgs").add(ChatMsg(eMsg.text.toString(), myUid)); eMsg.setText("")
        }
        lay.addView(rv); lay.addView(eMsg); lay.addView(btnS)
        AlertDialog.Builder(this).setTitle("💬 محادثة مع ${ad.userName}").setView(lay).setPositiveButton("إغلاق",null).show()
    }

    private fun showMyAds(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,80,0,0); setBackgroundColor(Color.parseColor("#070D17")) }
        lay.addView(TextView(this).apply { text="📦 إعلاناتي"; textSize=22f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(40,0,40,30) })
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        val myAdapter = AdAdapter(mutableListOf()) { ad-> AlertDialog.Builder(this).setTitle("حذف الإعلان؟").setPositiveButton("احذف"){_,_-> db.collection("ads").document(ad.id).delete(); toast("انحذف") }.setNegativeButton("لا",null).show() }
        rv.adapter=myAdapter; lay.addView(rv)
        db.collection("ads").whereEqualTo("uid", auth.currentUser!!.uid).orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { s,_ -> myAdapter.update(s?.documents?.map { d-> val a=d.toObject(Ad::class.java)!!; a.id=d.id; a }?.toMutableList()?: mutableListOf()) }
        contentFrame.removeAllViews(); contentFrame.addView(lay)
    }

    private fun showMessages(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,80,0,0) }
        lay.addView(TextView(this).apply { text="💬 رسائلي"; textSize=22f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(40,0,40,30) })
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        val list = mutableListOf<Map<String,Any>>()
        val adp = object: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
            override fun getItemCount()=list.size
            override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): RecyclerView.ViewHolder {
                val c=LinearLayout(p.context).apply { setPadding(35,35,35,35); orientation=LinearLayout.VERTICAL; background=GradientDrawable().apply { cornerRadius=25f; setColor(Color.parseColor("#162032")) }; layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(20,10,20,10) } }; return object: RecyclerView.ViewHolder(c){}
            }
            override fun onBindViewHolder(h:RecyclerView.ViewHolder,p:Int){
                val m=list[p]; val c=h.itemView as LinearLayout; c.removeAllViews()
                c.addView(TextView(c.context).apply { text=m["adTitle"].toString(); textSize=14f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD })
                c.addView(TextView(c.context).apply { text=m["last"].toString(); textSize=12f; setTextColor(Color.parseColor("#AAAAAA")) })
                c.setOnClickListener { val ad=Ad(id=m["adId"].toString(), title=m["adTitle"].toString(), uid=m["user2"].toString(), userName="البائع"); openChat(ad) }
            }
        }
        rv.adapter=adp; lay.addView(rv)
        db.collection("chats").get().addOnSuccessListener { snap-> list.clear(); snap.forEach { if(it.getString("user1")==auth.currentUser!!.uid || it.getString("user2")==auth.currentUser!!.uid) list.add(it.data) }; adp.notifyDataSetChanged() }
        contentFrame.removeAllViews(); contentFrame.addView(lay)
    }

    private fun showProfile(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(50,80,50,50) }
        lay.addView(TextView(this).apply { text="👤"; textSize=60f; gravity=Gravity.CENTER })
        lay.addView(TextView(this).apply { text=auth.currentUser?.email?:""; textSize=16f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(0,20,0,40) })
        val eName = inp("الاسم الجديد"); val ePhone = inp("رقم الواتساب")
        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { d-> eName.setText(d.getString("name")?:""); ePhone.setText(d.getString("phone")?:"") }
        val btnSave = btn("💾 حفظ البروفايل","#FFD700","#000") { db.collection("users").document(auth.currentUser!!.uid).update(mapOf("name" to eName.text.toString(), "phone" to ePhone.text.toString())).addOnSuccessListener { toast("تم الحفظ 👑") } }
        val btnOut = btn("🚪 تسجيل خروج","#1A2332","#FF6B00") { auth.signOut(); showAuth() }
        lay.addView(eName); lay.addView(ePhone); lay.addView(btnSave); lay.addView(btnOut)
        contentFrame.removeAllViews(); contentFrame.addView(lay)
    }

    private fun showAddDialog(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,10,30,10) }
        val eTitle=inpDark("عنوان أسطوري"); val ePrice=inpDark("السعر"); val eCity=inpDark("المدينة").apply { setText("الشعفة") }; val ePhone=inpDark("واتساب"); val eDesc=inpDark("وصف فخم...")
        val spin=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("سيارات","عقار","مواشي","أجهزة","أثاث","عام")) }
        listOf(eTitle,ePrice,eCity,ePhone,eDesc,spin).forEach { lay.addView(it) }
        AlertDialog.Builder(this).setTitle("👑 نشر إعلان أسطوري").setView(lay).setPositiveButton("انشر الآن ✨"){_,_ ->
            db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { u->
                val name=u.getString("name")?: auth.currentUser?.email?:""
                val ad=Ad(title=eTitle.text.toString(), price=ePrice.text.toString(), city=eCity.text.toString(), phone=ePhone.text.toString(), cat=spin.selectedItem.toString(), desc=eDesc.text.toString(), uid=auth.currentUser!!.uid, userName=name)
                db.collection("ads").add(ad).addOnSuccessListener { toast("اننشر 🔥"); showHomeScreen() }
            }
        }.setNegativeButton("إلغاء",null).show()
    }

    private fun inp(h:String)=EditText(this).apply { hint=h; setHintTextColor(Color.parseColor("#6A7A8A")); setTextColor(Color.WHITE); background=GradientDrawable().apply { cornerRadius=25f; setColor(Color.parseColor("#1A2332")) }; setPadding(40,38,40,38); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,12,0,12) } }
    private fun inpDark(h:String)=EditText(this).apply { hint=h; setPadding(40,38,40,38); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,10,0,10) } }
    private fun btn(t:String, bg:String, txt:String, onClick:()->Unit)=TextView(this).apply { text=t; gravity=Gravity.CENTER; setTextColor(Color.parseColor(txt)); textSize=13f; typeface=Typeface.DEFAULT_BOLD; setPadding(0,40,0,40); background=GradientDrawable().apply { cornerRadius=30f; setColor(Color.parseColor(bg)) }; layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,18,0,0) }; setOnClickListener { onClick() } }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()

    class AdAdapter(private var list: MutableList<Ad>, private val onClick:(Ad)->Unit): RecyclerView.Adapter<AdAdapter.VH>(){
        class VH(val card: LinearLayout): RecyclerView.ViewHolder(card)
        fun update(n:MutableList<Ad>){ list=n; notifyDataSetChanged() }
        override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): VH {
            val c=LinearLayout(p.context).apply { orientation=LinearLayout.VERTICAL; setPadding(35,35,35,35); background=GradientDrawable().apply { cornerRadius=28f; setColor(Color.parseColor("#121C2A")); setStroke(1, Color.parseColor("#1E2F44")) }; layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(22,12,22,12) } }; return VH(c)
        }
        override fun getItemCount()=list.size
        override fun onBindViewHolder(h:VH,p:Int){
            val ad=list[p]; val c=h.card.context; h.card.removeAllViews()
            h.card.addView(TextView(c).apply { text="⚜️ ${ad.cat} • 📍 ${ad.city}"; textSize=10f; setTextColor(Color.parseColor("#FFD700")) })
            h.card.addView(TextView(c).apply { text=ad.title; textSize=17f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(0,12,0,6) })
            h.card.addView(TextView(c).apply { text="💰 ${ad.price} • 👤 ${ad.userName}"; textSize=12f; setTextColor(Color.parseColor("#AAB8C8")) })
            h.card.setOnClickListener { onClick(ad) }
        }
    }
    class CommentAdapter(val list: MutableList<Comment>): RecyclerView.Adapter<CommentAdapter.VH>(){
        class VH(val v: LinearLayout): RecyclerView.ViewHolder(v)
        override fun getItemCount()=list.size
        override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): VH {
            val l=LinearLayout(p.context).apply { orientation=LinearLayout.VERTICAL; setPadding(20,15,20,15); background=GradientDrawable().apply { cornerRadius=15f; setColor(Color.parseColor("#1A2332")) }; layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(0,8,0,8) } }; return VH(l)
        }
        override fun onBindViewHolder(h:VH,p:Int){ val cm=list[p]; h.v.removeAllViews(); h.v.addView(TextView(h.v.context).apply { text="${cm.userName}: ${cm.text}"; textSize=13f; setTextColor(Color.WHITE) }) }
    }
    class ChatAdapter(val list: MutableList<ChatMsg>, val myUid:String): RecyclerView.Adapter<ChatAdapter.VH>(){
        class VH(val t: TextView): RecyclerView.ViewHolder(t)
        override fun getItemCount()=list.size
        override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): VH { val tv=TextView(p.context).apply { setPadding(25,18,25,18); textSize=13f; layoutParams=RecyclerView.LayoutParams(-2,-2).apply { setMargins(10,6,10,6) } }; return VH(tv) }
        override fun onBindViewHolder(h:VH,p:Int){ val m=list[p]; h.t.text=m.text; if(m.fromUid==myUid){ h.t.setTextColor(Color.BLACK); h.t.background=GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#FFD700")) } } else { h.t.setTextColor(Color.WHITE); h.t.background=GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#1E2F44")) } } }
    }
}anager=LinearLayoutManager(this@MainActivity) }
        adapter = AdAdapter(mutableListOf()) { ad -> showAdDetail(ad) }
        rv.adapter = adapter
        lay.addView(rv, LinearLayout.LayoutParams(-1,-1).apply { weight=1f })
        contentFrame.removeAllViews(); contentFrame.addView(lay)
        loadAds("الكل")
    }

    private fun loadAds(cat:String){
        val q = if(cat=="الكل") db.collection("ads").orderBy("time", Query.Direction.DESCENDING) else db.collection("ads").whereEqualTo("cat",cat).orderBy("time", Query.Direction.DESCENDING)
        q.addSnapshotListener { snap,_ ->
            val list = snap?.documents?.map { d-> val a=d.toObject(Ad::class.java)!!; a.id=d.id; a }?.toMutableList()?: mutableListOf()
            allAds=list; adapter.update(list)
        }
    }

    // ===== تفاصيل + تعليقات + مراسلة =====
    private fun showAdDetail(ad:Ad){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,20,30,20) }
        lay.addView(TextView(this).apply { text="⚜️ ${ad.cat} • ${ad.city}"; textSize=12f; setTextColor(Color.parseColor("#FFD700")) })
        lay.addView(TextView(this).apply { text=ad.title; textSize=20f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(0,15,0,10) })
        lay.addView(TextView(this).apply { text="💰 ${ad.price}\n${ad.desc}\n\n👤 ${ad.userName}"; textSize=14f; setTextColor(Color.parseColor("#CCCCCC")) })

        val btnChat = btn("💬 مراسلة صاحب الإعلان داخل التطبيق","#162032","#FFD700") { openChat(ad) }
        val btnWa = btn("💚 واتساب فوري","#25D366","#FFF") { try{ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${ad.phone}")))}catch(_:Exception){} }
        val btnCall = btn("📞 اتصال","#FF6B00","#FFF") { try{ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.phone}")))}catch(_:Exception){} }

        lay.addView(btnChat); lay.addView(btnWa); lay.addView(btnCall)
        lay.addView(TextView(this).apply { text="\n💬 التعليقات:"; textSize=16f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD })

        val rvC = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity); layoutParams=LinearLayout.LayoutParams(-1,400) }
        val cList = mutableListOf<Comment>(); val cAdapter = CommentAdapter(cList)
        rvC.adapter=cAdapter; lay.addView(rvC)

        db.collection("ads").document(ad.id).collection("comments").orderBy("time").addSnapshotListener { snap,_ ->
            cList.clear(); snap?.forEach { cList.add(it.toObject(Comment::class.java)) }; cAdapter.notifyDataSetChanged()
        }

        val eComment = inp("اكتب تعليق فخم..."); val btnSend = btn("إرسال التعليق ✨","#FFD700","#000") {
            if(eComment.text.isEmpty()) return@btn
            val userName = auth.currentUser?.email?.substringBefore("@")?:"مستخدم"
            val c = Comment(eComment.text.toString(), userName, auth.currentUser!!.uid)
            db.collection("ads").document(ad.id).collection("comments").add(c); eComment.setText("")
        }
        lay.addView(eComment); lay.addView(btnSend)

        val scroll = ScrollView(this).apply { addView(lay) }
        AlertDialog.Builder(this).setView(scroll).setPositiveButton("إغلاق",null).show()
    }

    private fun openChat(ad:Ad){
        val myUid = auth.currentUser!!.uid; val otherUid = ad.uid; if(myUid==otherUid){ toast("هذا إعلانك!"); return }
        val chatId = if(myUid < otherUid) "${myUid}_${otherUid}_${ad.id}" else "${otherUid}_${myUid}_${ad.id}"
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(20,10,20,10) }
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity); layoutParams=LinearLayout.LayoutParams(-1,500) }
        val msgs = mutableListOf<ChatMsg>(); val adp = ChatAdapter(msgs, myUid); rv.adapter=adp
        db.collection("chats").document(chatId).collection("msgs").orderBy("time").addSnapshotListener { snap,_ -> msgs.clear(); snap?.forEach { msgs.add(it.toObject(ChatMsg::class.java)) }; adp.notifyDataSetChanged(); rv.scrollToPosition(msgs.size-1) }
        val eMsg = inp("رسالتك..."); val btnS = btn("إرسال 🚀","#FFD700","#000") {
            if(eMsg.text.isEmpty()) return@btn
            db.collection("chats").document(chatId).set(mapOf("adId" to ad.id, "adTitle" to ad.title, "user1" to myUid, "user2" to otherUid, "last" to eMsg.text.toString(), "time" to System.currentTimeMillis()))
            db.collection("chats").document(chatId).collection("msgs").add(ChatMsg(eMsg.text.toString(), myUid)); eMsg.setText("")
        }
        lay.addView(rv); lay.addView(eMsg); lay.addView(btnS)
        AlertDialog.Builder(this).setTitle("💬 محادثة مع ${ad.userName}").setView(lay).setPositiveButton("إغلاق",null).show()
    }

    // ===== إعلاناتي =====
    private fun showMyAds(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,80,0,0); setBackgroundColor(Color.parseColor("#070D17")) }
        lay.addView(TextView(this).apply { text="📦 إعلاناتي"; textSize=22f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(40,0,40,30) })
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        val myAdapter = AdAdapter(mutableListOf()) { ad-> val b=AlertDialog.Builder(this); b.setTitle("حذف الإعلان؟"); b.setPositiveButton("احذف"){_,_-> db.collection("ads").document(ad.id).delete(); toast("انحذف") }; b.setNegativeButton("لا",null); b.show() }
        rv.adapter=myAdapter; lay.addView(rv)
        db.collection("ads").whereEqualTo("uid", auth.currentUser!!.uid).orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { s,_ -> myAdapter.update(s?.documents?.map { d-> val a=d.toObject(Ad::class.java)!!; a.id=d.id; a }?.toMutableList()?: mutableListOf()) }
        contentFrame.removeAllViews(); contentFrame.addView(lay)
    }

    // ===== رسائلي =====
    private fun showMessages(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,80,0,0) }
        lay.addView(TextView(this).apply { text="💬 رسائلي"; textSize=22f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(40,0,40,30) })
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        val list = mutableListOf<Map<String,Any>>(); val adp = object: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
            override fun getItemCount()=list.size
            override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): RecyclerView.ViewHolder {
                val c=LinearLayout(p.context).apply { setPadding(35,35,35,35); orientation=LinearLayout.VERTICAL; background=GradientDrawable().apply { cornerRadius=25f; setColor(Color.parseColor("#162032")) }; layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(20,10,20,10) } }; return object: RecyclerView.ViewHolder(c){}
            }
            override fun onBindViewHolder(h:RecyclerView.ViewHolder,p:Int){
                val m=list[p]; val c=h.itemView as LinearLayout; c.removeAllViews()
                c.addView(TextView(c.context).apply { text=m["adTitle"].toString(); textSize=14f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD })
                c.addView(TextView(c.context).apply { text=m["last"].toString(); textSize=12f; setTextColor(Color.parseColor("#AAAAAA")) })
                c.setOnClickListener { val ad=Ad(id=m["adId"].toString(), title=m["adTitle"].toString(), uid=if(m["user1"]==auth.currentUser!!.uid) m["user2"].toString() else m["user1"].toString(), userName="البائع"); openChat(ad) }
            }
        }
        rv.adapter=adp; lay.addView(rv)
        db.collection("chats").whereArrayContainsAny("user1", listOf(auth.currentUser!!.uid)).addSnapshotListener { _,_ ->
            db.collection("chats").get().addOnSuccessListener { snap->
                list.clear(); snap.forEach { if(it.getString("user1")==auth.currentUser!!.uid || it.getString("user2")==auth.currentUser!!.uid) list.add(it.data + mapOf("id" to it.id)) }; adp.notifyDataSetChanged()
            }
        }
        // تحميل مباشر
        db.collection("chats").get().addOnSuccessListener { snap-> list.clear(); snap.forEach { if(it.getString("user1")==auth.currentUser!!.uid || it.getString("user2")==auth.currentUser!!.uid) list.add(it.data + mapOf("id" to it.id, "adId" to (it.getString("adId")?:""), "adTitle" to (it.getString("adTitle")?:""))) }; adp.notifyDataSetChanged() }
        contentFrame.removeAllViews(); contentFrame.addView(lay)
    }

    // ===== البروفايل =====
    private fun showProfile(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(50,80,50,50) }
        lay.addView(TextView(this).apply { text="👤"; textSize=60f; gravity=Gravity.CENTER })
        lay.addView(TextView(this).apply { text=auth.currentUser?.email?:""; textSize=16f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(0,20,0,40) })
        var currentName=""; var currentPhone=""
        val eName = inp("الاسم الجديد"); val ePhone = inp("رقم الواتساب")
        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { d-> currentName=d.getString("name")?:""; currentPhone=d.getString("phone")?:""; eName.setText(currentName); ePhone.setText(currentPhone) }
        val btnSave = btn("💾 حفظ البروفايل","#FFD700","#000") {
            db.collection("users").document(auth.currentUser!!.uid).update(mapOf("name" to eName.text.toString(), "phone" to ePhone.text.toString())).addOnSuccessListener { toast("تم الحفظ أسطورياً 👑") }
        }
        val btnOut = btn("🚪 تسجيل خروج","#1A2332","#FF6B00") { auth.signOut(); showAuth() }
        lay.addView(eName); lay.addView(ePhone); lay.addView(btnSave); lay.addView(btnOut)
        contentFrame.removeAllViews(); contentFrame.addView(lay)
    }

    private fun showAddDialog(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,10,30,10) }
        val eTitle=inpDark("عنوان أسطوري"); val ePrice=inpDark("السعر"); val eCity=inpDark("المدينة").apply { setText("الشعفة") }; val ePhone=inpDark("واتساب"); val eDesc=inpDark("وصف فخم...")
        val spin=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("سيارات","عقار","مواشي","أجهزة","أثاث","عام")) }
        listOf(eTitle,ePrice,eCity,ePhone,eDesc,spin).forEach { lay.addView(it) }
        AlertDialog.Builder(this).setTitle("👑 نشر إعلان أسطوري").setView(lay).setPositiveButton("انشر الآن ✨"){_,_ ->
            db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { u->
                val name=u.getString("name")?: auth.currentUser?.email?:""
                val ad=Ad(title=eTitle.text.toString(), price=ePrice.text.toString(), city=eCity.text.toString(), phone=ePhone.text.toString(), cat=spin.selectedItem.toString(), desc=eDesc.text.toString(), uid=auth.currentUser!!.uid, userName=name)
                db.collection("ads").add(ad).addOnSuccessListener { toast("اننشر 🔥"); showHomeScreen() }
            }
        }.setNegativeButton("إلغاء",null).show()
    }

    // أدوات UI
    private fun inp(h:String)=EditText(this).apply { hint=h; setHintTextColor(Color.parseColor("#6A7A8A")); setTextColor(Color.WHITE); background=GradientDrawable().apply { cornerRadius=25f; setColor(Color.parseColor("#1A2332")) }; setPadding(40,38,40,38); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,12,0,12) } }
    private fun inpDark(h:String)=EditText(this).apply { hint=h; setPadding(40,38,40,38); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,10,0,10) } }
    private fun btn(t:String, bg:String, txt:String, onClick:()->Unit)=TextView(this).apply { text=t; gravity=Gravity.CENTER; setTextColor(Color.parseColor(txt)); textSize=13f; typeface=Typeface.DEFAULT_BOLD; setPadding(0,40,0,40); background=GradientDrawable().apply { cornerRadius=30f; setColor(Color.parseColor(bg)) }; layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,18,0,0) }; setOnClickListener { onClick() } }
    private fun toast(s:String)=Toast.makeText(this,s,0).show()

    class AdAdapter(private var list: MutableList<Ad>, private val onClick:(Ad)->Unit): RecyclerView.Adapter<AdAdapter.VH>(){
        class VH(val card: LinearLayout): RecyclerView.ViewHolder(card)
        fun update(n:MutableList<Ad>){ list=n; notifyDataSetChanged() }
        override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): VH {
            val c=LinearLayout(p.context).apply { orientation=LinearLayout.VERTICAL; setPadding(35,35,35,35); background=GradientDrawable().apply { cornerRadius=28f; setColor(Color.parseColor("#121C2A")); setStroke(1, Color.parseColor("#1E2F44")) }; layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(22,12,22,12) } }; return VH(c)
        }
        override fun getItemCount()=list.size
        override fun onBindViewHolder(h:VH,p:Int){
            val ad=list[p]; val c=h.card.context; h.card.removeAllViews()
            h.card.addView(TextView(c).apply { text="⚜️ ${ad.cat} • 📍 ${ad.city}"; textSize=10f; setTextColor(Color.parseColor("#FFD700")) })
            h.card.addView(TextView(c).apply { text=ad.title; textSize=17f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(0,12,0,6) })
            h.card.addView(TextView(c).apply { text="💰 ${ad.price} • 👤 ${ad.userName}"; textSize=12f; setTextColor(Color.parseColor("#AAB8C8")) })
            h.card.setOnClickListener { onClick(ad) }
        }
    }
    class CommentAdapter(val list: MutableList<Comment>): RecyclerView.Adapter<CommentAdapter.VH>(){
        class VH(val v: LinearLayout): RecyclerView.ViewHolder(v)
        override fun getItemCount()=list.size
        override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): VH {
            val l=LinearLayout(p.context).apply { orientation=LinearLayout.VERTICAL; setPadding(20,15,20,15); background=GradientDrawable().apply { cornerRadius=15f; setColor(Color.parseColor("#1A2332")) }; layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(0,8,0,8) } }; return VH(l)
        }
        override fun onBindViewHolder(h:VH,p:Int){
            val cm=list[p]; h.v.removeAllViews()
            h.v.addView(TextView(h.v.context).apply { text="${cm.userName}: ${cm.text}"; textSize=13f; setTextColor(Color.WHITE) })
        }
    }
    class ChatAdapter(val list: MutableList<ChatMsg>, val myUid:String): RecyclerView.Adapter<ChatAdapter.VH>(){
        class VH(val t: TextView): RecyclerView.ViewHolder(t)
        override fun getItemCount()=list.size
        override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): VH {
            val tv=TextView(p.context).apply { setPadding(25,18,25,18); textSize=13f; layoutParams=RecyclerView.LayoutParams(-2,-2).apply { setMargins(10,6,10,6) } }; return VH(tv)
        }
        override fun onBindViewHolder(h:VH,p:Int){
            val m=list[p]; h.t.text=m.text
            if(m.fromUid==myUid){ h.t.setTextColor(Color.BLACK); h.t.background=GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#FFD700")) }; h.t.gravity=Gravity.END } else { h.t.setTextColor(Color.WHITE); h.t.background=GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#1E2F44")) } }
        }
    }
}
private val LinearLayout.children get()= (0 until childCount).map { getChildAt(it) }or.parseColor(bg)) }; layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,20,0,0) } }
    private fun toast(s:String)=Toast.makeText(this,s,0).show()

    class AdAdapter(private var list: MutableList<Ad>, private val onClick:(Ad)->Unit): RecyclerView.Adapter<AdAdapter.VH>(){
        class VH(val card: LinearLayout): RecyclerView.ViewHolder(card)
        fun update(n:MutableList<Ad>){ list=n; notifyDataSetChanged() }
        override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): VH {
            val card=LinearLayout(p.context).apply { orientation=LinearLayout.VERTICAL; setPadding(35,35,35,35); background=GradientDrawable().apply { cornerRadius=30f; setColor(Color.parseColor("#162032")); setStroke(1, Color.parseColor("#2A3A4D")) } }
            card.layoutParams=RecyclerView.LayoutParams(-1,-2).apply { setMargins(25,15,25,15) }; return VH(card)
        }
        override fun getItemCount()=list.size
        override fun onBindViewHolder(h: VH, pos: Int) {
            val ad=list[pos]; val c=h.card.context; h.card.removeAllViews()
            val top=LinearLayout(c).apply { orientation=LinearLayout.HORIZONTAL }
            top.addView(TextView(c).apply { text="⚜️ ${ad.cat}"; textSize=10f; setTextColor(Color.BLACK); typeface=Typeface.DEFAULT_BOLD; setPadding(22,8,22,8); background=GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#FFD700")) } })
            top.addView(TextView(c).apply { text=" ${ad.userName.take(15)}"; textSize=10f; setTextColor(Color.parseColor("#888888")); setPadding(15,0,0,0) })
            h.card.addView(top)
            h.card.addView(TextView(c).apply { text=ad.title; textSize=17f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; setPadding(0,18,0,0) })
            h.card.addView(TextView(c).apply { text="💰 ${ad.price} • 📍 ${ad.city}"; textSize=13f; setTextColor(Color.parseColor("#FFD700")); setPadding(0,10,0,0) })
            h.card.setOnClickListener { onClick(ad) }
        }
    }
}

private val LinearLayout.children get()= (0 until childCount).map { getChildAt(it) }
