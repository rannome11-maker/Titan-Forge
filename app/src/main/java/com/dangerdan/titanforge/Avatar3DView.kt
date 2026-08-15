package com.dangerdan.titanforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/** Device-safe software 3D renderer using projected and depth-sorted 3D cuboids. */
class Avatar3DView(
    context: Context,
    private val heroIndex: Int,
    private val tier: Int,
    private val equipped: Set<String> = emptySet(),
    private val previewItem: String? = null
) : View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var angle=.32f; private var lastX=0f
    private val boxes=mutableListOf<Box>()
    private val started=System.currentTimeMillis()
    init { setBackgroundColor(Color.rgb(10,10,12)); isClickable=true }

    override fun onTouchEvent(event:MotionEvent):Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> lastX=event.x
            MotionEvent.ACTION_MOVE -> { angle+=(event.x-lastX)*.012f; lastX=event.x; invalidate() }
            MotionEvent.ACTION_UP -> performClick()
        }; return true
    }
    override fun performClick():Boolean { super.performClick(); return true }
    override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas); boxes.clear()
        val time=(System.currentTimeMillis()-started)/1000f
        if(previewItem!=null) buildItem(previewItem,0f,1.9f,0f,1.35f) else buildAvatar(time)
        drawScene(canvas); postInvalidateDelayed(32)
    }

    private fun buildAvatar(time:Float) {
        val bob=sin(time*1.8f)*.035f
        val cloth=palette(heroIndex); val armor=if(tier>=6) Color.rgb(180,125,30) else Color.rgb(68,73,82)
        box(0f,.04f,0f,4.7f,.08f,4.1f,Color.rgb(27,22,19)); box(0f,.12f,0f,1.55f,.14f,1.55f,Color.rgb(63,42,25))
        box(-.34f,.62f+bob,0f,.42f,1.05f,.48f,cloth); box(.34f,.62f+bob,0f,.42f,1.05f,.48f,cloth)
        box(-.34f,.12f+bob,.04f,.52f,.38f,.72f,armor); box(.34f,.12f+bob,.04f,.52f,.38f,.72f,armor)
        box(0f,1.78f+bob,0f,1.18f,1.35f,.62f,cloth); box(0f,1.22f+bob,.02f,1.25f,.22f,.68f,armor)
        box(0f,2.72f+bob,0f,.68f,.76f,.65f,Color.rgb(115,75,53))
        box(-.84f,1.78f+bob,0f,.38f,1.28f,.42f,cloth); box(.84f,1.78f+bob,0f,.38f,1.28f,.42f,cloth)
        val shoulder=.44f+tier*.055f
        box(-.82f,2.28f+bob,0f,shoulder,.34f,.72f,armor); box(.82f,2.28f+bob,0f,shoulder,.34f,.72f,armor)
        if(tier>=2) box(0f,2.96f+bob,0f,.78f,.35f,.72f,armor)
        if(tier>=3) box(0f,1.83f+bob,.38f,1.48f,1.62f,.10f,Color.rgb(68,5,11))
        if(tier>=4) { box(-.34f,3.42f+bob,0f,.13f,.72f,.13f,armor); box(.34f,3.42f+bob,0f,.13f,.72f,.13f,armor) }
        if(tier>=6) { box(-1.17f,1.65f,0f,.05f,2.8f,.05f,Color.rgb(224,147,24)); box(1.17f,1.65f,0f,.05f,2.8f,.05f,Color.rgb(224,147,24)) }
        if(tier>=8) box(0f,3.62f+bob,0f,1.05f,.08f,1.05f,Color.rgb(245,185,45))
        equipped.forEach { buildEquipped(it,bob) }
    }

    private fun buildEquipped(id:String,bob:Float) {
        when(id) {
            "iron_sword"->{ box(.98f,1.63f+bob,0f,.13f,1.75f,.16f,Color.LTGRAY); box(.98f,.78f+bob,0f,.62f,.12f,.16f,Color.rgb(85,45,15)) }
            "war_axe"->{ box(.98f,1.45f+bob,0f,.14f,1.9f,.14f,Color.rgb(80,45,20)); box(.98f,2.25f+bob,0f,.85f,.42f,.18f,Color.GRAY) }
            "storm_hammer"->{ box(.98f,1.42f+bob,0f,.16f,1.85f,.16f,Color.rgb(75,38,14)); box(.98f,2.25f+bob,0f,.78f,.52f,.48f,Color.rgb(55,70,88)) }
            "gold_armor"->box(0f,1.8f+bob,-.34f,1.27f,1.10f,.12f,Color.rgb(205,145,25))
            "shadow_cloak"->box(0f,1.55f+bob,.40f,1.58f,2.30f,.08f,Color.rgb(28,8,39))
            "war_crown"->{ box(0f,3.18f+bob,0f,.82f,.14f,.75f,Color.rgb(220,157,25)); box(0f,3.48f+bob,0f,.12f,.65f,.12f,Color.rgb(220,157,25)) }
            "ember_ring"->box(-1f,1.18f+bob,0f,.32f,.08f,.32f,Color.rgb(245,55,8))
            "winged_boots"->{ box(-.62f,.2f+bob,0f,.42f,.12f,.65f,Color.LTGRAY); box(.62f,.2f+bob,0f,.42f,.12f,.65f,Color.LTGRAY) }
        }
    }

    private fun buildItem(id:String,x:Float,y:Float,z:Float,s:Float) {
        when(id) {
            "iron_sword"->{ box(x,y,z,.14f*s,2.1f*s,.15f*s,Color.LTGRAY); box(x,y-.9f*s,z,.72f*s,.13f*s,.2f*s,Color.rgb(90,50,17)) }
            "war_axe"->{ box(x,y,z,.15f*s,2f*s,.15f*s,Color.rgb(80,44,15)); box(x,y+.72f*s,z,1.05f*s,.48f*s,.2f*s,Color.GRAY) }
            "storm_hammer"->{ box(x,y,z,.16f*s,2f*s,.16f*s,Color.rgb(77,40,13)); box(x,y+.72f*s,z,1f*s,.65f*s,.62f*s,Color.rgb(58,72,91)) }
            "gold_armor"->{ box(x,y,z,1.3f*s,1.5f*s,.58f*s,Color.rgb(205,145,25)); box(x-.78f*s,y+.48f*s,z,.5f*s,.36f*s,.7f*s,Color.rgb(205,145,25)); box(x+.78f*s,y+.48f*s,z,.5f*s,.36f*s,.7f*s,Color.rgb(205,145,25)) }
            "war_crown"->{ box(x,y,z,1.25f*s,.22f*s,1f*s,Color.rgb(220,157,25)); box(x,y+.52f*s,z,.18f*s,.9f*s,.18f*s,Color.rgb(220,157,25)) }
            "shadow_cloak"->box(x,y,z,1.35f*s,2f*s,.12f*s,Color.rgb(38,10,52))
            "ember_ring"->box(x,y,z,1.2f*s,.16f*s,1.2f*s,Color.rgb(245,55,8))
            else->{ box(x-.45f*s,y,z,.55f*s,.3f*s,1.1f*s,Color.LTGRAY); box(x+.45f*s,y,z,.55f*s,.3f*s,1.1f*s,Color.LTGRAY) }
        }
    }

    private fun box(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,color:Int) { boxes+=Box(x,y,z,sx,sy,sz,color) }
    private fun drawScene(canvas:Canvas) {
        val scale=width.coerceAtMost(height)*.115f; val cx=width/2f; val ground=height*.86f
        val faces=mutableListOf<Face>()
        boxes.forEach { b ->
            val pts=arrayOf(P(-.5f,-.5f,-.5f),P(.5f,-.5f,-.5f),P(.5f,.5f,-.5f),P(-.5f,.5f,-.5f),P(-.5f,-.5f,.5f),P(.5f,-.5f,.5f),P(.5f,.5f,.5f),P(-.5f,.5f,.5f)).map { p ->
                val x=p.x*b.sx; val z=p.z*b.sz; P(x*cos(angle)-z*sin(angle)+b.x,p.y*b.sy+b.y,x*sin(angle)+z*cos(angle)+b.z)
            }
            val sides=arrayOf(intArrayOf(0,1,2,3),intArrayOf(4,7,6,5),intArrayOf(0,4,5,1),intArrayOf(3,2,6,7),intArrayOf(1,5,6,2),intArrayOf(0,3,7,4))
            sides.forEachIndexed { i,side -> faces+=Face(side.map{pts[it]},shade(b.color,i)) }
        }
        faces.sortedBy { f -> f.p.sumOf { it.z.toDouble() }/f.p.size }.forEach { f ->
            val path=Path(); f.p.forEachIndexed { i,p -> val depth=1f+p.z*.055f; val px=cx+p.x*scale/depth; val py=ground-p.y*scale/depth; if(i==0) path.moveTo(px,py) else path.lineTo(px,py) }
            path.close(); paint.style=Paint.Style.FILL; paint.color=f.color; canvas.drawPath(path,paint)
            paint.style=Paint.Style.STROKE; paint.strokeWidth=1.5f; paint.color=Color.argb(130,230,190,90); canvas.drawPath(path,paint)
        }
    }
    private fun shade(c:Int,face:Int):Int { val f=floatArrayOf(.55f,.72f,1f,.82f,.66f,.48f)[face]; return Color.rgb((Color.red(c)*f).toInt().coerceIn(0,255),(Color.green(c)*f).toInt().coerceIn(0,255),(Color.blue(c)*f).toInt().coerceIn(0,255)) }
    private fun palette(i:Int)=listOf(Color.rgb(112,18,13),Color.rgb(26,54,92),Color.rgb(91,40,15),Color.rgb(62,68,75),Color.rgb(90,15,48),Color.rgb(25,83,62))[kotlin.math.abs(i)%6]
    private data class Box(val x:Float,val y:Float,val z:Float,val sx:Float,val sy:Float,val sz:Float,val color:Int)
    private data class P(val x:Float,val y:Float,val z:Float)
    private data class Face(val p:List<P>,val color:Int)
}
