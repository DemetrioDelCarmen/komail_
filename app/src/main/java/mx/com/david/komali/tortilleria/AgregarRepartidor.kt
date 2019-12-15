package mx.com.david.komali.tortilleria


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_agregar_repartidor.*
import mx.com.david.komali.R
import mx.com.david.komali.utility.Encriptar
import mx.com.david.komali.utility.Repartidor
import java.util.*

class AgregarRepartidor : AppCompatActivity() {
    lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_repartidor)

        FirebaseApp.initializeApp(this@AgregarRepartidor)
        myRef = FirebaseDatabase.getInstance().reference

        Nrepa_btn.setOnClickListener {
            val contra =  Nrepa_contra.text.toString()
            val confirm =  Nrepa_confirm.text.toString()
            if(contra == confirm){
                val nombre =  Nrepa_nombre.text.toString()
                val telefono =  Nrepa_telefono.text.toString()

                val co: String = Encriptar().toMexString(Encriptar().getHA(contra))!!
                val llave = intent.getStringExtra("llaveTortilleria") as String
                println(llave)

                val repartidor = Repartidor(nombre, telefono, co, llave)
                myRef.child("repartidores").child(UUID.randomUUID().toString())
                    .setValue(repartidor)
                finish()
            }else{
                Toast.makeText(this@AgregarRepartidor, "Error: contraseña incorrecta", Toast.LENGTH_SHORT).show()
            }

        }
    }
}