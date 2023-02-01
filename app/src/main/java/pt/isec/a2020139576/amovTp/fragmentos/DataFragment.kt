package pt.isec.a2020139576.amovTp.fragmentos

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_data.*
import pt.isec.a2020139576.amovTp.MaxValueGame
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.databinding.FragmentDataBinding

class DataFragment : Fragment() {

    lateinit var binding: FragmentDataBinding
    private val maxValueGame: MaxValueGame by activityViewModels()
    var tema: Int = 1

    lateinit var nivelJogador: TextView
    lateinit var scoreJogador: TextView
    lateinit var tempoJogador: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tema = Utils.setTema(activity as JogoActivity)
        binding = FragmentDataBinding.inflate(inflater)

        nivelJogador = binding.nivel
        scoreJogador = binding.score
        tempoJogador = binding.tempoJogo

        maxValueGame.state.observe(viewLifecycleOwner, Observer{value->
            nivelJogador.text = maxValueGame.nivelJogador.toString()
            scoreJogador.text = maxValueGame.scoreJogador.toString()
            acertos.text = maxValueGame.questoesAcertadas.toString()
            acertosRestantes.text = maxValueGame.acertosRestantes.toString()
        });

        maxValueGame.updateTime.observe(viewLifecycleOwner, Observer{value->

            nivelJogador.text = maxValueGame.nivelJogador.toString()
            tempoJogador.text = maxValueGame.updateTime.value.toString() + "s"
            scoreJogador.text = maxValueGame.scoreJogador.toString()
            acertos.text = maxValueGame.questoesAcertadas.toString()
            acertosRestantes.text = maxValueGame.acertosRestantes.toString()
        });

        setDataTheme(tema)
        return binding.root
    }

    private fun setDataTheme(tema: Int) {
        when (tema) {
            1 -> {
                binding.textNivel.setTextColor(Color.parseColor("#80381a"))
                binding.nivel.setTextColor(Color.parseColor("#80381a"))
                binding.tempoJogoText.setTextColor(Color.parseColor("#FAAA94"))
                binding.scoreText.setTextColor(Color.parseColor("#FAAA94"))
                binding.acertosText.setTextColor(Color.parseColor("#FAAA94"))
                binding.acertosRestantesText.setTextColor(Color.parseColor("#FAAA94"))

            }
            2 -> {
                binding.textNivel.setTextColor(Color.parseColor("#333A56"))
                binding.nivel.setTextColor(Color.parseColor("#333A56"))
                binding.tempoJogoText.setTextColor(Color.parseColor("#b0c4de"))
                binding.scoreText.setTextColor(Color.parseColor("#b0c4de"))
                binding.acertosText.setTextColor(Color.parseColor("#b0c4de"))
                binding.acertosRestantesText.setTextColor(Color.parseColor("#b0c4de"))
            }
            3 -> {
                binding.textNivel.setTextColor(Color.parseColor("#6E7649"))
                binding.nivel.setTextColor(Color.parseColor("#6E7649"))
                binding.tempoJogoText.setTextColor(Color.parseColor("#B1BCA0"))
                binding.scoreText.setTextColor(Color.parseColor("#B1BCA0"))
                binding.acertosText.setTextColor(Color.parseColor("#B1BCA0"))
                binding.acertosRestantesText.setTextColor(Color.parseColor("#B1BCA0"))
            }
        }
    }


}