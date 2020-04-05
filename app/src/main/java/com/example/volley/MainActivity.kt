package com.example.volley

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    val IP = "http://192.168.56.1"
    //"http://192.168.7.108"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getAllSensor(v: View) {
        val wsURL = IP + "/pweb/getSensado.php"
        val admin = AdminBD(this)
        admin.Ejecuta("DELETE FROM sensado")
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, wsURL, null,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                val sensadoJson = response.getJSONArray("sensado")
                for (i in 0 until sensadoJson.length()) {
                    val ids = sensadoJson.getJSONObject(i).getString("idsen")
                    val nom = sensadoJson.getJSONObject(i).getString("nomsensor")
                    val valor = sensadoJson.getJSONObject(i).getString("valor")
                    val sentencia =
                        "Insert into sensado(idsen,nomsensor,valor) values (${ids}, '${nom}',${valor})"
                    val res = admin.Ejecuta(sentencia)
                }
                Toast.makeText(this, "Se Cargado todo a la BD", Toast.LENGTH_LONG).show();
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    this,
                    "Error getAllSensor: " + error.message.toString(),
                    Toast.LENGTH_LONG
                ).show();
                Log.d("Zazueta", error.message.toString())
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun Agregar(view: View) {
        if (etId.text.toString().isEmpty() ||
            etNom.text.toString().isEmpty() ||
            etValor.text.toString().isEmpty()
        ) {
            Toast.makeText(
                this, "Falta información de capturar",
                Toast.LENGTH_LONG
            ).show();
            etId.requestFocus()
        } else {
            val id = etId.text.toString()
            val nom = etNom.text.toString()
            val valor = etValor.text.toString()
            val admin = AdminBD(this)
            val sentencia = "Insert into sensado(idsen,nomsensor,valor) values" +
                    "($id,'$nom',$valor)"
            if (admin.Ejecuta(sentencia)) {
                Toast.makeText(this, "Sensor Agregado", Toast.LENGTH_SHORT).show();
                etId.setText("")
                etNom.setText("")
                etValor.setText("")
                etId.requestFocus()
            } else {
                Toast.makeText(this, "Error al Guardar", Toast.LENGTH_SHORT).show();
                etId.requestFocus()
            }
        }
    }

    fun Eliminar(view: View) {
        if (etId.text.toString().isEmpty()) {
            Toast.makeText(
                this, "Falta información de capturar",
                Toast.LENGTH_LONG
            ).show();
            etId.requestFocus()
        } else {
            val id = etId.text.toString()
            val admin = AdminBD(this)
            val sentencia = "delete from sensado where idsen=$id"
            if (admin.Ejecuta(sentencia)) {
                Toast.makeText(this, "Sensor eliminado", Toast.LENGTH_SHORT).show();
                etId.setText("")
                etNom.setText("")
                etValor.setText("")
                etId.requestFocus()
            } else {
                Toast.makeText(this, "Error al Eliminar", Toast.LENGTH_SHORT).show();
                etId.requestFocus()
            }
        }
    }

    fun Actualiza(view: View) {
        if (etId.text.toString().isEmpty() ||
            etNom.text.toString().isEmpty() ||
            etValor.text.toString().isEmpty()
        ) {
            Toast.makeText(
                this, "Falta información de capturar",
                Toast.LENGTH_LONG
            ).show();
            etId.requestFocus()
        } else {
            val id = etId.text.toString()
            val nom = etNom.text.toString()
            val valor = etValor.text.toString()
            val admin = AdminBD(this)
            val sentencia = "update sensado set nomsensor='$nom',valor=$valor where idsen=$id"
            if (admin.Ejecuta(sentencia)) {
                Toast.makeText(this, "Sensor Actualizado", Toast.LENGTH_SHORT).show();
                etId.setText("")
                etNom.setText("")
                etValor.setText("")
                etId.requestFocus()
            } else {
                Toast.makeText(this, "Error al Actuaizar", Toast.LENGTH_SHORT).show();
                etId.requestFocus()
            }
        }
    }

    fun Consultar(view: View) {
        if (etId.text.toString().isEmpty()) {
            Toast.makeText(this, "Falta información del id", Toast.LENGTH_SHORT).show();
            etId.requestFocus()
        } else {
            val admin = AdminBD(this)
            val id: String = etId.text.toString()
            //                                       0     1         2
            val cur = admin.Consulta("select idsen,nomsensor,valor from sensado where idsen=$id")
            if (cur!!.moveToFirst()) {
                etNom.setText(cur.getString(1))
                etValor.setText(cur.getString(2))
            } else {
                Toast.makeText(this, "No existe el ID", Toast.LENGTH_SHORT).show();
                etId.requestFocus()
            }
        }
    }

    fun Limpia(view: View) {
        etId.setText("")
        etNom.setText("")
        etValor.setText("")
        etId.requestFocus()
    }

    fun respaldaSensado(v: View) {
        val admin = AdminBD(this)
        var sensadoJson: JSONObject
        var jsonArray: JSONArray = JSONArray()
        var jsonParam = JSONObject() // JSON Final
        jsonParam.put("usr", "postgres")
        jsonParam.put("pwd", "eduardo25")
        val cur = admin.Consulta("Select idsen,nomsensor,valor from sensado")
        if (cur!!.moveToFirst()) {
            do {
                sensadoJson = JSONObject()
                sensadoJson.put("idsen", cur!!.getString(0))
                sensadoJson.put("nomsensor", cur!!.getString(1))
                sensadoJson.put("valor", cur!!.getString(2))
                jsonArray.put(sensadoJson)
            } while (cur!!.moveToNext())
            cur.close()
        } // Fin del IF
        jsonParam.put("sensado", jsonArray)
        sendRequest(IP + "/pweb/respaldaSensado.php", jsonParam)
    }

    fun sendRequest(wsURL: String, jsonEntrada: JSONObject) {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, wsURL, jsonEntrada,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                Toast.makeText(this, "Success:${succ}  Message:${msg}", Toast.LENGTH_LONG).show();
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "${error.toString()}", Toast.LENGTH_LONG).show();
                Log.d("ERROR", "${error.toString()}")
                Toast.makeText(this, "API: Error de capa 8 en WS ):", Toast.LENGTH_SHORT).show();
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }


}
