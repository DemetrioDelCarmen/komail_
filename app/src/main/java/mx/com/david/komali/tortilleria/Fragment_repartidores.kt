package mx.com.david.komali.tortilleria

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import mx.com.david.komali.R
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_repartidores.*
import mx.com.david.komali.utility.Repartidor

class Fragment_repartidores : Fragment() {

    lateinit var cont: Context
    lateinit var myRef: DatabaseReference

    var arr: ArrayList<String> = arrayListOf()

    var repartidores: ArrayList<Repartidor> = arrayListOf()
    lateinit var llave: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_repartidores, container, false)

        FirebaseApp.initializeApp(cont)
        myRef = FirebaseDatabase.getInstance().reference

        val lvl_lista = view.findViewById<ListView>(R.id.lvl_lista)



        llave =  activity!!.intent.getStringExtra("llaveTortilleria") as String

        lvl_lista.setOnItemClickListener { parent, view, position, id ->
            activity?.let {
                val intent = Intent(it, Opciones_repartidor::class.java)
                intent.putExtra("repartidor", repartidores[position].UIDD)

                intent.putExtra("llave", llave)
                it.startActivity(intent)
            }
        }



        listaRepartidores()
        return view
    }

    fun listaRepartidores() {
        myRef.child("repartidores").orderByChild("jefe").equalTo(llave)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {
                    arr.clear()
                    repartidores.clear()
                    if (p0.exists()) {
                        for (h in p0.children) {
                            val p = h.getValue<Repartidor>(Repartidor::class.java)!!
                            p.UIDD = h.key.toString()
                            repartidores.add(p)

                            val activo = if (p.posicion.latitude == 0.0) "Inactivo" else "Activo"
                            arr.add("${p.nombre}\n$activo")

                        }
                        val adapter = ArrayAdapter(cont, android.R.layout.simple_list_item_1, arr)
                        lvl_lista.adapter = adapter

                        if (arr.size >= 5) {
                            btn_agregar_repartidor.visibility = View.GONE
                        } else {
                            /* btn_agregar_repartidor.setOnClickListener {
                                val intent = Intent(cont, AgregarRepartidor::class.java)
                                intent.putExtra("llaveTortilleria", llave)
                                startActivity(intent)
                            }*/

                            btn_agregar_repartidor.visibility = View.VISIBLE
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) { // Failed to read value
                    Toast.makeText(cont, "Error: Firebase", Toast.LENGTH_SHORT).show()
                }
            })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        cont = context
    }

}