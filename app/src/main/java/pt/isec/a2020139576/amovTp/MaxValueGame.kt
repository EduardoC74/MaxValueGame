package pt.isec.a2020139576.amovTp


import android.content.ContentValues.TAG
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.collections.HashMap
import kotlin.concurrent.thread

import kotlin.random.Random
import kotlin.random.nextInt

class MaxValueGame : ViewModel() {
    companion object {
        const val SERVER_PORT = 9999
    }

    enum class Estado {
        INICIO, JOGAR, NOVO_NIVEL, EM_JOGO, ESPERA_JOGAR, EM_ESPERA, JOGO_TERMINADO, PREPARANDO_MULTIPLAYER,JOGO_TERMINADO_MULTIPLAYER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    data class Jogadores(
        var nome: String?,
        var email: String?,
        var imagemPerfil: String?,
        var pontuacao: Int?,
        var index_tabuleiro: Int?, /*var tabuleiro: HashMap<Int,ArrayList<String>>?,*/
        var nivel: Int?,
        var n_acertos: Int?,
        var acertos_restantes: Int?,
        var eliminado: Boolean?
    )

    var dadosJogadores = arrayListOf<Jogadores>()
    var isServer: Boolean = false
    var nivelJogador: Int = 1
    var nivelMax: Int = 1
    var scoreJogador: Int = 0
    var questoesAcertadas: Int = 0
    var indexTabuleiroMax: Int = 0
    var eliminado: Boolean = false
    var indexTabuleiro: Int = 0
    var pedeTabuleiro: Boolean = false
    var tentativa: Int = 0

    var tempoUsado: Int = 0
    var venceu: Boolean = false
    var mapaExpressoes = HashMap<Int, ArrayList<String>>()
    var guardaMapa = HashMap<Int, ArrayList<String>>()

    var nomeJogador: String = ""
    var emailJogador: String = ""
    var imagemJogador: String = ""

    val state = MutableLiveData(Estado.INICIO)
    val _connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    val connectionState: LiveData<ConnectionState>
        get() = _connectionState

    private var listaSockets: ArrayList<Socket>? = ArrayList()

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var listaOutputs: ArrayList<OutputStream> = ArrayList()

    private var serverSocket: ServerSocket? = null

    private var threadsLista: ArrayList<Thread>? = null

    val listaOperations = ArrayList<String>(4)
    val listaOperationsNiveis = ArrayList<String>(4)
    var countOperations: Int = 1
    var numNivel: Int = 10
    var minCertosNivel: Int = 3
    var acertosRestantes: Int = 3
    val updateTime = MutableLiveData(60)
    val updateTabuleiro = MutableLiveData(0)
    val updateJogadores = MutableLiveData(0)
    var updateTimeBonus: Boolean = false
    lateinit var countdown_timer: CountDownTimer

    init {

        listaOperations.add("+")
        listaOperations.add("-")
        listaOperations.add("*")
        listaOperations.add("/")

        listaOperationsNiveis.add("+")

    }

    fun tempoAtualizado(time_in_seconds: Long) {
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
                if (state.value == Estado.EM_ESPERA && Constantes.MODOJOGO == 1) {
                    Log.i(TAG, "onFinish: RECEBaentrei ${state.value}")
                    nivelJogador++
                    updateTime.value = 60 - 5 * (nivelJogador - 1)


                    //tempo nao pode ser menor que 10
                    if (updateTime.value!! < 10)
                        updateTime.value = 10

                    if (countOperations != listaOperations.size) {
                        listaOperationsNiveis.add(listaOperations[countOperations])
                        countOperations++
                    }

                    questoesAcertadas = 0
                    numNivel += 10
                    minCertosNivel++
                    acertosRestantes = minCertosNivel

                    Constantes.MODOJOGO = 1

                    state.value = Estado.JOGAR

                } else if (updateTime.value != 0 && state.value == Estado.EM_JOGO) {
                    Log.i(TAG, "onFinish: cancel!espera ${state.value}")
                    countdown_timer.cancel()
                    tempoAtualizado((updateTime.value!! * 1000).toLong())

                } else if (state.value == Estado.NOVO_NIVEL && Constantes.MODOJOGO == 2) {
                    updateTime.value = 60 - 5 * (nivelJogador - 1)

                    if (updateTime.value!! < 10)
                        updateTime.value = 10

                    if (countOperations != listaOperations.size) {
                        listaOperationsNiveis.add(listaOperations[countOperations])
                        countOperations++
                    }

                    indexTabuleiro = 0

                    state.value = Estado.JOGAR

                    updateTabuleiro.postValue(updateTabuleiro.value!!.plus(1))  // faz vista atualizar
                } else if (updateTime.value == 0 && state.value != Estado.EM_ESPERA) {
                    eliminado = true

                    for (item: Jogadores in dadosJogadores) {
                        if (item.email.equals(emailJogador)) {
                            item.pontuacao = scoreJogador
                            item.index_tabuleiro = indexTabuleiro
                            item.nivel = nivelJogador
                            item.n_acertos = questoesAcertadas
                            item.eliminado = eliminado
                        }
                    }

                    state.value = Estado.JOGO_TERMINADO
                    Log.i(TAG, "state.value = Estado.JOGO_TERMINADO ")
                }

                //tempo de espera na mudança de nivel acabou

                Log.i(TAG, "onFinish: SAIFINISH ${state.value}+${updateTime.value}")

            }

            override fun onTick(p0: Long) {
                if (updateTimeBonus) {
                    if ((updateTime.value!!) + 5 <= 60 - 5 * (nivelJogador - 1))
                        updateTime.value = (updateTime.value!!) + 5
                    else
                        updateTime.value = 60 - 5 * (nivelJogador - 1)

                    updateTimeBonus = false
                } else {
                    updateTime.value = updateTime.value!! - 1
                    tempoUsado++
                }

                Log.i(TAG, "Segundos ${updateTime.value}")


            }
        }
        countdown_timer.start()
    }

    fun checkNivel(): Boolean { // verifica se todos acabaram nivel
        for (item: Jogadores in dadosJogadores) {
            if (item.n_acertos != minCertosNivel && item.eliminado != true) {
                return false
            }
        }
        return true
    }

    fun checkEnd(): Int { // verifica se todos acabaram nivel
        for (item: Jogadores in dadosJogadores) {
            if (!item.eliminado!!) {
                return -1
            }
        }

        var pontosMaior = 0
        var nivelMaior = 0
        var indice = 0

        for (item: Jogadores in dadosJogadores) {
            if (item.pontuacao!! > pontosMaior) {
                pontosMaior = item.pontuacao!!
                nivelMaior = item.nivel!!
                indice = dadosJogadores.indexOf(item)
            } else if(item.pontuacao!! == pontosMaior && item.nivel!! > nivelMaior) {
                pontosMaior = item.pontuacao!!
                nivelMaior = item.nivel!!
                indice = dadosJogadores.indexOf(item)
            }
        }
        return indice
    }

    fun verificaAcertou(acertou: Int) {
        if (acertou == Constantes.MAX_VALUE) {
            questoesAcertadas++
            acertosRestantes--
            scoreJogador += 2
            if (questoesAcertadas < minCertosNivel) {
                updateTimeBonus = true

            } else {
                updateTimeBonus = false
                countdown_timer.cancel()
                state.value = Estado.ESPERA_JOGAR
                if (Constantes.MODOJOGO == 1) {
                    updateTime.value = 5
                    tempoAtualizado(updateTime.value!! * 1000.toLong())
                }
            }
        } else if (acertou == Constantes.SECOND_MAX_VALUE) {
            scoreJogador++
        }

        if (Constantes.MODOJOGO == 1) {
            inicializarMapaTabuleiro()
            updateTabuleiro.value = updateTabuleiro.value!!.plus(1) // faz vista atualizar
        } else { // se multiplayer
            //pedir tabuleiro

            if (isServer) {

                indexTabuleiro++


                for (item: Jogadores in dadosJogadores) {
                    if (item.email.equals(emailJogador)) {
                        item.pontuacao = scoreJogador
                        item.index_tabuleiro = indexTabuleiro
                        item.nivel = nivelJogador
                        item.n_acertos = questoesAcertadas
                        item.acertos_restantes = acertosRestantes
                        item.eliminado = eliminado
                    }
                }


                var indice = checkEnd()
                if( indice != -1) {
                    Thread.sleep(200)
                    terminaJogo(indice)
                }else if (checkNivel()) {

                    comecaNivel()
                    if(!eliminado)
                        state.postValue(Estado.NOVO_NIVEL)
                    //state.postValue(Estado.JOGAR)
                } else {

                    //if(mapaExpressoes.size/5 <= indexTabuleiro+1){ // se tabuleiro não existe
                    if (indexTabuleiroMax < indexTabuleiro) { // se tabuleiro não existe

                        indexTabuleiroMax = indexTabuleiro
                        inicializarMapaTabuleiro()
                    }
                    //TODO copiar mapa
                    var linhas: Int = 0

                    mapaExpressoes.clear()
                    for (i in (indexTabuleiro * 5 + linhas) until (indexTabuleiro * 5 + 5)) {

                        var array: ArrayList<String> = ArrayList()

                        if (linhas % 2 == 0) {
                            for (j in 0 until 5) {
                                array.add(guardaMapa[i]!![j])

                            }
                        } else {
                            for (j in 0 until 3) {
                                array.add(guardaMapa[i]!![j])

                            }
                        }
                        mapaExpressoes[linhas] = array
                        linhas++
                    }

                    updateJogadores.postValue(updateTabuleiro.value!!.plus(1))
                    updateTabuleiro.postValue(updateTabuleiro.value!!.plus(1))

                    enviarTabuleiro(indexTabuleiro)
                }

            } else {
                //enviarDadosJogo()
            }
        }
    }

    fun pausaTimer() {
        countdown_timer.cancel()
    }

    fun comecaTimer() {
        tempoAtualizado(updateTime.value!!.toLong() * 1000)
    }

    fun gameFinish() {
        countdown_timer.cancel()
    }

    fun startServer() {
        if (serverSocket != null || socket != null || _connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return
        isServer = true
        _connectionState.postValue(ConnectionState.SERVER_CONNECTING)
        state.postValue(Estado.PREPARANDO_MULTIPLAYER)

        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.run {
                try {

                    while (state.value != Estado.JOGAR) {

                        val socketClient = serverSocket!!.accept()

                        listaSockets!!.add(socketClient)
                        startComm(socketClient)
                    }

                } catch (_: Exception) {

                    _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                } finally {
                    serverSocket?.close()
                    serverSocket = null
                }
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        _connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
    }


    fun inicializaDados() {

        if (state.value != Estado.PREPARANDO_MULTIPLAYER) { // TODO !isServer

            socketO?.run {
                thread {
                    try {
                        val jsonNew = JSONObject()
                        jsonNew.put("nomeJogador", nomeJogador)
                        jsonNew.put("emailJogador", emailJogador)
                        jsonNew.put("imagemJogador", imagemJogador)
                        //json.put(jsonNew)


                        val msg = jsonNew.toString()

                        val printStream = PrintStream(this)
                        printStream.println(msg)
                        printStream.flush()

                    } catch (e: Exception) {

                        stopGame()
                    }
                }
            }
        }
    }

    fun enviarDadosJogo(key: Int, type: Int) { // Cliente Envia
        socketO?.run {
            thread {
                try {
                    //indexTabuleiro++
                    val jsonNew = JSONObject()
                    jsonNew.put("emailJogador", emailJogador)
                    jsonNew.put("pontuacao", scoreJogador)
                    jsonNew.put("index_tabuleiro", indexTabuleiro)
                    jsonNew.put("nivel", nivelJogador)
                    jsonNew.put("n_acertos", questoesAcertadas)
                    jsonNew.put("acertos_restantes", acertosRestantes)
                    jsonNew.put("eliminado", eliminado)
                    jsonNew.put("index", key)
                    jsonNew.put("linha_coluna", type)

                    val msg = jsonNew.toString()

                    val printStream = PrintStream(this)
                    printStream.println(msg)
                    printStream.flush()

                    pedeTabuleiro = true

                } catch (e: Exception) {

                    stopGame()
                }
            }
        }
        //}
    }

    fun carregaDados() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            val db = Firebase.firestore
            db.collection("Jogadores")
                .document(FirebaseAuth.getInstance().currentUser!!.email!!)
                .addSnapshotListener { docSS, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    if (docSS != null && docSS.exists()) {
                        val nome = docSS.getString("Nome")
                        if (nome != null)
                            nomeJogador = nome

                        val imagem = docSS.getString("ImagemPerfilUrl")

                        if (imagem == "")
                            return@addSnapshotListener
                        if (imagem != null)
                            imagemJogador = imagem

                        val email = docSS.getString("E-mail")
                        if (email != null)
                            emailJogador = email

                        val item = Jogadores(
                            nomeJogador, emailJogador, imagemJogador,
                            0, 0, 1, 0,minCertosNivel, false
                        )
                        dadosJogadores.add(item)


                    }
                }
        }
    }

    fun comecaNivel() {
        numNivel += 10
        minCertosNivel++
        nivelMax++
        indexTabuleiroMax = 0
        if(!eliminado) {
            acertosRestantes = minCertosNivel
            questoesAcertadas = 0
            nivelJogador = nivelMax
        }

        for (o: OutputStream in listaOutputs) {
            o.run {
                thread {
                    try {
                        val jsonArray = JSONArray()
                        var json: JSONObject

                        json = JSONObject()
                        json.put("min_acertos", minCertosNivel)
                        json.put("novo_nivel", nivelMax)
                        jsonArray.put(json)


                        for (a: Jogadores in dadosJogadores) {
                            if(!a.eliminado!!) {
                                a.index_tabuleiro = 0
                                a.nivel = nivelMax
                                a.n_acertos = 0
                                a.acertos_restantes = minCertosNivel
                            }
                        }

                        for (a: Jogadores in dadosJogadores) {
                            json = JSONObject()
                            json.put("nome", a.nome)
                            json.put("email", a.email)
                            json.put("imagemPerfil", a.imagemPerfil)
                            json.put("index_tabuleiro", 0)
                            json.put("nivel", a.nivel)
                            json.put("eliminado", a.eliminado)
                            json.put("n_acertos", 0)
                            json.put("acertos_restantes", minCertosNivel)
                            json.put("pontuacao", a.pontuacao)
                            jsonArray.put(json)
                        }

                        val jsonn = JSONObject()

                        guardaMapa.clear()
                        inicializarMapaTabuleiro()

                        //inicializar mapa do servidor
                        mapaExpressoes.clear()
                        for (i in 0 until 5) {

                            var array: ArrayList<String> = ArrayList()

                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    array.add(guardaMapa[i]!![j])

                                }
                            } else {
                                for (j in 0 until 3) {
                                    array.add(guardaMapa[i]!![j])

                                }
                            }
                            mapaExpressoes[i] = array
                        }

                        //updateTabuleiro.postValue(updateTabuleiro.value!!.plus(1))

                        //mandar mapa para os clientes
                        for (i in 0 until 5) {
                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    jsonn.put("tabuleiro$i$j", guardaMapa[i]!![j])


                                }
                            } else {
                                for (j in 0 until 3) {
                                    jsonn.put("tabuleiro$i$j", guardaMapa[i]!![j])


                                }
                            }
                        }

                        jsonArray.put(jsonn)

                        val msg = jsonArray.toString()
                        val printStream = PrintStream(this)
                        printStream.println(msg)

                        printStream.flush()

                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }


        }

    }

    fun terminaJogo(indice: Int) {
            var jogador = dadosJogadores.get(indice)
            if(jogador.email.equals(emailJogador)){

                venceu = true
            }

            state.postValue(Estado.JOGO_TERMINADO_MULTIPLAYER)


        for (o: OutputStream in listaOutputs) {
            o.run {
                thread {
                    try {
                        val jsonArray = JSONArray()
                        var json: JSONObject

                        json = JSONObject()
                        json.put("min_acertos", indice)
                        json.put("novo_nivel", -1)
                        jsonArray.put(json)

                        for (a: Jogadores in dadosJogadores) {
                            json = JSONObject()
                            json.put("nome", a.nome)
                            json.put("email", a.email)
                            json.put("imagemPerfil", a.imagemPerfil)
                            json.put("index_tabuleiro", 0)
                            json.put("nivel", a.nivel)
                            json.put("eliminado", a.eliminado)
                            json.put("n_acertos", 0)
                            json.put("acertos_restantes", minCertosNivel)
                            json.put("pontuacao", a.pontuacao)
                            jsonArray.put(json)
                        }

                        val jsonn = JSONObject()

                        //inicializar mapa do servidor
                        mapaExpressoes.clear()
                        for (i in 0 until 5) {

                            var array: ArrayList<String> = ArrayList()

                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    array.add(guardaMapa[i]!![j])

                                }
                            } else {
                                for (j in 0 until 3) {
                                    array.add(guardaMapa[i]!![j])

                                }
                            }
                            mapaExpressoes[i] = array
                        }

                        //updateTabuleiro.postValue(updateTabuleiro.value!!.plus(1))

                        //mandar mapa para os clientes
                        for (i in 0 until 5) {
                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    jsonn.put("tabuleiro$i$j", guardaMapa[i]!![j])

                                }
                            } else {
                                for (j in 0 until 3) {
                                    jsonn.put("tabuleiro$i$j", guardaMapa[i]!![j])
                                }
                            }
                        }

                        jsonArray.put(jsonn)

                        val msg = jsonArray.toString()
                        val printStream = PrintStream(this)
                        printStream.println(msg)

                        printStream.flush()

                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }


        }

    }

    private fun startComm(newSocket: Socket) { //
        var threadComm: Thread? = null

        socket = newSocket // referencia de cada clientSocket

        threadComm = thread {
            try {
            if (socketI == null)
                return@thread

            listaOutputs.add(socketO!!)

            _connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
            inicializaDados()

            val bufI = socketI!!.bufferedReader()


            while (state.value != Estado.JOGO_TERMINADO_MULTIPLAYER ) {

                val message = bufI.readLine()

                //se for servidor e jogo ainda n comecou
                if (state.value == Estado.PREPARANDO_MULTIPLAYER && isServer) {
                    val json = JSONObject(message)

                    val nomeJ = json.getString("nomeJogador")
                    val emailJ = json.getString("emailJogador")
                    val imagemJ = json.getString("imagemJogador")

                    dadosJogadores.add(
                        Jogadores(
                            nomeJ, emailJ, imagemJ,
                            0, 0, 1, 0, minCertosNivel,false
                        )
                    )


                    //clientes recebm dados no inicio do jogo

                    //se for servidor e o jogo ja tiver começado
                }
                else if (isServer) {

                    val json = JSONObject(message)


                    val emailJ = json.getString("emailJogador")
                    val pontuacaoJ = json.getInt("pontuacao")
                    var index_tabuleiroJ = json.getInt("index_tabuleiro")
                    val nivel = json.getInt("nivel")
                    var n_acertosJ = json.getInt("n_acertos")
                    var acertos_restantesJ = json.getInt("acertos_restantes")
                    val eliminadoJ = json.getBoolean("eliminado")

                    val indexJ = json.getInt("index")
                    val linha_colunaJ = json.getInt("linha_coluna")


                    for (item: Jogadores in dadosJogadores) {
                        if (item.email.equals(emailJ)) {
                            item.pontuacao = pontuacaoJ
                            item.index_tabuleiro = index_tabuleiroJ
                            item.nivel = nivel
                            item.n_acertos = n_acertosJ
                            item.acertos_restantes = acertos_restantesJ
                            item.eliminado = eliminadoJ
                        }
                    }

                    //TODO analisaJogada
                    var acertou = analisaJogadaMultiplayer(indexJ, linha_colunaJ, index_tabuleiroJ)


                    index_tabuleiroJ++
                    if(acertou == 2) {
                        n_acertosJ++
                        acertos_restantesJ--
                    }

                    updateJogadores.postValue(updateTabuleiro.value!!.plus(1))

                    for (item: Jogadores in dadosJogadores) {
                        if (item.email.equals(emailJ)) {
                            item.pontuacao = pontuacaoJ + acertou
                            item.index_tabuleiro = index_tabuleiroJ
                            item.nivel = nivel
                            item.n_acertos = n_acertosJ
                            item.acertos_restantes = acertos_restantesJ
                            item.eliminado = eliminadoJ
                        }
                    }

                        //mudei
                        if (indexTabuleiroMax < index_tabuleiroJ) { // se tabuleiro não existe

                            indexTabuleiroMax = index_tabuleiroJ

                            inicializarMapaTabuleiro()
                        }

                        enviarTabuleiro(index_tabuleiroJ)

                    var indice = checkEnd()

                    if( indice != -1) {
                        Thread.sleep(200)
                        terminaJogo(indice)
                    }else if (checkNivel()) {
                        //enviarTabuleiro(index_tabuleiroJ)
                        Thread.sleep(200)
                        comecaNivel()
                        if (!eliminado)
                            state.postValue(Estado.NOVO_NIVEL)
                    }

                    //se for cliente e estiver em espera pelo comeco do jogo
                }
                else if (!isServer && state.value == Estado.INICIO) { // se for cliente e for começo de jogo
                    val json = JSONArray(message)

                    val jsons = json.getJSONObject(0)
                    val novoNivel = jsons.optInt("novo_nivel")
                    val minAcertos = jsons.optInt("min_acertos")

                    nivelJogador = novoNivel
                    minCertosNivel = minAcertos

                    dadosJogadores.clear()
                    //recebe jogadores
                    for (i in 1 until json.length() - 1) {
                        val jsonObject = json.getJSONObject(i)

                        val nome = jsonObject.optString("nome")
                        val email = jsonObject.optString("email")
                        val imagemPerfil = jsonObject.optString("imagemPerfil")
                        val index_tabuleiro = jsonObject.optString("index_tabuleiro").toInt()

                        val nivel = jsonObject.optString("nivel").toInt()
                        val eliminado = jsonObject.optString("eliminado").toBoolean()
                        val n_acertos = jsonObject.optString("n_acertos").toInt()
                        val acertos_restantes = jsonObject.optString("acertos_restantes").toInt()
                        val pontuacao = jsonObject.optString("pontuacao").toInt()

                        dadosJogadores.add(
                            Jogadores(
                                nome, email, imagemPerfil,
                                pontuacao, index_tabuleiro, nivel, n_acertos, acertos_restantes, eliminado
                            )
                        )

                    }

                    for (item: Jogadores in dadosJogadores) {
                        if (item.email.equals(emailJogador)) {
                            scoreJogador = item.pontuacao!!
                            indexTabuleiro = item.index_tabuleiro!!
                            nivelJogador = item.nivel!!
                            questoesAcertadas = item.n_acertos!!
                            acertosRestantes = item.acertos_restantes!!
                        }
                    }

                    val jsonObject = json.getJSONObject(json.length() - 1)

                    mapaExpressoes.clear()

                    //recebe tabuleiro
                    for (i in 0 until 5) {

                        var array: ArrayList<String> = ArrayList()

                        if (i % 2 == 0) {
                            for (j in 0 until 5) {
                                array.add(jsonObject.optString("tabuleiro$i$j"))
                            }
                        } else {
                            for (j in 0 until 3) {
                                array.add(jsonObject.optString("tabuleiro$i$j"))
                            }
                        }

                        mapaExpressoes.put(i, array)
                    }

                    state.postValue(Estado.JOGAR)



                    //se for cliente e estiver a jogar
                }
                else if (!isServer && state.value == Estado.EM_JOGO) {

                    val json = JSONArray(message)

                    dadosJogadores.clear()
                    //recebe jogadores
                    for (i in 1 until json.length() - 1) {
                        val jsonObject = json.getJSONObject(i)

                        val nome = jsonObject.optString("nome")
                        val email = jsonObject.optString("email")
                        val imagemPerfil = jsonObject.optString("imagemPerfil")
                        val index_tabuleiro = jsonObject.optString("index_tabuleiro").toInt()

                        val nivel = jsonObject.optString("nivel").toInt()
                        val eliminado = jsonObject.optString("eliminado").toBoolean()
                        val n_acertos = jsonObject.optString("n_acertos").toInt()
                        val acertos_restantes = jsonObject.optString("acertos_restantes").toInt()
                        val pontuacao = jsonObject.optString("pontuacao").toInt()

                        dadosJogadores.add(
                            Jogadores(
                                nome, email, imagemPerfil,
                                pontuacao, index_tabuleiro, nivel, n_acertos, acertos_restantes, eliminado
                            )
                        )

                    }

                    for (item: Jogadores in dadosJogadores) {
                        if (item.email.equals(emailJogador)) {
                            if(item.pontuacao == scoreJogador + 2) {
                                tentativa = 2
                                updateTimeBonus = true
                            }
                            else if(item.pontuacao == scoreJogador + 1)
                                tentativa = 1
                            else
                                tentativa = 0

                            scoreJogador = item.pontuacao!!
                            questoesAcertadas = item.n_acertos!!
                            indexTabuleiro = item.index_tabuleiro!!
                            acertosRestantes = item.acertos_restantes!!

                        }
                    }

                    if(acertosRestantes == 0) {
                        updateTimeBonus = false
                        countdown_timer.cancel()
                        state.postValue(Estado.ESPERA_JOGAR)
                    }

                    val jsonObject = json.getJSONObject(json.length() - 1)

                    if (pedeTabuleiro) {
                        mapaExpressoes.clear()

                        //recebe tabuleiro
                        for (i in 0 until 5) {

                            var array: ArrayList<String> = ArrayList()

                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    array.add(jsonObject.getString("tabuleiro$i$j"))
                                }
                            } else {
                                for (j in 0 until 3) {
                                    array.add(jsonObject.getString("tabuleiro$i$j"))
                                }
                            }

                            mapaExpressoes.put(i, array)
                        }

                        updateTabuleiro.postValue(updateTabuleiro.value!!.plus(1))  // faz vista atualizar
                        pedeTabuleiro = false
                    }

                }
                else if (!isServer && state.value == Estado.EM_ESPERA) {

                    val json = JSONArray(message)
                    var jsons: JSONObject
                    jsons = json.getJSONObject(0)
                    val novoNivel = jsons.optInt("novo_nivel")
                    val minAcertos = jsons.optInt("min_acertos")

                    if(novoNivel == -1) {
                        var jogador = dadosJogadores.get(minAcertos)
                        if(jogador.email.equals(emailJogador)){
                            venceu = true
                        }
                        state.postValue(Estado.JOGO_TERMINADO_MULTIPLAYER)
                    } else {

                    var leTab = false

                    //recebe jogadores
                    dadosJogadores.clear()

                    for (i in 1 until json.length() - 1) {
                        jsons = json.getJSONObject(i)

                        val nome = jsons.optString("nome")
                        val email = jsons.optString("email")
                        val imagemPerfil = jsons.optString("imagemPerfil")
                        val index_tabuleiro = jsons.optString("index_tabuleiro").toInt()

                        val nivel = jsons.optString("nivel").toInt()

                        val eliminado = jsons.optString("eliminado").toBoolean()
                        val n_acertos = jsons.optString("n_acertos").toInt()
                        val acertos_restantes = jsons.optString("acertos_restantes").toInt()
                        val pontuacao = jsons.optString("pontuacao").toInt()

                        dadosJogadores.add(
                            Jogadores(
                                nome, email, imagemPerfil,
                                pontuacao, index_tabuleiro, nivel, n_acertos, acertos_restantes, eliminado
                            )
                        )

                    }

                    for (item: Jogadores in dadosJogadores) {
                        if (item.email.equals(emailJogador)) {
                            scoreJogador = item.pontuacao!!
                            questoesAcertadas = item.n_acertos!!
                            indexTabuleiro = item.index_tabuleiro!!
                            acertosRestantes = item.acertos_restantes!!
                        }
                    }

                    if (novoNivel > nivelJogador && !eliminado) {
                        leTab = true
                        //questoesAcertadas = 0
                        nivelJogador = novoNivel
                        //minCertosNivel = minAcertos
                        acertosRestantes = minAcertos
                        state.postValue(Estado.NOVO_NIVEL)
                        //state.postValue(Estado.JOGAR)
                    }

                    val jsonObject = json.getJSONObject(json.length() - 1)

                    if (leTab) {
                        mapaExpressoes.clear()
                        //recebe tabuleiro
                        for (i in 0 until 5) {

                            var array: ArrayList<String> = ArrayList()

                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    array.add(jsonObject.getString("tabuleiro$i$j"))
                                }
                            } else {
                                for (j in 0 until 3) {
                                    array.add(jsonObject.getString("tabuleiro$i$j"))
                                }
                            }

                            mapaExpressoes.put(i, array)
                        }

                        //TODO tirar daqui e so atualizar quando muda para estado jogar
                        //updateTabuleiro.postValue(updateTabuleiro.value!!.plus(1))  // faz vista atualizar
                    }

                    updateJogadores.postValue(updateTabuleiro.value!!.plus(1))
                    }
                }

            }
            } catch (_: Exception) {
                stopGame()
            }
        }
        threadsLista?.add(threadComm)
    }

    fun enviarTabuleiro(index: Int) {

        for (o: OutputStream in listaOutputs) {
            o.run {
                thread {
                    try {

                        val jsonArray = JSONArray()

                        val json = JSONObject()
                        json.put("novo_nivel", nivelMax)
                        json.put("min_acertos", minCertosNivel)
                        jsonArray.put(json)

                        for (a: Jogadores in dadosJogadores) {
                            val json = JSONObject()
                            json.put("nome", a.nome)
                            json.put("email", a.email)
                            json.put("imagemPerfil", a.imagemPerfil)
                            json.put("index_tabuleiro", a.index_tabuleiro)
                            json.put("nivel", a.nivel)
                            json.put("eliminado", a.eliminado)
                            json.put("n_acertos", a.n_acertos)
                            json.put("acertos_restantes", a.acertos_restantes)
                            json.put("pontuacao", a.pontuacao)
                            jsonArray.put(json)
                        }

                        val jsonn = JSONObject()

                        var linhas: Int = 0
                        for (i in (index * 5 + linhas) until (index * 5 + 5)) {
                            if (linhas % 2 == 0) {
                                for (j in 0 until 5) {
                                    jsonn.put("tabuleiro$linhas$j", guardaMapa[i]!![j])

                                }
                            } else {
                                for (j in 0 until 3) {
                                    jsonn.put("tabuleiro$linhas$j", guardaMapa[i]!![j])

                                }
                            }
                            linhas++
                        }


                        jsonArray.put(jsonn)

                        val msg = jsonArray.toString()
                        val printStream = PrintStream(this)
                        printStream.println(msg)
                        printStream.flush()

                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }

        }
    }

    fun comecaMultijogador() {

        inicializarMapaTabuleiro()

        for (o: OutputStream in listaOutputs) {
            o.run {
                thread {
                    try {

                        val jsonArray = JSONArray()
                        var json: JSONObject
                        json = JSONObject()
                        json.put("novo_nivel", nivelMax)
                        json.put("min_acertos", minCertosNivel)
                        jsonArray.put(json)

                        for (a: Jogadores in dadosJogadores) {
                            json = JSONObject()
                            json.put("nome", a.nome)
                            json.put("email", a.email)
                            json.put("imagemPerfil", a.imagemPerfil)
                            json.put("index_tabuleiro", a.index_tabuleiro)

                            json.put("nivel", a.nivel)
                            json.put("eliminado", a.eliminado)
                            json.put("n_acertos", a.n_acertos)
                            json.put("acertos_restantes", a.acertos_restantes)
                            json.put("pontuacao", a.pontuacao)
                            jsonArray.put(json)
                        }

                        json = JSONObject()
                        for (i in 0 until 5) {
                            if (i % 2 == 0) {
                                for (j in 0 until 5) {
                                    json.put("tabuleiro$i$j", guardaMapa[i]!![j])

                                }
                            } else {
                                for (j in 0 until 3) {
                                    json.put("tabuleiro$i$j", guardaMapa[i]!![j])

                                }
                            }

                        }

                        jsonArray.put(json)

                        val msg = jsonArray.toString()
                        val printStream = PrintStream(this)
                        printStream.println(msg)
                        printStream.flush()

                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }


        }

        mapaExpressoes.clear()
        for (i in 0 until 5) {

            var array: ArrayList<String> = ArrayList()

            if (i % 2 == 0) {
                for (j in 0 until 5) {
                    array.add(guardaMapa[i]!![j])
                }
            } else {
                for (j in 0 until 3) {
                    array.add(guardaMapa[i]!![j])
                }
            }

            mapaExpressoes.put(i, array)
        }


    }

    fun stopGame() {
        try {
            //_state.postValue(State.GAME_OVER)
            _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            for (a: Thread in threadsLista!!) {
                a.interrupt()
                //a = null
            }
        } catch (_: Exception) {
        }
    }

    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {

        if (socket != null)
            return
        isServer = false
        thread {
            _connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {

                val newsocket = Socket()

                newsocket.connect(InetSocketAddress(serverIP, serverPort), 5000)

                startComm(newsocket)
            } catch (_: Exception) {
                _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                stopGame()
            }
        }
    }

    fun inicializarMapaTabuleiro() {

        val randomString = Random(System.currentTimeMillis())

        for (linhas in 0..4) { // numero de linhas
            val listaTemp = ArrayList<String>()
            if (linhas % 2 != 0) { // se for so expressoes
                for (colunas in 0..2) {

                    val randomValue =
                        listaOperationsNiveis[randomString.nextInt(listaOperationsNiveis.size)]
                    listaTemp.add(randomValue)
                }
            } else { // se for numeros e operacoes
                for (colunas in 0..4) { // n colunas por linha
                    if (colunas % 2 != 0) {

                        val randomValue =
                            listaOperationsNiveis[randomString.nextInt(listaOperationsNiveis.size)]
                        listaTemp.add(randomValue)
                    } else {

                        val randomValue = randomString.nextInt(0 until numNivel)
                        listaTemp.add(randomValue.toString())

                    }
                }
            }

            if (Constantes.MODOJOGO == 1)
                mapaExpressoes[linhas] = listaTemp
            else {
                //mudei
                guardaMapa[linhas + indexTabuleiroMax * 5] = listaTemp
            }
            //mapaExpressoes[linhas] = listaTemp
        }
    }

    fun changeState(estado: Estado) {
        state.postValue(estado)
    }

    fun maxValue(): ArrayList<Int> {
        var maxValue = 0
        var secondMaxValue = 0

        //maior valor entre colunas
        for (key in 0 until mapaExpressoes.size) { // corre as linhas
            var coluna = 0

            if (key % 2 == 0) { // se for a linha 0,2,4
                if ((mapaExpressoes.get(3)!![key / 2] != "*" && mapaExpressoes.get(3)!![key / 2] != "/") ||
                    (mapaExpressoes.get(1)!![key / 2] == "*" && mapaExpressoes.get(1)!![key / 2] == "/")
                ) { // se prioridade indiferente

                    when (mapaExpressoes.get(1)!![key / 2]) {
                        "+" -> {
                            coluna =
                                mapaExpressoes.get(0)!![key].toInt() + mapaExpressoes.get(2)!![key].toInt()
                        }
                        "-" -> {
                            coluna =
                                mapaExpressoes.get(0)!![key].toInt() - mapaExpressoes.get(2)!![key].toInt()
                        }
                        "*" -> {
                            coluna =
                                mapaExpressoes.get(0)!![key].toInt() * mapaExpressoes.get(2)!![key].toInt()
                        }
                        "/" -> {
                            if (mapaExpressoes.get(2)!![key].toInt() != 0) {
                                coluna =
                                    mapaExpressoes.get(0)!![key].toInt() / mapaExpressoes.get(2)!![key].toInt()
                            } else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaExpressoes.get(3)!![key / 2]) {
                        "+" -> {
                            coluna += mapaExpressoes.get(4)!![key].toInt()
                        }
                        "-" -> {
                            coluna -= mapaExpressoes.get(4)!![key].toInt()
                        }
                        "*" -> {
                            coluna *= mapaExpressoes.get(4)!![key].toInt()
                        }
                        "/" -> {
                            if (mapaExpressoes.get(4)!![key].toInt() != 0)
                                coluna /= mapaExpressoes.get(4)!![key].toInt()
                            else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }

                } else {
                    when (mapaExpressoes.get(3)!![key / 2]) {
                        "+" -> {
                            coluna =
                                mapaExpressoes.get(2)!![key].toInt() + mapaExpressoes.get(4)!![key].toInt()
                        }
                        "-" -> {
                            coluna =
                                mapaExpressoes.get(2)!![key].toInt() - mapaExpressoes.get(4)!![key].toInt()
                        }
                        "*" -> {
                            coluna =
                                mapaExpressoes.get(2)!![key].toInt() * mapaExpressoes.get(4)!![key].toInt()
                        }
                        "/" -> {
                            if (mapaExpressoes.get(4)!![key].toInt() != 0)
                                coluna =
                                    mapaExpressoes.get(2)!![key].toInt() / mapaExpressoes.get(4)!![key].toInt()
                            else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaExpressoes.get(1)!![key / 2]) {
                        "+" -> {
                            coluna += mapaExpressoes.get(0)!![key].toInt()
                        }
                        "-" -> {
                            coluna = mapaExpressoes.get(0)!![key].toInt() - coluna
                        }
                        "*" -> {
                            coluna *= mapaExpressoes.get(0)!![key].toInt()
                        }
                        "/" -> {
                            if (coluna != 0)
                                coluna = mapaExpressoes.get(0)!![key].toInt() / coluna
                            else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }
                }

                if (coluna > maxValue) {
                    secondMaxValue = maxValue
                    maxValue = coluna
                } else if (coluna > secondMaxValue)
                    secondMaxValue = coluna
                // }
            }
            //}
        }

        //maior valor entre linhas
        for (key in mapaExpressoes.keys) {
            var linha = 0
            if (mapaExpressoes.get(key)!!.size != 3) { // se n�o for linha de express�es

                if ((mapaExpressoes.get(key)!![3] != "*" && mapaExpressoes.get(key)!![3] != "/") ||
                    (mapaExpressoes.get(key)!![1] == "*" && mapaExpressoes.get(key)!![1] == "/")
                ) { // prioridade indiferente

                    when (mapaExpressoes.get(key)!![1]) {
                        "+" -> {
                            linha =
                                mapaExpressoes.get(key)!![0].toInt() + mapaExpressoes.get(key)!![2].toInt()
                        }
                        "-" -> {
                            linha =
                                mapaExpressoes.get(key)!![0].toInt() - mapaExpressoes.get(key)!![2].toInt()
                        }
                        "*" -> {
                            linha =
                                mapaExpressoes.get(key)!![0].toInt() * mapaExpressoes.get(key)!![2].toInt()
                        }
                        "/" -> {
                            if (mapaExpressoes.get(key)!![2].toInt() != 0)
                                linha =
                                    mapaExpressoes.get(key)!![0].toInt() / mapaExpressoes.get(key)!![2].toInt()
                            else
                                linha = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaExpressoes.get(key)!![3]) {
                        "+" -> {
                            linha += mapaExpressoes.get(key)!![4].toInt()
                        }
                        "-" -> {
                            linha -= mapaExpressoes.get(key)!![4].toInt()
                        }
                        "*" -> {
                            linha *= mapaExpressoes.get(key)!![4].toInt()
                        }
                        "/" -> {
                            if (mapaExpressoes.get(key)!![4].toInt() != 0)
                                linha /= mapaExpressoes.get(key)!![4].toInt()
                            else
                                linha = Constantes.NAO_EXISTE
                        }
                    }

                } else {
                    when (mapaExpressoes.get(key)!![3]) {
                        "+" -> {
                            linha =
                                mapaExpressoes.get(key)!![2].toInt() + mapaExpressoes.get(key)!![4].toInt()
                        }
                        "-" -> {
                            linha =
                                mapaExpressoes.get(key)!![2].toInt() - mapaExpressoes.get(key)!![4].toInt()
                        }
                        "*" -> {
                            linha =
                                mapaExpressoes.get(key)!![2].toInt() * mapaExpressoes.get(key)!![4].toInt()
                        }
                        "/" -> {
                            if (mapaExpressoes.get(key)!![4].toInt() != 0)
                                linha =
                                    mapaExpressoes.get(key)!![2].toInt() / mapaExpressoes.get(key)!![4].toInt()
                            else
                                linha = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaExpressoes.get(key)!![1]) {
                        "+" -> {
                            linha += mapaExpressoes.get(key)!![0].toInt()
                        }
                        "-" -> {
                            linha = mapaExpressoes.get(key)!![0].toInt() - linha
                        }
                        "*" -> {
                            linha *= mapaExpressoes.get(key)!![0].toInt()
                        }
                        "/" -> {
                            if (linha != 0)
                                linha = mapaExpressoes.get(key)!![0].toInt() / linha
                            else
                                linha = Constantes.NAO_EXISTE

                        }
                    }
                }


                if (linha > maxValue) {
                    secondMaxValue = maxValue
                    maxValue = linha
                } else if (linha > secondMaxValue)
                    secondMaxValue = linha
            }
        }


        return arrayListOf(maxValue, secondMaxValue)
    }

    fun maxValueMultiplayer(mapaAux : HashMap<Int, ArrayList<String>>): ArrayList<Int> {
        var maxValue = 0
        var secondMaxValue = 0

        //maior valor entre colunas
        for (key in 0 until mapaAux.size) { // corre as linhas
            var coluna = 0

            if (key % 2 == 0) { // se for a linha 0,2,4
                if ((mapaAux.get(3)!![key / 2] != "*" && mapaAux.get(3)!![key / 2] != "/") ||
                    (mapaAux.get(1)!![key / 2] == "*" && mapaAux.get(1)!![key / 2] == "/")
                ) { // se prioridade indiferente

                    when (mapaAux.get(1)!![key / 2]) {
                        "+" -> {
                            coluna =
                                mapaAux.get(0)!![key].toInt() + mapaAux.get(2)!![key].toInt()
                        }
                        "-" -> {
                            coluna =
                                mapaAux.get(0)!![key].toInt() - mapaAux.get(2)!![key].toInt()
                        }
                        "*" -> {
                            coluna =
                                mapaAux.get(0)!![key].toInt() * mapaAux.get(2)!![key].toInt()
                        }
                        "/" -> {
                            if (mapaAux.get(2)!![key].toInt() != 0) {
                                coluna =
                                    mapaAux.get(0)!![key].toInt() / mapaAux.get(2)!![key].toInt()
                            } else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaAux.get(3)!![key / 2]) {
                        "+" -> {
                            coluna += mapaAux.get(4)!![key].toInt()
                        }
                        "-" -> {
                            coluna -= mapaAux.get(4)!![key].toInt()
                        }
                        "*" -> {
                            coluna *= mapaAux.get(4)!![key].toInt()
                        }
                        "/" -> {
                            if (mapaAux.get(4)!![key].toInt() != 0)
                                coluna /= mapaAux.get(4)!![key].toInt()
                            else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }

                } else {
                    when (mapaAux.get(3)!![key / 2]) {
                        "+" -> {
                            coluna =
                                mapaAux.get(2)!![key].toInt() + mapaAux.get(4)!![key].toInt()
                        }
                        "-" -> {
                            coluna =
                                mapaAux.get(2)!![key].toInt() - mapaAux.get(4)!![key].toInt()
                        }
                        "*" -> {
                            coluna =
                                mapaAux.get(2)!![key].toInt() * mapaAux.get(4)!![key].toInt()
                        }
                        "/" -> {
                            if (mapaAux.get(4)!![key].toInt() != 0)
                                coluna =
                                    mapaAux.get(2)!![key].toInt() / mapaAux.get(4)!![key].toInt()
                            else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaAux.get(1)!![key / 2]) {
                        "+" -> {
                            coluna += mapaAux.get(0)!![key].toInt()
                        }
                        "-" -> {
                            coluna = mapaAux.get(0)!![key].toInt() - coluna
                        }
                        "*" -> {
                            coluna *= mapaAux.get(0)!![key].toInt()
                        }
                        "/" -> {
                            if (coluna != 0)
                                coluna = mapaAux.get(0)!![key].toInt() / coluna
                            else
                                coluna = Constantes.NAO_EXISTE
                        }
                    }
                }

                if (coluna > maxValue) {
                    secondMaxValue = maxValue
                    maxValue = coluna
                } else if (coluna > secondMaxValue)
                    secondMaxValue = coluna
                // }
            }
            //}
        }

        //maior valor entre linhas
        for (key in mapaAux.keys) {
            var linha = 0
            if (mapaAux.get(key)!!.size != 3) { // se n�o for linha de express�es

                if ((mapaAux.get(key)!![3] != "*" && mapaAux.get(key)!![3] != "/") ||
                    (mapaAux.get(key)!![1] == "*" && mapaAux.get(key)!![1] == "/")
                ) { // prioridade indiferente

                    when (mapaAux.get(key)!![1]) {
                        "+" -> {
                            linha =
                                mapaAux.get(key)!![0].toInt() + mapaAux.get(key)!![2].toInt()
                        }
                        "-" -> {
                            linha =
                                mapaAux.get(key)!![0].toInt() - mapaAux.get(key)!![2].toInt()
                        }
                        "*" -> {
                            linha =
                                mapaAux.get(key)!![0].toInt() * mapaAux.get(key)!![2].toInt()
                        }
                        "/" -> {
                            if (mapaAux.get(key)!![2].toInt() != 0)
                                linha =
                                    mapaAux.get(key)!![0].toInt() / mapaAux.get(key)!![2].toInt()
                            else
                                linha = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaAux.get(key)!![3]) {
                        "+" -> {
                            linha += mapaAux.get(key)!![4].toInt()
                        }
                        "-" -> {
                            linha -= mapaAux.get(key)!![4].toInt()
                        }
                        "*" -> {
                            linha *= mapaAux.get(key)!![4].toInt()
                        }
                        "/" -> {
                            if (mapaAux.get(key)!![4].toInt() != 0)
                                linha /= mapaAux.get(key)!![4].toInt()
                            else
                                linha = Constantes.NAO_EXISTE
                        }
                    }

                } else {
                    when (mapaAux.get(key)!![3]) {
                        "+" -> {
                            linha =
                                mapaAux.get(key)!![2].toInt() + mapaAux.get(key)!![4].toInt()
                        }
                        "-" -> {
                            linha =
                                mapaAux.get(key)!![2].toInt() - mapaAux.get(key)!![4].toInt()
                        }
                        "*" -> {
                            linha =
                                mapaAux.get(key)!![2].toInt() * mapaAux.get(key)!![4].toInt()
                        }
                        "/" -> {
                            if (mapaAux.get(key)!![4].toInt() != 0)
                                linha =
                                    mapaAux.get(key)!![2].toInt() / mapaAux.get(key)!![4].toInt()
                            else
                                linha = Constantes.NAO_EXISTE
                        }
                    }

                    when (mapaAux.get(key)!![1]) {
                        "+" -> {
                            linha += mapaAux.get(key)!![0].toInt()
                        }
                        "-" -> {
                            linha = mapaAux.get(key)!![0].toInt() - linha
                        }
                        "*" -> {
                            linha *= mapaAux.get(key)!![0].toInt()
                        }
                        "/" -> {
                            if (linha != 0)
                                linha = mapaAux.get(key)!![0].toInt() / linha
                            else
                                linha = Constantes.NAO_EXISTE

                        }
                    }
                }


                if (linha > maxValue) {
                    secondMaxValue = maxValue
                    maxValue = linha
                } else if (linha > secondMaxValue)
                    secondMaxValue = linha
            }
        }


        return arrayListOf(maxValue, secondMaxValue)
    }

    fun analisaJogadaMultiplayer(key: Int, type: Int, tabuleiro: Int): Int {
        var valor = 0

        var mapaAux = HashMap<Int, ArrayList<String>>()

        var linhas = 0
        for (i in (tabuleiro * 5 + linhas) until (tabuleiro * 5 + 5)) {

            var array: ArrayList<String> = ArrayList()

            if (linhas % 2 == 0) {
                for (j in 0 until 5) {
                    array.add(guardaMapa[i]!![j])
                }
            } else {
                for (j in 0 until 3) {
                    array.add(guardaMapa[i]!![j])
                }
            }
            mapaAux[linhas] = array
            linhas++
        }


        if (type == Constantes.LINHA) {

            if ((mapaAux.get(key)!![3] != "*" && mapaAux.get(key)!![3] != "/") ||
                (mapaAux.get(key)!![1] == "*" && mapaAux.get(key)!![1] == "/")
            ) { // prioridade indiferente

                when (mapaAux.get(key)!![1]) {
                    "+" -> {
                        valor =
                            mapaAux.get(key)!![0].toInt() + mapaAux.get(key)!![2].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaAux.get(key)!![0].toInt() - mapaAux.get(key)!![2].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaAux.get(key)!![0].toInt() * mapaAux.get(key)!![2].toInt()
                    }
                    "/" -> {
                        if (mapaAux.get(key)!![2].toInt() != 0)
                            valor =
                                mapaAux.get(key)!![0].toInt() / mapaAux.get(key)!![2].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaAux.get(key)!![3]) {
                    "+" -> {
                        valor += mapaAux.get(key)!![4].toInt()
                    }
                    "-" -> {
                        valor -= mapaAux.get(key)!![4].toInt()
                    }
                    "*" -> {
                        valor *= mapaAux.get(key)!![4].toInt()
                    }
                    "/" -> {
                        if (mapaAux.get(key)!![4].toInt() != 0)
                            valor /= mapaAux.get(key)!![4].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

            } else {
                when (mapaAux.get(key)!![3]) {
                    "+" -> {
                        valor =
                            mapaAux.get(key)!![2].toInt() + mapaAux.get(key)!![4].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaAux.get(key)!![2].toInt() - mapaAux.get(key)!![4].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaAux.get(key)!![2].toInt() * mapaAux.get(key)!![4].toInt()
                    }
                    "/" -> {
                        if (mapaAux.get(key)!![4].toInt() != 0)
                            valor =
                                mapaAux.get(key)!![2].toInt() / mapaAux.get(key)!![4].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaAux.get(key)!![1]) {
                    "+" -> {
                        valor += mapaAux.get(key)!![0].toInt()
                    }
                    "-" -> {
                        valor = mapaAux.get(key)!![0].toInt() - valor
                    }
                    "*" -> {
                        valor *= mapaAux.get(key)!![0].toInt()
                    }
                    "/" -> {
                        if (valor != 0)
                            valor = mapaAux.get(key)!![0].toInt() / valor
                        else
                            valor = Constantes.NAO_EXISTE

                    }
                }
            }
        } else {

            if ((mapaAux.get(3)!![key / 2] != "*" && mapaAux.get(3)!![key / 2] != "/") ||
                (mapaAux.get(1)!![key / 2] == "*" && mapaAux.get(1)!![key / 2] == "/")
            ) { // se prioridade indiferente

                when (mapaAux.get(1)!![key / 2]) {
                    "+" -> {
                        valor =
                            mapaAux.get(0)!![key].toInt() + mapaAux.get(2)!![key].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaAux.get(0)!![key].toInt() - mapaAux.get(2)!![key].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaAux.get(0)!![key].toInt() * mapaAux.get(2)!![key].toInt()
                    }
                    "/" -> {
                        if (mapaAux.get(2)!![key].toInt() != 0) {
                            valor =
                                mapaAux.get(0)!![key].toInt() / mapaAux.get(2)!![key].toInt()
                        } else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaAux.get(3)!![key / 2]) {
                    "+" -> {
                        valor += mapaAux.get(4)!![key].toInt()
                    }
                    "-" -> {
                        valor -= mapaAux.get(4)!![key].toInt()
                    }
                    "*" -> {
                        valor *= mapaAux.get(4)!![key].toInt()
                    }
                    "/" -> {
                        if (mapaAux.get(4)!![key].toInt() != 0)
                            valor /= mapaAux.get(4)!![key].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

            } else {
                when (mapaAux.get(3)!![key / 2]) {
                    "+" -> {
                        valor =
                            mapaAux.get(2)!![key].toInt() + mapaAux.get(4)!![key].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaAux.get(2)!![key].toInt() - mapaAux.get(4)!![key].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaAux.get(2)!![key].toInt() * mapaAux.get(4)!![key].toInt()
                    }
                    "/" -> {
                        if (mapaAux.get(4)!![key].toInt() != 0)
                            valor =
                                mapaAux.get(2)!![key].toInt() / mapaAux.get(4)!![key].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaAux.get(1)!![key / 2]) {
                    "+" -> {
                        valor += mapaAux.get(0)!![key].toInt()
                    }
                    "-" -> {
                        valor = mapaAux.get(0)!![key].toInt() - valor
                    }
                    "*" -> {
                        valor *= mapaAux.get(0)!![key].toInt()
                    }
                    "/" -> {
                        if (valor != 0)
                            valor = mapaAux.get(0)!![key].toInt() / valor
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }
            }
        }

        if (valor == maxValueMultiplayer(mapaAux)[0])
            return Constantes.MAX_VALUE
        else if (valor == maxValueMultiplayer(mapaAux)[1])
            return Constantes.SECOND_MAX_VALUE

        return 0
    }

    fun analisaJogada(key: Int, type: Int): Int {
        var valor = 0

        if (type == Constantes.LINHA) {

            if ((mapaExpressoes.get(key)!![3] != "*" && mapaExpressoes.get(key)!![3] != "/") ||
                (mapaExpressoes.get(key)!![1] == "*" && mapaExpressoes.get(key)!![1] == "/")
            ) { // prioridade indiferente

                when (mapaExpressoes.get(key)!![1]) {
                    "+" -> {
                        valor =
                            mapaExpressoes.get(key)!![0].toInt() + mapaExpressoes.get(key)!![2].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaExpressoes.get(key)!![0].toInt() - mapaExpressoes.get(key)!![2].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaExpressoes.get(key)!![0].toInt() * mapaExpressoes.get(key)!![2].toInt()
                    }
                    "/" -> {
                        if (mapaExpressoes.get(key)!![2].toInt() != 0)
                            valor =
                                mapaExpressoes.get(key)!![0].toInt() / mapaExpressoes.get(key)!![2].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaExpressoes.get(key)!![3]) {
                    "+" -> {
                        valor += mapaExpressoes.get(key)!![4].toInt()
                    }
                    "-" -> {
                        valor -= mapaExpressoes.get(key)!![4].toInt()
                    }
                    "*" -> {
                        valor *= mapaExpressoes.get(key)!![4].toInt()
                    }
                    "/" -> {
                        if (mapaExpressoes.get(key)!![4].toInt() != 0)
                            valor /= mapaExpressoes.get(key)!![4].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

            } else {
                when (mapaExpressoes.get(key)!![3]) {
                    "+" -> {
                        valor =
                            mapaExpressoes.get(key)!![2].toInt() + mapaExpressoes.get(key)!![4].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaExpressoes.get(key)!![2].toInt() - mapaExpressoes.get(key)!![4].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaExpressoes.get(key)!![2].toInt() * mapaExpressoes.get(key)!![4].toInt()
                    }
                    "/" -> {
                        if (mapaExpressoes.get(key)!![4].toInt() != 0)
                            valor =
                                mapaExpressoes.get(key)!![2].toInt() / mapaExpressoes.get(key)!![4].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaExpressoes.get(key)!![1]) {
                    "+" -> {
                        valor += mapaExpressoes.get(key)!![0].toInt()
                    }
                    "-" -> {
                        valor = mapaExpressoes.get(key)!![0].toInt() - valor
                    }
                    "*" -> {
                        valor *= mapaExpressoes.get(key)!![0].toInt()
                    }
                    "/" -> {
                        if (valor != 0)
                            valor = mapaExpressoes.get(key)!![0].toInt() / valor
                        else
                            valor = Constantes.NAO_EXISTE

                    }
                }
            }
        } else {

            if ((mapaExpressoes.get(3)!![key / 2] != "*" && mapaExpressoes.get(3)!![key / 2] != "/") ||
                (mapaExpressoes.get(1)!![key / 2] == "*" && mapaExpressoes.get(1)!![key / 2] == "/")
            ) { // se prioridade indiferente

                when (mapaExpressoes.get(1)!![key / 2]) {
                    "+" -> {
                        valor =
                            mapaExpressoes.get(0)!![key].toInt() + mapaExpressoes.get(2)!![key].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaExpressoes.get(0)!![key].toInt() - mapaExpressoes.get(2)!![key].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaExpressoes.get(0)!![key].toInt() * mapaExpressoes.get(2)!![key].toInt()
                    }
                    "/" -> {
                        if (mapaExpressoes.get(2)!![key].toInt() != 0) {
                            valor =
                                mapaExpressoes.get(0)!![key].toInt() / mapaExpressoes.get(2)!![key].toInt()
                        } else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaExpressoes.get(3)!![key / 2]) {
                    "+" -> {
                        valor += mapaExpressoes.get(4)!![key].toInt()
                    }
                    "-" -> {
                        valor -= mapaExpressoes.get(4)!![key].toInt()
                    }
                    "*" -> {
                        valor *= mapaExpressoes.get(4)!![key].toInt()
                    }
                    "/" -> {
                        if (mapaExpressoes.get(4)!![key].toInt() != 0)
                            valor /= mapaExpressoes.get(4)!![key].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

            } else {
                when (mapaExpressoes.get(3)!![key / 2]) {
                    "+" -> {
                        valor =
                            mapaExpressoes.get(2)!![key].toInt() + mapaExpressoes.get(4)!![key].toInt()
                    }
                    "-" -> {
                        valor =
                            mapaExpressoes.get(2)!![key].toInt() - mapaExpressoes.get(4)!![key].toInt()
                    }
                    "*" -> {
                        valor =
                            mapaExpressoes.get(2)!![key].toInt() * mapaExpressoes.get(4)!![key].toInt()
                    }
                    "/" -> {
                        if (mapaExpressoes.get(4)!![key].toInt() != 0)
                            valor =
                                mapaExpressoes.get(2)!![key].toInt() / mapaExpressoes.get(4)!![key].toInt()
                        else
                            valor = Constantes.NAO_EXISTE
                    }
                }

                when (mapaExpressoes.get(1)!![key / 2]) {
                    "+" -> {
                        valor += mapaExpressoes.get(0)!![key].toInt()
                    }
                    "-" -> {
                        valor = mapaExpressoes.get(0)!![key].toInt() - valor
                    }
                    "*" -> {
                        valor *= mapaExpressoes.get(0)!![key].toInt()
                    }
                    "/" -> {
                        if (valor != 0)
                            valor = mapaExpressoes.get(0)!![key].toInt() / valor
                        else
                            valor = Constantes.NAO_EXISTE

                    }
                }
            }
        }

        if (valor == maxValue()[0])
            return Constantes.MAX_VALUE
        else if (valor == maxValue()[1])
            return Constantes.SECOND_MAX_VALUE


        return 0
    }

}