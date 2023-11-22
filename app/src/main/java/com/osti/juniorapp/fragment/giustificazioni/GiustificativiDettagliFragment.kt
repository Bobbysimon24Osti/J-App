package com.osti.juniorapp.fragment.giustificazioni

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.MainActivity
import com.osti.juniorapp.application.ActivationController
import com.osti.juniorapp.application.JuniorApplication
import com.osti.juniorapp.application.JuniorUser
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.GiustificheTable
import com.osti.juniorapp.utils.GiustificheConverter
import com.osti.juniorapp.utils.Utils.FORMATDATE
import com.osti.juniorapp.utils.Utils.FORMATDATEDB
import com.osti.juniorapp.utils.Utils.FORMATDATEHOURS
import com.osti.juniorapp.utils.Utils.FORMATTIME
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class GiustificativiDettagliFragment : Fragment(){

    companion object{
        var selectedGiust : GiustificheTable? = null
    }

    lateinit var spinnerText: Spinner
    lateinit var textViewStartDate: TextView
    lateinit var textViewEndDate: TextView
    lateinit var editTextStartTime: TextView
    lateinit var editTextEndtTime: TextView
    lateinit var editTextValore: TextView
    lateinit var editTextNote: TextInputEditText
    lateinit var checkBoxGiorniNonLavorativi: CheckBox
    lateinit var buttonInviaGiust: Button
    lateinit var containerAl: CardView
    lateinit var containerOrari: CardView
    lateinit var textViewDal: TextView
    lateinit var textViewQuantita: TextView

    lateinit var dalPiu: ImageView
    lateinit var dalMeno: ImageView
    lateinit var alPiu: ImageView
    lateinit var alMeno: ImageView
    lateinit var valorePiu: ImageView
    lateinit var valoreMeno: ImageView
    lateinit var inizioPiu: ImageView
    lateinit var inizioMeno: ImageView
    lateinit var finePiu: ImageView
    lateinit var fineMeno: ImageView

    var lastClick: View? = null

    lateinit var datePickerDialog: DatePickerDialog
    lateinit var timePickerDialog: TimePickerDialog

    var giustifiche: MutableList<GiustificheTable>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_richiesta_giustificativo, container, false)
        // Inflate the layout for this fragment
        init(v)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initSpinner(context)
    }

    private fun init(v :View){
        activity?.runOnUiThread{
            spinnerText = v.findViewById(R.id.spinner_giustifiche)
            textViewStartDate = v.findViewById(R.id.textViewDateStart)
            textViewEndDate = v.findViewById(R.id.textViewDateEnd)
            editTextStartTime = v.findViewById(R.id.editTextTimeStart)
            editTextEndtTime = v.findViewById(R.id.editTextTimeEnd)
            editTextValore = v.findViewById(R.id.editText_valore)
            editTextNote = v.findViewById(R.id.editText_Note)
            checkBoxGiorniNonLavorativi = v.findViewById(R.id.checkBox_giorniNonLavorativi)
            buttonInviaGiust = v.findViewById(R.id.button_invia_giust)
            dalPiu = v.findViewById(R.id.imageView_dalPiu)
            dalMeno = v.findViewById(R.id.imageView_dalMeno)
            alPiu = v.findViewById(R.id.imageView_alPiu)
            alMeno = v.findViewById(R.id.imageView_alMeno)
            valorePiu = v.findViewById(R.id.imageView_valorePiu)
            valoreMeno = v.findViewById(R.id.imageView_valoreMeno)
            inizioPiu = v.findViewById(R.id.imageView_inizioPiu)
            inizioMeno = v.findViewById(R.id.imageView_inizioMeno)
            finePiu = v.findViewById(R.id.imageView_finePiu)
            fineMeno = v.findViewById(R.id.imageView_fineMeno)
            textViewDal = v.findViewById(R.id.textView_dal)
            textViewQuantita = v.findViewById(R.id.textView_quantita)
            containerAl = v.findViewById(R.id.cardView_al)
            containerOrari = v.findViewById(R.id.cardView_orari)
            dalPiu.setOnClickListener(this::onClickImages)
            dalMeno.setOnClickListener(this::onClickImages)
            alPiu.setOnClickListener(this::onClickImages)
            alMeno.setOnClickListener(this::onClickImages)
            valorePiu.setOnClickListener(this::onClickImages)
            valoreMeno.setOnClickListener(this::onClickImages)
            inizioPiu.setOnClickListener(this::onClickImages)
            inizioMeno.setOnClickListener(this::onClickImages)
            finePiu.setOnClickListener(this::onClickImages)
            fineMeno.setOnClickListener(this::onClickImages)
            textViewStartDate.setOnClickListener(this::onClick)
            textViewEndDate.setOnClickListener(this::onClick)
            editTextStartTime.setOnClickListener(this::onClick)
            editTextEndtTime.setOnClickListener(this::onClick)
            editTextValore.setOnClickListener(this::onClick)
            buttonInviaGiust.setOnClickListener(this::send)
            editTextStartTime.tag = "time"
            editTextEndtTime.tag = "time"

            val year = Calendar.getInstance().get(Calendar.YEAR)
            val month = Calendar.getInstance().get(Calendar.MONTH)
            val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            datePickerDialog = DatePickerDialog(requireContext(), this::onDatePicked, year, month, day)
            textViewStartDate.text = FORMATDATE.format(Calendar.getInstance().timeInMillis)
            textViewEndDate.text = textViewStartDate.text
        }
    }

    private fun initSpinner(context:Context){
        CoroutineScope(Dispatchers.IO).launch {
            JuniorApplication.myDatabaseController.getGiustificheFlowNoCausaleVuota().collect{
                if(it.isNotEmpty()){
                    val nomiGiustifiche = GiustificheConverter.getAllNamesAndAbbrNoCausaleVuota(it)
                    showSpinner(nomiGiustifiche, context)
                }
            }
        }
    }

    private fun showSpinner(nomiGiustifiche:List<String>, context: Context){
        activity?.runOnUiThread{
            spinnerText.adapter = ArrayAdapter(context, R.layout.spinner_text_layout, nomiGiustifiche)
            spinnerText.onItemSelectedListener = MySelectionListener(editTextValore, this)
            spinnerText.setPopupBackgroundDrawable(ResourcesCompat.getDrawable(context.resources, R.color.bluosti_light, null))
        }
    }

    val onTimePicker = TimePickerDialog.OnTimeSetListener{ picker, hours, minutes ->

        var myHours = hours.toString()
        var myMinutes = minutes.toString()

        if(myHours.length == 1){
            myHours = "0$myHours"
        }
        if(myMinutes.length == 1){
            myMinutes = "0${myMinutes}"
        }

        val str = "$myHours:$myMinutes"
        if(lastClick is TextView){
            (lastClick as TextView).text = str
        }
    }

    fun showContainerOrari (show:Boolean){
        if(show){
            containerOrari.visibility = View.VISIBLE
        }
        else{
            containerOrari.visibility = View.GONE
        }
    }

    fun setMancataTimbrText(mancataTimbr:Boolean){
        if(mancataTimbr){
            textViewDal.text = "Giorno"
            textViewQuantita.text = "Orario"
        }
        else{
            textViewDal.text = "Dal"
            textViewQuantita.text = "Quantità"
        }
    }

    fun showContainerAl (show:Boolean){
        if(show){
            containerAl.visibility = View.VISIBLE
        }
        else{
            containerAl.visibility = View.GONE
        }
    }

    class MySelectionListener(val view:TextView, val fragment: GiustificativiDettagliFragment) : OnItemSelectedListener{
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val nomi = GiustificheConverter.getAllGiustNoCausaleVuota()
            if(p1 is TextView && !nomi.isNullOrEmpty()){

                val type = GiustificheConverter.getTypeById(nomi[p2].id)
                selectedGiust = GiustificheConverter.getGiustificaTableById(nomi[p2].id)
                val oreValore = GiustificheConverter.getOreValoreById(nomi[p2].id)
                if (oreValore == "ore"){
                    setTimeType()
                    view.tag = "time"
                }
                else if (type == "assenza"){
                    setSignedType()
                    view.tag = "ass"
                }
                else if (type == "neutro"){
                    setDecimalType()
                    view.tag = "neutro"
                }

                if (selectedGiust != null && selectedGiust!!.id == 5L){
                    fragment.showContainerAl(false)
                    fragment.showContainerOrari(false)
                    fragment.setMancataTimbrText(true)
                }
                else{
                    fragment.showContainerAl(true)
                    fragment.showContainerOrari(true)
                    fragment.setMancataTimbrText(false)
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            //NIENTE
        }

        private fun setSignedType(){
            view.text = ""
            view.removeTextChangedListener(MyTextWatcher())
            view.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            view.hint = "0.0"
            view.invalidate()
        }

        private fun setDecimalType(){
            view.text = ""
            view.removeTextChangedListener(MyTextWatcher())
            view.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            view.hint = "0"
            view.setOnClickListener(null)
            view.invalidate()
        }

        private fun setTimeType(){
            view.text = "00:00"
            view.addTextChangedListener(MyTextWatcher())
            view.inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
            view.setOnClickListener(fragment::onClick)
            view.invalidate()
        }
    }

    /**
     * RITORNA SE TRUE SE LA PRIMA DATA E' PRECEDENTE O UGUALE ALLA SECONDA
     */
    fun compareDates(start: Date?, end:Date?) : Boolean? {
        if(start== null && end== null){
            return null
        }
        if (start!!.time <= end!!.time){
            return true
        }
        return false
    }

    private fun changeDate(view:TextView, operation:Char){
        if(operation == '+'){
            val tmpDataInizio = FORMATDATE.parse(view.text.toString())
            val c = Calendar.getInstance()
            c.time = tmpDataInizio ?: Calendar.getInstance().time
            c.add(Calendar.DAY_OF_YEAR, 1)
            view.text = FORMATDATE.format(c.timeInMillis)
            if(view.id == textViewStartDate.id){
                val cEnd = Calendar.getInstance()
                cEnd.time = FORMATDATE.parse(textViewEndDate.text.toString()) ?: Calendar.getInstance().time
                if(compareDates(c.time, cEnd.time) == false){
                    textViewEndDate.text = textViewStartDate.text
                }
            }
        }
        if(operation == '-'){
            val tmpDataInizio = FORMATDATE.parse(view.text.toString())
            val c = Calendar.getInstance()
            c.time = tmpDataInizio ?: Calendar.getInstance().time
            c.add(Calendar.DAY_OF_YEAR, -1)
            view.text = FORMATDATE.format(c.timeInMillis)
            if(view.id == textViewEndDate.id){
                val cStart = Calendar.getInstance()
                cStart.time = FORMATDATE.parse(textViewStartDate.text.toString()) ?: Calendar.getInstance().time
                if(compareDates(c.time, cStart.time) == true){
                    textViewStartDate.text = textViewEndDate.text
                }
            }
        }
    }

    private fun changeValore(view:TextView, operation:Char){
        try{
            view.setOnClickListener(null)
            if(view.tag == "time"){
                view.setOnClickListener(this::onClick)
                if(view.text.isBlank()){
                    view.setText("00:00")
                }
                if(view.text.length ==1){
                    view.setText("0${view.text}:00")
                }
                if(view.text.length ==2){
                    view.setText("${view.text}:00")
                }
                if(view.text.length ==3){
                    view.setText("${view.text}00")
                }
                if(view.text.length ==4){
                    view.setText("${view.text}0")
                }
                val tmpOraInizio = FORMATTIME.parse(view.text.toString())
                val c = Calendar.getInstance()
                c.time = tmpOraInizio ?: Calendar.getInstance().time
                if(operation == '+'){
                    c.add(Calendar.HOUR, 1)
                    view.setText(FORMATTIME.format(c.timeInMillis))
                }
                if(operation == '-'){
                    c.add(Calendar.HOUR, -1)
                    view.setText(FORMATTIME.format(c.timeInMillis))
                }
            }
            if(view.tag == "neutro"){
                if(view.text.isBlank()){
                    view.setText("0")
                }
                val tmpVal = view.text.toString().toIntOrNull()
                if(tmpVal != null){
                    if(operation == '+'){
                        val res = tmpVal + 1
                        view.setText(res.toString())
                    }
                    if(operation == '-' && tmpVal > 0){
                        val res = tmpVal - 1
                        view.setText(res.toString())
                    }
                }
            }

            if(view.tag == "assenza"){
                if(view.text.isBlank()){
                    view.setText("0.0")
                }
                val tmpVal = view.text.toString().toFloatOrNull()
                if(tmpVal != null){
                    if(operation == '+' && tmpVal < 1){
                        val res = tmpVal?.plus(0.5)
                        view.setText(res.toString())
                    }
                    if(operation == '-' && tmpVal > 0F){
                        val res = tmpVal - 0.5
                        view.setText(res.toString())
                    }
                }
            }
        }
        catch (e:java.lang.Exception){
            val i = e
        }
        view.invalidate()
    }

    private fun onClickImages(v:View){
        when(v.id){
            dalPiu.id ->{
                changeDate(textViewStartDate, '+')
            }
            dalMeno.id ->{
                changeDate(textViewStartDate, '-')
            }
            alPiu.id ->{
                changeDate(textViewEndDate, '+')
            }
            alMeno.id ->{
                changeDate(textViewEndDate, '-')
            }
            valorePiu.id ->{
                changeValore(editTextValore, '+')
            }
            valoreMeno.id ->{
                changeValore(editTextValore, '-')
            }
            inizioPiu.id ->{
                changeValore(editTextStartTime, '+')
            }
            inizioMeno.id ->{
                changeValore(editTextStartTime, '-')
            }
            finePiu.id ->{
                changeValore(editTextEndtTime, '+')
            }
            fineMeno.id ->{
                changeValore(editTextEndtTime, '-')
            }
        }
    }

    class MyTextWatcher : TextWatcher{

        var pos = -1
        var str:CharSequence? = ""

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            pos = p1
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0 != null && str!= null) {
                if(str!!.length < p0.length && pos == 2 && p0.length<4 && p0[p0.length-1] != ':'){
                    p0.replace(2, 2, ":")
                }
            }
            str = p0.toString()
        }
    }


    private fun showSuccessToast(){
        activity?.runOnUiThread{
            Toasty.success(requireContext(), R.string.toast_successo_richiesta_giustificativo).show()
        }
    }

    private fun onClick(v:View){
        lastClick = v
        if(v is TextView && (v.id == textViewStartDate.id || v.id == textViewEndDate.id)){
            val tmp1 = FORMATDATE.parse(v.text.toString())
            val cal = Calendar.getInstance()
            if(tmp1 != null){
                cal.time = tmp1
            }
            datePickerDialog = DatePickerDialog(requireContext(),this::onDatePicked, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            if(v.id == textViewEndDate.id){
                datePickerDialog.datePicker.minDate = tmp1?.time ?: 0
            }
            datePickerDialog.show()
        }
        else{
            var hours = 12
            var minutes = 0
            if(v is TextView && v.text.isNotEmpty()){
                val tmp  = v.text.split(":")
                hours = tmp[0].toInt()
                minutes = tmp[1].toInt()
            }
            timePickerDialog = TimePickerDialog(requireContext(), onTimePicker, hours, minutes, true)
            timePickerDialog.show()
        }
    }

    private fun send(button:View){
        if(checkAllFields()){
            var oraInizio = 0
            if(editTextStartTime.text.isNotEmpty() && editTextStartTime.text.contains(':')){
                val oraTmp = FORMATTIME.parse(editTextStartTime.text.toString())
                if (oraTmp != null) {
                    oraInizio = GiustificheConverter.getMinFromOra(oraTmp)
                }
            }
            else if(selectedGiust?.id == 5L){
                oraInizio = GiustificheConverter.getMinFromOra(FORMATTIME.parse(editTextValore.text.toString()) ?: Date(0))
            }
            else{
                oraInizio = 0
            }
            var oraFine = 0
            if(editTextEndtTime.text.isNotEmpty() && editTextStartTime.text.contains(':')){
                val oraTmp = FORMATTIME.parse(editTextEndtTime.text.toString())
                if (oraTmp != null) {
                    oraFine = GiustificheConverter.getMinFromOra(oraTmp)
                }
            }
            else if(selectedGiust?.id == 5L){
                oraFine = GiustificheConverter.getMinFromOra(FORMATTIME.parse(editTextValore.text.toString()) ?: Date(0))
            }
            else{
                oraFine = 0
            }

            val tmpDataInizio = FORMATDATE.parse(textViewStartDate.text.toString())
            val dataInizio= FORMATDATEDB.format(tmpDataInizio ?: FORMATDATE.format(Calendar.getInstance().timeInMillis))

            val tmpDataFine = FORMATDATE.parse(textViewEndDate.text.toString())

            var dataFine= FORMATDATEDB.format(tmpDataFine ?: FORMATDATE.format(Calendar.getInstance().timeInMillis))
            if(selectedGiust?.id == 5L){
                dataFine = dataInizio
            }



            val tmp = GiustificheConverter.getAllGiustNoCausaleVuota()
            if(tmp != null){
                val tmpGiust = tmp.get(spinnerText.selectedItemId.toInt())

                var valore = 0.0
                if(GiustificheConverter.getOreValoreById(tmpGiust?.id) == "ore"){
                    val tmp =  FORMATTIME.parse(editTextValore.text.toString())
                    if (tmp != null) {
                        valore = (((tmp.time + 3600000) / 1000)/60).toDouble()
                    }
                }
                else{
                    valore = editTextValore.text.toString().toDouble()
                }

                try{
                    val time = FORMATDATEHOURS.format(Calendar.getInstance().timeInMillis)
                    JuniorApplication.myDatabaseController.creaGiustificheRecord(
                        GiustificheRecord(
                            null,
                            tmpGiust.id,
                            JuniorUser.JuniorDipendente.serverId,
                            dataInizio,
                            dataFine,
                            valore,
                            "offline",
                            editTextNote.text.toString(),
                            null,
                            JuniorUser.JuniorDipendente.nome,
                            JuniorUser.JuniorDipendente.badge,
                            GiustificheConverter.getCompleteName(tmpGiust) ?:"sconosciuto",
                            GiustificheConverter.getType(tmpGiust) ?: "sconosciuto",
                            GiustificheConverter.getOrevalore(tmpGiust) ?: "ore",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            oraInizio,
                            oraFine,
                            time,
                            null,
                            checkBoxGiorniNonLavorativi.isChecked,
                            tmpGiust.abbreviativo))
                }
                catch (e: Exception){
                    //Niente, serve solo per non rischiare un loop
                    return
                }
                showSuccessToast()
                closeFragment()
            }
        }

    }

    private fun closeFragment(){
        activity?.runOnUiThread{
            if(activity is MainActivity){
                (activity as MainActivity).showOldGiustificheFragment()
            }
        }
        /*requireActivity().supportFragmentManager.beginTransaction().apply{
            replace(R.id.fragmentContainerView, TimbrVirtualeFragment())
            commit()
        }*/
    }

    private fun checkAllFields(): Boolean{
        if(selectedGiust != null && selectedGiust!!.abbreviativo == "MT"){
            if (
                editTextValore.text.isBlank() ||
                !isDate(textViewStartDate.text.toString())){
                showMissingCampiAlert()
                return false
            }
            else{
                return true
            }
        }
        if(
            editTextValore.text.isBlank() ||
                    !isDate(textViewStartDate.text.toString()) ||
                    !isDate(textViewEndDate.text.toString()) ||
                    (ActivationController.workFlowValoriObbligatori == "1" && (editTextStartTime.text.isBlank() || editTextEndtTime.text.isBlank())) ||
                    ((selectedGiust != null &&  GiustificheConverter.isNoteObb(selectedGiust!!)) && editTextNote.text?.isBlank() != false) ||
                    JuniorUser.userLogged ||
                    JuniorUser.JuniorDipendente.serverId == -1L
        ){
            showMissingCampiAlert()
            return false
        }
        else if(FORMATDATE.parse(textViewStartDate.text.toString())?.after(FORMATDATE.parse(textViewEndDate.text.toString())) == true){
            AlertDialog.Builder(requireContext())
                .setTitle("Attenzione")
                .setMessage("la data di fine non può essere precedente alla data di inizio")
                .setPositiveButton("Ok", null)
                .show()
            return false
        }
        return true
    }

    private fun showMissingCampiAlert(){
        AlertDialog.Builder(requireContext())
            .setTitle("Attenzione")
            .setMessage("Inserire tutti i campi richiesti")
            .setPositiveButton("Ok", null)
            .show()
    }


    private fun isDate(str:String):Boolean{
        try{
            FORMATDATE.parse(str.trim())
        }
        catch (e:Exception){
            return false
        }
        return true
    }

    fun isValore(valore:String): Boolean{
        try{
            val tmp = valore.toDouble()
        }
        catch (e:Exception){
            return false
        }
        return true
    }

    private fun onDatePicked(view:View, year:Int, monthOfYear:Int, dayOfMonth:Int){
        if(lastClick!= null){
            val cStart = Calendar.getInstance()
            cStart.time = FORMATDATE.parse(textViewStartDate.text.toString()) ?: Calendar.getInstance().time
            val cEnd = Calendar.getInstance()
            cStart.time = FORMATDATE.parse(textViewEndDate.text.toString()) ?: Calendar.getInstance().time

            val month:String = if(monthOfYear<9) {
                "0${monthOfYear+1}"
            }
            else{
                (monthOfYear+1).toString()
            }
            val day:String = if(dayOfMonth<10) {
                "0${dayOfMonth}"
            }
            else{
                (dayOfMonth).toString()
            }
            val str = "$day/${month}/$year"
            when(lastClick!!.id){

                textViewEndDate.id -> {
                    textViewEndDate.text = str
                    cEnd.time = FORMATDATE.parse(str) ?: Calendar.getInstance().time
                    if(compareDates(cStart.time, cEnd.time) == false){
                        textViewStartDate.text = str
                    }
                }

                textViewStartDate.id -> {
                    textViewStartDate.text = str
                    cStart.time = FORMATDATE.parse(str) ?: Calendar.getInstance().time
                    if(compareDates(cStart.time, cEnd.time) == false){
                        textViewEndDate.text = str
                    }
                }
            }
        }
    }
}

