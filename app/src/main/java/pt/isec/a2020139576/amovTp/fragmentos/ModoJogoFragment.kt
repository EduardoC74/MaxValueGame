package pt.isec.a2020139576.amovTp.fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.isec.a2020139576.amovTp.Constantes
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
//import pt.isec.a2020139576.tp.Utils
//import pt.isec.a2020139576.tp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.atividades.MainActivity
import pt.isec.a2020139576.amovTp.atividades.ModoJogoActivity
import pt.isec.a2020139576.amovTp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.databinding.FragmentModoJogoBinding

//import pt.isec.a2020139576.tp.atividades.ModoJogoActivity


class ModoJogoFragment : Fragment(){


    lateinit var b: FragmentModoJogoBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        b = FragmentModoJogoBinding.inflate(inflater)


        b.voltarAtras.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        b.BtnModo1.setOnClickListener {
            Constantes.MODOJOGO = 1

            val intent = Intent(activity, JogoActivity::class.java)
            startActivity(intent)
        }

        b.BtnModo2.setOnClickListener {
            Constantes.MODOJOGO = 2
            findNavController().navigate(R.id.action_modoJogoFragment_to_modoComunicacao)
        }

        //setThemeHelper()
        setTheme()
        return b.root
    }

    fun setTheme(){
        var tema = Utils.setTema((activity as ModoJogoActivity))
        when (tema){
            1-> {
                b.voltarAtrasText.setBackgroundResource(R.color.bottonSec_colorWarm)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.bottonSec_colorWarm)
            }
            2->{
                b.voltarAtrasText?.setBackgroundResource(R.color.botton_sec_color)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.botton_sec_color)
            }
            3->{
                b.voltarAtrasText?.setBackgroundResource(R.color.bottonSec_colorExotic)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.bottonSec_colorExotic)
            }
            else->{
                b.voltarAtrasText?.setBackgroundResource(R.color.botton_sec_color)
                b.voltarAtrasIMG?.setBackgroundResource(R.color.botton_sec_color)
            }
        }
    }

}