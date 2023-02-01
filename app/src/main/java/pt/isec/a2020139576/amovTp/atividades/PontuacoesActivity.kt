package pt.isec.a2020139576.amovTp.atividades

import android.app.DownloadManager.Query
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.model.Values
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.listview_top_5.view.*
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.databinding.ActivityPontuacoesBinding
import pt.isec.a2020139576.amovTp.databinding.ActivityUmJogadorBinding
import pt.isec.a2020139576.amovTp.databinding.FragmentTabuleiroBinding
import pt.isec.a2020139576.amovTp.fragmentos.OpcoesFragment

class PontuacoesActivity : AppCompatActivity() {

    data class Pontuacoes(var nivel: Int?, var pontuacao: Int?, var tempoUsado: Int?)

    lateinit var binding: ActivityPontuacoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPontuacoesBinding.inflate(layoutInflater)
        Utils.setTema(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.scoreboards)
        //Utils.mostrarImagemPerfil(this, R.id.Imagem_Perfil_Pontuacoes)
        setContentView(binding.root)

        val db = Firebase.firestore
        db.collection("Jogadores")
            .document(FirebaseAuth.getInstance().currentUser!!.email!!)
            .addSnapshotListener { docSS, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (docSS != null && docSS.exists()) {
                    val username = docSS.getString("Nome")
                    binding.nomeJogadorScoreText.text = username

                    val imagem = docSS.getString("ImagemPerfilUrl")
                    if (imagem == "")
                        return@addSnapshotListener

                    Glide.with(this).load(imagem).into(binding.ImagemPerfilPontuacoes)
                }
            }

        val data = arrayListOf<Pontuacoes>()
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {

            db.collection("Jogadores").document(email).collection("Pontuacoes")
                .orderBy("Pontuacao", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                //.orderBy("TempoUsado")
                .get()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        for (document in task.result) {
                            val nivel = document.getLong("Nivel")!!.toInt()
                            val pontuacao = document.getLong("Pontuacao")!!.toInt()
                            val tempoUsado = document.getLong("TempoUsado")!!.toInt()

                            val item = Pontuacoes(nivel, pontuacao, tempoUsado)
                            data.add(item)

                        }
                    } else {
                        Log.d("SCORE", "Erro a receber documentos: ", task.exception)
                    }

                    val adapter = AdaptarTop5(data)
                    binding.listaTop5.adapter = adapter

                }

        }
    }

    class AdaptarTop5(val data : ArrayList<Pontuacoes>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any {
            return data[position]
        }

        override fun getItemId(position: Int): Long = position.toLong()

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.listview_top_5,parent,false)

            view.nivel.text = data[position].nivel.toString()
            view.pontuacao.text = data[position].pontuacao.toString()
            view.tempo.text = data[position].tempoUsado.toString()

            return view
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

}