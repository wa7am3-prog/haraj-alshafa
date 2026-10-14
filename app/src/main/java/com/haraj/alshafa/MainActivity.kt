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
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : Activity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var rv: RecyclerView

    data class Ad(var id:String="", val title:String="", val price:String="", val city:String="", val phone:String="", val cat:String="", val desc:String="", val uid:String="", val userName:String="", val time:Long=0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if(auth.currentUser==null) showLogin() else showHome()
    }

    fun showLogin(){
        val root = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(50,50,50,50); setBackgroundColor(Color.parseColor("#070D17"))}
        val t = TextView(this).apply{text="⚜️ حراج الشعفة\nالأسطوري"; textSize=26f; setTextColor(Color.parseColor("#FFD700")); gravity=Gravity.CENTER; setPadding(0,0,0,40); typeface=Typeface.DEFAULT_BOLD}
        val eEmail = EditText(this).apply{hint="البريد"; setTextColor(Color.WHITE)}
        val ePass = EditText(this).apply{hint="كلمة السر"; inputType=129; setTextColor(Color.WHITE)}
        val eName = EditText(this).apply{hint="اسمك (للتسجيل)"; setTextColor(Color.WHITE)}
        val b1 = Button(this).apply{text="دخول"; setOnClickListener{
            auth.signInWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener{showHome()}.addOnFailureListener{Toast.makeText(this@MainActivity,it.message,Toast.LENGTH_SHORT).show()}
        }}
        val b2 = Button(this).apply{text="إنشاء حساب جديد"; setOnClickListener{
            auth.createUserWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener{
                db.collection("users").document(it.user!!.uid).set(mapOf("name" to eName.text.toString(), "email" to eEmail.text.toString())); showHome()
            }.addOnFailureListener{Toast.makeText(this@MainActivity,it.message,Toast.LENGTH_SHORT).show()}
        }}
        root.addView(t); root.addView(eName); root.addView(eEmail); root.addView(ePass); root.addView(b1); root.addView(b2)
        setContentView(root)
    }

    fun showHome(){
        val main = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#070D17"))}
        val head = TextView(this).apply{text="👑 حراج الشعفة - فخم كامل الشاشة"; textSize=16f; setTextColor(Color.WHITE); setPadding(30,80,30,20); typeface=Typeface.DEFAULT_BOLD}
        rv = RecyclerView(this).apply{layoutManager=LinearLayoutManager(this@MainActivity)}
        main.addView(head); main.addView(rv, LinearLayout.LayoutParams(-1,-1).apply{weight=1f})
        val bottom = LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL; setPadding(0,15,0,15); setBackgroundColor(Color.parseColor("#0D1B2A"))}
        val bHome = TextView(this).apply{text="🏠 الرئيسية"; gravity=Gravity.CENTER; setTextColor(Color.parseColor("#FFD700")); layoutParams=LinearLayout.LayoutParams(0,-2).apply{weight=1f}; setOnClickListener{showHome()}}
        val bAdd = TextView(this).apply{text="＋ نشر"; gravity=Gravity.CENTER; setTextColor(Color.BLACK); background=GradientDrawable().apply{cornerRadius=30f; setColor(Color.parseColor("#FFD700"))}; layoutParams=LinearLayout.LayoutParams(0,-2).apply{weight=1f}; setOnClickListener{showAdd()}}
        val bProfile = TextView(this).apply{text="👤 حسابي"; gravity=Gravity.CENTER; setTextColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(0,-2).apply{weight=1f}; setOnClickListener{showProfile()}}
        val bOut = TextView(this).apply{text="🚪 خروج"; gravity=Gravity.CENTER; setTextColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(0,-2).apply{weight=1f}; setOnClickListener{auth.signOut(); showLogin()}}
        bottom.addView(bHome); bottom.addView(bAdd); bottom.addView(bProfile); bottom.addView(bOut)
        main.addView(bottom)
        setContentView(main)
        loadAds()
    }

    fun loadAds(){
        db.collection("ads").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener{snap,_->
            val list = snap?.documents?.map{d-> Ad(id=d.id, title=d.getString("title")?:"", price=d.getString("price")?:"", city=d.getString("city")?:"", phone=d.getString("phone")?:"", cat=d.getString("cat")?:"", desc=d.getString("desc")?:"", uid=d.getString("uid")?:"", userName=d.getString("userName")?:"")}?: emptyList()
            rv.adapter = object: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
                override fun getItemCount()=list.size
                override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): RecyclerView.ViewHolder{
                    val card = LinearLayout(p.context).apply{orientation=LinearLayout.VERTICAL; setPadding(30,30,30,30); background=GradientDrawable().apply{cornerRadius=20f; setColor(Color.parseColor("#121C2A"))}; layoutParams=RecyclerView.LayoutParams(-1,-2).apply{setMargins(20,10,20,10)}}
                    return object: RecyclerView.ViewHolder(card){}
                }
                override fun onBindViewHolder(h:RecyclerView.ViewHolder,p:Int){
                    val ad=list[p]; val c=h.itemView as LinearLayout; c.removeAllViews()
                    c.addView(TextView(c.context).apply{text="${ad.cat} • ${ad.city} • ${ad.userName}"; textSize=11f; setTextColor(Color.parseColor("#FFD700"))})
                    c.addView(TextView(c.context).apply{text=ad.title; textSize=16f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD})
                    c.addView(TextView(c.context).apply{text="💰 ${ad.price}"; textSize=13f; setTextColor(Color.LTGRAY)})
                    c.setOnClickListener{showDetail(ad)}
                }
            }
        }
    }

    fun showDetail(ad:Ad){
        val lay = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(20,20,20,20)}
        lay.addView(TextView(this).apply{text=ad.title+"\n"+ad.desc+"\n\n👤 ${ad.userName}\n📍 ${ad.city}\n💰 ${ad.price}"; setTextColor(Color.WHITE)})
        val eComment = EditText(this).apply{hint="اكتب تعليق..."}
        val btnComment = Button(this).apply{text="إرسال تعليق"; setOnClickListener{
            if(eComment.text.isEmpty()) return@setOnClickListener
            val cm = mapOf("text" to eComment.text.toString(), "userName" to (auth.currentUser?.email?.substringBefore("@")?:"مستخدم"), "uid" to auth.currentUser!!.uid, "time" to System.currentTimeMillis())
            db.collection("ads").document(ad.id).collection("comments").add(cm); Toast.makeText(this@MainActivity,"تم التعليق",Toast.LENGTH_SHORT).show(); eComment.setText("")
        }}
        val btnChat = Button(this).apply{text="💬 مراسلة صاحب الإعلان"; setOnClickListener{
            if(auth.currentUser!!.uid==ad.uid){Toast.makeText(this@MainActivity,"هذا إعلانك",Toast.LENGTH_SHORT).show(); return@setOnClickListener}
            val chatId = ad.id+"_"+auth.currentUser!!.uid
            db.collection("chats").document(chatId).set(mapOf("adId" to ad.id, "adTitle" to ad.title, "user1" to auth.currentUser!!.uid, "user2" to ad.uid, "last" to "مرحبا بخصوص ${ad.title}", "time" to System.currentTimeMillis()))
            Toast.makeText(this@MainActivity,"تم إرسال رسالة",Toast.LENGTH_SHORT).show()
        }}
        val btnWa = Button(this).apply{text="💚 واتساب"; setOnClickListener{try{startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${ad.phone}")))}catch(_:Exception){}}}
        lay.addView(eComment); lay.addView(btnComment); lay.addView(btnChat); lay.addView(btnWa)
        AlertDialog.Builder(this).setTitle("تفاصيل").setView(lay).setPositiveButton("إغلاق",null).show()
    }

    fun showAdd(){
        val lay = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; setPadding(20,20,20,20)}
        val eT=EditText(this).apply{hint="عنوان"}; val eP=EditText(this).apply{hint="سعر"}; val eC=EditText(this).apply{hint="مدينة"}; val ePh=EditText(this).apply{hint="واتساب"}; val eD=EditText(this).apply{hint="وصف"}
        lay.addView(eT); lay.addView(eP); lay.addView(eC); lay.addView(ePh); lay.addView(eD)
        AlertDialog.Builder(this).setTitle("نشر إعلان").setView(lay).setPositiveButton("نشر"){_,_->
            db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener{u->
                val name=u.getString("name")?:"مستخدم"
                val ad=mapOf("title" to eT.text.toString(), "price" to eP.text.toString(), "city" to eC.text.toString(), "phone" to ePh.text.toString(), "desc" to eD.text.toString(), "cat" to "عام", "uid" to auth.currentUser!!.uid, "userName" to name, "time" to System.currentTimeMillis())
                db.collection("ads").add(ad); Toast.makeText(this,"تم النشر",Toast.LENGTH_SHORT).show()
            }
        }.setNegativeButton("إلغاء",null).show()
    }

    fun showProfile(){
        val lay = LinearLayout(this).apply{orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(40,100,40,40); setBackgroundColor(Color.parseColor("#070D17"))}
        lay.addView(TextView(this).apply{text=auth.currentUser?.email?:""; textSize=18f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(0,0,0,30)})
        val eName=EditText(this).apply{hint="اسم جديد"; setTextColor(Color.WHITE)}; val ePhone=EditText(this).apply{hint="رقم جديد"; setTextColor(Color.WHITE)}
        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener{d-> eName.setText(d.getString("name")?:""); ePhone.setText(d.getString("phone")?:"")}
        val bSave=Button(this).apply{text="حفظ البروفايل"; setOnClickListener{db.collection("users").document(auth.currentUser!!.uid).update(mapOf("name" to eName.text.toString(), "phone" to ePhone.text.toString())).addOnSuccessListener{Toast.makeText(this@MainActivity,"تم الحفظ",Toast.LENGTH_SHORT).show(); showHome()}}}
        val bBack=Button(this).apply{text="رجوع للرئيسية"; setOnClickListener{showHome()}}
        lay.addView(eName); lay.addView(ePhone); lay.addView(bSave); lay.addView(bBack)
        setContentView(lay)
    }
}ستخدم"
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
