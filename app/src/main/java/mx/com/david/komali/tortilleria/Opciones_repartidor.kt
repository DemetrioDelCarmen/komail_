package mx.com.david.komali.tortilleria

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_opciones_repartidor.*
import mx.com.david.komali.R
import mx.com.david.komali.utility.Posicion
import mx.com.david.komali.utility.Repartidor

class Opciones_repartidor : AppCompatActivity() {

    lateinit var myRef: DatabaseReference
    lateinit var llave: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opciones_repartidor)

        myRef = FirebaseDatabase.getInstance().reference

        llave = intent.getStringExtra("repartidor") as String
        listaRepartidores()

    }

    fun listaRepartidores() {
        myRef.child("repartidores").child(llave).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                println(llave)
                if (p0.exists()) {
                    val r = p0.getValue<Repartidor>(Repartidor::class.java)!!

                    txt_or_estado.text = if (r.posicion.latitude == 0.0) "Inactivo" else "Activo"
                    txt_or_nombre.text = r.nombre
                    txt_or_telefono.text = r.telefono

                    btnDefinirRuta.text = if (txt_or_estado.text.toString() == "Inactivo") "DEFINIR RUTA" else "TERMINAR RUTA"


                    if (txt_or_estado.text.toString() == "Inactivo") {
                        btnDefinirRuta.setBackgroundColor(Color.BLUE)
                    } else {
                        btnDefinirRuta.setBackgroundColor(Color.RED)
                    }

                    btnDefinirRuta.setOnClickListener {
                        if (btnDefinirRuta.text.toString() == "DEFINIR RUTA") {
                            myRef.child("repartidores").child(llave).child("posicion")
                                .setValue(Posicion(1.0, 1.0))
                            btnDefinirRuta.setBackgroundColor(Color.BLUE)
                        } else {
                            myRef.child("repartidores").child(llave).child("posicion")
                                .setValue(Posicion())
                            btnDefinirRuta.setBackgroundColor(Color.RED)
                        }
                        finish()
                        startActivity(intent)
                    }


                }

            }

            override fun onCancelled(error: DatabaseError) { // Failed to read value
                Toast.makeText(this@Opciones_repartidor, "Error: Firebase", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repartidores_opc, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.itm_eliminar -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setTitle("¿Eliminar?").setCancelable(true)
                    builder.setPositiveButton("OK") { _, _ ->
                        myRef.child("repartidores").child(llave).setValue(null)
                        finish()
                    }
                    builder.setNegativeButton("Cancelar") { dialog, _->
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
}
