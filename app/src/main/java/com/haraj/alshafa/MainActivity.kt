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

    data class Ad(var id:String="", val title:String="", val price:String="", val city:String="", val phone:String="", val uid:String="", val userName:String="", val time:Long=0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if(auth.currentUser==null) showLogin() else showHome()
    }

    fun showLogin(){
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(50,50,50,50)
        root.setBackgroundColor(Color.parseColor("#070D17"))
        val t = TextView(this)
        t.text = "حراج الشعفة الاسطوري"
        t.setTextColor(Color.parseColor("#FFD700"))
        t.gravity = Gravity.CENTER
        t.textSize = 22f
        val eEmail = EditText(this); eEmail.hint = "البريد"
        eEmail.setTextColor(Color.WHITE)
        val ePass = EditText(this); ePass.hint = "كلمة السر"
        ePass.inputType = 129
        ePass.setTextColor(Color.WHITE)
        val eName = EditText(this); eName.hint = "اسمك"
        eName.setTextColor(Color.WHITE)
        val b1 = Button(this); b1.text = "دخول"
        b1.setOnClickListener{
            auth.signInWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener{showHome()}.addOnFailureListener{Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()}
        }
        val b2 = Button(this); b2.text = "انشاء حساب"
        b2.setOnClickListener{
            auth.createUserWithEmailAndPassword(eEmail.text.toString(), ePass.text.toString()).addOnSuccessListener{
                db.collection("users").document(it.user!!.uid).set(mapOf("name" to eName.text.toString()))
                showHome()
            }.addOnFailureListener{Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()}
        }
        root.addView(t); root.addView(eName); root.addView(eEmail); root.addView(ePass); root.addView(b1); root.addView(b2)
        setContentView(root)
    }

    fun showHome(){
        val main = LinearLayout(this)
        main.orientation = LinearLayout.VERTICAL
        main.setBackgroundColor(Color.parseColor("#070D17"))
        val head = TextView(this)
        head.text = "حراج الشعفة - الرئيسي"
        head.setTextColor(Color.WHITE)
        head.setPadding(30,80,30,20)
        rv = RecyclerView(this)
        rv.layoutManager = LinearLayoutManager(this)
        main.addView(head)
        main.addView(rv, LinearLayout.LayoutParams(-1,-1,1f))
        val bottom = LinearLayout(this)
        bottom.orientation = LinearLayout.HORIZONTAL
        bottom.setBackgroundColor(Color.parseColor("#0D1B2A"))
        val bHome = Button(this); bHome.text = "الرئيسية"
        bHome.setOnClickListener{showHome()}
        val bAdd = Button(this); bAdd.text = "نشر"
        bAdd.setOnClickListener{showAdd()}
        val bOut = Button(this); bOut.text = "خروج"
        bOut.setOnClickListener{auth.signOut(); showLogin()}
        bottom.addView(bHome, LinearLayout.LayoutParams(0,-2,1f))
        bottom.addView(bAdd, LinearLayout.LayoutParams(0,-2,1f))
        bottom.addView(bOut, LinearLayout.LayoutParams(0,-2,1f))
        main.addView(bottom)
        setContentView(main)
        loadAds()
    }

    fun loadAds(){
        db.collection("ads").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener{snap,_->
            val list = snap?.documents?.map{d->
                Ad(id=d.id, title=d.getString("title")?:"", price=d.getString("price")?:"", city=d.getString("city")?:"", phone=d.getString("phone")?:"", uid=d.getString("uid")?:"", userName=d.getString("userName")?:"")
            }?: emptyList()
            rv.adapter = object: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
                override fun getItemCount() = list.size
                override fun onCreateViewHolder(p:android.view.ViewGroup,t:Int): RecyclerView.ViewHolder{
                    val card = LinearLayout(p.context)
                    card.orientation = LinearLayout.VERTICAL
                    card.setPadding(30,30,30,30)
                    card.background = GradientDrawable().apply{cornerRadius=20f; setColor(Color.parseColor("#121C2A"))}
                    card.layoutParams = RecyclerView.LayoutParams(-1,-2).apply{setMargins(20,10,20,10)}
                    return object: RecyclerView.ViewHolder(card){}
                }
                override fun onBindViewHolder(h:RecyclerView.ViewHolder,p:Int){
                    val ad=list[p]
                    val c=h.itemView as LinearLayout
                    c.removeAllViews()
                    val tv1 = TextView(c.context); tv1.text = ad.title; tv1.setTextColor(Color.WHITE); tv1.textSize = 16f
                    val tv2 = TextView(c.context); tv2.text = ad.price + " - " + ad.city; tv2.setTextColor(Color.LTGRAY)
                    c.addView(tv1); c.addView(tv2)
                    c.setOnClickListener{showDetail(ad)}
                }
            }
        }
    }

    fun showDetail(ad:Ad){
        val lay = LinearLayout(this); lay.orientation = LinearLayout.VERTICAL; lay.setPadding(20,20,20,20)
        val tv = TextView(this); tv.text = ad.title + "\n" + ad.city + "\n" + ad.price + "\n" + ad.phone; tv.setTextColor(Color.WHITE)
        val eComment = EditText(this); eComment.hint = "اكتب تعليق"
        val bComment = Button(this); bComment.text = "ارسل تعليق"
        bComment.setOnClickListener{
            val cm = mapOf("text" to eComment.text.toString(), "userName" to "مستخدم", "time" to System.currentTimeMillis())
            db.collection("ads").document(ad.id).collection("comments").add(cm)
            Toast.makeText(this,"تم التعليق",Toast.LENGTH_SHORT).show()
        }
        val bChat = Button(this); bChat.text = "مراسلة"
        bChat.setOnClickListener{
            db.collection("chats").document(ad.id+"_"+auth.currentUser!!.uid).set(mapOf("adTitle" to ad.title, "last" to "مرحبا", "time" to System.currentTimeMillis(), "user1" to auth.currentUser!!.uid, "user2" to ad.uid))
            Toast.makeText(this,"تم ارسال رسالة",Toast.LENGTH_SHORT).show()
        }
        val bWa = Button(this); bWa.text = "واتساب"
        bWa.setOnClickListener{try{startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"+ad.phone)))}catch(_:Exception){}}
        lay.addView(tv); lay.addView(eComment); lay.addView(bComment); lay.addView(bChat); lay.addView(bWa)
        AlertDialog.Builder(this).setTitle("تفاصيل").setView(lay).setPositiveButton("اغلاق",null).show()
    }

    fun showAdd(){
        val lay = LinearLayout(this); lay.orientation = LinearLayout.VERTICAL; lay.setPadding(20,20,20,20)
        val eT=EditText(this); eT.hint="عنوان"
        val eP=EditText(this); eP.hint="سعر"
        val eC=EditText(this); eC.hint="مدينة"
        val ePh=EditText(this); ePh.hint="واتساب"
        lay.addView(eT); lay.addView(eP); lay.addView(eC); lay.addView(ePh)
        AlertDialog.Builder(this).setTitle("نشر").setView(lay).setPositiveButton("نشر"){_,_->
            val ad=mapOf("title" to eT.text.toString(), "price" to eP.text.toString(), "city" to eC.text.toString(), "phone" to ePh.text.toString(), "uid" to auth.currentUser!!.uid, "userName" to (auth.currentUser?.email?:""), "time" to System.currentTimeMillis())
            db.collection("ads").add(ad)
            Toast.makeText(this,"تم النشر",Toast.LENGTH_SHORT).show()
        }.setNegativeButton("الغاء",null).show()
    }
}
