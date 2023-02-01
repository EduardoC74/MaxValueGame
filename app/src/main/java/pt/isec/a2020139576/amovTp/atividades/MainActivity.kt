package pt.isec.a2020139576.amovTp.atividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.Utils.Companion.mostrarImagemPerfil
import pt.isec.a2020139576.amovTp.Utils.Companion.setFullscreen
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    var tema: Int = 1
    lateinit var btnDefinicoes: ImageButton
    lateinit var mainLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullscreen(this)

        mostrarImagemPerfil(this, R.id.perfilJogador)

        tema = Utils.setTema(this)
        Utils.changeLanguage(this)
        setContentView(R.layout.activity_main)
        btnDefinicoes = findViewById(R.id.Btn_Definicoes)
        mainLayout = findViewById(R.id.mainlayout)
        setBtnTheme(tema)
    }

    fun onNovoJogo(view: android.view.View) {

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, getString(R.string.iniciarSessaoRequired), Toast.LENGTH_SHORT)
                .show()

            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)

        } else {

            val intent = Intent(this, ModoJogoActivity::class.java)

            startActivity(intent)
        }
    }

    fun onPerfil(view: android.view.View) {
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }

    fun onPontuacoes(view: android.view.View) {
        val intent = Intent(this, PontuacoesActivity::class.java)
        startActivity(intent)
    }

    fun onOpcoes(view: android.view.View) {
        val intent = Intent(this, OpcoesActivity::class.java)
        startActivity(intent)
    }

    fun onSair(view: android.view.View) {
        finishAffinity()
        exitProcess(0)
    }

    fun setBtnTheme(tema: Int) {
        when (tema) {
            1 -> {
                btnDefinicoes.setImageResource(R.drawable.opcoes_icon_warm)
                //mainLayout.setBackgroundResource(R.color.botton_colorWarm)
            }
            2 -> {
                btnDefinicoes.setImageResource(R.drawable.opcoes_icon_cold)
                //mainLayout.setBackgroundResource(R.color.botton_color)
            }
            3 -> {
                btnDefinicoes.setImageResource(R.drawable.opcoes_icon_exotic)
                //mainLayout.setBackgroundResource(R.color.botton_colorExotic)
            }
        }
    }


}
