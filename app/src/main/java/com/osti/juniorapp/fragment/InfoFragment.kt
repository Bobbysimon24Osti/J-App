package com.osti.juniorapp.fragment

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.gson.JsonArray
import com.osti.juniorapp.BuildConfig
import com.osti.juniorapp.R
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.network.NetworkController
import com.osti.juniorapp.network.NetworkNotifiche
import com.osti.juniorapp.notifiche.JuniorNotifiche
import com.osti.juniorapp.db.resolvers.JuniorNotificheResolver
import com.osti.juniorapp.utils.JuniorLicenza
import java.io.File


class InfoFragment : Fragment() {

    lateinit var layout: LinearLayout
    lateinit var textViewVersione: TextView
    lateinit var textViewLicenza: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_info, container, false)
        init(v)
        return v
    }

    private fun init(v:View){
        activity?.runOnUiThread{
            textViewVersione = v.findViewById(R.id.textView_versioneApp)
            textViewLicenza = v.findViewById(R.id.textview_licenza)
            layout = v.findViewById(R.id.linearLayout_layout)

            textViewLicenza.setOnClickListener{
                showLicense()
            }

            val versioneString = textViewVersione.text.toString() + BuildConfig.VERSION_NAME
            textViewVersione.text = versioneString

            //testnotifiche()
        }
    }

    private fun testnotifiche(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notClass = JuniorNotifiche(requireContext())
            val net = NetworkNotifiche(NetworkController.apiCliente)
            val b = Button(requireContext())
            b.id = View.generateViewId()
            b.text = "NOTIFICHE"
            b.setOnClickListener{
                net.getnotifiche{
                    if(it.oldValue is JsonArray){
                        val list = it.oldValue as JsonArray
                        for (item in list){
                            notClass.showNotifica()
                        }
                    }
                }
            }
            layout.addView(b)
        }
    }

    fun createLicense(dataRegistrazione:String?, pIva:String?, ragSoc:String?, codiceApp:String? ){
        val directory =
            requireActivity().filesDir
        val file = File(directory, "licenza.html")
        if(!file.exists()){
            file.writeBytes(calculateLicense2(
                dataRegistrazione ?: "null",  pIva ?: "null", ragSoc ?: "null", codiceApp ?: "null").toByteArray())
        }
    }

    fun showLicense(){
        val directory =
            requireActivity().filesDir
        val file  = File(directory, "licenza.html")
        if(file.exists()){
            file.delete()
        }
        JuniorApplication.myDatabaseController.getLicenseInfo{
            if(it.newValue != null && it.newValue is JuniorLicenza){
                val licenza = it.newValue as JuniorLicenza
                createLicense(licenza.attivazione, licenza.pIva , licenza.ragSoc, licenza.codice)
                val uri = FileProvider.getUriForFile(requireActivity().baseContext, "com.osti.juniorapp.JFileProvider", file)
                JuniorApplication.setLastFragment(InfoFragment::class.simpleName, requireContext())
                val myIntent = Intent(Intent.ACTION_VIEW)
                myIntent.data = uri
                myIntent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
                requireActivity().startActivity(myIntent)
            }
        }
    }

    fun calculateLicense2(dataRegistrazione:String, pIva:String, ragSoc:String, codiceAttivazione:String ): String{
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "            <TITLE>pdf-html</TITLE>\n" +
                "            <META name=\"generator\" content=\"BCL easyConverter SDK 5.0.252\">\n" +
                "            <META name=\"author\" content=\"G. Osti Sistemi srl\">\n" +
                "            <META name=\"title\" content=\"CONDIZIONI DI LICENZA\">\n" +
                "            <META name=\"subject\" content=\"JuniorWEB &#169; \">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <p><h1>CONDIZIONI GENERALI LICENZA D'USO DI J-App\u00A9</h1></p>\n" +
                "        <p>Il software viene concesso in licenza d'uso da G. Osti Sistemi s.r.l., o per suo tramite da un suo Fornitore, al\n" +
                "            Cliente (di seguito “Licenziatario”) con diritto esclusivo all'utilizzo con esclusione di qualsiasi diritto di proprietà e/o\n" +
                "            titolarità sullo stesso che resta di proprietà esclusiva di G. Osti Sistemi s.r.l. con sede in Bologna via Lugo 10,\n" +
                "            PIVA: 02969631205 e/o dell'autore, i cui diritti sono protetti ai sensi della normativa sul diritto d'autore e sugli altri\n" +
                "            diritti di proprietà intellettuale.\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            Il Licenziatario è in particolare autorizzato a:\n" +
                "            <ol type= \"I\">\n" +
                "                <li>fruire delle opere a tempo indeterminato dalla data di attivazione della presente licenza;</li>\n" +
                "                <li>\n" +
                "                    permettere la fruizione delle opere soltanto per l’utilizzo di strumenti di rilevazione presenze e controllo accessi\n" +
                "                    a terzi nei limiti di quanto la normale fruizione permette (senza perciò dare accesso agli eventuali codici sorgenti, o\n" +
                "                    altro).\n" +
                "                </li>\n" +
                "            </ol>\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            Il Licenziatario non è autorizzato a:\n" +
                "            <ol type=\"a\">\n" +
                "                <li>utilizzare le opere per l'erogazione diretta di Servizi a pagamento a favore di terzi;</li>\n" +
                "                <li>modificare il Software in alcuna parte, nessuna esclusa;</li>\n" +
                "                <li>accedere agli eventuali codici sorgenti per qualsiasi scopo (ivi compreso quello di appropriarsene, copiarli,\n" +
                "                    riutilizzarli, ecc);</li>\n" +
                "                <li>d) trasmettere o fornire accesso agli eventuali codici sorgenti a terzi;</li>\n" +
                "                <li>e) assegnare in sub licenza, affittare, vendere, locare, distribuire o trasferire in altra maniera le opere o parte di\n" +
                "                    esse. In caso di trasferimento, affitto o cessione anche di ramo d’azienda, le limitazioni previste dalla presente\n" +
                "                    clausola e tutti i diritti connessi allo sfruttamento dei Servizi, rimarranno di titolarità esclusiva del contraente\n" +
                "                    originale che non potrà cedere, vendere, affittare, dare in comodato o altro a terzi, senza il preventivo consenso\n" +
                "                    scritto di G. Osti Sistemi S.r.l.</li>\n" +
                "            </ol>\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            Eventuali opere di terzi utilizzate nella fornitura dei Servizi mantengono la propria licenza.\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            E’ esclusa qualsivoglia responsabilità di G. Osti Sistemi S.r.l. per danni diretti e indiretti, anche a cose o persone,\n" +
                "            causati dall’uso del programma. La risarcibilità dei danni diretti, dovuti al malfunzionamento del Software o a\n" +
                "            seguito di anomala interruzione dell’attività e/o del programma, verranno risarciti da G. Osti Sistemi S.r.l.\n" +
                "            esclusivamente nei limiti del corrispettivo previsto dal costo della licenza.\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            E’ inoltre esclusa qualsiasi responsabilità o obbligo di G. Osti Sistemi S.r.l. per aggiornamento del Software in\n" +
                "            caso di cambi o aggiornamenti di hardware o software del Cliente, fatti salvi diversi accordi precedentemente\n" +
                "            intercorsi tra le Parti.\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            Al fine di prevenire la contraffazione del prodotto e tutelare i diritti del licenziante nonché per garantire il loro\n" +
                "            aggiornamento in tempo reale, i Software invieranno periodicamente ai server della G. Osti Sistemi s.r.l. i dati\n" +
                "            richiesti in sede di registrazione. In caso di utilizzo del software non conforme alle presenti condizioni la G. Osti\n" +
                "            Sistemi s.r.l. si riserva il diritto di limitare l’accesso all’applicativo fatto salvo il risarcimento del danno.\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>\n" +
                "            Il diritto di licenza avrà una durata a tempo indeterminato e terminerà automaticamente in ogni ipotesi di violazione\n" +
                "            di qualsiasi clausola del presente contratto.\n" +
                "        </p>\n" +
                "        <br>\n" +
                "        <p>DATI REGISTRAZIONE LICENZA</p>" +
                "        <br>\n" +
                "        <br>\n" +
                "        <p>Codice attivazione J-App\u00A9: <b>$codiceAttivazione</b></p>\n" +
                "        <p>attivata il <b>$dataRegistrazione</b></p>\n" +
                "        <p><b>$ragSoc</b></p>" +
                "        <p>P.iva: <b>$pIva</b></p>"+
                "    </body>\n" +
                "</html>"
    }
}
