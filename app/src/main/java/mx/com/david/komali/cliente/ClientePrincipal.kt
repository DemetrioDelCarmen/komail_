package mx.com.david.komali.cliente

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.cliente_principal.*
import mx.com.david.komali.R
import mx.com.david.komali.utility.Posicion
import mx.com.david.komali.utility.Repartidor

class ClientePrincipal : AppCompatActivity(), LocationListener {

    lateinit var firebase: FirebaseDatabase
    lateinit var myRef: DatabaseReference
    lateinit var mMap: GoogleMap

    lateinit var location: LocationManager
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var longitude: Double = 0.0
    lateinit var clienteid: String

    var pedidoActivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cliente_principal)

        FirebaseApp.initializeApp(this@ClientePrincipal)
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.reference

        val sharedP = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        clienteid = sharedP.getString("clienteid", "1")!!
        location = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        verifyPermissions(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapacliente) as SupportMapFragment
        mapFragment.getMapAsync {
            mMap = it!!
            mMap.isMyLocationEnabled = true
        }
        fab2.visibility = View.GONE

        if (sharedP.getString("clienteactivo", "#") != "#") {
            myRef.child("cliente").child(clienteid).child("posicion")
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            val value = dataSnapshot.getValue<Posicion>(Posicion::class.java)!!
                            mMap.addMarker(
                                MarkerOptions().position(LatLng(value.latitude, value.longitude)).title(
                                    "Mi posicion"
                                )
                            )
                            fab.visibility = View.GONE
                            fab2.visibility = View.VISIBLE
                            pedidoActivo = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { // Failed to read value
                        Toast.makeText(this@ClientePrincipal, "Error: Firebase", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

        }



        fab.setOnClickListener { view ->
            fab.visibility = View.GONE
            fab2.visibility = View.VISIBLE
            pedidoActivo = true
            with(sharedP.edit()) {
                putString("clienteactivo", "activo")
                commit()
            }
        }
        fab2.setOnClickListener { _ ->
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("¿Cancelar pedido?").setCancelable(true)
                builder.setPositiveButton("OK") { _, _ ->

                    myRef.child("cliente").child(clienteid).child("posicion").setValue(null)
                    fab2.visibility = View.GONE
                    fab.visibility = View.VISIBLE
                    with(sharedP.edit()) {
                        putString("clienteactivo", "#")
                        commit()
                    }
                }
                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
                show()
            }
            pedidoActivo = false
            soloUnaVez = true
        }
        marcarReaprtidor()
    }

    var soloUnaVez = true
    override fun onLocationChanged(location: Location?) {
        if (soloUnaVez) {
            if (pedidoActivo) {
                // Toast.makeText(this, location!!.latitude.toString(), Toast.LENGTH_SHORT).show()
                latitude = location!!.latitude
                longitude = location.longitude
                altitude = location.altitude
                myRef.child("cliente").child(clienteid).child("posicion")
                    .setValue(Posicion(latitude, longitude, altitude))

                soloUnaVez = false
                mMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            latitude,
                            longitude
                        )
                    ).title("Yo")
                )
            }
        }
    }



    val repartidorActual = arrayListOf<Marker>()
    val repa = arrayListOf<Marker>()

    fun marcarReaprtidor() {
        myRef.child("repartidores").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in repartidorActual) {
                        i.remove()
                    }
                    var latLng: LatLng
                    for (h in p0.children) {
                        val p = h.getValue<Repartidor>(Repartidor::class.java)!!
                        if (p.posicion.latitude != 0.0 && 0.0 != p.posicion.longitude) {
                            latLng = LatLng(p.posicion.latitude, p.posicion.longitude)
                            repa.add(
                                mMap.addMarker(
                                    MarkerOptions().position(latLng).title("Repartidor ${p.nombre}").icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_GREEN
                                        )
                                    )
                                )
                            )
                        }
                    }
                    repartidorActual.clear()
                    repartidorActual.addAll(repa)

                }
            }
        })
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


    private fun verifyPermissions(context: Activity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
//Si el permiso no se concedió, explicar porque se ocupa
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
//Muestra la explicación de manera asíncrona
                val builder = AlertDialog.Builder(this)
                builder.setMessage("El permiso de localización es necesario.")
                    .setTitle("Permiso requerido")
                builder.setPositiveButton("OK") { dialog, id ->
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10
                    )
                }
                val dialog = builder.create()
                dialog.show()

            } else {
                //No se necesita explicación, solicitar el permiso
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10
                )
                // 10 es una constante para saber que se pidió el permiso de localización
            }
        } else {
            //El permiso se concedió, se pide la localización
            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            10 -> {
                if (grantResults.isEmpty() || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED
                ) {

                } else {
                    location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
                }
            }
        }
    }
}
