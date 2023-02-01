package pt.isec.a2020139576.amovTp.fragmentos

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pt.isec.a2020139576.amovTp.Constantes
import pt.isec.a2020139576.amovTp.MaxValueGame
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.databinding.FragmentEsperaBinding

class EsperaFragment : Fragment(){
    private var countBotaoPausa : Int = 0
    private val maxValueGame: MaxValueGame by activityViewModels()
    lateinit var binding: FragmentEsperaBinding
    var tema: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        tema = Utils.setTema(activity as JogoActivity)
        binding = FragmentEsperaBinding.inflate(inflater)

        if (savedInstanceState != null) {
            countBotaoPausa = savedInstanceState.getInt("count")
        }

        if (countBotaoPausa % 2 == 0) {
            setPausaTheme(tema, Constantes.PAUSA)
        } else {
            setPausaTheme(tema, Constantes.PLAY)
        }

        maxValueGame.state.observe(requireActivity()) {
            Log.i(ContentValues.TAG, "Observer : cheguei")
        }

        binding.pauseImg.setOnClickListener(View.OnClickListener {
            if (countBotaoPausa % 2 == 0) {
                maxValueGame.pausaTimer()
                setPausaTheme(tema, Constantes.PLAY)
            } else {
                maxValueGame.comecaTimer()
                setPausaTheme(tema, Constantes.PAUSA)
            }

            countBotaoPausa++
        });

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("count", countBotaoPausa)
    }

    private fun setPausaTheme(tema: Int, botao: Int) {
        when (tema) {
            1 -> {
                if(botao == Constantes.PLAY)
                    binding.pauseImg.setBackgroundResource(R.drawable.ic_play_warm)
                else
                    binding.pauseImg.setBackgroundResource(R.drawable.ic_pause_game_warm)
            }
            2 -> {
                if(botao == Constantes.PLAY)
                    binding.pauseImg.setBackgroundResource(R.drawable.ic_play_foreground)
                else
                    binding.pauseImg.setBackgroundResource(R.drawable.ic_pause_game_foreground)
            }
            3 -> {
                if(botao == Constantes.PLAY)
                    binding.pauseImg.setBackgroundResource(R.drawable.ic_play_exotic)
                else
                    binding.pauseImg.setBackgroundResource(R.drawable.ic_pause_game_exotic)
            }
        }
    }


}