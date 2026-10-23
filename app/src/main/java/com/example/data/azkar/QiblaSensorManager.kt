package com.example.data.azkar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Kaaba Coordinates in Makkah
const val KAABA_LATITUDE = 21.4225
const val KAABA_LONGITUDE = 39.8262

data class QiblaState(
  val azimuthDegrees: Float = 0f,
  val qiblaBearingDegrees: Float = 0f,
  val differenceDegrees: Float = 0f,
  val isAligned: Boolean = false,
  val distanceToKaabaKm: Int = 0,
  val userLatitude: Double = KAABA_LATITUDE,
  val userLongitude: Double = KAABA_LONGITUDE,
  val hasSensors: Boolean = true,
  val accuracyStatus: String = "جيد"
)

class QiblaSensorManager(
  private val context: Context
) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
  private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  private val magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

  private val _qiblaState = MutableStateFlow(QiblaState())
  val qiblaState: StateFlow<QiblaState> = _qiblaState.asStateFlow()

  private var gravity = FloatArray(3)
  private var geomagnetic = FloatArray(3)
  private var hasGravity = false
  private var hasGeomagnetic = false

  private var smoothedAzimuth = 0f
  private val smoothingFactor = 0.15f // Low pass filter for jitter-free needle

  private var userLat: Double = KAABA_LATITUDE
  private var userLon: Double = KAABA_LONGITUDE

  fun updateUserLocation(lat: Double, lon: Double) {
    userLat = lat
    userLon = lon
    val bearing = calculateQiblaBearing(lat, lon)
    val distance = calculateDistanceKm(lat, lon, KAABA_LATITUDE, KAABA_LONGITUDE)
    _qiblaState.update {
      it.copy(
        userLatitude = lat,
        userLongitude = lon,
        qiblaBearingDegrees = bearing,
        distanceToKaabaKm = distance
      )
    }
  }

  fun startListening() {
    val sm = sensorManager ?: run {
      _qiblaState.update { it.copy(hasSensors = false) }
      return
    }

    if (rotationSensor != null) {
      sm.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
    } else if (accelerometer != null && magnetometer != null) {
      sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
      sm.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    } else {
      _qiblaState.update { it.copy(hasSensors = false) }
    }
  }

  fun stopListening() {
    sensorManager?.unregisterListener(this)
  }

  override fun onSensorChanged(event: SensorEvent) {
    var rawAzimuth = 0f

    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
      val rotationMatrix = FloatArray(9)
      SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
      val orientation = FloatArray(3)
      SensorManager.getOrientation(rotationMatrix, orientation)
      rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
    } else {
      if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
        System.arraycopy(event.values, 0, gravity, 0, 3)
        hasGravity = true
      } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
        hasGeomagnetic = true
      }

      if (hasGravity && hasGeomagnetic) {
        val r = FloatArray(9)
        val i = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
          val orientation = FloatArray(3)
          SensorManager.getOrientation(r, orientation)
          rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        }
      }
    }

    // Normalize rawAzimuth to [0, 360)
    rawAzimuth = (rawAzimuth + 360f) % 360f

    // Smooth filter
    smoothedAzimuth = smoothAngle(smoothedAzimuth, rawAzimuth, smoothingFactor)

    val qibla = _qiblaState.value.qiblaBearingDegrees
    val diff = calculateAngleDifference(smoothedAzimuth, qibla)
    val isAligned = kotlin.math.abs(diff) <= 3.5f

    _qiblaState.update {
      it.copy(
        azimuthDegrees = smoothedAzimuth,
        differenceDegrees = diff,
        isAligned = isAligned
      )
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    val status = when (accuracy) {
      SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "عالية جداً"
      SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "متوسطة"
      SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "منخفضة (حرك الهاتف على شكل 8)"
      else -> "غير معروفة"
    }
    _qiblaState.update { it.copy(accuracyStatus = status) }
  }

  companion object {
    /**
     * Great Circle Bearing formula to calculate exact bearing towards Kaaba
     */
    fun calculateQiblaBearing(lat1: Double, lon1: Double): Float {
      val lat1Rad = Math.toRadians(lat1)
      val lon1Rad = Math.toRadians(lon1)
      val lat2Rad = Math.toRadians(KAABA_LATITUDE)
      val lon2Rad = Math.toRadians(KAABA_LONGITUDE)

      val dLon = lon2Rad - lon1Rad

      val y = sin(dLon) * cos(lat2Rad)
      val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

      val bearingRad = atan2(y, x)
      var bearingDeg = Math.toDegrees(bearingRad)
      bearingDeg = (bearingDeg + 360.0) % 360.0
      return bearingDeg.toFloat()
    }

    /**
     * Haversine distance in kilometers
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
      val r = 6371.0 // Earth radius in km
      val dLat = Math.toRadians(lat2 - lat1)
      val dLon = Math.toRadians(lon2 - lon1)
      val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
      val c = 2 * atan2(sqrt(a), sqrt(1 - a))
      return (r * c).toInt()
    }

    private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
      var diff = target - current
      while (diff < -180f) diff += 360f
      while (diff > 180f) diff -= 360f
      return (current + diff * factor + 360f) % 360f
    }

    private fun calculateAngleDifference(azimuth: Float, qibla: Float): Float {
      var diff = qibla - azimuth
      while (diff < -180f) diff += 360f
      while (diff > 180f) diff -= 360f
      return diff
    }
  }
}
