package com.example.a3dfc

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.name_animal.*
import org.w3c.dom.Text
import java.lang.Exception
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener {

    var arrayView: Array<View>? = null
    private var arFragment: ArFragment? = null
    private var selected: Int = 1 //Default value

    private lateinit var bearRendereable: ModelRenderable
    private lateinit var catRendereable: ModelRenderable
    private lateinit var dogRendereable: ModelRenderable
    private lateinit var cowRendereable: ModelRenderable
    private lateinit var elephantRendereable: ModelRenderable
    private lateinit var ferretRendereable: ModelRenderable
    private lateinit var hippopotamusRendereable: ModelRenderable
    private lateinit var horseRendereable: ModelRenderable
    private lateinit var koala_bearRendereable: ModelRenderable
    private lateinit var lionRendereable: ModelRenderable
    private lateinit var reindeerRendereable: ModelRenderable
    private lateinit var wolverineRendereable: ModelRenderable

    private lateinit var animalName: ViewRenderable
    private lateinit var animalName2: ViewRenderable


    ///* Sensors ------------
    private lateinit var sensorManager: SensorManager
    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null
    //*/ end of sensors ----------

    //lateinit var textToSpeech: TextToSpeech
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            if (inputMessage.what == 0) {
                val msg = inputMessage.obj.toString() //.take(30)
                //textviewfetch.text = msg //inputMessage.obj.toString()
                GlobalModel.testi = msg
            }
            if (inputMessage.what == 1) {
                val msg = inputMessage.obj.toString() //.take(30)
                GlobalModel.testi2 = msg
                textviewfetch.text = "dddd" //inputMessage.obj.toString()
            }

            //Setup AR
            arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_ux_fragment) as ArFragment

            setArrayView()
            setClickListener()
            setupModel()

            arFragment?.setOnTapArPlaneListener { hitResult, _, _ ->

                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment?.arSceneView?.scene)

                createModel(anchorNode, selected)

            }
        }
    }
    var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        tts = TextToSpeech(this, this)
//        fun speakOut() {
//            val text = "Holla holla get dolla!"
//            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
//        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Check internet connection
        if (isNetworkAvailable()) {
            val myRunnable = Conn(mHandler)
            val myThread = Thread(myRunnable)
            myThread.start()
        }

        // DialogBox Intro --
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(R.string.intro).setPositiveButton(R.string.intro_ok, DialogInterface.OnClickListener { dialog, id -> }).show()

        // Sensors--
        this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.v("Total sensors",""+deviceSensors.size)
        deviceSensors.forEach {
            Log.v("Sensor name", "" + it)
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // You can register listener to get data and use them.
            Log.v("-----SUCCESS-----","Gyroscope success")
        } else {
            Log.v("-----FAILURE-----","No sensor found")
        }

        if (mSensors == null) {
            mSensors = if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            } else {
                Log.v("-----FAILURE-----", "No sensor found")
                null
            }
        }
        //End of sensors--

        //Text to speech button
        speak_button.setOnClickListener {
            // TextToSpeechFunction()
            Toast.makeText(applicationContext, "Press the button below and name a color.\nFor example: 'RED'", Toast.LENGTH_LONG).show()
        }

        voice_button.setOnClickListener{

            if (selected == 1) {
                val text = GlobalModel.testi
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
            } else if (selected == 2) {
                val text = GlobalModel.testi2
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
            } else {
                val text = "Place a animal model"
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

            }
            //val text = "Holla holla get dolla!"
            //tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
    override fun onSensorChanged(p0: SensorEvent?) {
        Log.v("----onSensorChanged----","p0!!.values[0]")
        //Sensor change value
        val data = p0!!.values[0]
        val data2 = p0.values[1]
        val data3 = p0.values[2]

        val x = "%.4f".format(data)
        val y = "%.4f".format(data2)
        val z = "%.4f".format(data3)

        val r = 1
        val g = 1
        val b = 1

        //val red = r*data
        //val green = g*data2
        val blue = b*data3
        sensor.text = ("r: $x   g: $y   b: $z")

        if (blue > 3) {
            direction.text = "left"
        } else if (blue < -3) {
            direction.text = "right"
        }

        if (p0.sensor.type == Sensor.TYPE_GYROSCOPE) {
            Log.v("----Sensor data----","$data")
            Log.v("----Sensor data----","$data2")
            Log.v("----Sensor data----","$data3")
        } else {
            Log.v("-----FAILURE-----","No sensor data")
            Toast.makeText(this, "#####FAIL #####", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.v("---onAccuracyChanged---","onAccuracyChanged Started!!!!!]")
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mSensors, 999999998,999999999)//SensorManager.SENSOR_DELAY_NORMAL
        //        Register the sensor on resume of the activity

    }

    override fun onPause() {
        super.onPause()
        Log.v("-----onPause-----","unregisterListener")
        sensorManager.unregisterListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK || null != data) {
                val res: ArrayList<String> = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                textview.text = res[0]
                GlobalModel.speechText = res[0].toLowerCase(Locale.US)
            }
        }

        //Set action based on speech
        if (GlobalModel.speechText == "red") {
            main.setBackgroundColor(Color.RED)
        } else if (GlobalModel.speechText == "green") {
            main.setBackgroundColor(Color.GREEN)
        } else if (GlobalModel.speechText == "blue") {
            main.setBackgroundColor(Color.BLUE)
        } else if (GlobalModel.speechText == "black") {
            main.setBackgroundColor(Color.BLACK)
        } else if (GlobalModel.speechText == "white") {
            main.setBackgroundColor(Color.WHITE)
        } else if (GlobalModel.speechText == "next") {
            Toast.makeText(applicationContext,"Next...", Toast.LENGTH_LONG).show()
        } else if (GlobalModel.speechText == "previous") {
            Toast.makeText(applicationContext,"Previous...", Toast.LENGTH_LONG).show()
        }
    }

    private fun SpeechFunction() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US) //Locale.getDefault()
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something...")
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (exp: ActivityNotFoundException)
        {
            Toast.makeText(applicationContext,"Speech Not Supported...", Toast.LENGTH_LONG).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = this.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected ?: false
    }


    //----- old below

    private fun setupModel() {
        ModelRenderable.builder()
            .setSource(this, R.raw.bear)
            .build()
            .thenAccept { renderable -> bearRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.dog)
            .build()
            .thenAccept { renderable -> dogRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.cat)
            .build()
            .thenAccept { renderable -> catRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.cow)
            .build()
            .thenAccept { renderable -> cowRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.elephant)
            .build()
            .thenAccept { renderable -> elephantRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.ferret)
            .build()
            .thenAccept { renderable -> ferretRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.hippopotamus)
            .build()
            .thenAccept { renderable -> hippopotamusRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.horse)
            .build()
            .thenAccept { renderable -> horseRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.koala_bear)
            .build()
            .thenAccept { renderable -> koala_bearRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.lion)
            .build()
            .thenAccept { renderable -> lionRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.reindeer)
            .build()
            .thenAccept { renderable -> reindeerRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        ModelRenderable.builder()
            .setSource(this, R.raw.wolverine)
            .build()
            .thenAccept { renderable -> wolverineRendereable = renderable }
            .exceptionally {
                Toast.makeText(this, "Load failed ${it.message}", Toast.LENGTH_SHORT).show()
                null
            }

        val inflater:LayoutInflater = LayoutInflater.from(applicationContext)
        val view = inflater.inflate(R.layout.name_animal, root_layout, false)
        val textView : TextView = view?.findViewById(R.id.nameAnimal) as TextView
        textView.text = GlobalModel.testi

        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> animalName = renderable }

        val inflater2:LayoutInflater = LayoutInflater.from(applicationContext)
        val view2 = inflater2.inflate(R.layout.name_animal2, root_layout, false)
        val textView2 : TextView = view2?.findViewById(R.id.nameAnimal2) as TextView
        textView2.text = GlobalModel.testi2

        ViewRenderable.builder()
            .setView(this, view2)
            .build()
            .thenAccept { renderable -> animalName2 = renderable }



    }

    private fun createModel(anchorNode: AnchorNode, selected: Int) {
        val renderableNode = TransformableNode(arFragment?.transformationSystem)
        renderableNode.setParent(anchorNode)
        when (selected) {
            1 -> {
                renderableNode.renderable = bearRendereable
                renderableNode.select()

                val nameView = TransformableNode(arFragment?.transformationSystem)
                nameView.localPosition = Vector3(0f, renderableNode.localPosition.y + 0.5f, 0f)
                nameView.setParent(anchorNode)
                nameView.renderable = animalName
                nameView.select()
            }
            2 ->{
                renderableNode.renderable = catRendereable
                renderableNode.select()
                val nameView = TransformableNode(arFragment?.transformationSystem)
                nameView.localPosition = Vector3(0f, renderableNode.localPosition.y + 0.5f, 0f)
                nameView.setParent(anchorNode)
                nameView.renderable = animalName2
                nameView.select()
            }
            3 ->{
                renderableNode.renderable = dogRendereable
                renderableNode.select()
            }
            4 ->{
                renderableNode.renderable = cowRendereable
                renderableNode.select()
            }
            5 ->{
                renderableNode.renderable = elephantRendereable
                renderableNode.select()
            }
            6 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            7 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            8 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            9 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            10 ->{
                renderableNode.renderable = ferretRendereable
                renderableNode.select()
            }
            11 ->{
                renderableNode.renderable = reindeerRendereable
                renderableNode.select()
            }
            12 ->{
                renderableNode.renderable = wolverineRendereable
                renderableNode.select()
            }
        }
    }


    private fun setArrayView() {
        arrayView = arrayOf(
            bear
            , cat
            , dog
            , cow
            , elephant
            , ferret
            , hippopotamus
            , horse
            , koala_bear
            , lion
            , reindeer
            , wolverine
        )
    }

    private fun setClickListener() {
        for (view in arrayView!!) {
            view.setOnClickListener { thisView ->
                setBackground(thisView.id)
                when (thisView.id) {
                    R.id.bear ->            selected = 1
                    R.id.cat ->             selected = 2
                    R.id.dog ->             selected = 3
                    R.id.cow ->             selected = 4
                    R.id.elephant ->        selected = 5
                    R.id.ferret ->          selected = 6
                    R.id.hippopotamus ->    selected = 7
                    R.id.horse ->           selected = 8
                    R.id.koala_bear ->      selected = 9
                    R.id.lion ->            selected = 10
                    R.id.reindeer ->        selected = 11
                    R.id.wolverine ->       selected = 12
                }
                Log.d("asdf", "My animal is: $selected")
            }
        }
    }

    private fun setBackground(id: Int) {
        for (view in arrayView!!) {
            if (view.id == id) {
                view.setBackgroundResource(R.drawable.animal_button)
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("asdf", "Lang error")
            } else {
                voice_button.isEnabled = true
            }
        } else {
            Log.d("asdf", "Text to Speech init fail")
        }
    }
}
// End of MainAvtivity

object GlobalModel {
    var position = 0 // for wiki texts
    var speechText = "" // for speech to text placeholder
    var testi = ""
    var testi2 = ""
}

class Conn(mHand: Handler?) : Runnable {
    val myHandler = mHand
    override fun run() {
        try {
            val res = URL("https://users.metropolia.fi/~jaripie/karhu").readText()
            Log.d("res", res)
            val msg = myHandler?.obtainMessage()
            msg?.what = 0
            msg?.obj = res
            myHandler?.sendMessage(msg)

            val res2 = URL("https://users.metropolia.fi/~jaripie/cat").readText()
            Log.d("res", res)
            val msg2 = myHandler?.obtainMessage()
            msg2?.what = 1
            msg2?.obj = res2
            myHandler?.sendMessage(msg2)

        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }
}


/*
http://users.metropolia.fi/~jaripie/karhu
http://users.metropolia.fi/~jaripie/cat
http://users.metropolia.fi/~jaripie/cow
http://users.metropolia.fi/~jaripie/dog
http://users.metropolia.fi/~jaripie/elephant
http://users.metropolia.fi/~jaripie/reindeer
http://users.metropolia.fi/~jaripie/wolverine
http://users.metropolia.fi/~jaripie/lion

class ExampleClass {
    companion object{
        fun writeText(textValue:String,mainActivity:MainActivity) {
            mainActivity.testMessage.text = textValue
        }
    }
}

                //addName(anchorNode, renderableNode, "Wolverine")

               // val inflater:LayoutInflater = LayoutInflater.from(applicationContext)
                //val view = inflater.inflate(R.layout.name_animal, root_layout, true)
                //val textView : TextView = view?.findViewById(R.id.nameAnimal) as TextView
                //textView.visibility = View.INVISIBLE
                //textView.text = GlobalModel.testi
                //textView.setTextColor(Color.RED)
                //textView.visibility = View.INVISIBLE


        //textView.visibility = View.INVISIBLE
        //setContentView(R.layout.name_animal)
        //nameAnimal.text = GlobalModel.testi


        /*val inflater:LayoutInflater = LayoutInflater.from(applicationContext)
        val view = inflater.inflate(R.layout.name_animal, root_layout, true)
        val textView : TextView = view?.findViewById(R.id.nameAnimal) as TextView
        textView.text = GlobalModel.testi
        textView.setTextColor(Color.RED)

             setContentView(R.layout.name_animal)
        val ss = findViewById(R.id.nameAnimal) as TextView
        ss.setTextColor(Color.GREEN)
        ss.text = "sss"
        Log.d("AAAA", "${ss.text}")



        // Get the LayoutInflater from Context
        val layoutInflater:LayoutInflater = LayoutInflater.from(applicationContext)


        // Inflate the layout using LayoutInflater
        val view: View = layoutInflater.inflate(
            R.layout.name_animal, // Custom view/ layout
            root_layout, // Root layout to attach the view
            false // Attach with root layout or not
        )
        // Find the text view from custom layout
        val label = view.findViewById<TextView>(R.id.planetInfoCard)

        // Set the text of custom view's text view widget
        label.text = "Cute Flower."

        // Finally, add the view/custom layout to the activity root layout
        root_layout.addView(view,0)
*/


        textView.text = GlobalModel.testi2

        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> animalName2 = renderable }

 */