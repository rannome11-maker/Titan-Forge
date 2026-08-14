package com.dangerdan.titanforge

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private val GOLD = Color.rgb(212,175,55)
private val BLACK = Color.rgb(9,9,9)
private val PANEL = Color.rgb(24,24,24)

private data class ShopItem(val id:String, val name:String, val icon:String, val type:String, val cost:Int)

class MainActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("forge", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private val male = listOf("Ares","Ronin","Titan","Warden","Berserker","Paladin","Ranger","Spartan","Revenant","Sentinel")
    private val female = listOf("Valkyrie","Nyx","Athena","Huntress","Oracle","Tempest","Ember","Siren","Raven","Sovereign")
    private val shopItems = listOf(
        ShopItem("iron_sword","Iron Sword","⚔","Weapon",25), ShopItem("war_axe","War Axe","🪓","Weapon",60),
        ShopItem("storm_hammer","Storm Hammer","🔨","Weapon",120), ShopItem("gold_armor","Golden Armor","🛡","Armor",80),
        ShopItem("shadow_cloak","Shadow Cloak","♠","Clothing",55), ShopItem("war_crown","War Crown","♛","Accessory",150),
        ShopItem("ember_ring","Ember Ring","◉","Accessory",40), ShopItem("winged_boots","Winged Boots","♜","Clothing",70)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        show(if (prefs.getBoolean("setup", false)) ::station else ::welcome)
    }

    private fun show(screen: () -> Unit) {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,28,32,28); setBackgroundColor(BLACK) }
        setContentView(ScrollView(this).apply { setBackgroundColor(BLACK); addView(root) })
        if (prefs.getBoolean("setup",false)) topNav()
        screen()
    }

    private fun topNav() {
        LinearLayout(this).apply {
            gravity=Gravity.END or Gravity.CENTER_VERTICAL
            addView(TextView(this@MainActivity).apply { text="◈ ${prefs.getInt("coins",0)} COINS"; setTextColor(GOLD); textSize=15f; setPadding(8,8,24,8) })
            addView(Button(this@MainActivity).apply { text="⌂ HOME"; setTextColor(BLACK); setBackgroundColor(GOLD); setOnClickListener { show(::station) } })
        }.also { root.addView(it,LinearLayout.LayoutParams(-1,-2)) }
    }

    private fun title(text: String, size: Float = 30f) = TextView(this).apply {
        this.text=text; textSize=size; setTextColor(GOLD); setTypeface(typeface,Typeface.BOLD); gravity=Gravity.CENTER; setPadding(4,16,4,16)
    }.also(root::addView)
    private fun copy(text: String, size: Float = 16f) = TextView(this).apply { this.text=text; textSize=size; setTextColor(Color.WHITE); setPadding(4,8,4,12) }.also(root::addView)
    private fun button(text: String, action: () -> Unit) = Button(this).apply { this.text=text; setTextColor(BLACK); setBackgroundColor(GOLD); setTypeface(typeface,Typeface.BOLD); setOnClickListener{action()}; setPadding(12,12,12,12) }.also { root.addView(it, LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,10,0,10)}) }
    private fun field(hint: String, numeric: Boolean=false) = EditText(this).apply { this.hint=hint; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); setBackgroundColor(PANEL); setPadding(22,18,22,18); if(numeric) inputType=2 }.also { root.addView(it,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,7,0,7)}) }

    private fun welcome() {
        Space(this).also { root.addView(it, LinearLayout.LayoutParams(1,100)) }
        title("TITAN FORGE",42f); copy("BUILD THE BODY. EVOLVE THE WARRIOR.",18f)
        copy("This is not a calorie counter. Every completed trial strengthens a living avatar. Consistency unlocks armor, weapons, rank and power.")
        button("ENTER THE FORGE") { chooseSex() }
    }

    private fun chooseSex() { show { title("CHOOSE YOUR PATH"); copy("Choose an avatar collection. This does not affect workout recommendations."); button("MALE HEROES") { chooseHero(male,"Male") }; button("FEMALE HEROES") { chooseHero(female,"Female") } } }

    private fun chooseHero(heroes: List<String>, sex: String) { show {
        title("CHOOSE YOUR WARRIOR"); copy("Each warrior has 8 forms—from Initiate to Ascendant.")
        heroes.forEachIndexed { i, hero ->
            val b=Button(this).apply { text="${glyph(i,1)}  $hero  •  INITIATE"; textSize=17f; setTextColor(if(i%2==0) GOLD else Color.WHITE); setBackgroundColor(PANEL); setOnClickListener { prefs.edit().putString("sex",sex).putString("hero",hero).putInt("heroIndex",i).apply(); profile() } }
            root.addView(b,LinearLayout.LayoutParams(-1,130).apply{setMargins(0,6,0,6)})
        }
    } }

    private fun profile() { show {
        title("KNOW YOUR STARTING POINT")
        val age=field("Age",true); val weight=field("Weight (lb)",true); val days=field("Training days per week (2–7)",true)
        copy("Primary objective")
        val goal=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Build muscle","Lose fat","Strength","Mobility & conditioning")) }.also(root::addView)
        copy("Target area")
        val target=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Full body","Chest & arms","Back & shoulders","Core","Legs & glutes")) }.also(root::addView)
        copy("Experience")
        val level=Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Beginner","Intermediate","Advanced")) }.also(root::addView)
        button("FORGE MY PROGRAM") {
            if(age.text.isBlank()||weight.text.isBlank()) Toast.makeText(this,"Enter age and weight",Toast.LENGTH_SHORT).show() else {
                prefs.edit().putInt("age",age.text.toString().toInt()).putInt("weight",weight.text.toString().toInt()).putInt("days",days.text.toString().toIntOrNull()?.coerceIn(2,7)?:4).putString("goal",goal.selectedItem.toString()).putString("target",target.selectedItem.toString()).putString("level",level.selectedItem.toString()).apply(); reminder()
            }
        }
    } }

    private fun reminder() { show {
        title("DAILY SUMMONS"); copy("When should Titan Forge call you back to battle?")
        listOf("MORNING • 8:00 AM" to 8,"NOON • 12:00 PM" to 12,"NIGHT • 8:00 PM" to 20).forEach { (label,hour) -> button(label) { schedule(hour); finishSetup(hour) } }
        button("NOT NOW") { finishSetup(-1) }
    } }

    private fun finishSetup(hour:Int) { prefs.edit().putBoolean("setup",true).putInt("reminder",hour).apply(); show(::station) }

    private fun station() {
        weeklyEvolution()
        val hero=prefs.getString("hero","Titan")!!; val tier=prefs.getInt("tier",1); val xp=prefs.getInt("xp",0)
        title("MAIN STATION",28f)
        avatarCard(hero,tier)
        copy("POWER  ${100+tier*85+xp/10}     XP  $xp     STREAK  ${prefs.getInt("streak",0)}\nWEEKLY VICTORIES  ${prefs.getInt("weekDone",0)} / ${prefs.getInt("days",4)}")
        button("TODAY'S WORKOUT") { show(::dashboard) }
        button("STORE") { show(::store) }
        button("SPECIAL QUESTS") { show(::quests) }
        button("INVENTORY") { show(::inventory) }
        button("WORLD CHAT") { show(::worldChat) }
        button("FULL CHARACTER STATS") { show(::stats) }
    }

    private fun avatarCard(hero:String, tier:Int) {
        val equipped=equippedItems().mapNotNull { id -> shopItems.find { it.id==id } }
        val gear=if(equipped.isEmpty()) "NO EQUIPMENT" else equipped.joinToString("  ") { it.icon }
        TextView(this).apply {
            text="${glyph(prefs.getInt("heroIndex",0),tier)}\n\n$hero\n${tierName(tier)}\n$gear"
            textSize=30f; gravity=Gravity.CENTER; setTextColor(GOLD); setBackgroundColor(PANEL); setPadding(10,40,10,40)
        }.also(root::addView)
    }

    private fun dashboard() {
        weeklyEvolution()
        val hero=prefs.getString("hero","Titan")!!; val tier=prefs.getInt("tier",1); val xp=prefs.getInt("xp",0); val streak=prefs.getInt("streak",0)
        title("TODAY'S FORGE",26f); copy("${LocalDate.now()}  •  DAY ${streak+1}",14f)
        avatarCard(hero,tier)
        copy("POWER  ${100+tier*85+xp/10}     XP  $xp     STREAK  $streak")
        ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply { max=700; progress=xp%700; progressTintList=android.content.res.ColorStateList.valueOf(GOLD) }.also(root::addView)
        title("TODAY'S TRIALS",22f)
        val exercises=routine(); val today=LocalDate.now().toString(); var completed=0
        exercises.forEachIndexed { i, ex ->
            val key="done_${today}_$i"; CheckBox(this).apply { text=ex; textSize=17f; setTextColor(Color.WHITE); buttonTintList=android.content.res.ColorStateList.valueOf(GOLD); isChecked=prefs.getBoolean(key,false); if(isChecked) completed++; setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key,checked).apply() } }.also { root.addView(it,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,5,0,5)}) }
        }
        button(if(completed==exercises.size) "TRIALS COMPLETE" else "CLAIM TODAY'S VICTORY") { claim(exercises.size) }
        button("VIEW CHARACTER STATS") { stats() }
    }

    private fun routine(): List<String> {
        val target=prefs.getString("target","Full body")!!; val level=prefs.getString("level","Beginner")!!; val reps=if(level=="Beginner") "3 × 10" else if(level=="Intermediate") "4 × 10" else "5 × 8"
        val base=when(target){
            "Chest & arms"->listOf("Push-ups — $reps","Floor press — $reps","Chair dips — 3 × 8","Curls — $reps")
            "Back & shoulders"->listOf("Rows — $reps","Pike push-ups — 3 × 8","Reverse fly — $reps","Superman hold — 3 × 30 sec")
            "Core"->listOf("Plank — 3 × 30 sec","Dead bug — $reps","Mountain climbers — 3 × 20","Leg raises — 3 × 10")
            "Legs & glutes"->listOf("Squats — $reps","Reverse lunges — 3 × 10/side","Glute bridge — $reps","Calf raises — 3 × 20")
            else->listOf("Squats — $reps","Push-ups — $reps","Rows — $reps","Plank — 3 × 30 sec")
        }
        return listOf("Warm-up march — 5 min")+base+"Cooldown mobility — 5 min"
    }

    private fun claim(total:Int) {
        val today=LocalDate.now().toString(); val complete=(0 until total).count{prefs.getBoolean("done_${today}_$it",false)}
        if(complete<total) { Toast.makeText(this,"Complete all trials first: $complete/$total",Toast.LENGTH_SHORT).show(); return }
        if(prefs.getString("claimed","")==today) { Toast.makeText(this,"Victory already claimed",Toast.LENGTH_SHORT).show(); return }
        val yesterday=LocalDate.now().minusDays(1).toString(); val streak=if(prefs.getString("lastDay","")==yesterday) prefs.getInt("streak",0)+1 else 1
        prefs.edit().putString("claimed",today).putString("lastDay",today).putInt("streak",streak).putInt("xp",prefs.getInt("xp",0)+100).putInt("weekDone",prefs.getInt("weekDone",0)+1).putInt("coins",prefs.getInt("coins",0)+50).apply()
        Toast.makeText(this,"+100 XP • +50 DAILY COINS",Toast.LENGTH_LONG).show(); show(::station)
    }

    private fun weeklyEvolution() {
        val week=LocalDate.now().get(WeekFields.of(Locale.US).weekOfWeekBasedYear())
        val saved=prefs.getInt("week",week)
        if(saved!=week) { val target=prefs.getInt("days",4); val done=prefs.getInt("weekDone",0); val tier=prefs.getInt("tier",1); prefs.edit().putInt("week",week).putInt("weekDone",0).putInt("tier",if(done>=target) min(8,tier+1) else tier).apply() }
        else if(!prefs.contains("week")) prefs.edit().putInt("week",week).apply()
    }

    private fun stats() { show {
        val tier=prefs.getInt("tier",1); title("WARRIOR RECORD"); copy("${prefs.getString("hero","Titan")} • ${tierName(tier)}")
        copy("POWER: ${100+tier*85+prefs.getInt("xp",0)/10}\nTOTAL XP: ${prefs.getInt("xp",0)}\nCURRENT STREAK: ${prefs.getInt("streak",0)} days\nTHIS WEEK: ${prefs.getInt("weekDone",0)} / ${prefs.getInt("days",4)} victories\nGOAL: ${prefs.getString("goal","")}\nTARGET: ${prefs.getString("target","")}\nLEVEL: ${prefs.getString("level","")}")
        title("EVOLUTION ROAD",22f); (1..8).forEach { copy("${if(it<=tier) "◆" else "◇"}  $it  ${tierName(it)}",17f) }; button("RETURN TO STATION") { show(::station) }
    } }

    private fun store() {
        title("FORGE STORE"); copy("Spend coins earned from workouts and quests. Purchased gear appears on your avatar when equipped.")
        val owned=ownedItems()
        shopItems.forEach { item ->
            val isOwned=item.id in owned
            button("${item.icon}  ${item.name} • ${item.type} • ${if(isOwned) "OWNED" else "${item.cost} COINS"}") {
                if(isOwned) equip(item) else buy(item)
            }
        }
    }

    private fun buy(item:ShopItem) {
        val coins=prefs.getInt("coins",0)
        if(coins<item.cost) { Toast.makeText(this,"You need ${item.cost-coins} more coins",Toast.LENGTH_SHORT).show(); return }
        val owned=ownedItems()+item.id
        prefs.edit().putInt("coins",coins-item.cost).putString("owned",owned.joinToString(",")).apply()
        equip(item)
    }

    private fun equip(item:ShopItem) {
        val equipped=equippedItems().filterNot { id -> shopItems.find { it.id==id }?.type==item.type }.toMutableSet()
        if(item.id !in equipped) equipped.add(item.id)
        prefs.edit().putString("equipped",equipped.joinToString(",")).apply()
        Toast.makeText(this,"${item.name} equipped",Toast.LENGTH_SHORT).show(); show(::store)
    }

    private fun inventory() {
        title("INVENTORY"); copy("◈ ${prefs.getInt("coins",0)} COINS\nQuest points: ${prefs.getInt("questPoints",0)}")
        val owned=ownedItems()
        if(owned.isEmpty()) copy("Your vault is empty. Earn coins and visit the Forge Store.")
        else shopItems.filter { it.id in owned }.forEach { item ->
            val equipped=item.id in equippedItems()
            button("${item.icon}  ${item.name} • ${if(equipped) "EQUIPPED" else "TAP TO EQUIP"}") { equip(item) }
        }
        title("EVENT VAULT",22f); copy("Limited event items, seasonal trophies, and future quest rewards will be stored here.")
    }

    private fun quests() {
        title("SPECIAL QUESTS")
        val points=prefs.getInt("questPoints",0)
        copy("EVERY REP = 1 POINT\nEVERY 10 POINTS = 1 COIN\n\nLifetime quest points: $points")
        listOf("Push-ups","Sit-ups","Squats","Lunges","Burpees","Pull-ups","Other exercise").forEach { exercise ->
            button("LOG $exercise".uppercase()) { repEntry(exercise) }
        }
    }

    private fun repEntry(exercise:String) {
        val input=EditText(this).apply { hint="Number of verified reps"; inputType=2; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY) }
        android.app.AlertDialog.Builder(this).setTitle(exercise).setView(input).setNegativeButton("CANCEL",null).setPositiveButton("ADD") { _,_ ->
            val reps=input.text.toString().toIntOrNull()?.coerceIn(1,1000) ?: 0
            if(reps>0) {
                val old=prefs.getInt("questPoints",0); val gained=(old+reps)/10-old/10
                prefs.edit().putInt("questPoints",old+reps).putInt("coins",prefs.getInt("coins",0)+gained).apply()
                Toast.makeText(this,"+$reps points • +$gained coins",Toast.LENGTH_LONG).show(); show(::quests)
            }
        }.show()
    }

    private fun worldChat() {
        title("WORLD CHAT")
        copy("GLOBAL FORGE • LIVE BOARD",14f)
        TextView(this).apply { text="WORLD CHAT BACKEND REQUIRED\n\nThe chat interface is ready, but public messages will remain locked until secure accounts, moderation, reporting, and a hosted real-time database are connected."; setTextColor(Color.LTGRAY); textSize=17f; setBackgroundColor(PANEL); setPadding(24,30,24,30) }.also(root::addView)
        val message=field("Message the world...")
        button("SEND TO WORLD") { Toast.makeText(this,"World Chat is not online yet",Toast.LENGTH_SHORT).show(); message.text.clear() }
    }

    private fun ownedItems()=prefs.getString("owned","")!!.split(",").filter { it.isNotBlank() }.toSet()
    private fun equippedItems()=prefs.getString("equipped","")!!.split(",").filter { it.isNotBlank() }.toSet()

    private fun tierName(t:Int)=listOf("","INITIATE","IRONBOUND","VANGUARD","WARLORD","MYTHIC","CELESTIAL","IMMORTAL","ASCENDANT")[t.coerceIn(1,8)]
    private fun glyph(i:Int,t:Int):String { val weapons=listOf("†","⚔","♜","⛨","ϟ","⌁","➶","Λ","☠","◆"); return "${"✦".repeat(max(1,t/2))} ${weapons[i%weapons.size]} ${if(t>=4) "♛" else "◉"} ${"✦".repeat(max(1,t/2))}" }

    private fun schedule(hour:Int) {
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),117)
        val alarm=getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending=PendingIntent.getBroadcast(this,117,Intent(this,ReminderReceiver::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val whenAt=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); if(timeInMillis<=System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1) }
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,whenAt.timeInMillis,AlarmManager.INTERVAL_DAY,pending)
    }
}
