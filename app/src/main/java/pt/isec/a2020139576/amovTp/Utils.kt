package pt.isec.a2020139576.amovTp

import android.content.Context
import android.content.SharedPreferences
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.a2020139576.amovTp.atividades.OpcoesActivity
import java.util.*



class Utils {


    companion object {
        lateinit var sharedPreferences: SharedPreferences
        private lateinit var theme: String
        var tema : Int = 2

        fun setFullscreen(activity: AppCompatActivity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
            activity.supportActionBar?.hide() // hide the title bar
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN) //enable full screen

        }


        fun mostrarImagemPerfil(activity: AppCompatActivity, imageView: Int) {

            val db = Firebase.firestore


            if (FirebaseAuth.getInstance().currentUser != null) {

                db.collection("Jogadores").document(FirebaseAuth.getInstance().currentUser?.email!!)
                    .addSnapshotListener { docSS, e ->

                        if (e != null) {
                            return@addSnapshotListener
                        }
                        if (docSS != null && docSS.exists()) {

                            val imagem = docSS.getString("ImagemPerfilUrl")

                            if (imagem == "")
                                return@addSnapshotListener


                            val imagemPerfil = activity.findViewById<ImageView>(imageView)
                            //Glide.with(activity).load(imagem).into(imagemPerfil)


                            Glide.with(activity.applicationContext).load(imagem).into(imagemPerfil)
                        }
                    }
            }
        }


        fun resetImagemPerfil(activity: AppCompatActivity, imageView: Int) {
            if (FirebaseAuth.getInstance().currentUser == null) {
                val imagemPerfil = activity.findViewById<ImageView>(imageView)
                imagemPerfil.setImageResource(R.drawable.avatar)
            }
        }

        fun setAppLocale(context: Context, language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        fun changeLanguage(activity: AppCompatActivity) {
            sharedPreferences = getDefaultSharedPreferences(activity)
            val check = sharedPreferences.getString("language", "").toString()
            when {
                check == "English" || check == "Inglês" || check == "Inglés"-> {
                    setAppLocale(activity, "en")
                }
                check == "Portuguese" || check == "Português" || check == "Portugués"-> {
                    setAppLocale(activity, "pt")
                }
                check == "Spanish" || check == "Espanhol" || check == "Español" -> {
                    setAppLocale(activity, "es")
                }
            }

        }

        fun setTema(activity: AppCompatActivity): Int {
            sharedPreferences = getDefaultSharedPreferences(activity)
            theme = sharedPreferences.getString("temas", "").toString()


            when {
                theme.equals("Quente") -> {
                    activity.setTheme(R.style.temaWarm);tema = 1;return 1
                }
                theme.equals("Frio") -> {
                    activity.setTheme(R.style.temaCold);tema=2;return 2
                }
                theme.equals("Exótico") -> {
                    activity.setTheme(R.style.temaExotic);tema=3;return 3
                }

                theme.equals("Warm") -> {
                    activity.setTheme(R.style.temaWarm);tema = 1;return 1
                }
                theme.equals("Cold") -> {
                    activity.setTheme(R.style.temaCold);tema=2;return 2
                }
                theme.equals("Exotic") -> {
                    activity.setTheme(R.style.temaExotic);tema=3;return 3
                }

                theme.equals("Caliente") -> {
                    activity.setTheme(R.style.temaWarm);tema = 1;return 1
                }
                theme.equals("Frío") -> {
                    activity.setTheme(R.style.temaCold);tema=2;return 2
                }
                theme.equals("Exótico") -> {
                    activity.setTheme(R.style.temaExotic);tema=3;return 3
                }
            }
            activity.setTheme(R.style.temaCold)
            tema=2
            return 2
        }


    }
}