package pt.isec.a2020139576.amovTp.atividades

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    companion object {
        private const val MULTIJOGADOR_MODE = 0
        private const val UMJOGADOR_MODE = 1

        fun getServerModeIntent(context : Context) : Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra("mode", MULTIJOGADOR_MODE)
            }
        }

        fun getClientModeIntent(context : Context) : Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra("mode", UMJOGADOR_MODE)
            }
        }
    }
}
