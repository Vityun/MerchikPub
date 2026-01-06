package ua.com.merchik.merchik.features.main.Main

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import ua.com.merchik.merchik.Activities.DetailedReportActivity.RecycleViewDRAdapterTovar.ViewHolder.getArticle
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogMap
import ua.com.merchik.merchik.dialogs.DialogPhotoTovar
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.io.File


@Composable
fun CardItemsUI(title: String, item: DataItemUI, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var offsetSizeFont by remember { mutableStateOf(viewModel.offsetSizeFonts.value) }
    var showToolTip by remember { mutableStateOf(false) }

    val items = uiState.items

    // ✅ текущий индекс в списке
    val startIndex = remember(item, items) {
        items.indexOfFirst { it.rawObj == item.rawObj }.takeIf { it >= 0 }
            ?: items.indexOf(item).takeIf { it >= 0 }
            ?: 0
    }

    var currentIndex by rememberSaveable(items.size, item /* ключ на смену */) {
        mutableIntStateOf(startIndex)
    }

    // ✅ защита если список обновился и индекс вышел за границы
    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) currentIndex = currentIndex.coerceIn(0, items.lastIndex)
        else currentIndex = 0
    }

    // ✅ то, что реально показываем
    val currentItem = items.getOrNull(currentIndex) ?: item

    val visibleFields = currentItem.fields.filter { field ->
        val setting =
            uiState.settingsItemsForCard.firstOrNull { it.key.equals(field.key, ignoreCase = true) }
        setting?.isEnabled == true
    }

    val visibilityColumName =
        if (uiState.settingsItemsForCard.firstOrNull { it.key == "column_name" }?.isEnabled == true) View.VISIBLE else View.GONE

    val screenH = LocalConfiguration.current.screenHeightDp.dp
    // чтобы диалог не упирался в края экрана
    val dialogVerticalPadding = 24.dp
    val dialogHorizontalPadding = 16.dp
    val contentMaxHeight =
        screenH - (dialogVerticalPadding * 2) - 80.dp // запас под верхнюю строку/кнопки

    // А обработчики кнопок:
    val canLeft = items.isNotEmpty() && currentIndex > 0
    val canRight = items.isNotEmpty() && currentIndex < items.lastIndex


    // ✅ временный костыль: одна и та же картинка для всех элементов
    val forcedResImage = R.drawable.gps

// если у текущего item уже есть id_res_image — используем его, иначе форсим
    val idResImageForUi = remember(currentItem) {
        (currentItem.fields.firstOrNull {
            it.key.equals(
                "id_res_image",
                true
            )
        }?.value?.rawValue as? Int)
            ?: forcedResImage
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // ✅ ширина больше не "узкая"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dialogHorizontalPadding, vertical = dialogVerticalPadding)
                .wrapContentHeight(),
        ) {
            // ✅ Верхняя строка: "хвостик" + кнопки на одном уровне
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .height(48.dp)
                        .align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(top = 2.dp),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                ImageButton(
                    id = R.drawable.ic_question_1,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 22.dp,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        bottom = 8.dp
                    ),
                    onClick = { showToolTip = true }
                )

                ImageButton(
                    id = R.drawable.ic_settings,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        bottom = 8.dp
                    ),
                    onClick = { showToolTip = true }
                )

                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        bottom = 8.dp
                    ),
                    onClick = onDismiss
                )
            }

            // ✅ Карточка диалога: width max, height по контенту
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                ),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight()
                ) {
                    Text(text = "## описание карточки $title")
                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ Ограничиваем высоту контента и включаем скролл при необходимости
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = contentMaxHeight)
                                .verticalScroll(scrollState)
                        ) {
                            Row(Modifier.padding(7.dp)) {
                                val idResImage = idResImageForUi

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 5.dp)
                                        .border(1.dp, Color.LightGray)
                                        .background(Color.White)
                                        .align(Alignment.Top)
                                        .clipToBounds(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // ✅ если есть реальные фото — показываем их, иначе дефолтный ресурс
                                    val painter: Painter = when {
                                        !currentItem.images.isNullOrEmpty() -> {
                                            val file = File(currentItem.images.first())
                                            if (file.exists()) rememberAsyncImagePainter(model = file)
                                            else painterResource(idResImage)
                                        }

                                        else -> painterResource(idResImage)
                                    }

                                    Image(
                                        painter = painter,
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .size(100.dp)
                                            .clickable {
                                                viewModel.context?.let { context ->
                                                    onClickItemImage(currentItem, context, viewModel)
                                                }
                                            },
                                        contentScale = ContentScale.Fit,
                                        contentDescription = null
                                    )
                                }

//                                currentItem.fields.firstOrNull { it.key.equals("id_res_image", true) }
//                                    ?.let { it ->
//                                        val idResImage =
//                                            (it.value.rawValue as? Int) ?: R.drawable.merchik
//
//                                        Box(
//                                            modifier = Modifier
//                                                .weight(1f)
//                                                .padding(end = 5.dp)
//                                                .border(1.dp, Color.LightGray)
//                                                .background(Color.White)
//                                                .align(Alignment.Top)
//                                                .clipToBounds(),
//                                            contentAlignment = Alignment.Center
//                                        ) {
//                                            val images = mutableListOf<Painter>()
//                                            if (currentItem.images.isNullOrEmpty()) {
//                                                images.add(painterResource(idResImage))
//                                            } else {
//                                                currentItem.images.forEach { pathImage ->
//                                                    val file = File(pathImage)
//                                                    images.add(
//                                                        if (file.exists()) rememberAsyncImagePainter(
//                                                            model = file
//                                                        )
//                                                        else painterResource(idResImage)
//                                                    )
//                                                }
//                                            }
//
//                                            Image(
//                                                painter = images[0],
//                                                modifier = Modifier
//                                                    .padding(2.dp)
//                                                    .size(100.dp)
//                                                    .clickable { viewModel.context?.let { context ->
//                                                        onClickItemImage(currentItem, context)
//                                                    } },
//                                                contentScale = ContentScale.Fit,
//                                                contentDescription = null
//                                            )
//                                        }
//                                    }

                                Column(modifier = Modifier.weight(2f)) {
                                    visibleFields.forEachIndexed { index, field ->
                                        if (!field.key.equals("id_res_image", true)) {
                                            ItemFieldValue(field, visibilityColumName)

                                            if (index < visibleFields.lastIndex) {
                                                val bg = currentItem.modifierContainer?.background
                                                val color = when {
                                                    bg == null -> Color.LightGray
                                                    bg.isLighterThan(Color.LightGray) -> Color.LightGray
                                                    else -> Color.White
                                                }
                                                HorizontalDivider(color = color)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            ImageButton(
                                id = R.drawable.ic_angle_left_solid,
                                shape = CircleShape,
                                colorImage = ColorFilter.tint(Color.Gray),
                                sizeButton = 40.dp,
                                sizeImage = 25.dp,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    start = 12.dp,
                                    end = 22.dp
                                ),
                                onClick = {
                                    if (canLeft) currentIndex -= 1
                                }
                            )

                            ImageButton(
                                id = R.drawable.ic_angle_right_solid,
                                shape = CircleShape,
                                colorImage = ColorFilter.tint(Color.Gray),
                                sizeButton = 40.dp,
                                sizeImage = 25.dp,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    start = 12.dp
                                ),
                                onClick = {
                                    if (canRight) currentIndex += 1
                                }
                            )


                        }


                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.updateContent()
                                viewModel.updateOffsetSizeFonts(offsetSizeFont)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(
                                    id = R.color.orange
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.close)))
                        }
                    }

                }
            }
        }
    }

    if (showToolTip) {
        MessageDialog(
            title = "Не доступно",
            status = DialogStatus.ALERT,
            message = "Данный раздел находится в стадии в разработки",
            okButtonName = "Ок",
            onDismiss = { showToolTip = false },
            onConfirmAction = { showToolTip = false }
        )
    }
}

fun onClickItemImage(clickedDataItemUI: DataItemUI, context: Context, viewModel: MainViewModel) {
//        super.onClickItemImage(clickedDataItemUI, context)
    (clickedDataItemUI.rawObj.firstOrNull { it is TovarDB } as? TovarDB)?.let {
        val tovId = it.getiD().toInt()

        val stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(tovId, it.photoId, 18, true)

        if (stackPhotoDB != null)
            if (stackPhotoDB.getPhoto_num() != null && stackPhotoDB.getPhoto_num() != "") {
                Log.e(
                    "ФОТО_ТОВАРОВ",
                    "displayFullSizeTovarPhotoDialog: " + Uri.parse(stackPhotoDB.getPhoto_num())
                )

                val dialogPhotoTovar = DialogPhotoTovar(context)

                dialogPhotoTovar.setPhotoTovar(Uri.parse(stackPhotoDB.getPhoto_num()))

                val sb = StringBuilder()
                sb.append("Штрихкод: ").append(it.barcode).append("\n")
                sb.append("Артикул: ").append(getArticle(it, 0))

                dialogPhotoTovar.setPhotoBarcode(it.barcode)
                dialogPhotoTovar.setTextInfo(sb)

                dialogPhotoTovar.setClose { dialogPhotoTovar.dismiss() }
                dialogPhotoTovar.show()
            }
    }
    (clickedDataItemUI.rawObj.firstOrNull { it is LogMPDB } as? LogMPDB)?.let {
        val activity = (context as? AppCompatActivity) ?: return

        val wpDataDB = Gson().fromJson(viewModel.dataJson, WpDataDB::class.java)

        val addressSDB = RoomManager.SQL_DB.addressDao().getById(wpDataDB.addr_id)

            val dialogMap = DialogMap(
                activity,
                "",
                addressSDB?.locationXd ?: 0f,
                addressSDB?.locationYd ?: 0f,
                "Місцеположення ТТ",
                it.CoordX,
                it.CoordY,
                "Ваше місцеположення"
            )
            dialogMap.setData("", "Місцеположення")
            dialogMap.show()

    }
}

