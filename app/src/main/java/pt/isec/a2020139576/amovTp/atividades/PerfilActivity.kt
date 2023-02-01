package pt.isec.a2020139576.amovTp.atividades


import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_perfil.*
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.databinding.ActivityPerfilBinding
import pt.isec.a2020139576.amovTp.getTempFilename
import pt.isec.a2020139576.amovTp.setPic
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PerfilActivity : AppCompatActivity() {

    lateinit var b: ActivityPerfilBinding

    lateinit var gso: GoogleSignInOptions
    lateinit var gsi: GoogleSignInClient
    lateinit var fireAuth: FirebaseAuth
    private val google_web_id =
        "657194742102-fa6s354mk0vkjb25ho3fpb0ib9ck458m.apps.googleusercontent.com" //ir a configuracao sdk da web google
    var tema: Int = 1
    var imagemPerfilUri: Uri? = null
    var fotoTirada: Boolean = false
    private var imagePath : String? = null
    private lateinit var data: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setFullscreen(this)
        b = ActivityPerfilBinding.inflate(layoutInflater)
        tema = Utils.setTema(this)


        Utils.mostrarImagemPerfil(this, R.id.Imagem_Perfil)

        fireAuth = Firebase.auth
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(google_web_id).requestEmail().build()


        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.fragment_Perfil) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.navperfil)

        if (fireAuth.currentUser != null) {
            graph.setStartDestination(R.id.visualizarPerfilFragment)
        } else
            graph.setStartDestination(R.id.registarLoginPerfilFragment)

        navHostFragment.navController.graph = graph

        setTheme()
        setContentView(b.root)
        //checkTheme()
    }

    /* Retirado e adaptado de https://memorynotfound.com/java-display-list-countries-using-locale-getisocountries/ */
    fun spinnerCountries(spinner: Spinner) {
        val countries = ArrayList<String>()
        val isoCountries = Locale.getISOCountries()
        for (country in isoCountries) {
            val locale = Locale("en", country)
            val iso = locale.isO3Country
            val code = locale.country
            val name = locale.displayCountry
            if ("" != iso && "" != code && "" != name) {
                countries.add(countryCodeToEmoji(code) + " " + name)
            }
        }
        countries.sort()
        countries.add(0, "Escolha o seu País: ")

        val countryAdapter = ArrayAdapter(this.applicationContext,
            android.R.layout.simple_spinner_item, countries)

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = countryAdapter
    }

    /* Retirado e adaptado de https://attacomsian.com/blog/how-to-convert-country-code-to-emoji-in-java  */
    fun countryCodeToEmoji(code: String?): String {

        // offset between uppercase ascii and regional indicator symbols
        var code = code
        val OFFSET = 127397
        // validate code
        if (code == null || code.length != 2) {
            return ""
        }
        //fix for uk -> gb
        if (code.equals("uk", ignoreCase = true)) {
            code = "gb"
        }
        // convert code to uppercase
        code = code.uppercase(Locale.getDefault())
        val emojiStr = StringBuilder()
        //loop all characters
        for (element in code) {
            emojiStr.appendCodePoint(element.code + OFFSET)
        }
        // return emoji
        return emojiStr.toString()
    }


    fun onVoltarAtras(view: android.view.View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    fun onTirarFotografia(view: android.view.View) {

        takePhoto_v2()

    }

    private fun takePhoto_v2() {
        imagePath = getTempFilename(this)
       // Log.i(FotografiaActivity.TAG, "takePhoto: $imagePath")
        startActivityForTakePhotoResult.launch(
            FileProvider.getUriForFile( this,
                "pt.isec.a2020139576.amovTp.android.fileprovider", File(imagePath)
            ))

        var savedUri: Uri? = null

        savedUri = Uri.fromFile(File(imagePath))

        val imagem: ImageView = findViewById(R.id.Imagem_Perfil)
        imagem.setImageURI(Uri.parse(savedUri.toString()))

        data = Intent()
        data.data = Uri.fromFile(File(imagePath))

    }
    var startActivityForTakePhotoResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
       // Log.i(FotografiaActivity.TAG, "startActivityForTakePhotoResult: $success")
        if (!success)
            imagePath = null
        updatePreview()
        fotoTirada = true

    }

    fun updatePreview() {
        if (imagePath != null){
            setPic(b.ImagemPerfil, imagePath!!)

            var savedUri: Uri? = null

            savedUri = Uri.fromFile(File(imagePath))

            val imagem: ImageView = findViewById(R.id.Imagem_Perfil)
            imagem.setImageURI(Uri.parse(savedUri.toString()))

            data = Intent()
            data.data = Uri.fromFile(File(imagePath))



            imagemPerfilUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imagemPerfilUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            b.ImagemPerfil.setImageDrawable(bitmapDrawable)

            fotoTirada = true
            //finish()
        }

    }

    fun inserirImagemFireBaseStorage(email: String) {

        FirebaseAuth.getInstance().signInAnonymously()
        val ref = FirebaseStorage.getInstance().getReference("/Imagens/$email")
        //Log.i(FotografiaActivity.TAG, "inserirImagemFireBaseStorage: $imagemPerfilUri")
        ref.putFile(imagemPerfilUri!!).addOnSuccessListener {
            Log.d("PerfilActivity",
                "Colocada com sucesso na FireBaseStorage a imagem: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener { url ->
                Log.d("PerfilActivity", "Localizaçao da imagem: $url")

                val db = Firebase.firestore
                val jogador = db.collection("Jogadores").document(email)

                jogador.get(Source.SERVER)
                    .addOnSuccessListener {
                        jogador.update("ImagemPerfilUrl", url.toString())
                    }
            }
        }
            .addOnFailureListener {
                Log.d("PerfilActivity", "Erro a colocar a imagem na FireStoreDatabase!")
            }

    }

   fun setTheme(){
        var tema = Utils.setTema(this)
        when (tema){
            1-> {
                b.voltarAtrasID.setBackgroundResource(R.color.bottonSec_colorWarm)
                b.voltarAtrasImgID?.setBackgroundResource(R.color.bottonSec_colorWarm)
            }
            2->{
                b.voltarAtrasID?.setBackgroundResource(R.color.botton_sec_color)
                b.voltarAtrasImgID?.setBackgroundResource(R.color.botton_sec_color)
            }
            3->{
                b.voltarAtrasID?.setBackgroundResource(R.color.bottonSec_colorExotic)
                b.voltarAtrasImgID?.setBackgroundResource(R.color.bottonSec_colorExotic)
            }
            else->{
                b.voltarAtrasID?.setBackgroundResource(R.color.botton_sec_color)
                b.voltarAtrasImgID?.setBackgroundResource(R.color.botton_sec_color)
            }
        }
    }

}