package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Translate
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus

class WpDataTabsDialogViewModel : ViewModel() {

    private var registrationFlowStarted = false

    fun startRegistrationIfNeeded(
        activity: Activity,
        isEmptyScenario: Boolean,
        user: UsersSDB?
    ) {
        if (!isEmptyScenario || registrationFlowStarted)
            return
        registrationFlowStarted = true

        viewModelScope.launch {
            runRegistrationFlow(activity, user)
        }
    }

    private suspend fun runRegistrationFlow(
        activity: Activity,
        user: UsersSDB?
    ) {
        // Отдел кадров
        val hrUser = RoomManager.SQL_DB.usersDao()
            .getUserById(Globals.OTDEL_KADROV_USER_ID)
        val hrPhone = hrUser?.tel ?: ""
        val instruktor = RoomManager.SQL_DB.usersDao().getUserById(user?.instructorId ?: 0)
        val instruktorNumber = instruktor?.tel ?: 0

        val welcomeDialog = MessageDialogBuilder(activity)
        val exitDialog = MessageDialogBuilder(activity)

        val mainPart = Translate.translationText(
            9214,
            "Вас приветствует приложение merchik! Если Вы работаете мерчандайзером, торговым представителем, продавцом-консультантом в магазинеи т.д. то, при помощи данного приложения, Вы сможете получить дополнительные доходы выполняя заказы наших клиентов. Нажмите %s для того, чтобы зарегистрироваться и получить %s."
        )
        val partOne = "тут"
        val partTwo = Translate.translationText(9217, "доступ к заказам")

//        val spannableMessage = buildClickablePhoneText(
//            mainPart = mainPart,
//            partOne = partOne,
//            partTwo = partTwo,
//            phone = hrPhone
//        )

        val htmlMessage = String.format(
            mainPart,
            "<a href=\"tel:$hrPhone\">$partOne</a>",
            "<a href=\"tel:$hrPhone\">$partTwo</a>"
        )

        val isRegistration = true

        while (true) {
            // ---------- 1. Приветственный диалог ----------
            if (isRegistration)
                welcomeDialog
                    .setStatus(DialogStatus.NORMAL)
                    .setTitle("Регистрация")
                    .setMessage(
                        "Приветствую! Вы зарегистрированы в системе Merchik. " +
                                "Теперь Вам следует пройти инструктаж и получить доступ к заказам. "
                    )
                    .setOnConfirmAction("Пройти иструктаж") {
                        // "Да" – выходим из приложения
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$instruktorNumber")
                        }
                        activity.startActivity(intent)
                    }
                    .show()
            else
                welcomeDialog
                    .setStatus(DialogStatus.NORMAL)
                    .setTitle("Регистрация")
                    .setMessage(
                        htmlMessage
                    )
                    .setOnConfirmAction("Зарегистрироваться") {
                        if (hrPhone.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$hrPhone")
                            }
                            activity.startActivity(intent)
                        }
                    }
                    .show()

            // ждём любого закрытия первого диалога (кнопка, крестик, back)
            while (welcomeDialog.isShowing()) {
                delay(250)
            }

            // ---------- 2. Диалог "нужна регистрация / выйти?" ----------
            exitDialog
                .setTitle("Регистрация")
                .setStatus(DialogStatus.ERROR)
                .setCancelable(false) // не закрывать back/тапом мимо
                .setMessage(
                    if (isRegistration) "Для того, чтобы продолжить работу, Вы должны пройти инструктаж." else
                        "Для того, чтобы продолжить работу, Вы должны пройти этап регистрации."
                )
                .setOnConfirmAction("Вернуться") {
                    exitDialog.dismiss()
                }
                .setOnCancelAction("Закрыть приложение") {
                    activity.finishAffinity()
                }
                .show()

            // ждём закрытия второго диалога
            while (exitDialog.isShowing()) {
                delay(250)
            }

            // Если нажали "Да", activity, скорее всего, закроется и корутина умрёт сама.
            // Если "Отмена" — выходим из цикла на верх и снова покажем welcomeDialog.
        }
    }

    private val PHONE_TAG = "phone_tag"

    fun buildClickablePhoneText(
        mainPart: String,
        partOne: String,
        partTwo: String,
        phone: String,
        linkColor: Color = Color(0xFF1565C0) // синий
    ): AnnotatedString = buildAnnotatedString {
        val fullText = String.format(mainPart, partOne, partTwo)
        append(fullText)

        val style = SpanStyle(
            color = linkColor,
            textDecoration = TextDecoration.Underline
        )

        fun markPart(part: String) {
            val start = fullText.indexOf(part)
            if (start < 0) return
            val end = start + part.length

            addStyle(style, start, end)
            addStringAnnotation(
                tag = PHONE_TAG,
                annotation = phone,
                start = start,
                end = end
            )
        }

        markPart(partOne)
        markPart(partTwo)
    }

//    fun buildPhoneSpannable(
//        context: Context,
//        mainPart: String,
//        partOne: String,
//        partTwo: String,
//        phone: String
//    ): CharSequence {
//        val fullText = String.format(mainPart, partOne, partTwo)
//        val spannable = SpannableStringBuilder(fullText)
//
//        val linkColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
//
//        fun makeClickable(part: String) {
//            val start = fullText.indexOf(part)
//            if (start < 0) return
//            val end = start + part.length
//
//            val span = object : ClickableSpan() {
//                override fun onClick(widget: android.view.View) {
//                    val intent = Intent(Intent.ACTION_DIAL).apply {
//                        data = Uri.parse("tel:$phone")
//                    }
//                    context.startActivity(intent)
//                }
//
//                override fun updateDrawState(ds: TextPaint) {
//                    super.updateDrawState(ds)
//                    ds.isUnderlineText = true          // подчёркивание
//                    ds.color = linkColor               // синий цвет
//                }
//            }
//
//            spannable.setSpan(
//                span,
//                start,
//                end,
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
//
//        // делаем кликабельными обе части
//        makeClickable(partOne)
//        makeClickable(partTwo)
//
//        return spannable
//    }

}
