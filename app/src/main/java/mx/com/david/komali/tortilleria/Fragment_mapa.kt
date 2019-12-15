package mx.com.david.komali.tortilleria

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import mx.com.david.komali.R
import mx.com.david.komali.utility.Cliente
import mx.com.david.komali.utility.Repartidor
import mx.com.david.komali.utility.Tienda


class Fragment_mapa : Fragment(), OnMapReadyCallback, LocationListener {

    lateinit var firebase: FirebaseDatabase
    lateinit var myRef: DatabaseReference
    private lateinit var mMap: GoogleMap
    lateinit var sydney: LatLng

    lateinit var location: LocationManager

    lateinit var cont: Context

    var latitude: Double = 0.0
    var altitude: Double = 0.0
    var longitude: Double = 0.0

    lateinit var llave: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)
        llave = activity!!.intent.getStringExtra("llaveTortilleria") as String


        FirebaseApp.initializeApp(cont)
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.reference


        location = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        val myMAPF =
            childFragmentManager.findFragmentById(R.id.mapatortilleria) as SupportMapFragment?
        myMAPF!!.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Toast.makeText(cont, "onMapReady", Toast.LENGTH_SHORT).show()
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        //  mMap.setMaxZoomPreference(18f) //Mayor numero más cerca
        //mMap.setMinZoomPreference(16f)

        mMap.isMyLocationEnabled = true
        leerDatos()
        marcarVenta()
        marcarReaprtidor()
    }


    override fun onLocationChanged(p0: Location?) {
        latitude = p0!!.latitude
        altitude = p0.altitude
        longitude = p0.longitude
        sydney = LatLng(latitude, longitude)


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cont = context!!
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
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
                            marca.add(
                                mMap.addMarker(
                                    MarkerOptions().position(latLng).title(p.nombre).title(
                                        "Cliente"
                                    )
                                )
                            )

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

    fun leerDatos() {
        myRef.child("tortilleria").child(llave)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue<Tienda>(Tienda::class.java)!!

                    println("${value.nombre} ${value.posicion.longitude}")
                    if (value.posicion.latitude != 0.0 && 0.0 != value.posicion.longitude) {
                        println("${value.posicion.latitude} ${value.posicion.longitude}")
                        val latLng = LatLng(value.posicion.latitude, value.posicion.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(latLng).title("Tienda ${value.nombre}").icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_YELLOW
                                )
                            )
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                }

                override fun onCancelled(error: DatabaseError) { // Failed to read value
                    Toast.makeText(cont, "Error: Firebase", Toast.LENGTH_SHORT).show()
                }
            })
    }


}

