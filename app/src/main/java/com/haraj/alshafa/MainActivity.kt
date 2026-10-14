package com.haraj.alshafa

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Ad(val title:String="", val price:String="", val city:String="", val phone:String="", val cat:String="", val time:Long=System.currentTimeMillis())

class MainActivity : Activity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdAdapter
    private var filter = "الكل"
    private val cats = listOf("الكل","سيارات","عقار","مواشي","أجهزة","أثاث","عام")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#F5F6FA")) }
        val header = LinearLayout(this).apply { setBackgroundColor(Color.parseColor("#0D1B2A")); orientation=LinearLayout.VERTICAL; setPadding(40,80,40,40) }
        header.addView(TextView(this).apply { text="حراج الشعفة 🔥 مباشر"; textSize=24f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD })
        root.addView(header)
        val rv = RecyclerView(this).apply { layoutManager=LinearLayoutManager(this@MainActivity) }
        adapter = AdAdapter(mutableListOf()){ ad-> openContact(ad) }
        rv.adapter=adapter; root.addView(rv, LinearLayout.LayoutParams(-1,-1).apply { weight=1f })
        val fab = Button(this).apply { text="+ نشر إعلان مباشر"; setTextColor(Color.WHITE); background=android.graphics.drawable.GradientDrawable().apply { cornerRadius=100f; setColor(Color.parseColor("#FF6B00")) }; setOnClickListener { showAdd() } }
        root.addView(LinearLayout(this).apply { setPadding(30,20,30,30); addView(fab, LinearLayout.LayoutParams(-1,140)) })
        setContentView(root); load()
    }
    private fun load(){
        val q = if(filter=="الكل") db.collection("ads").orderBy("time", Query.Direction.DESCENDING) else db.collection("ads").whereEqualTo("cat",filter).orderBy("time", Query.Direction.DESCENDING)
        q.addSnapshotListener { snap,_ -> adapter.update(snap?.toObjects(Ad::class.java)?.toMutableList()?: mutableListOf()) }
    }
    private fun showAdd(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(40,20,40,20) }
        val eTitle = EditText(this).apply { hint="وش تبي تبيع؟" }
        val ePrice = EditText(this).apply { hint="السعر" }
        val eCity = EditText(this).apply { hint="المدينة"; setText("الشعفة") }
        val ePhone = EditText(this).apply { hint="جوالك" }
        val spinner = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, cats.filter { it!="الكل" }) }
        listOf(eTitle,ePrice,eCity,ePhone,spinner).forEach { lay.addView(it) }
        AlertDialog.Builder(this).setTitle("نشر مباشر").setView(lay).setPositiveButton("انشر"){_,_ ->
            val ad = Ad(eTitle.text.toString(), ePrice.text.toString(), eCity.text.toString(), ePhone.text.toString(), spinner.selectedItem.toString(), System.currentTimeMillis())
            db.collection("ads").add(ad).addOnSuccessListener { Toast.makeText(this,"تم النشر أونلاين!",0).show() }
        }.setNegativeButton("إلغاء",null).show()
    }
    private fun openContact(ad: Ad){ AlertDialog.Builder(this).setTitle(ad.title).setMessage("${ad.price} - ${ad.city}").setItems(arrayOf("واتساب","اتصال")){_,i-> if(i==0) try{ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${ad.phone}")))}catch(_:Exception){} else try{ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.phone}")))}catch(_:Exception){} }.show() }
    class AdAdapter(private var list: MutableList<Ad>, private val onClick:(Ad)->Unit): RecyclerView.Adapter<AdAdapter.VH>(){
        class VH(val card: LinearLayout): RecyclerView.ViewHolder(card)
        fun update(newList: MutableList<Ad>){ list=newList; notifyDataSetChanged() }
        override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): VH {
            val card = LinearLayout(p.context).apply { orientation=LinearLayout.VERTICAL; setPadding(30,30,30,30); setBackgroundColor(Color.WHITE); background=android.graphics.drawable.GradientDrawable().apply { cornerRadius=30f; setColor(Color.WHITE) } }
            card.layoutParams = RecyclerView.LayoutParams(-1,-2).apply { setMargins(30,15,30,15) }; return VH(card)
        }
        override fun getItemCount() = list.size
        override fun onBindViewHolder(h: VH, pos: Int) { val ad = list[pos]; val ctx = h.card.context; h.card.removeAllViews(); h.card.addView(TextView(ctx).apply { text=ad.cat; textSize=10f; setPadding(20,8,20,8); setTextColor(Color.WHITE); background=android.graphics.drawable.GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#0D1B2A")) } }); h.card.addView(TextView(ctx).apply { text=ad.title; textSize=16f; setTextColor(Color.BLACK); typeface=android.graphics.Typeface.DEFAULT_BOLD; setPadding(0,15,0,0) }); h.card.addView(TextView(ctx).apply { text=ad.price; textSize=18f; setTextColor(Color.parseColor("#FF6B00")); typeface=android.graphics.Typeface.DEFAULT_BOLD }); h.card.setOnClickListener { onClick(ad) } }
    }
}
