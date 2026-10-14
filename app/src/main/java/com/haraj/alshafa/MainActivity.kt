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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Ad(val title:String, val price:String, val city:String, val phone:String, val cat:String, val time:String)

class MainActivity : Activity() {
    private val prefsName = "haraj_shafa"
    private var ads = mutableListOf<Ad>()
    private var filter = "الكل"
    private lateinit var adapter: AdAdapter
    private val cats = listOf("الكل","سيارات","عقار","مواشي","أجهزة","أثاث","عام")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAds()
        if(ads.isEmpty()){
            ads.addAll(listOf(
                Ad("هايلوكس 2019 نظيف","85,000 ر.س","الشعفة","05xxxxxxxx","سيارات","الآن"),
                Ad("أرض للبيع 600م","120,000 ر.س","الشعفة","05xxxxxxxx","عقار","منذ ساعة"),
                Ad("غنم نعيمي 20 راس","18,000 ر.س","الشعفة","05xxxxxxxx","مواشي","اليوم")
            ))
        }

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#F5F6FA")) }

        // هيدر فخم
        val header = LinearLayout(this).apply {
            setBackgroundColor(Color.parseColor("#0D1B2A")); orientation = LinearLayout.VERTICAL; setPadding(40,80,40,40)
        }
        header.addView(TextView(this).apply { text="حراج الشعفة"; textSize=26f; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) })
        header.addView(TextView(this).apply { text="بيع وشراء في الشعفة وما حولها"; textSize=13f; setTextColor(Color.parseColor("#A0B1C5")) })
        root.addView(header)

        // فئات
        val catScroll = HorizontalScrollView(this)
        val catRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(20,20,20,10) }
        cats.forEach { c ->
            val chip = TextView(this).apply {
                text = c; textSize=14f; setPadding(40,20,40,20); gravity=Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply { cornerRadius=50f; setColor(if(c==filter) Color.parseColor("#0D1B2A") else Color.WHITE); setStroke(2, Color.parseColor("#E0E0E0")) }
                setTextColor(if(c==filter) Color.WHITE else Color.parseColor("#333333"))
                setOnClickListener { filter=c; recreate() }
            }
            val lp = LinearLayout.LayoutParams(-2,-2).apply { setMargins(10,0,10,0) }
            catRow.addView(chip, lp)
        }
        catScroll.addView(catRow)
        root.addView(catScroll)

        // لستة الإعلانات
        val rv = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@MainActivity) }
        adapter = AdAdapter(getFiltered()) { ad -> openContact(ad) }
        rv.adapter = adapter
        root.addView(rv, LinearLayout.LayoutParams(-1,-1).apply { weight=1f })

        // زر إضافة رهيب
        val fab = Button(this).apply {
            text = "+ نشر إعلان جديد"; textSize=16f; setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply { cornerRadius=100f; setColor(Color.parseColor("#FF6B00")) }
            setOnClickListener { showAddDialog() }
        }
        val fabWrap = LinearLayout(this).apply { setPadding(30,20,30,30); gravity=Gravity.CENTER; addView(fab, LinearLayout.LayoutParams(-1,140)) }
        root.addView(fabWrap)

        setContentView(root)
    }

    private fun getFiltered() = if(filter=="الكل") ads else ads.filter { it.cat==filter }.toMutableList()

    private fun showAddDialog(){
        val lay = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(40,20,40,20) }
        val eTitle = EditText(this).apply { hint="وش تبي تبيع؟ مثال: كامري 2020" }
        val ePrice = EditText(this).apply { hint="السعر" }
        val eCity = EditText(this).apply { hint="المدينة"; setText("الشعفة") }
        val ePhone = EditText(this).apply { hint="رقم الجوال"; inputType=android.text.InputType.TYPE_CLASS_PHONE }
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cats.filter { it!="الكل" })
        listOf(eTitle,ePrice,eCity,ePhone,spinner).forEach { lay.addView(it) }

        AlertDialog.Builder(this).setTitle("نشر إعلان في حراج الشعفة").setView(lay)
           .setPositiveButton("انشر الآن") { _,_ ->
                if(eTitle.text.isBlank()) return@setPositiveButton
                val ad = Ad(eTitle.text.toString(), ePrice.text.toString()+" ر.س", eCity.text.toString(), ePhone.text.toString(), spinner.selectedItem.toString(), "الآن")
                ads.add(0, ad); saveAds(); adapter.update(getFiltered()); Toast.makeText(this,"تم النشر!",0).show()
            }.setNegativeButton("إلغاء",null).show()
    }

    private fun openContact(ad: Ad){
        val opts = arrayOf("واتساب: ${ad.phone}","اتصال","نسخ الإعلان")
        AlertDialog.Builder(this).setTitle(ad.title).setMessage("${ad.price} - ${ad.city}\n${ad.cat}").setItems(opts){_,i->
            when(i){
                0 -> { try{ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${ad.phone.replace("0","966")}")))}catch(_:Exception){} }
                1 -> { try{ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.phone}")))}catch(_:Exception){} }
                2 -> { Toast.makeText(this,"تم النسخ: ${ad.title} ${ad.price}",1).show() }
            }
        }.show()
    }

    private fun saveAds(){ getSharedPreferences(prefsName,0).edit().putString("ads", Gson().toJson(ads)).apply() }
    private fun loadAds(){ try{ val json = getSharedPreferences(prefsName,0).getString("ads",null)?: return; val type = object: TypeToken<MutableList<Ad>>(){}.type; ads = Gson().fromJson(json, type) }catch(_:Exception){} }

    class AdAdapter(private var list: MutableList<Ad>, private val onClick:(Ad)->Unit): RecyclerView.Adapter<AdAdapter.VH>(){
        class VH(val card: LinearLayout): RecyclerView.ViewHolder(card)
        fun update(newList: MutableList<Ad>){ list=newList; notifyDataSetChanged() }
        override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): VH {
            val card = LinearLayout(p.context).apply {
                orientation=LinearLayout.VERTICAL; setPadding(30,30,30,30); setBackgroundColor(Color.WHITE)
                background = android.graphics.drawable.GradientDrawable().apply { cornerRadius=30f; setColor(Color.WHITE); setStroke(1, Color.parseColor("#EEEEEE")) }
            }
            val lp = RecyclerView.LayoutParams(-1,-2).apply { setMargins(30,15,30,15) }
            card.layoutParams = lp
            return VH(card)
        }
        override fun getItemCount() = list.size
        override fun onBindViewHolder(h: VH, pos: Int) {
            val ad = list[pos]; val ctx = h.card.context; h.card.removeAllViews()
            val top = LinearLayout(ctx).apply { orientation=LinearLayout.HORIZONTAL }
            top.addView(TextView(ctx).apply { text=ad.cat; textSize=10f; setPadding(20,8,20,8); setTextColor(Color.WHITE); background = android.graphics.drawable.GradientDrawable().apply { cornerRadius=20f; setColor(Color.parseColor("#0D1B2A")) } })
            top.addView(TextView(ctx).apply { text=" ${ad.time} • ${ad.city}"; textSize=11f; setTextColor(Color.GRAY) })
            h.card.addView(top)
            h.card.addView(TextView(ctx).apply { text=ad.title; textSize=16f; setTextColor(Color.parseColor("#111111")); setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0,15,0,0) })
            h.card.addView(TextView(ctx).apply { text=ad.price; textSize=18f; setTextColor(Color.parseColor("#FF6B00")); setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0,10,0,0) })
            h.card.addView(TextView(ctx).apply { text="📞 ${ad.phone}"; textSize=12f; setTextColor(Color.parseColor("#555555")); setPadding(0,10,0,0) })
            h.card.setOnClickListener { onClick(ad) }
        }
    }
}
