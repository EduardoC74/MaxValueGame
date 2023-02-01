package pt.isec.a2020139576.amovTp.atividades

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.dialog_final_jogo.view.*
import kotlinx.android.synthetic.main.fragment_tabuleiro.view.*
import pt.isec.a2020139576.amovTp.Constantes
import pt.isec.a2020139576.amovTp.MaxValueGame
import pt.isec.a2020139576.amovTp.MaxValueGame.Companion.SERVER_PORT
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.databinding.ActivityUmJogadorBinding
import pt.isec.a2020139576.amovTp.fragmentos.DataFragment
import pt.isec.a2020139576.amovTp.fragmentos.EsperaFragment
import pt.isec.a2020139576.amovTp.fragmentos.EsperaMultiplayerFragment
import pt.isec.a2020139576.amovTp.fragmentos.TabuleiroFragment


class JogoActivity : AppCompatActivity() {
    companion object {
        private const val SERVER_MODE = 0
        private const val CLIENT_MODE = 1

        fun getServerModeIntent(context : Context) : Intent {
            return Intent(context,JogoActivity::class.java).apply {
                putExtra("modoJogo", SERVER_MODE)
            }
        }

        fun getClientModeIntent(context : Context) : Intent {
            return Intent(context,JogoActivity::class.java).apply {
                putExtra("modoJogo", CLIENT_MODE)
            }
        }

    }

    lateinit var binding: ActivityUmJogadorBinding
    var tema: Int = 1
    private lateinit var detector: GestureDetectorCompat
    val maxValueGame: MaxValueGame by viewModels()
    private var dlg: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        tema = Utils.setTema(this)
        Utils.setFullscreen(this)
        super.onCreate(savedInstanceState)
        binding = ActivityUmJogadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detector = GestureDetectorCompat(this, DiaryGestureListener())

        if (Constantes.MODOJOGO == 2 && maxValueGame.state.value == MaxValueGame.Estado.INICIO) {

            maxValueGame.carregaDados()

            maxValueGame.connectionState.observe(this) { state ->
                //updateUI()
                if (maxValueGame.state.value == MaxValueGame.Estado.JOGAR &&
                    dlg?.isShowing == true) {
                    dlg?.dismiss()
                    dlg = null
                }

                if (state == MaxValueGame.ConnectionState.CONNECTION_ERROR ||
                    state == MaxValueGame.ConnectionState.CONNECTION_ENDED ) {
                    finish()
                }
            }

            if (maxValueGame.connectionState.value == MaxValueGame.ConnectionState.SETTING_PARAMETERS) {
                when (intent.getIntExtra("modoJogo", SERVER_MODE)) {
                    SERVER_MODE -> startAsServer()
                    CLIENT_MODE -> startAsClient()
                }
            }

        }

        //NOVO
        if(maxValueGame.state.value == MaxValueGame.Estado.INICIO && Constantes.MODOJOGO == 1) {
            maxValueGame.changeState(MaxValueGame.Estado.JOGAR)
        }

        maxValueGame.state.observe(this) {
            if (maxValueGame.state.value == MaxValueGame.Estado.ESPERA_JOGAR && Constantes.MODOJOGO == 1) {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabuleiroFragment, EsperaFragment()).addToBackStack(null)
                    .commit()

                maxValueGame.changeState(MaxValueGame.Estado.EM_ESPERA)

            }
            else  if (maxValueGame.state.value == MaxValueGame.Estado.ESPERA_JOGAR && Constantes.MODOJOGO == 2) {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabuleiroFragment, EsperaMultiplayerFragment()).addToBackStack(null)
                    .commit()

                maxValueGame.changeState(MaxValueGame.Estado.EM_ESPERA)

            }
            else if (maxValueGame.state.value == MaxValueGame.Estado.JOGAR && Constantes.MODOJOGO == 1) {



                //SINGLE
                maxValueGame.inicializarMapaTabuleiro()

                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabuleiroFragment, TabuleiroFragment()).addToBackStack(null)
                    .commit()

                try {
                    maxValueGame.pausaTimer()
                } catch (e: java.lang.Exception) {
                }

                maxValueGame.tempoAtualizado(maxValueGame.updateTime.value!! * 1000.toLong())

                maxValueGame.changeState(MaxValueGame.Estado.EM_JOGO)

                //NOVO
            }
            else if (maxValueGame.state.value == MaxValueGame.Estado.JOGAR && Constantes.MODOJOGO == 2) {
                //SINGLE
                //maxValueGame.inicializarMapaTabuleiro()

                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabuleiroFragment, TabuleiroFragment()).addToBackStack(null)
                    .commit()

                try {
                    maxValueGame.pausaTimer()
                } catch (e: java.lang.Exception) {
                }

                maxValueGame.tempoAtualizado(maxValueGame.updateTime.value!! * 1000.toLong())

                maxValueGame.changeState(MaxValueGame.Estado.EM_JOGO)

                //NOVO
            }
            else if (maxValueGame.state.value == MaxValueGame.Estado.NOVO_NIVEL && Constantes.MODOJOGO == 2) {
                //SINGLE
                //maxValueGame.inicializarMapaTabuleiro()

                try {
                    maxValueGame.pausaTimer()
                } catch (e: java.lang.Exception) {
                }

                maxValueGame.updateTime.value = 5
                //updateTime.value = 60 - 5 * (nivelJogador - 1)
                maxValueGame.tempoAtualizado(maxValueGame.updateTime.value!! * 1000.toLong())
                //NOVO
            }
            else if(maxValueGame.state.value == MaxValueGame.Estado.INICIO) {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabuleiroFragment, TabuleiroFragment()).addToBackStack(null)
                    .commit()

            }
            else if (maxValueGame.state.value == MaxValueGame.Estado.JOGO_TERMINADO_MULTIPLAYER && Constantes.MODOJOGO == 2){
                val dialogBuilder: AlertDialog.Builder =
                    AlertDialog.Builder(this)
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_final_jogo, null)
                dialogBuilder.setView(dialogView)


                val alertDialog: AlertDialog = dialogBuilder.create()
                if(maxValueGame.venceu){
                    alertDialog.setTitle(getString(R.string.venceu))

                }else{
                    alertDialog.setTitle(getString(R.string.perdeu))
                }
                alertDialog.show()
                dialogView.Btn_sair_dialog.setOnClickListener() {
                    maxValueGame.stopServer()
                    maxValueGame.stopGame()
                    this.finish()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    try {
                        maxValueGame.pausaTimer()

                    } catch (e: java.lang.Exception) { }



                    alertDialog.dismiss()
                }
                dialogView.Btn_jogarNovamente.setOnClickListener() {
                    maxValueGame.stopServer()
                    maxValueGame.stopGame()
                    this.finish()
                    try {
                        maxValueGame.pausaTimer()
                    } catch (e: java.lang.Exception) {
                    }

                    alertDialog.dismiss()
                }
            }

        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.dataFragment, DataFragment()).addToBackStack(null).commit()

    }

    private fun startAsServer() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format(getString(R.string.msg_ip_address),strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this)
            .setTitle(R.string.server_mode)
            .setView(ll)
            .setPositiveButton(R.string.startGame) { _: DialogInterface, _: Int ->
                if (maxValueGame.connectionState.value != MaxValueGame.ConnectionState.CONNECTION_ESTABLISHED) {
                    Toast.makeText(this@JogoActivity, R.string.no_clients, Toast.LENGTH_LONG).show()
                    startAsServer()
                } else {
                    maxValueGame.changeState(MaxValueGame.Estado.JOGAR)
                    //informar os clientes que o jogo vai começar

                    maxValueGame.comecaMultijogador()

                }
            }
            .setOnCancelListener {
                maxValueGame.stopServer()
                finish()
            }
            .create()

        maxValueGame.startServer()
        dlg?.show()

    }

    private fun startAsClient() {
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it == '.')
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }
        val dlg = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.client_mode)
            .setMessage(R.string.ask_ip)
            .setPositiveButton(R.string.button_connect) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(this@JogoActivity, R.string.error_address, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    maxValueGame.startClient(strIP)
                }
            }
            .setNeutralButton(R.string.btn_emulator) { _: DialogInterface, _: Int ->
                maxValueGame.startClient("10.0.2.2", SERVER_PORT-1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
                finish()
            }
            .setCancelable(false)
            .setView(edtBox)
            .create()

        dlg.show()
    }

    override fun onBackPressed() {
            val dialogBuilder: AlertDialog.Builder =
                AlertDialog.Builder(this)
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_final_jogo, null)
            dialogBuilder.setView(dialogView)


            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.setTitle(getString(R.string.exit))
            alertDialog.show()

            dialogView.Btn_sair_dialog.setOnClickListener() {
                if(Constantes.MODOJOGO == 1) {
                    try {
                        maxValueGame.pausaTimer()
                    } catch (e: java.lang.Exception) {
                    }
                    this.finish()
                    alertDialog.dismiss()
                } else {
                    maxValueGame.stopServer()
                    maxValueGame.stopGame()
                    this.finish()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    try {
                        maxValueGame.pausaTimer()
                    } catch (e: java.lang.Exception) {
                    }

                    alertDialog.dismiss()
                }
            }
            dialogView.Btn_jogarNovamente.setOnClickListener() {
                if (Constantes.MODOJOGO == 1) {
                    try {
                        maxValueGame.pausaTimer()
                    } catch (e: java.lang.Exception) {
                    }
                    this.finish()
                    val intent = Intent(this, JogoActivity::class.java)
                    startActivity(intent)
                    alertDialog.dismiss()
                } else {
                    maxValueGame.stopServer()
                    maxValueGame.stopGame()
                    this.finish()
                    try {
                        maxValueGame.pausaTimer()
                    } catch (e: java.lang.Exception) {
                    }

                    alertDialog.dismiss()
                }
            }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(maxValueGame.state.value != MaxValueGame.Estado.EM_ESPERA) {
            return if (detector.onTouchEvent(event!!)) {
                true
            } else {
                super.onTouchEvent(event)
            }
        } else
            return false
    }

    inner class DiaryGestureListener: GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESOLD = 100
        var posMapa = HashMap<Int, ArrayList<Int>>()
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if(maxValueGame.state.value != MaxValueGame.Estado.EM_JOGO)
               return super.onFling(e1, e2, velocityX, velocityY)

            var diffX = e2?.x?.minus(e1!!.x) ?: 0.0F
            var diffY = e2?.y?.minus(e1!!.y) ?: 0.0F
            val buttons = arrayListOf(binding.tabuleiroFragment.btn1,binding.tabuleiroFragment.btn2,binding.tabuleiroFragment.btn3,
                binding.tabuleiroFragment.btn4,binding.tabuleiroFragment.btn5,binding.tabuleiroFragment.btn6,
                binding.tabuleiroFragment.btn8,binding.tabuleiroFragment.btn10,binding.tabuleiroFragment.btn11,
                binding.tabuleiroFragment.btn12,binding.tabuleiroFragment.btn13,binding.tabuleiroFragment.btn14,
                binding.tabuleiroFragment.btn15,binding.tabuleiroFragment.btn16,binding.tabuleiroFragment.btn18,
                binding.tabuleiroFragment.btn20,binding.tabuleiroFragment.btn21,binding.tabuleiroFragment.btn22,
                binding.tabuleiroFragment.btn23,binding.tabuleiroFragment.btn24,binding.tabuleiroFragment.btn25)

            for(i in 0 until  buttons.size) {
                var listaTemp = ArrayList<Int>()
                val location = IntArray(2)
                buttons[i].getLocationOnScreen(location)
                listaTemp.add(location[0])
                listaTemp.add(location[1])
                posMapa.put(i,listaTemp)
            }
            return if(Math.abs(diffX) > Math.abs(diffY)) {
                if(Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESOLD) { // se fizer uma linha
                    //linhas
                    if(e2!!.x >= posMapa[0]!!.get(0) && e2.x <= posMapa[4]!!.get(0) + (posMapa[1]!!.get(0)-posMapa[0]!!.get(0)) ) {
                        var acertou = 0
                        //primeira linha
                        if(e2.y >= posMapa[0]!!.get(1) && e2.y <= posMapa[5]!!.get(1)) {

                            //mandar msg ao servidor
                            if(Constantes.MODOJOGO == 2 && !maxValueGame.isServer) {
                                maxValueGame.enviarDadosJogo(0, Constantes.LINHA)

                                var mColors: Drawable

                                Thread.sleep(300)


                                if (maxValueGame.tentativa == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!



                                } else if (maxValueGame.tentativa == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                } else { // se errar

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                for (i in 0 until 5) { // se acertar

                                    var transition = arrayOf(mColors1, mColors)

                                    if (i == 1 || i == 3)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)
                                }

                            } else {
                                acertou = maxValueGame.analisaJogada(0, Constantes.LINHA)

                                var mColors: Drawable

                                if (acertou == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!



                                } else if (acertou == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!
                                    //color = Color.YELLOW
                                } else { // se errar
                                    //color = Color.RED
                                    // mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,theme)!!
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                for (i in 0 until 5) { // se acertar

                                    var transition = arrayOf(mColors1, mColors)

                                    if (i == 1 || i == 3)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)
                                }
                                maxValueGame.verificaAcertou(acertou)
                            }
                        } //segunda linha
                        else if(e2.y >= posMapa[8]!!.get(1) && e2.y <= posMapa[13]!!.get(1)) {

                            if(Constantes.MODOJOGO == 2 && !maxValueGame.isServer) {
                                maxValueGame.enviarDadosJogo(2, Constantes.LINHA)

                                var mColors: Drawable

                                Thread.sleep(300)


                                if (maxValueGame.tentativa == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!




                                } else if (maxValueGame.tentativa == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                } else { // se errar

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                for (i in 8 until 13) { // se acertar

                                    var transition = arrayOf(mColors1, mColors)

                                    if (i == 9 || i == 11)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)

                                }

                            } else {
                                acertou = maxValueGame.analisaJogada(2, Constantes.LINHA)
                                var mColors: Drawable

                                if (acertou == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!


                                } else if (acertou == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                    //color = Color.YELLOW
                                } else { // se errar
                                    //color = Color.RED
                                    // mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,theme)!!
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        null
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                for (i in 8 until 13) { // se acertar

                                    var transition = arrayOf(mColors1, mColors)

                                    if (i == 9 || i == 11)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)

                                }
                                maxValueGame.verificaAcertou(acertou)
                            }
                        } //terceira linha
                        else if(e2.y >= posMapa[16]!!.get(1) && e2.y <= posMapa[16]!!.get(1) +
                            (posMapa[16]!!.get(1)-posMapa[13]!!.get(1))) {

                            if(Constantes.MODOJOGO == 2 && !maxValueGame.isServer) {
                                maxValueGame.enviarDadosJogo(4, Constantes.LINHA)

                                var mColors: Drawable

                                Thread.sleep(300)


                                if (maxValueGame.tentativa == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!



                                } else if (maxValueGame.tentativa == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                } else { // se errar

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                for (i in 16 until 21) { // se acertar

                                    var transition = arrayOf(mColors1, mColors)

                                    if (i == 17 || i == 19)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)

                                }

                            } else {
                                acertou = maxValueGame.analisaJogada(4, Constantes.LINHA)
                                var mColors: Drawable

                                if (acertou == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!


                                } else if (acertou == Constantes.SECOND_MAX_VALUE) {

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                    //color = Color.YELLOW
                                } else { // se errar
                                    //color = Color.RED
                                    // mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,theme)!!
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        null
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!


                                for (i in 16 until 21) { // se acertar

                                    var transition = arrayOf(mColors1, mColors)

                                    if (i == 17 || i == 19)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)

                                }
                                maxValueGame.verificaAcertou(acertou)
                            }

                        }
                    }
                    true
                } else {
                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            } else {
                if(Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESOLD) {
                    //colunas
                    if( e2!!.y >= posMapa[0]!!.get(1) && e2.y <= posMapa[16]!!.get(1) + (posMapa[16]!!.get(1)-posMapa[13]!!.get(1))){
                        var acertou = 0
                        if( e2!!.x >= posMapa[0]!!.get(0) && e2.x <= posMapa[1]!!.get(0)){

                            if(Constantes.MODOJOGO == 2 && !maxValueGame.isServer) {
                                maxValueGame.enviarDadosJogo(0, Constantes.COLUNA)

                                var mColors: Drawable

                                Thread.sleep(300)


                                if (maxValueGame.tentativa == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!



                                } else if (maxValueGame.tentativa == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                } else { // se errar

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                var i = 0
                                while (i < 21) {
                                    var transition = arrayOf(mColors1, mColors)

                                    if(i == 0 || i == 5 || i == 8 || i == 13 || i == 16) {

                                        if(i == 5 || i == 13)
                                            transition = arrayOf(mColors2, mColors)

                                        val mTransition = TransitionDrawable(transition)

                                        buttons[i].background = mTransition// quando e operacao
                                        mTransition.startTransition(1000)
                                        mTransition.reverseTransition(1000)

                                    }
                                    i++
                                }

                            } else {
                            acertou = maxValueGame.analisaJogada(0,Constantes.COLUNA)
                            var mColors : Drawable

                            if(acertou == Constantes.MAX_VALUE) {
                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_maxvalue,theme)!!


                            } else if(acertou == Constantes.SECOND_MAX_VALUE) {
                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_secondmaxvalue,theme)!!


                            } else{ // se errar

                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,null)!!

                            }
                            var mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape,theme)!!
                            if(tema == 1)
                                mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_warm,theme)!!
                            else if(tema == 3)
                                mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_exotic,theme)!!

                            var mColors2 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_operation,theme)!!


                            var i = 0
                            while (i < 21) {
                                var transition = arrayOf(mColors1, mColors)

                                if(i == 0 || i == 5 || i == 8 || i == 13 || i == 16) {

                                    if(i == 5 || i == 13)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)

                                }
                                i++
                            }
                            maxValueGame.verificaAcertou(acertou)
                            }

                        }else if( e2!!.x >= posMapa[2]!!.get(0) && e2.x <= posMapa[3]!!.get(0)){

                            if(Constantes.MODOJOGO == 2 && !maxValueGame.isServer) {
                                maxValueGame.enviarDadosJogo(2, Constantes.COLUNA)

                                var mColors: Drawable

                                Thread.sleep(300)


                                if (maxValueGame.tentativa == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!



                                } else if (maxValueGame.tentativa == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                } else { // se errar

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                var i = 0
                                while (i < 21) {
                                    var transition = arrayOf(mColors1, mColors)

                                    if(i == 2 || i == 6 || i == 10 || i == 14 || i == 18) {

                                        if(i == 6 || i == 14)
                                            transition = arrayOf(mColors2, mColors)

                                        val mTransition = TransitionDrawable(transition)

                                        buttons[i].background = mTransition// quando e operacao
                                        mTransition.startTransition(1000)
                                        mTransition.reverseTransition(1000)
                                    }
                                    i++
                                }

                            } else {
                            acertou = maxValueGame.analisaJogada(2,Constantes.COLUNA)
                            var mColors : Drawable

                            if(acertou == Constantes.MAX_VALUE) {
                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_maxvalue,theme)!!

                                //color = Color.GREEN
                            } else if(acertou == Constantes.SECOND_MAX_VALUE) {

                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_secondmaxvalue,theme)!!

                                //color = Color.YELLOW
                            } else{ // se errar
                                //color = Color.RED
                                // mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,theme)!!
                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,null)!!

                            }
                            var mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape,theme)!!
                            if(tema == 1)
                                mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_warm,theme)!!
                            else if(tema == 3)
                                mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_exotic,theme)!!

                            var mColors2 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_operation,theme)!!
                            var i = 0
                            while (i < 21) {
                                var transition = arrayOf(mColors1, mColors)

                                if(i == 2 || i == 6 || i == 10 || i == 14 || i == 18) {

                                    if(i == 6 || i == 14)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)

                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)
                                }
                                i++
                            }
                            maxValueGame.verificaAcertou(acertou)
                            }


                        }else if( e2!!.x >= posMapa[4]!!.get(0) && e2!!.x <= posMapa[4]!!.get(0) +
                            (posMapa[4]!!.get(0)-posMapa[3]!!.get(0))){

                            if(Constantes.MODOJOGO == 2 && !maxValueGame.isServer) {
                                maxValueGame.enviarDadosJogo(4, Constantes.COLUNA)

                                var mColors: Drawable

                                Thread.sleep(300)


                                if (maxValueGame.tentativa == Constantes.MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_maxvalue,
                                        theme
                                    )!!



                                } else if (maxValueGame.tentativa == Constantes.SECOND_MAX_VALUE) {
                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_secondmaxvalue,
                                        theme
                                    )!!

                                } else { // se errar

                                    mColors = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_wrongvalue,
                                        theme
                                    )!!

                                }
                                var mColors1 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape,
                                    theme
                                )!!
                                if (tema == 1)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_warm,
                                        theme
                                    )!!
                                else if (tema == 3)
                                    mColors1 = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.button_shape_exotic,
                                        theme
                                    )!!

                                var mColors2 = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.button_shape_operation,
                                    theme
                                )!!

                                var i = 0
                                while (i < 21) {
                                    var transition = arrayOf(mColors1, mColors)

                                    if(i == 4 || i == 7 || i == 12 || i == 15 || i == 20) {

                                        if(i == 7 || i == 15)
                                            transition = arrayOf(mColors2, mColors)

                                        val mTransition = TransitionDrawable(transition)
                                        buttons[i].background = mTransition// quando e operacao
                                        mTransition.startTransition(1000)
                                        mTransition.reverseTransition(1000)
                                    }
                                    i++
                                }

                            } else {
                            acertou = maxValueGame.analisaJogada(4,Constantes.COLUNA)
                            var mColors : Drawable

                            if(acertou == Constantes.MAX_VALUE) {
                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_maxvalue,theme)!!

                                //color = Color.GREEN


                            } else if(acertou == Constantes.SECOND_MAX_VALUE) {

                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_secondmaxvalue,theme)!!

                                //color = Color.YELLOW
                            } else{ // se errar
                                //color = Color.RED
                                // mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,theme)!!
                                mColors = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_wrongvalue,null)!!

                            }
                            var mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape,theme)!!
                            if(tema == 1)
                                mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_warm,theme)!!
                            else if(tema == 3)
                                mColors1 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_exotic,theme)!!

                            var mColors2 = ResourcesCompat.getDrawable(resources,R.drawable.button_shape_operation,theme)!!

                            var i = 0
                            while (i < 21) {
                                var transition = arrayOf(mColors1, mColors)

                                if(i == 4 || i == 7 || i == 12 || i == 15 || i == 20) {

                                    if(i == 7 || i == 15)
                                        transition = arrayOf(mColors2, mColors)

                                    val mTransition = TransitionDrawable(transition)
                                    buttons[i].background = mTransition// quando e operacao
                                    mTransition.startTransition(1000)
                                    mTransition.reverseTransition(1000)
                                }
                                i++
                            }
                            //aqui tmb das
                            maxValueGame.verificaAcertou(acertou)
                        }
                        }
                    }
                    true
                } else {
                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            }
        }
    }

}
