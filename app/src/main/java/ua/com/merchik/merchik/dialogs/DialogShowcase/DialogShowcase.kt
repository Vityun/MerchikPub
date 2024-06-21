package ua.com.merchik.merchik.dialogs.DialogShowcase

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ViewHolders.Clicks.click
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.GroupTypeRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogData
import ua.com.merchik.merchik.dialogs.DialogVideo

class DialogShowcase(private val context: Context?) : DialogData() {

    private var dialog: Dialog? = null
    var click: click? = null
    var close: ImageButton? = null
    var help: ImageButton? = null
    var videoHelp: ImageButton? = null
    var call: ImageButton? = null
    var merchikIco: ImageButton? = null
    private var title: TextView? = null
    private var cancel: Button? = null
    private var recyclerView: RecyclerView? = null
    private var searchView: EditText? = null

    @JvmField
    var wpDataDB: WpDataDB? = null

    @JvmField
    var photoType: Int? = null


    init {
        try {
            initializeDialog()
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "DialogShowcase", "Exception e: $e")
        }
    }

    override fun setClose(clickListener: DialogClickListener) {
        close!!.setOnClickListener { v: View? -> clickListener.clicked() }
    }

    override fun setLesson(context: Context, visualise: Boolean, objectId: Int) {
        if (visualise) help!!.visibility = View.VISIBLE
        val data = RealmManager.getLesson(objectId)
        help!!.setOnClickListener { v: View? ->
            if (data != null) {
                val dialogLesson = DialogData(context)
                dialogLesson.setTitle("Подсказка")
                dialogLesson.setText(data.comments)
                dialogLesson.show()
            } else {
                Toast.makeText(context, "Для этой странички урок ещё не создан.", Toast.LENGTH_LONG)
                    .show()
            }
        }
        help!!.setOnLongClickListener { v: View? ->
            if (data != null) {
                Toast.makeText(context, data.nm, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Для этой странички урок ещё не создан.", Toast.LENGTH_LONG)
                    .show()
            }
            true
        }
    }

    fun setVideoLesson(
        context: Context,
        visualise: Boolean,
        objectId: Int,
        clickListener: DialogClickListener?
    ) {
        Log.e("setVideoLesson", "click0 Oid: $objectId")
        try {
            if (visualise) {
                videoHelp!!.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    videoHelp!!.background.setTint(Color.RED)
                } else {
                    videoHelp!!.setBackgroundColor(Color.RED)
                }
                videoHelp!!.setColorFilter(Color.WHITE)
            }
            val `object` = RealmManager.getLesson(objectId)
            Log.e("setVideoLesson", "object: " + `object`.id)
            var data: SiteHintsDB? = null
            try {
                if (`object`.lessonId != null) {
                    data = RealmManager.getVideoLesson(`object`.lessonId.toInt())
                } else {
                    Log.e("setVideoLesson", "getLessonId=null")
                }
            } catch (e: Exception) {
                Log.e("setVideoLesson", "Exception e: $e")
                Globals.writeToMLOG("ERROR", "DialogEKL/EXCEPTION/2", "Exception e: $e")
            }
            val finalData = data
            videoHelp!!.setOnClickListener { v: View? ->
                Log.e("setVideoLesson", "click")
                if (finalData != null) {
                    Log.e("setVideoLesson", "click1")
                    if (clickListener == null) {
                        var s = finalData.url
                        Log.e("setVideoLesson", "click2.URL: $s")
                        s = s.replace("http://www.youtube.com/", "http://www.youtube.com/embed/")
                        Log.e("setVideoLesson", "click2.replace.URL: $s")
                        s = s.replace("watch?v=", "")
                        Log.e("setVideoLesson", "click2.replace.URL: $s")

                        // Отображаем видео
                        // Samsung A6 Galaxy
                        val video = DialogVideo(context)
                        //                    video.setMerchikIco();
                        video.setTitle("" + finalData.nm)
                        video.setClose {
                            Log.e("DialogVideo", "click X")
                            video.dismiss()
                        }
                        video.setVideoLesson(context, true, 0, {
                            Log.e("DialogVideo", "click Video")
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(finalData.url)
                                )
                            )
                        }, null)
                        video.setVideo("<html><body><iframe width=\"700\" height=\"600\" src=\"$s\"></iframe></body></html>")
                        video.show()
                    } else {
                        Log.e("setVideoLesson", "click3")
                        // Переходим по ссылке
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(finalData.url)
                            )
                        ) // Запускаем стартовый ролик - презентацию
                        clickListener.clicked()
                    }
                } else {
                    Log.e("setVideoLesson", "click4")
                    Toast.makeText(
                        context,
                        "Для этой странички Видеоурок ещё не создан.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            videoHelp!!.setOnLongClickListener { v: View? ->
                if (finalData != null) {
                    Toast.makeText(context, finalData.nm, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        context,
                        "Для этой странички Видеоурок ещё не создан.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                true
            }
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "DialogEKL/EXCEPTION/3", "Exception e: $e")
        }
    }

    override fun setImgBtnCall(context: Context) {
        Log.e("setImgBtnCall", "i`m here")
        call!!.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            call!!.background.setTint(context.resources.getColor(R.color.greenCol))
        } else {
            call!!.setBackgroundColor(Color.GREEN)
        }
        call!!.setColorFilter(Color.WHITE)
        call!!.setOnClickListener { v: View? ->
            Globals.telephoneCall(
                context,
                Globals.HELPDESK_PHONE_NUMBER
            )
        }
        Log.e("setImgBtnCall", "and here")
    }

    override fun dismiss() {
        if (dialog != null) dialog!!.dismiss()
    }

    override fun show() {
        if (dialog != null) dialog!!.show()
    }

    private fun initializeDialog() {
        dialog = Dialog(context)
        dialog!!.setCancelable(false)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.setContentView(R.layout.dialog_showcase)
        val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.70).toInt()
        dialog!!.window!!.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        close = dialog!!.findViewById(R.id.imageButtonClose)
        help = dialog!!.findViewById(R.id.imageButtonLesson)
        videoHelp = dialog!!.findViewById(R.id.imageButtonVideoLesson)
        call = dialog!!.findViewById(R.id.imageButtonCall)
        title = dialog!!.findViewById(R.id.title)
        cancel = dialog!!.findViewById(R.id.button)
        recyclerView = dialog!!.findViewById(R.id.recyclerView)
        searchView = dialog!!.findViewById(R.id.filterEditText)
        merchikIco = dialog!!.findViewById<ImageButton>(R.id.merchik_ico)
        merchikIco.setImageDrawable(context.resources.getDrawable(R.drawable.ic_caution))
    }

    fun setCurrTitle(title: String){
        this.title!!.text = title
    }

    fun populateDialogData(click: click?) {
        this.click = click
        setRecyclerView()
    }

    fun populateDialogDataPlanogramm(click: click?) {
        this.click = click
        setRecyclerViewPlanogramm()
    }

    fun result(click: click?) {
        this.click = click
    }

    private fun setRecyclerView() {
        try {
            val list: MutableList<Int> = ArrayList()
            if (photoType == 0 || photoType == 14){
                list.add(0)
                list.add(1)
                list.add(2)
            }else if (photoType == 45){
                list.add(3)
                list.add(5)
                list.add(8)
            }

            var showcaseDataListTest = RoomManager.SQL_DB.showcaseDao().getByDocTP(
                wpDataDB!!.client_id, wpDataDB!!.addr_id
            )

            Log.e("setRecyclerView", "showcaseDataListTest: $showcaseDataListTest")

            var showcaseDataList = RoomManager.SQL_DB.showcaseDao().getByDocTP(
                wpDataDB!!.client_id, wpDataDB!!.addr_id, list
            )

            Log.e("setRecyclerView", "showcaseDataList: $showcaseDataList")
            try {
                for (item in showcaseDataList) {
                    // Нахожу группу товара, если её нет
                    if (item.tovarGrp != null) {
                        val group = GroupTypeRealm.getGroupTypeById(item.tovarGrp)
                        if (group != null && group.nm != null) {
                            item.tovarGrpTxt = group.nm
                        }
                    }

                    val stackPhotoDBS =
                        StackPhotoRealm.getShowcase(item.id, wpDataDB!!.code_dad2, photoType)
                    if (stackPhotoDBS != null && stackPhotoDBS.size > 0) {
                        item.showcasePhoto =
                            stackPhotoDBS.size // Меня тут просили ставить просто 1 или 0, но я слишком умный, да
                    } else {
                        item.showcasePhoto = 0
                    }
                }

                // Сортируем по кол-ву фоток
                showcaseDataList = showcaseDataList
                    .sortedBy { it.showcasePhoto }
//                    .filter { it.id > 5 && it.id < 15 }
//                    .take()
//                    .map {  }
//                    .firstOrNull { it.id == 80988 }
//                    .find {  }
//                    .any { it.id == 80988 }
//                    .asSequence() // todo read

            } catch (e: Exception) {
                Globals.writeToMLOG("ERROR", "showcaseDataList", "Exception e: $e")
            }
            val showcaseDataListT = ArrayList(showcaseDataList)
            val adapter = ShowcaseAdapter(showcaseDataListT, click)
            setFilter(adapter)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } catch (e: Exception) {
            Log.e("setRecyclerView", "Exception e: $e")
            e.printStackTrace()
        }
    }

    private fun setRecyclerViewPlanogramm() {
        try {
            var adress = RoomManager.SQL_DB.addressDao().getById(wpDataDB!!.addr_id);
            var planogrammDataList = RoomManager.SQL_DB.planogrammDao().getByClientAddress(
                wpDataDB!!.client_id,
                wpDataDB!!.addr_id,
//                adress.ttId,
                null,
                Clock.getHumanTimeSecPattern(System.currentTimeMillis() / 1000, "yyyy-MM-dd")
            )

            Log.e("setRecyclerView", "planogrammDataList: $planogrammDataList")
            try {
                for (item in planogrammDataList) {
                    val stackPhotoDBS =
                        StackPhotoRealm.getPlanogramm(item.planogrammPhotoId, wpDataDB!!.code_dad2, photoType)
                    if (stackPhotoDBS != null && stackPhotoDBS.size > 0) {
                        item.planogrammPhoto = stackPhotoDBS.size // Меня тут просили ставить просто 1 или 0, но я слишком умный, да
                    } else {
                        item.planogrammPhoto = 0
                    }
                }

            } catch (e: Exception) {
                Globals.writeToMLOG("ERROR", "planogrammDataList", "Exception e: $e")
            }
            val planogrammDataListT = ArrayList(planogrammDataList)
            val adapter = PlanogramAdapter(planogrammDataListT, click)
            setFilter(adapter)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } catch (e: Exception) {
            Log.e("setRecyclerView", "Exception e: $e")
            e.printStackTrace()
        }
    }

    private fun newTestShowcase(id: Int): ShowcaseSDB {
        val res = ShowcaseSDB()
        res.id = id
        res.tovarGrp = id
        res.photoPlanogramId = id
        return res
    }

    private fun setFilter(adapter: ShowcaseAdapter) {
        searchView!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("ShowcaseAdapter", "onTextChanged s: $s")
                if (s.length != 0) {
                    Log.e("FilterShowcase", "onTextChanged HAVE")
                    adapter.filter.filter(s)
                    recyclerView!!.scheduleLayoutAnimation()
                } else {
                    Log.e("FilterShowcase", "onTextChanged ZERO")
                    adapter.filter.filter(s)
                    recyclerView!!.scheduleLayoutAnimation()
                }
            }
        })
    }

    private fun setFilter(adapter: PlanogramAdapter) {
        searchView!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("PlanogramAdapter", "onTextChanged s: $s")
                if (s.length != 0) {
                    Log.e("FilterPlanogram", "onTextChanged HAVE")
                    adapter.filter.filter(s)
                    recyclerView!!.scheduleLayoutAnimation()
                } else {
                    Log.e("FilterPlanogram", "onTextChanged ZERO")
                    adapter.filter.filter(s)
                    recyclerView!!.scheduleLayoutAnimation()
                }
            }
        })
    }
}