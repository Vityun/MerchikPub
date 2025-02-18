package ua.com.merchik.merchik.Activities.DetailedReportActivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import io.realm.Realm
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ServerExchange.Exchange
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogFilter.Click
import ua.com.merchik.merchik.features.main.DBViewModels.OpinionSDBViewModel


@Composable
fun OpinionAndCommentView(
    wpDataDB: WpDataDB,
    viewModel: CommentViewModel = viewModel()
) {

    val context = LocalContext.current

    val opinionDataHolder = OpinionDataHolder.instance()
    var opinionID by remember { mutableStateOf(opinionDataHolder.opinionID) }
    var opinionName by remember { mutableStateOf(opinionDataHolder.opinionName) }
    var opinionNameFromWpData by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }


    var isEditing by remember { mutableStateOf(false) } // Состояние редактирования

    val comment by viewModel.comment.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val wpDataUserOpinionID = wpDataDB.user_opinion_id?.toInt() ?: 0
    val wpDataUserComment = wpDataDB.user_comment ?: ""

    if (wpDataUserOpinionID > 0) {
        val opinion = RoomManager.SQL_DB.opinionDao().getOpinionById(wpDataUserOpinionID)
        Log.e("OpinionAndCommentView", "opinion.nm: ${opinion.nm}")
        opinionNameFromWpData = opinion.nm
    }

    // Обработчик результата
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK &&
                opinionDataHolder.opinionID != null &&
                opinionDataHolder.opinionName != null
            ) {
                opinionID = opinionDataHolder.opinionID
                opinionName = opinionDataHolder.opinionName!!
                val startTime = System.currentTimeMillis() / 1000
                RealmManager.INSTANCE.executeTransaction { realm: Realm ->
                    wpDataDB.dt_update = startTime
                    wpDataDB.user_opinion_id = opinionID.toString()
                    wpDataDB.user_opinion_author_id = wpDataDB.user_id.toString()
                    wpDataDB.user_opinion_dt_update = startTime
                    wpDataDB.startUpdate = true
                    realm.insertOrUpdate(wpDataDB)
                }
            }
        }

//    LaunchedEffect(comment) {
//        if (comment.length > 10) {
//            Log.e("!!!!!!!", "+++++")
////            viewModel.comment = comment
//            viewModel.setSavedDialogShow(true)
//        } else {
//            viewModel.setSavedDialogShow(false)
//        }
//    }
    LaunchedEffect(isEditing) {
        if (isEditing) {
            delay(100) // Небольшая задержка для корректной работы
            focusRequester.requestFocus()
        }
    }

    val themeId = wpDataDB.theme_id
    // Обработка клика
    val onClick = {
        val intent = Intent(context, FeaturesActivity::class.java)
        val bundle = Bundle()
        bundle.putString("viewModel", OpinionSDBViewModel::class.java.canonicalName)
        bundle.putString("contextUI", ContextUI.ADD_OPINION_FROM_DETAILED_REPORT.toString())
        bundle.putString("modeUI", ModeUI.ONE_SELECT.toString())
        bundle.putString("dataJson", Gson().toJson(themeId.toInt()))
        bundle.putString("title", "Оставить мнение")
        bundle.putString(
            "subTitle",
            "Выберите мнение которое вы хотите оставить о данном посещении"
        )
        intent.putExtras(bundle)
        opinionDataHolder.init()

        launcher.launch(intent)

    }

//    Column {
//        HorizontalDivider(
//            color = Color.Gray, // Цвет черты
//            thickness = 1.dp, // Толщина черты
//            modifier = Modifier.padding(vertical = 6.dp) // Отступы вокруг черты
//        )


    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
    ) {

//            Text(
//                buildAnnotatedString {
//                    withStyle(
//                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
//                            .copy(fontWeight = FontWeight.Bold)
//                    ) {
//                        append("Мнение исп. о посещении: ")
//                    }
//                    withStyle(
//                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
//                            .copy(textDecoration = TextDecoration.Underline)
//                    ) {
//                        append(
//                            opinionName ?: opinionNameFromWpData
//                            ?: "Нажмите для выбора мнения о посещении"
//                        )
//                    }
//                },
//                color = Color(-1979711488),
//                modifier = Modifier.clickable(onClick = onClick)
//            )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                        .copy(fontWeight = FontWeight.Bold)
                ) {
                    append("Мнение исп. о посещении: ")
                }
                withStyle(
                    style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                        .copy(textDecoration = TextDecoration.Underline)
                ) {
                    append(
                        opinionName ?: opinionNameFromWpData
                        ?: "Нажмите для выбора мнения о посещении"
                    )
                }
            },
            color = Color(-1979711488),
            modifier = Modifier.clickable(onClick = onClick),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 0.8f // Уменьшаем интерлиньяж на 25%
            )
        )

        // Поле для ввода комментария
        if (isEditing) {
            OutlinedTextField(
                value = comment.ifBlank { wpDataUserComment },
                onValueChange = { viewModel.updateComment(it) },
                placeholder = { Text("Оставить комментарий о посещении") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged {
//                            if (!it.isFocused) isEditing = false // Закрываем при потере фокуса
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.blue),
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.LightGray,
                    disabledBorderColor = Color.Gray,
                    focusedTextColor = Color.DarkGray
                ),
                singleLine = false,
                maxLines = 99
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                            .copy(fontWeight = FontWeight.Bold)
                    ) {
                        append("Комментарий исп. о посещении: ")
                    }
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                            .copy(textDecoration = TextDecoration.Underline)
                    ) {
                        append(comment.ifBlank {
                            wpDataUserComment.ifBlank {
                                "Нажмите для оставления комментария"
                            }
                        })
                    }
                },
                color = Color(-1979711488),
                modifier = Modifier
                    .clickable {
                    isEditing = true
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 0.8f // Уменьшаем интерлиньяж на 25%
                )
            )
//            Text(
//                buildAnnotatedString {
//                    withStyle(
//                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
//                            .copy(fontWeight = FontWeight.Bold)
//                    ) {
//                        append("Комментарий исполнителя о посещении: ")
//                    }
//                    withStyle(
//                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
//                            .copy(textDecoration = TextDecoration.Underline)
//                    ) {
//                        append(comment.ifBlank { wpDataUserComment.ifBlank { "Нажмите для оставления комментария" } })
//                    }
//                },
//                color = Color(-1979711488),
//                modifier = Modifier
//                    .clickable {
//                        isEditing = true
//                    } // Открываем поле по клику
//                    .padding(vertical = 8.dp)
//            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                //TODO логика сохранения
                if (comment.length > 15) {
                    Toast.makeText(context, "Комментарий сохранен", Toast.LENGTH_LONG).show()
                    isEditing = false
                    // сохранили
                    viewModel.setSave(false)

                    val startTime = System.currentTimeMillis() / 1000
                    RealmManager.INSTANCE.executeTransaction { realm: Realm ->
                        wpDataDB.dt_update = startTime
                        wpDataDB.user_comment = comment
                        wpDataDB.user_comment_author_id = wpDataDB.user_id
                        wpDataDB.user_comment_dt_update = startTime
                        wpDataDB.startUpdate = true
                        realm.insertOrUpdate(wpDataDB)
                    }


                    // Это жосткие костыли
                    val exchange = Exchange()
                    exchange.sendWpDataToServer(object : Click {
                        override fun <T> onSuccess(data: T) {
                            val msg = data as String
                            Globals.writeToMLOG(
                                "INFO", "DetailedReportButtons.class.pressStartWork.onSuccess",
                                "msg: $msg"
                            )
                        }

                        override fun onFailure(error: String) {
                            Globals.writeToMLOG(
                                "INFO", "DetailedReportButtons.class.pressStartWork.onFailure",
                                "error: $error"
                            )
                        }
                    })


                } else
                    Toast.makeText(context, "Слишком короткий комментарий", Toast.LENGTH_LONG)
                        .show()
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(
                    id = R.color.blue
                )
            ),
            modifier = Modifier
                .align(Alignment.End)
                .alpha(if (comment.isNotBlank() && isSaved) 1f else 0f)
        ) {
            Text("Сохранить")
        }

//        }
    }

}

@Composable
fun CustomText(text: String, fontSize: TextUnit = 16.sp) {
    val textMeasurer = rememberTextMeasurer()

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(fontSize = fontSize)
    )

    val defaultLineHeight = textLayoutResult.size.height.toFloat() / textLayoutResult.lineCount
    val reducedLineHeight = defaultLineHeight * 0.8f // Уменьшаем на 20%

    Text(
        text = text,
        style = TextStyle(
            fontSize = fontSize,
            lineHeight = reducedLineHeight.sp
        )
    )
}
