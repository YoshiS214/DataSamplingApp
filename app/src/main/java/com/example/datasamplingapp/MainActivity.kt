package com.example.datasamplingapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.BufferedReader
import java.io.FileInputStream
import kotlin.properties.Delegates
import java.util.Calendar
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URI

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager : SensorManager by Delegates.notNull<SensorManager>()
    private var accelerations : ArrayList<DoubleArray>? = ArrayList()
    private var linearAccelerations : ArrayList<DoubleArray>? = ArrayList()
    //private var orientations : ArrayList<DoubleArray>? = ArrayList()

    private val accelerometerReading = FloatArray(3)
    private val linearReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var time: Calendar by Delegates.notNull<Calendar>()
    private var uri : Uri by Delegates.notNull<Uri>()
    private lateinit var textLog : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        time = Calendar.getInstance() as Calendar


        val textNote = findViewById<EditText>(R.id.text_note) as EditText
        val startButton = findViewById<Button>(R.id.start_record) as Button
        val stopButton = findViewById<Button>(R.id.stop_record) as Button
        val resetButton = findViewById<Button>(R.id.reset_data) as Button
        val showButton = findViewById<Button>(R.id.show_data) as Button
        textLog = findViewById<TextView>(R.id.text_log) as TextView

        var txt = ""

        //createCSV()
        stopButton.isClickable = false
        disableSensor()

        startButton.setOnClickListener { v ->
            updateLog("Started!!", true)
            initialise()
            enableSensor()
            stopButton.isClickable = true
            startButton.isClickable = false

        }

        showButton.setOnClickListener { v ->
            for (x in 0..2){
                for (y in 1..3){
                    txt = "$txt${rotationMatrix[x*3 + y -1]}|"
                }
                txt += "\n"
            }
            updateLog("Rotation\n$txt")
            txt = ""
            for (x: Float in orientationAngles){
                txt = "$txt$x|"
            }
            updateLog("Orientation\n$txt")
            txt = ""
        }

        resetButton.setOnClickListener { v ->
            disableSensor()
            updateLog("Stopped!!", true)
            initialise()
            //writeCSV(textNote.text.toString(), null)
            //writeCSV("Acceleration", accelerations)
            //writeCSV("Linear Acceleration", linearAccelerations)
            //writeCSV("Orientation", orientations)
            startButton.isClickable = true
            stopButton.isClickable = false
        }

        stopButton.setOnClickListener {v ->
            disableSensor()
            updateLog("Stopped!!", true)
            //writeCSV(textNote.text.toString(), null)
            //writeCSV("Acceleration", accelerations)
            //writeCSV("Linear Acceleration", linearAccelerations)
            //writeCSV("Orientation", orientations)
            startButton.isClickable = true
            stopButton.isClickable = false
        }
    }

    override fun onResume() {
        super.onResume()
        enableSensor()
    }

    override fun onPause() {
        super.onPause()
        disableSensor()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var data : DoubleArray = doubleArrayOf(0.0,0.0,0.0,0.0)
        if (event != null) {
            data[0] = System.currentTimeMillis().toDouble()
            data[1] = event.values[0].toDouble()
            data[2] = event.values[1].toDouble()
            data[3] = event.values[2].toDouble()
            //updateLog(event.sensor.type.toString()+"|"+data[0]+"|"+data[1]+"|"+data[2]+"|"+data[3])
            when{
                (event.sensor.type == Sensor.TYPE_ACCELEROMETER) -> System.arraycopy(event.values, 0,accelerometerReading,0,accelerometerReading.size)
                (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) -> linearAccelerations?.add(data)
                //(event.sensor.type == Sensor.TYPE_ORIENTATION) -> orientations?.add(data)
                (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) -> System.arraycopy(event.values, 0,magnetometerReading,0,magnetometerReading.size)
            }
            if (accelerometerReading != FloatArray(3) && magnetometerReading != FloatArray(3)){
                Calculate()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun createCSV(){
        val intent: Intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "text/csv"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra("data.csv", "")
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, URI.create("/storage/emulated/0/Download"))
        startActivityForResult(intent, 32)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        resultData?.data?.also{uriOfData: Uri ->
            uri = uriOfData
        }
    }

    private fun initialise(){
        accelerations?.clear()
        linearAccelerations?.clear()
        //orientations?.clear()
    }

    private fun enableSensor(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST)
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST)
    }

    private fun disableSensor(){
        sensorManager.unregisterListener(this)

    }

    private fun Calculate(){
        SensorManager.getRotationMatrix(rotationMatrix,null,accelerometerReading,magnetometerReading)
        SensorManager.getOrientation(rotationMatrix,orientationAngles)
    }

    private fun updateLog(txt : String, clear : Boolean = false){
        if (textLog != null) {
            if (clear){
                textLog.text = txt
            }else{
                var previousLines = textLog.text.toString()
                textLog.text = "$previousLines\n$txt"
            }

        }
    }

    private fun readCSV() : String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line+"\n")
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun writeCSV(sensor: String, data: ArrayList<DoubleArray>?){
        val previousTXT = readCSV()
        var txt = "Time,X-value,Y-value,Z-value"
        if (data != null) {
            for (column in data){
                txt += ("\n" + column[0].toString() + "," + column[1].toString() + "," + column[2].toString() + "," + column[3].toString())
            }
        }
        txt = sensor + "\n" + txt
        txt = previousTXT + "\n" + txt
        updateLog(txt)

        contentResolver.openFileDescriptor(uri, "w")?.use{
            FileOutputStream(it.fileDescriptor).use{
                it.write(txt.toByteArray())
            }
        }

    }

}