package ua.com.merchik.merchik.features.main.componentsUI

import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog

@Composable
fun CustomAditionalDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subTitle: String = "",
    helpTitle: String = title,
    helpMessage: String = subTitle,
    showHelpButton: Boolean = helpMessage.isNotBlank(),
    contentScrollable: Boolean = true,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    var subTitleMaxLines by remember { mutableIntStateOf(1) }
    var showToolTip by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current

        SideEffect {
            (dialogView.parent as? DialogWindowProvider)
                ?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 40.dp,
                        bottom = 40.dp
                    )
                    .imePadding()
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (showHelpButton) {
                        ImageButton(
                            id = R.drawable.ic_question_1,
                            shape = CircleShape,
                            colorImage = ColorFilter.tint(Color.Gray),
                            sizeButton = 40.dp,
                            sizeImage = 22.dp,
                            modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                            onClick = { showToolTip = true }
                        )
                    }

                    ImageButton(
                        id = R.drawable.ic_letter_x,
                        shape = CircleShape,
                        colorImage = ColorFilter.tint(Color.Gray),
                        sizeButton = 40.dp,
                        sizeImage = 25.dp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                        onClick = onDismiss
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        if (subTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.padding(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                Text(
                                    text = subTitle,
                                    maxLines = subTitleMaxLines,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (subTitleMaxLines == 1) {
                                        TextDecoration.Underline
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .padding(start = 2.dp, end = 2.dp)
                                        .clickable {
                                            subTitleMaxLines =
                                                if (subTitleMaxLines == 1) 99 else 1
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.padding(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(7.dp)
                                    .then(
                                        if (contentScrollable) {
                                            Modifier.verticalScroll(scrollState)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                content = content
                            )
                        }

                        actions?.let { actionsContent ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                content = actionsContent
                            )
                        }
                    }
                }
            }
        }
    }

    if (showToolTip) {
        MessageDialog(
            title = helpTitle,
            status = DialogStatus.ALERT,
            message = helpMessage,
            okButtonName = "OK",
            onDismiss = { showToolTip = false },
            onConfirmAction = { showToolTip = false }
        )
    }
}

@Composable
fun CustomAditionalDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @ColorRes colorResId: Int = R.color.blue
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = colorResId)
        ),
        modifier = modifier.padding(top = 12.dp)
    ) {
        Text(text)
    }
}
