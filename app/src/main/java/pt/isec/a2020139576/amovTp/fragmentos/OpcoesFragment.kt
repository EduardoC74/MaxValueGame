package pt.isec.a2020139576.amovTp.fragmentos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.dialog_credits.view.*
import pt.isec.a2020139576.amovTp.R
import pt.isec.a2020139576.amovTp.Utils
import pt.isec.a2020139576.amovTp.atividades.OpcoesActivity

class OpcoesFragment : PreferenceFragmentCompat() {

    var tema: Int = 1

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        tema = Utils.setTema(activity as OpcoesActivity)

        setPreferencesFromResource(R.xml.opcoes, rootKey)
        val prefLanguage = findPreference("language") as Preference?
        val prefTemas = findPreference("temas") as Preference?
        var prefCreditos = findPreference("creditos") as Preference?
        prefLanguage!!.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
                (activity as OpcoesActivity).finish()
                val intent = Intent((activity as OpcoesActivity),OpcoesActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        prefTemas!!.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener{
            override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
                (activity as OpcoesActivity).finish()
                val intent = Intent((activity as OpcoesActivity),OpcoesActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        prefCreditos!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: Preference): Boolean {
                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder((activity as OpcoesActivity))
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_credits, null)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
                // dialogView.setPadding(50, 50, 50, 50)
                dialogView.layoutParams = params

                dialogBuilder.setView(dialogView)
                val alertDialog: AlertDialog = dialogBuilder.create()

                //  alertDialog.setTitle("About")
                setTheme(tema, dialogView)

                alertDialog.show()

                return true
            }
        }
    }

    private fun setTheme(tema: Int, dialogView: View) {
        when (tema) {
            1 -> {
                dialogView.ano_letivo.setTextColor(android.graphics.Color.parseColor("#80381a"))
                dialogView.trabalho_realizado.setTextColor(android.graphics.Color.parseColor("#FAAA94"))
                dialogView.bia.setTextColor(android.graphics.Color.parseColor("#FAAA94"))
                dialogView.edu.setTextColor(android.graphics.Color.parseColor("#FAAA94"))
                dialogView.xico.setTextColor(android.graphics.Color.parseColor("#FAAA94"))
            }
            2 -> {
                dialogView.ano_letivo.setTextColor(android.graphics.Color.parseColor("#333A56"))
                dialogView.trabalho_realizado.setTextColor(android.graphics.Color.parseColor("#88BBD6"))
                dialogView.bia.setTextColor(android.graphics.Color.parseColor("#88BBD6"))
                dialogView.edu.setTextColor(android.graphics.Color.parseColor("#88BBD6"))
                dialogView.xico.setTextColor(android.graphics.Color.parseColor("#88BBD6"))
            }
            3 -> {
                dialogView.ano_letivo.setTextColor(android.graphics.Color.parseColor("#6E7649"))
                dialogView.trabalho_realizado.setTextColor(android.graphics.Color.parseColor("#B1BCA0"))
                dialogView.bia.setTextColor(android.graphics.Color.parseColor("#B1BCA0"))
                dialogView.edu.setTextColor(android.graphics.Color.parseColor("#B1BCA0"))
                dialogView.xico.setTextColor(android.graphics.Color.parseColor("#B1BCA0"))
            }
        }
    }


}