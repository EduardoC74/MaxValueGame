package pt.isec.a2020139576.amovTp.fragmentos

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.listview_jogadores.view.*
import kotlinx.android.synthetic.main.listview_top_5.view.*
import kotlinx.android.synthetic.main.listview_top_5.view.nivel
import kotlinx.android.synthetic.main.listview_top_5.view.pontuacao
import pt.isec.a2020139576.amovTp.MaxValueGame
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.atividades.PontuacoesActivity
import pt.isec.a2020139576.amovTp.databinding.FragmentEsperaMultiplayerBinding

class EsperaMultiplayerFragment : Fragment(){

    data class mostraJogadores(var foto: String?, var nome: String?, var nivel: Int?, var pontuacao: Int?)

    private val maxValueGame: MaxValueGame by activityViewModels()
    lateinit var binding: FragmentEsperaMultiplayerBinding
    var tema: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        tema = Utils.setTema(activity as JogoActivity)
        binding = FragmentEsperaMultiplayerBinding.inflate(inflater)

        val data = arrayListOf<mostraJogadores>()

        maxValueGame.updateJogadores.observe(requireActivity()) {

            data.clear()
            for(jogador in maxValueGame.dadosJogadores) {
                val foto = jogador.imagemPerfil
                val nome = jogador.nome
                val nivel = jogador.nivel
                val pontuacao = jogador.pontuacao


                val item = mostraJogadores(foto,nome, nivel, pontuacao)
                data.add(item)
            }

            val adapter = AdaptarJogadores(data)
            binding.listaJogadores.adapter = adapter
        }

        return binding.root
    }

    class AdaptarJogadores(val data : ArrayList<mostraJogadores>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any {
            return data[position]
        }

        override fun getItemId(position: Int): Long = position.toLong()

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.listview_jogadores,parent,false)

            Glide.with(view).load(data[position].foto).into(view.jogadorImg)
            view.nome.text = data[position].nome.toString()
            view.nivel.text = data[position].nivel.toString()
            view.pontuacao.text = data[position].pontuacao.toString()

            return view
        }

    }

    private fun setPausaTheme(tema: Int, botao: Int) {
        when (tema) {
            1 -> {

            }
            2 -> {

            }
            3 -> {

            }
        }
    }


}