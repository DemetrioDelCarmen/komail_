package mx.com.david.komali.tortilleria

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.dialog_precio.view.*
import kotlinx.android.synthetic.main.fragment_ajustes.*
import mx.com.david.komali.InicioSesion
import mx.com.david.komali.R
import mx.com.david.komali.utility.Tienda


class Fragment_ajustes : Fragment() {
    lateinit var cont: Context
    lateinit var firebase: FirebaseDatabase
    lateinit var myRef: DatabaseReference

    lateinit var nombre: TextView
    lateinit var telefono: TextView
    lateinit var correo: TextView
    lateinit var precio: TextView
    lateinit var llave: String

    lateinit var viewDialog: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ajustes, container, false)

        nombre = view.findViewById(R.id.fa_nombre)
        telefono = view.findViewById(R.id.fa_telefono)
        correo = view.findViewById(R.id.fa_correo)
        precio = view.findViewById(R.id.fa_precio)

        nombre.text = "XXXXXXXXXXXXX"
        telefono.text = "XXXXXXXXXXXXX"
        correo.text = "XXXXXXXXXXXXX"
        precio.text = "XXXXXXXXXXXXX"


        llave = activity!!.intent.getStringExtra("llaveTortilleria") as String

        iniciarFirebase()
        leerDatos()
        setHasOptionsMenu(true)

        return view
    }


    fun leerDatos() {
        myRef.child("tortilleria").child(llave).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Tienda>(Tienda::class.java)!!
                Log.d(TAG, "Value is: ${value.nombre}")
                nombre.text = value.nombre
                telefono.text = value.telefono
                correo.text = value.correo
                precio.text = value.precio


                btn_dialog_precio.setOnClickListener {
                    mostrarDialog().show()
                }
            }

            override fun onCancelled(error: DatabaseError) { // Failed to read value
                Toast.makeText(cont, "Error: Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        cont = context!!
    }

    fun iniciarFirebase() {
        FirebaseApp.initializeApp(cont)
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.reference
    }

    fun mostrarDialog(): AlertDialog.Builder {
        viewDialog = LayoutInflater.from(cont).inflate(R.layout.dialog_precio, null)
        viewDialog.edt_precio_modificar.text = precio.text.toString()

        val builder = AlertDialog.Builder(cont).setView(viewDialog).setTitle("Modificar precio kg.")
        builder.apply {
            setPositiveButton("Cambiar") { dialog, which ->
                val precio = viewDialog.edt_precio_modificar.text.toString()
                myRef.child("tortilleria").child(llave).child("precio").setValue(precio)
            }
            setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
        }

        viewDialog.btn_restar.setOnClickListener {
            viewDialog.edt_precio_modificar.text =
                viewDialog.edt_precio_modificar.text.toString().toDouble().minus(0.50).toString()
            viewDialog.btn_restar.isEnabled =
                viewDialog.edt_precio_modificar.text.toString() != "0.0"
            viewDialog.btn_sumar.isEnabled = true
        }
        viewDialog.btn_sumar.setOnClickListener {
            viewDialog.edt_precio_modificar.text =
                viewDialog.edt_precio_modificar.text.toString().toDouble().plus(0.50).toString()
            viewDialog.btn_sumar.isEnabled =
                viewDialog.edt_precio_modificar.text.toString() != "150.0"
            viewDialog.btn_restar.isEnabled = true

        }
        return builder
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity!!.menuInflater.inflate(R.menu.menu_salir, menu)
        return (super.onCreateOptionsMenu(menu, inflater))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_salir_sesion -> {
                val builder = AlertDialog.Builder(cont)
                builder.apply {
                    setTitle("¿Cerrar sesión?").setCancelable(true)
                    builder.setPositiveButton("OK") { _, _ ->
                        val sp = activity!!.getSharedPreferences("sesion", Context.MODE_PRIVATE)
                        with(sp.edit()){
                            putString("user", "#")
                            putString("password", "#")
                            commit()
                        }
                        var intent = Intent(cont, InicioSesion::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity!!.finish()
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

}
