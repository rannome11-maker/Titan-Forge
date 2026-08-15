package com.dangerdan.titanforge

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sin

/** Lightweight procedural dark-fantasy character and equipment renderer. */
class Avatar3DView(
    context: Context,
    heroIndex: Int,
    tier: Int,
    equipped: Set<String> = emptySet(),
    previewItem: String? = null
) : GLSurfaceView(context) {
    private val avatarRenderer = AvatarRenderer(heroIndex, tier, equipped, previewItem)
    private var lastX = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(avatarRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setBackgroundColor(0xFF090909.toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) avatarRenderer.rotation += (event.x-lastX)*0.6f
        lastX=event.x
        return true
    }
}

private class AvatarRenderer(
    private val hero: Int,
    private val tier: Int,
    private val gear: Set<String>,
    private val preview: String?
) : GLSurfaceView.Renderer {
    var rotation=18f
    private lateinit var cube: CubeMesh
    private val projection=FloatArray(16); private val view=FloatArray(16); private val vp=FloatArray(16)
    private var start=System.currentTimeMillis()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.025f,0.025f,0.03f,1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST); cube=CubeMesh()
    }

    override fun onSurfaceChanged(gl: GL10?, width:Int, height:Int) {
        GLES20.glViewport(0,0,width,height)
        Matrix.perspectiveM(projection,0,42f,width.toFloat()/height.coerceAtLeast(1),0.1f,100f)
        Matrix.setLookAtM(view,0,0f,2.1f,8.3f,0f,1.7f,0f,0f,1f,0f)
        Matrix.multiplyMM(vp,0,projection,0,view,0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val t=(System.currentTimeMillis()-start)/1000f
        if(preview!=null) drawItem(preview,0f,1.6f,0f,1.5f,rotation+t*12f) else drawAvatar(t)
    }

    private fun drawAvatar(t:Float) {
        val bob=sin(t*1.7f)*0.035f
        val base=palette(hero); val armor=floatArrayOf(0.11f+hero%3*.035f,0.12f,0.14f+hero%4*.025f,1f)
        val metal=if(tier>=6) floatArrayOf(.72f,.55f,.17f,1f) else floatArrayOf(.28f,.30f,.33f,1f)
        part(0f,.05f,0f,4.4f,.08f,4.4f,floatArrayOf(.07f,.065f,.06f,1f),0f)
        part(0f,.12f,0f,1.55f,.14f,1.55f,floatArrayOf(.16f,.12f,.08f,1f),rotation)
        // boots and legs
        part(-.34f,.58f+bob,0f,.42f,1.05f,.48f,armor,rotation); part(.34f,.58f+bob,0f,.42f,1.05f,.48f,armor,rotation)
        part(-.34f,.08f+bob,.04f,.52f,.38f,.72f,metal,rotation); part(.34f,.08f+bob,.04f,.52f,.38f,.72f,metal,rotation)
        // torso, belt, head
        part(0f,1.75f+bob,0f,1.18f,1.35f,.62f,base,rotation)
        part(0f,1.22f+bob,.02f,1.25f,.22f,.68f,metal,rotation)
        part(0f,2.72f+bob,0f,.68f,.76f,.65f,floatArrayOf(.36f,.25f,.19f,1f),rotation)
        // arms and tier-scaled pauldrons
        part(-.84f,1.77f+bob,0f,.38f,1.28f,.42f,base,rotation); part(.84f,1.77f+bob,0f,.38f,1.28f,.42f,base,rotation)
        val shoulder=.44f+tier*.055f
        part(-.82f,2.27f+bob,0f,shoulder,.34f,.72f,metal,rotation); part(.82f,2.27f+bob,0f,shoulder,.34f,.72f,metal,rotation)
        // evolution: helm, crown/horns, cape and aura pillars
        if(tier>=2) part(0f,2.93f+bob,0f,.78f,.35f,.72f,metal,rotation)
        if(tier>=3) part(0f,1.82f+bob,.35f,1.48f,1.55f,.10f,floatArrayOf(.20f,.015f,.02f,1f),rotation)
        if(tier>=4) { part(-.34f,3.43f+bob,0f,.13f,.72f,.13f,metal,rotation-18f); part(.34f,3.43f+bob,0f,.13f,.72f,.13f,metal,rotation+18f) }
        if(tier>=6) { val aura=floatArrayOf(.55f,.35f,.05f,1f); part(-1.18f,1.6f,0f,.035f,2.7f,.035f,aura,rotation); part(1.18f,1.6f,0f,.035f,2.7f,.035f,aura,rotation) }
        if(tier>=8) part(0f,3.58f+bob,0f,1.05f,.08f,1.05f,floatArrayOf(.83f,.62f,.12f,1f),rotation+t*24f)
        gear.forEachIndexed { i,id -> drawEquipped(id,i,t,bob) }
    }

    private fun drawEquipped(id:String,index:Int,t:Float,bob:Float) {
        when(id) {
            "iron_sword" -> { part(.98f,1.62f+bob,0f,.13f,1.75f,.16f,floatArrayOf(.55f,.57f,.60f,1f),rotation-12f); part(.98f,.78f+bob,0f,.62f,.12f,.16f,floatArrayOf(.20f,.12f,.05f,1f),rotation-12f) }
            "war_axe" -> { part(.98f,1.45f+bob,0f,.14f,1.9f,.14f,floatArrayOf(.22f,.12f,.06f,1f),rotation); part(.98f,2.25f+bob,0f,.85f,.42f,.18f,floatArrayOf(.42f,.44f,.46f,1f),rotation) }
            "storm_hammer" -> { part(.98f,1.42f+bob,0f,.16f,1.85f,.16f,floatArrayOf(.25f,.15f,.07f,1f),rotation); part(.98f,2.25f+bob,0f,.78f,.52f,.48f,floatArrayOf(.20f,.24f,.28f,1f),rotation) }
            "gold_armor" -> part(0f,1.78f+bob,-.33f,1.27f,1.10f,.12f,floatArrayOf(.72f,.51f,.10f,1f),rotation)
            "shadow_cloak" -> part(0f,1.55f+bob,.40f,1.58f,2.30f,.08f,floatArrayOf(.035f,.02f,.06f,1f),rotation)
            "war_crown" -> { part(0f,3.18f+bob,0f,.82f,.14f,.75f,floatArrayOf(.80f,.58f,.10f,1f),rotation); part(0f,3.48f+bob,0f,.12f,.65f,.12f,floatArrayOf(.80f,.58f,.10f,1f),rotation) }
            "ember_ring" -> part(-1.0f,1.18f+bob,0f,.32f,.08f,.32f,floatArrayOf(.85f,.16f,.03f,1f),rotation+t*80f)
            "winged_boots" -> { part(-.62f,.18f+bob,0f,.42f,.12f,.65f,floatArrayOf(.58f,.58f,.62f,1f),rotation); part(.62f,.18f+bob,0f,.42f,.12f,.65f,floatArrayOf(.58f,.58f,.62f,1f),rotation) }
        }
    }

    private fun drawItem(id:String,x:Float,y:Float,z:Float,s:Float,spin:Float) {
        when(id) {
            "iron_sword" -> { part(x,y,z,.14f*s,2.1f*s,.15f*s,floatArrayOf(.60f,.62f,.65f,1f),spin); part(x,y-.9f*s,z,.72f*s,.13f*s,.20f*s,floatArrayOf(.27f,.15f,.05f,1f),spin) }
            "war_axe" -> { part(x,y,z,.15f*s,2f*s,.15f*s,floatArrayOf(.25f,.14f,.06f,1f),spin); part(x,y+.72f*s,z,1.05f*s,.48f*s,.20f*s,floatArrayOf(.48f,.50f,.52f,1f),spin) }
            "storm_hammer" -> { part(x,y,z,.16f*s,2f*s,.16f*s,floatArrayOf(.23f,.12f,.05f,1f),spin); part(x,y+.72f*s,z,1.0f*s,.65f*s,.62f*s,floatArrayOf(.18f,.23f,.29f,1f),spin) }
            "gold_armor" -> { part(x,y,z,1.3f*s,1.5f*s,.58f*s,floatArrayOf(.72f,.51f,.10f,1f),spin); part(x-.78f*s,y+.48f*s,z,.5f*s,.36f*s,.7f*s,floatArrayOf(.72f,.51f,.10f,1f),spin); part(x+.78f*s,y+.48f*s,z,.5f*s,.36f*s,.7f*s,floatArrayOf(.72f,.51f,.10f,1f),spin) }
            else -> part(x,y,z,1.15f*s,1.45f*s,.32f*s,palette(id.hashCode()),spin)
        }
    }

    private fun part(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,color:FloatArray,ry:Float) {
        val model=FloatArray(16); val mvp=FloatArray(16)
        Matrix.setIdentityM(model,0); Matrix.translateM(model,0,x,y,z); Matrix.rotateM(model,0,ry,0f,1f,0f); Matrix.scaleM(model,0,sx,sy,sz)
        Matrix.multiplyMM(mvp,0,vp,0,model,0); cube.draw(mvp,color)
    }

    private fun palette(i:Int)=listOf(
        floatArrayOf(.22f,.035f,.025f,1f),floatArrayOf(.06f,.10f,.16f,1f),floatArrayOf(.15f,.07f,.03f,1f),
        floatArrayOf(.10f,.11f,.12f,1f),floatArrayOf(.16f,.03f,.08f,1f),floatArrayOf(.05f,.15f,.12f,1f)
    )[kotlin.math.abs(i)%6]
}

private class CubeMesh {
    private val vertices=floatArrayOf(
        -0.5f,-0.5f,0.5f, 0.5f,-0.5f,0.5f, 0.5f,0.5f,0.5f, -0.5f,0.5f,0.5f,
        -0.5f,-0.5f,-0.5f, -0.5f,0.5f,-0.5f, 0.5f,0.5f,-0.5f, 0.5f,-0.5f,-0.5f,
        -0.5f,0.5f,-0.5f, -0.5f,0.5f,0.5f, 0.5f,0.5f,0.5f, 0.5f,0.5f,-0.5f,
        -0.5f,-0.5f,-0.5f, 0.5f,-0.5f,-0.5f, 0.5f,-0.5f,0.5f, -0.5f,-0.5f,0.5f,
        0.5f,-0.5f,-0.5f, 0.5f,0.5f,-0.5f, 0.5f,0.5f,0.5f, 0.5f,-0.5f,0.5f,
        -0.5f,-0.5f,-0.5f, -0.5f,-0.5f,0.5f, -0.5f,0.5f,0.5f, -0.5f,0.5f,-0.5f)
    private val indices=byteArrayOf(0,1,2,0,2,3,4,5,6,4,6,7,8,9,10,8,10,11,12,13,14,12,14,15,16,17,18,16,18,19,20,21,22,20,22,23)
    private val vb=ByteBuffer.allocateDirect(vertices.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }
    private val ib=ByteBuffer.allocateDirect(indices.size).apply { put(indices); position(0) }
    private val program:Int
    init {
        val vs=GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also { GLES20.glShaderSource(it,"uniform mat4 uMVP; attribute vec3 aPos; varying float shade; void main(){ gl_Position=uMVP*vec4(aPos,1.0); shade=.70+aPos.y*.22+aPos.z*.08; }"); GLES20.glCompileShader(it) }
        val fs=GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also { GLES20.glShaderSource(it,"precision mediump float; uniform vec4 uColor; varying float shade; void main(){ gl_FragColor=vec4(uColor.rgb*shade,uColor.a); }"); GLES20.glCompileShader(it) }
        program=GLES20.glCreateProgram().also { GLES20.glAttachShader(it,vs); GLES20.glAttachShader(it,fs); GLES20.glLinkProgram(it) }
    }
    fun draw(mvp:FloatArray,color:FloatArray) {
        GLES20.glUseProgram(program); val p=GLES20.glGetAttribLocation(program,"aPos"); GLES20.glEnableVertexAttribArray(p)
        GLES20.glVertexAttribPointer(p,3,GLES20.GL_FLOAT,false,12,vb); GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program,"uMVP"),1,false,mvp,0)
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(program,"uColor"),1,color,0); GLES20.glDrawElements(GLES20.GL_TRIANGLES,indices.size,GLES20.GL_UNSIGNED_BYTE,ib)
        GLES20.glDisableVertexAttribArray(p)
    }
}
