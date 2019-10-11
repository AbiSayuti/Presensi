package com.abisayuti.presensiotomatis

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity() {


    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    lateinit var webView: WebView
    lateinit var pb: ProgressBar
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView=findViewById(R.id.webview)
        pb=findViewById(R.id.pb)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                PermissionAllowed()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
//            PermissionDenied()
        }


    }

    private fun PermissionDenied() {
        webView.settings.javaScriptEnabled = true
        webView.settings.saveFormData = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient=object : WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress==100){
                    pb.visibility= View.GONE
                }
            }
        }
        webView.loadUrl("http://learningthings.mine.e-iso27001.com/public/login?lat=&long=&is_gps=no")
    }

    private fun PermissionAllowed() {
        locationManager = (getSystemService(LOCATION_SERVICE) as LocationManager?)!!
        getLocation()
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {
            if (hasGps) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            locationGps = location
                            var lat = locationGps!!.latitude
                            var long = locationGps!!.longitude
                            webView.settings.javaScriptEnabled = true
                            webView.settings.saveFormData = true
                            webView.webViewClient = WebViewClient()
                            webView.webChromeClient=object : WebChromeClient(){
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    if (newProgress==100){
                                        pb.visibility= View.GONE
                                    }
                                }
                            }
                            webView.loadUrl("http://learningthings.mine.e-iso27001.com/public/login?lat=${lat}&long=${long}&is_gps=yes")
                            locationManager.removeUpdates(this)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation


                if (locationGps != null && locationNetwork != null) {
                    if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                        Log.d("CodeAndroidLocation", " Network Latitude2 : " + locationNetwork!!.latitude)
                        Log.d("CodeAndroidLocation", " Network Longitude2 : " + locationNetwork!!.longitude)
                    } else if (locationGps!!.accuracy < locationNetwork!!.accuracy) {
                        Log.d("CodeAndroidLocation", " GPS Latitude3 : " + locationGps!!.latitude)
                        Log.d("CodeAndroidLocation", " GPS Longitude3 : " + locationGps!!.longitude)
                    } else {
                        Toast.makeText(this, "Can't get Location ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Can't get Location ", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }


    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                        PermissionDenied()
                    } else {
                        Toast.makeText(this, "Permission denied twice", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                PermissionAllowed()
        }
    }

}
