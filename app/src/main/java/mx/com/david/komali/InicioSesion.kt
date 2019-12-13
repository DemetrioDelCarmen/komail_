package mx.com.david.komali

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import mx.com.david.komali.utility.Encriptar
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_inicio_sesion.*
import mx.com.david.komali.cliente.ClientePrincipal
import mx.com.david.komali.repartidor.RepartidorPrincipal
import mx.com.david.komali.tortilleria.TortilleriaPrincipal
import java.util.*

class InicioSesion : AppCompatActivity() {
    lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)
        card_login.visibility= View.GONE

        btn_socio.setOnClickListener {
            btn_cliente.visibility= View.GONE
            card_login.visibility= View.VISIBLE
            btn_socio.visibility=View.GONE
        }

        btn_atras.setOnClickListener{

        }

        FirebaseApp.initializeApp(this@InicioSesion)
        myRef = FirebaseDatabase.getInstance().reference

        val sharedP = getSharedPreferences("sesion", Context.MODE_PRIVATE)

        val userSave = sharedP.getString("user", "#") as String
        val passSave = sharedP.getString("password", "#") as String

        if (userSave != "#" && passSave != "#") {
            finish()
            validarTortilleria(userSave, passSave)
            validarRepartidor(userSave, passSave)
            return
        }

        btn_login_iniciar.setOnClickListener {
            val usuario = log_edt_user.text.toString()
            var clave = log_edt_pass.text.toString()
            clave = Encriptar().toMexString(Encriptar().getHA(clave))!!
            validarTortilleria(usuario, clave)
            validarRepartidor(usuario, clave)
        }

        btn_ir_tortilleria.setOnClickListener {
            validarTortilleria("7775509300", Encriptar().toMexString(Encriptar().getHA("7775509300"))!!)
        }

        btn_ir_repartidor.setOnClickListener {
            validarRepartidor("7772095404", Encriptar().toMexString(Encriptar().getHA("7772095404"))!!)
        }

        btn_cliente.setOnClickListener {
            if(sharedP.getString("clienteid", "#") == "#"){
                with(sharedP.edit()) {
                    putString("clienteid", UUID.randomUUID().toString())
                    commit()
                }
            }
            irCliente()
        }
    }

    fun irCliente(){
        val intent = Intent(this@InicioSesion, ClientePrincipal::class.java)
        startActivity(intent)
        finish()
    }

    fun validarTortilleria(num: String,num2: String) {
        myRef.child("tortilleria").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for(i in p0.children){
                        if(i.child("telefono").value == num){
                            if(i.child("contrasenia").value == num2){
                                val sp = getSharedPreferences("sesion", Context.MODE_PRIVATE)
                                with(sp.edit()) {
                                    putString("user", num)
                                    putString("password", num2)
                                    commit()
                                }
                                val intent = Intent(this@InicioSesion, TortilleriaPrincipal::class.java)
                                intent.putExtra("llaveTortilleria", i.key)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                }
            }
        })
    }

    fun validarRepartidor(num: String,num2: String) {
        myRef.child("repartidores").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for(i in p0.children){
                        if(i.child("telefono").value == num){
                            if(i.child("contrasenia").value ==  num2 ){
                                val sp = getSharedPreferences("sesion", Context.MODE_PRIVATE)
                                with(sp.edit()) {
                                    putString("user", num)
                                    putString("password",num2)
                                    commit()
                                }
                                val intent = Intent(this@InicioSesion, RepartidorPrincipal::class.java)
                                intent.putExtra("llave", i.key)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }
        })
    }
}
