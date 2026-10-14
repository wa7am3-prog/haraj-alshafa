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
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Ad(val title:String="", val price:String="", val city:String="", val phone:String="", val cat:String="", val uid:String="", val userName:String="", val time:Long=System.currentTimeMillis())

class MainActivity : Activity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var root: LinearLayout
    private lateinit var adapter: AdAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser == null) showAuthScreen() else showMainScreen()
    }

    // شاشة الدخول الفخمة
    private fun showAuthScreen() {
        val bg = GradientDrawable().apply {
            colors = intArrayOf(Color.parseColor("#0D1B2A"), Color.parseColor("#1B2838"), Color.parseColor("#0D1B2A"))
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
        }
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = bg; gravity = Gravity.CENTER; setPadding(60,0,60,0) }

        val logo = TextView(this).apply { text="⚜️ حراج الشعفة ⚜️"; textSize=28f; setTextColor(Color.parseColor("#FFD700")); typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(0,0,0,20) }
        val sub = TextView(this).apply { text="السوق الأسطوري الأول"; textSize=14f; setTextColor(Color.parseColor("#AAAAAA")); gravity=Gravity.CENTER; setPadding(0,0,0,60) }

        val card = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setPadding(50,50,50,50)
            background=GradientDrawable().apply { cornerRadius=40f; setColor(Color.parseColor("#1E2F44")); setStroke(2, Color.parseColor("#FFD700")) }
        }
        val eEmail = styledEdit("البريد الإلكتروني")
        val ePass = styledEdit("كلمة السر").apply { inputType = 129 }
        val eName = styledEdit("اسمك الكريم (للتسجيل فقط)")

        val btnLogin = styledButton("✨ تسجيل دخول", "#FFD700", "#000000")
        val btnReg = styledButton("👑 إنشاء حساب أسطوري", "#FF6B00", "#FFFFFF")

        btnLogin.setOnClickListener {
            if(eEmail.text.isEmpty()||ePass.text.isEmpty()){ toast("عبي البيانات"); return@setOnClickListener }
            auth.signInWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener { showMainScreen() }.addOnFailureListener { toast("خطأ: ${it.message}") }
        }
        btnReg.setOnClickListener {
            if(eEmail.text.isEmpty()||ePass.text.isEmpty()||eName.text.isEmpty()){ toast("عبي كل البيانات"); return@setOnClickListener }
            auth.createUserWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener {
                val data = mapOf("name" to eName.text.toString(), "email" to eEmail.text.toString(), "time" to System.currentTimeMillis())
                db.collection("users").document(it.user!!.uid).set(data)
                showMainScreen()
            }.addOnFailureListener { toast(it.message?:"خطأ") }
        }

        card.addView(eName); card.addView(eEmail); card.addView(ePass); card.addView(btnLogin); card.addView(btnReg)
        root.addView(logo); root.addView(sub); root.addView(card)
        setContentView(root)
    }

    // الشاشة الرئيسية الفخمة
    private fun showMainScreen() {
        root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#0A0E14")) }

        // هيدر أسطوري
        val header = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setPadding(40,90,40,40)
            background=GradientDrawable().apply { colors=intArrayOf(Color.parseColor("#0D1B2A"), Color.parseColor("#1B2A41")); cornerRadius=0f }
        }
        val topRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        topRow.addView(TextView(this).apply { text="👑 حراج الشعفة\n${auth.currentUser?.email}"; textSize=16f; setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD; layoutParams=LinearLayout.LayoutParams(0,-2).apply { weight=1f } })
        val btnOut = TextView(this).apply { text="خروج"; setTextColor(Color.parseColor("#FF6B00")); textSize=14f; setPadding(30,20,30,20); background=GradientDrawable().apply { cornerRadius=50f; setStroke(2, Color.parseColor("#FF6B00")) }; setOnClickListener { auth.signOut(); showAuthScreen() } }
        topRow.addView(btnOut)
        header.addView(topRow)

        // فلاتر فخمة
        val filterScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false; setPadding(0,30,0,10) }
        val filterRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(20,0,20,0) }
        listOf("الكل","سيارات","عقار","مواشي","أجهزة","أثاث","عام").forEach { cat ->
            val chip = TextView(this).apply {
                text=cat; setPadding(40,18,40,18); setTextColor(if(cat=="الكل") Color.BLACK else Color.WHITE); textSize=12f; typeface=Typeface.DEFAULT_BOLD
                background=GradientDrawable().apply { cornerRadius=50f; setColor(if(cat=="الكل") Color.parseColor("#FFD700") else Color.parseColor("#1E2F44")) }
                setOnClickListener { load(cat); filterRow.children.forEach { (it as TextView).let{ t-> t.background=GradientDrawable().apply { cornerRadius=50f; setColor(Color.parseColor("#1E2F44")) }; t.setTextColor(Color.WHITE) } }; background=GradientDrawable().apply { cornerRadius=50f; setColor(Color.parseColor("#FFD700")) }; setTextColor(Color.BLACK) }
                layoutParams=LinearLayout.LayoutParams(-2,-2).apply { setMargins(10,0,10,0) }
            }
            filterRow.addView(chip)
        }
        filterScroll.addView(filterRow)
        header.addView(filterScroll)
        root.addView(header)

        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        adapter = AdAdapter(mutableListOf()){ ad-> contactDialog(ad) }
        rv.adapter=adapter; root.addView(rv, LinearLayout.LayoutParams(-1,-1).apply { weight=1f })

        val fab = TextView(this).apply {
            text="＋ نشر إعلان فخم"; gravity=Gravity.CENTER; textSize=16f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Color.BLACK); setPadding(0,40,0,40)
            background=GradientDrawable().apply { cornerRadius=100f; colors=intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FF6B00")); orientation=GradientDrawable.Orientation.LEFT_RIGHT }
            setOnClickListener { showAddDialog() }
        }
        val fabWrap = LinearLayout(this).apply { setPadding(30,20,30,30); addView(fab, LinearLayout.LayoutParams(-1,150)) }
        root.addView(fabWrap)
        setContentView(root); load("الكل")
    }

    private fun load(cat:String){
        val q = if(cat=="الكل") db.collection("ads").orderBy("time", Query.Direction.DESCENDING) else db.collection("ads").whereEqualTo("cat",cat).orderBy("time", Query.Direction.DESCENDING)
        q.addSnapshotListener { snap,_ -> adapter.update(snap?.toObjects(Ad::class.java)?.toMutableList()?: mutableListOf()) }
    }

    private fun showAddDialog(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,10,30,10) }
        val eTitle=styledEditDark("عنوان الإعلان الأسطوري"); val ePrice=styledEditDark("السعر"); val eCity=styledEditDark("المدينة").apply { setText("الشعفة") }; val ePhone=styledEditDark("واتساب")
        val spinner=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("سيارات","عقار","مواشي","أجهزة","أثاث","عام")) }
        listOf(eTitle,ePrice,eCity,ePhone,spinner).forEach { lay.addView(it) }
        AlertDialog.Builder(this).setTitle("👑 إعلان جديد فخم").setView(lay).setPositiveButton("انشر الآن ✨"){_,_ ->
            val ad=Ad(eTitle.text.toString(), ePrice.text.toString(), eCity.text.toString(), ePhone.text.toString(), spinner.selectedItem.toString(), auth.currentUser!!.uid, auth.currentUser!!.email?:"", System.currentTimeMillis())
            db.collection("ads").add(ad).addOnSuccessListener { toast("تم النشر أسطورياً! 🔥") }
        }.setNegativeButton("إلغاء",null).show()
    }

    private fun contactDialog(ad:Ad){
        AlertDialog.Builder(this).setTitle("💎 ${ad.title}").setMessage("السعر: ${ad.price}\nالمدينة: ${ad.city}\nالبائع: ${ad.userName}\n\nتواصل فخم:").setItems(arrayOf("💬 واتساب فوري","📞 اتصال مباشر")){_,i-> if(i==0) try{ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${ad.phone}")))}catch(_:Exception){} else try{ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.phone}")))}catch(_:Exception){} }.show()
    }

    private fun styledEdit(h:String)=EditText(this).apply { hint=h; setHintTextColor(Color.parseColor("#888888")); setTextColor(Color.WHITE); background=GradientDrawable().apply { cornerRadius=25f; setColor(Color.parseColor("#2A3A4D")) }; setPadding(40,35,40,35); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,12,0,12) } }
    private fun styledEditDark(h:String)=EditText(this).apply { hint=h; setPadding(40,35,40,35); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,12,0,12) } }
    private fun styledButton(t:String, bg:String, txt:String)=TextView(this).apply { text=t; gravity=Gravity.CENTER; setTextColor(Color.parseColor(txt)); textSize=14f; typeface=Typeface.DEFAULT_BOLD; setPadding(0,38,0,38); background=GradientDrawable().apply { cornerRadius=30f; setColor(Color.parseColor(bg)) }; layoutParams=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,20,0,0) } }
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
