package pt.isec.a2020139576.amovTp.fragmentos


import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_final_jogo.view.*
import pt.isec.a2020139576.amovTp.Constantes
import pt.isec.a2020139576.amovTp.MaxValueGame
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.atividades.JogoActivity
import pt.isec.a2020139576.amovTp.databinding.FragmentTabuleiroBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TabuleiroFragment : Fragment(){
    private val maxValueGame: MaxValueGame by activityViewModels()
    lateinit var binding: FragmentTabuleiroBinding
    var tema: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        tema = Utils.setTema(activity as JogoActivity)
        binding = FragmentTabuleiroBinding.inflate(inflater)

        maxValueGame.updateTabuleiro.observe(viewLifecycleOwner, Observer{value->
            Log.i(TAG, "onCreateViewinicializaTabuleiroView: ")

            //NOVO
            if(maxValueGame.state.value != MaxValueGame.Estado.INICIO)
                inicializaTabuleiroView()
        })

        setTabuleiroTheme(tema)

        maxValueGame.state.observe(viewLifecycleOwner, Observer {
            if(maxValueGame.state.value == MaxValueGame.Estado.JOGO_TERMINADO){

                if(Constantes.MODOJOGO == 1){
                    val dialogBuilder: AlertDialog.Builder =
                        AlertDialog.Builder((activity as JogoActivity))
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_final_jogo, null)
                    dialogBuilder.setView(dialogView)


                    val alertDialog: AlertDialog = dialogBuilder.create()
                    alertDialog.setTitle(getString(R.string.exit))
                    alertDialog.show()

                    dialogView.Btn_sair_dialog.setOnClickListener() {
                        (activity as JogoActivity).finish()
                        alertDialog.dismiss()
                    }
                    dialogView.Btn_jogarNovamente.setOnClickListener() {
                        (activity as JogoActivity).finish()
                        val intent = Intent((activity as JogoActivity), JogoActivity::class.java)
                        startActivity(intent)
                        alertDialog.dismiss()
                    }

                    insereTop5()
                }else  if(Constantes.MODOJOGO == 2){
                    maxValueGame.state.value = MaxValueGame.Estado.ESPERA_JOGAR

                    if(maxValueGame.isServer) {

                        var indice = maxValueGame.checkEnd()
                        if( indice != -1) {
                            Thread.sleep(200)
                            maxValueGame.terminaJogo(indice)
                        }else if (maxValueGame.checkNivel()) {

                            maxValueGame.comecaNivel()
                            if(!maxValueGame.eliminado)
                                maxValueGame.changeState(MaxValueGame.Estado.NOVO_NIVEL)
                            //state.postValue(Estado.JOGAR)
                        }
                    } else
                        maxValueGame.enviarDadosJogo(0,0) //TODO MUDAR
                }
            }


        })
        return binding.root
    }

    fun insereTop5() {


        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm")
        val formatted = current.format(formatter)
        //   if(reversi.pontJogador1 > reversi.pontJogador2) {
        val db = Firebase.firestore
        val email = FirebaseAuth.getInstance().currentUser?.email
        val score = hashMapOf(
            "Nivel" to maxValueGame.nivelJogador,
            "Pontuacao" to maxValueGame.scoreJogador,
            "TempoUsado" to maxValueGame.tempoUsado
        )
        if (email != null) {
            db.collection("Jogadores").document(email).collection("Pontuacoes")
                .document("Pontuacao:$formatted").set(score)
        }


    }

    fun inicializaTabuleiroView(){

        val buttons = arrayListOf(binding.btn1,binding.btn2,binding.btn3,binding.btn4,binding.btn5,binding.btn6,
            binding.btn8,binding.btn10,binding.btn11,binding.btn12,binding.btn13,binding.btn14,binding.btn15,binding.btn16,
            binding.btn18,binding.btn20,binding.btn21,binding.btn22,binding.btn23,binding.btn24,binding.btn25)


            var index = 0
            for (linhas in 0 until 5) {
                for (colunas in 0 until maxValueGame.mapaExpressoes[linhas]!!.size) {
                    buttons[index].text = maxValueGame.mapaExpressoes[linhas]!![colunas]
                    index++
                }
            }



    }

    private fun setTabuleiroTheme(tema: Int) {
        when (tema) {
            1 -> {
                binding.btn1.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn3.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn5.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn11.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn13.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn15.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn21.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn23.setBackgroundResource(R.drawable.button_shape_warm)
                binding.btn25.setBackgroundResource(R.drawable.button_shape_warm)

            }
            2 -> {
                binding.btn1.setBackgroundResource(R.drawable.button_shape)
                binding.btn3.setBackgroundResource(R.drawable.button_shape)
                binding.btn5.setBackgroundResource(R.drawable.button_shape)
                binding.btn11.setBackgroundResource(R.drawable.button_shape)
                binding.btn13.setBackgroundResource(R.drawable.button_shape)
                binding.btn15.setBackgroundResource(R.drawable.button_shape)
                binding.btn21.setBackgroundResource(R.drawable.button_shape)
                binding.btn23.setBackgroundResource(R.drawable.button_shape)
                binding.btn25.setBackgroundResource(R.drawable.button_shape)
            }
            3 -> {
                binding.btn1.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn3.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn5.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn11.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn13.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn15.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn21.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn23.setBackgroundResource(R.drawable.button_shape_exotic)
                binding.btn25.setBackgroundResource(R.drawable.button_shape_exotic)
            }
        }
    }

}