package mx.com.david.komali.repartidor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import mx.com.david.komali.InicioSesion
import mx.com.david.komali.R
import mx.com.david.komali.utility.Cliente
import mx.com.david.komali.utility.Posicion

class RepartidorPrincipal : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    lateinit var firebase: FirebaseDatabase
    lateinit var myRef: DatabaseReference
    lateinit var mMap: GoogleMap

    lateinit var location: LocationManager
    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var longitude: Double = 0.0

    var marker : MarkerOptions = MarkerOptions()

    lateinit var llave :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.repartidor_principal)


        llave =  intent.getStringExtra("llave") as String


        FirebaseApp.initializeApp(this@RepartidorPrincipal)
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.reference

        location = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 50f, this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.maparepartidor) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        leerDatos()
        marcarVenta()

    }
    fun leerDatos() {
        myRef.child("repartidores").child(llave).child("posicion")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(!dataSnapshot.exists()){
                        Toast.makeText(this@RepartidorPrincipal, "No tienes ruta", Toast.LENGTH_SHORT).show()
                        finish()
                    }else{
                        mMap.clear()
                        val value = dataSnapshot.getValue<Posicion>(Posicion::class.java)!!
                        val latLng = LatLng(value.latitude, value.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                        mMap.addMarker(marker.position(latLng).title("Mi posición").icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN
                            )
                        )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) { // Failed to read value
                    Toast.makeText(this@RepartidorPrincipal, "Error: Firebase", Toast.LENGTH_SHORT).show()
                }
            })
    }

    var marcadoresActuales: ArrayList<Marker> = arrayListOf()
    var marca: ArrayList<Marker> = arrayListOf()

    fun marcarVenta() {
        myRef.child("cliente").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in marcadoresActuales) {
                        i.remove()
                    }
                    var latLng: LatLng
                    for (h in p0.children) {
                        val p = h.getValue<Cliente>(Cliente::class.java)!!
                        if (p.posicion.latitude != 0.0 && 0.0 != p.posicion.longitude) {
                            latLng = LatLng(p.posicion.latitude, p.posicion.longitude)
                            marca.add(mMap.addMarker(MarkerOptions().position(latLng).title("Cliente")))

                            println(p.nombre)
                            println(p.posicion.latitude)
                            println(p.posicion.longitude)
                        }
                    }
                    marcadoresActuales.clear()
                    marcadoresActuales.addAll(marca)
                }
            }
        })
    }
    override fun onLocationChanged(location: Location?) {
        latitude = location!!.latitude
        longitude = location.longitude
        altitude = location.altitude
        marker.position(LatLng(latitude, longitude)).title("Mi posición").icon(
            BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN
            )
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_salir, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_salir_sesion -> {
                val builder = AlertDialog.Builder(this@RepartidorPrincipal)
                builder.apply {
                    setTitle("¿Cerrar sesión?").setCancelable(true)
                    builder.setPositiveButton("OK") { _, _ ->
                        val sp = getSharedPreferences("sesion", Context.MODE_PRIVATE)
                        with(sp.edit()){
                            putString("user", "#")
                            putString("password", "#")
                            commit()
                        }
                        var intent = Intent(this@RepartidorPrincipal, InicioSesion::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        finish()
                        startActivity(intent)
                    }
                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.cancel()
                    }
                    show()
                }
            }
            R.id.itm_editar -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}
