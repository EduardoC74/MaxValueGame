package pt.isec.a2020139576.amovTp.fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.atividades.ModoJogoActivity
import pt.isec.a2020139576.amovTp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.databinding.FragmentModoComunicacaoBinding

class ModoComunicacaoFragment : Fragment() {


    lateinit var b: FragmentModoComunicacaoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        b = FragmentModoComunicacaoBinding.inflate(inflater)

        b.voltarAtras.setOnClickListener {
            findNavController().navigate(R.id.action_modoComunicacao_to_modoJogoFragment)
        }


        b.BtnModoServidor.setOnClickListener {
            startActivity(JogoActivity.getServerModeIntent(activity as ModoJogoActivity))
        }

        b.BtnModoCliente.setOnClickListener {
             startActivity(JogoActivity.getClientModeIntent(activity as ModoJogoActivity))
        }
        setTheme()
        return b.root;

    }

    /*fun iniciarJogo(modoJogo: Int) {
        val intent = Intent(activity, UmJogadorActivity::class.java).apply {
            putExtra("modoJogo", modoJogo)
        }
        startActivity(intent)
    }*/

    fun setTheme(){
        var tema = Utils.setTema((activity as ModoJogoActivity))
        when (tema){
            1-> {
                b.voltarAtrasTXT.setBackgroundResource(R.color.bottonSec_colorWarm)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.bottonSec_colorWarm)
            }
            2->{
                b.voltarAtrasTXT?.setBackgroundResource(R.color.botton_sec_color)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.botton_sec_color)
            }
            3->{
                b.voltarAtrasTXT?.setBackgroundResource(R.color.bottonSec_colorExotic)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.bottonSec_colorExotic)
            }
            else->{
                b.voltarAtrasTXT?.setBackgroundResource(R.color.botton_sec_color)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.botton_sec_color)
            }
        }
    }

}