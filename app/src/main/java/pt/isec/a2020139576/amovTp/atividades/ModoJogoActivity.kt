package pt.isec.a2020139576.amovTp.atividades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils

class ModoJogoActivity : AppCompatActivity() {
    var tema : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setFullscreen(this)
        tema = Utils.setTema(this)
        setContentView(R.layout.activity_modo_jogo)
    }
}