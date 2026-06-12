package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.Main.MainViewModel


@Composable
fun TextFieldInputRounded(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChangedParent: (Boolean) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var isFocusedSearchView by remember { mutableStateOf(false) }

    fun activateSearch() {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .animateContentSize(animationSpec = tween(220))
            .onFocusChanged {
                val focused = it.hasFocus
                isFocusedSearchView = focused
                onFocusChangedParent(focused)
            }
    ) {
        if (!isFocusedSearchView && value.isEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 7.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        activateSearch()
                    },
                text = viewModel.getTranslateString(
                    stringResource(id = R.string.ui_text_find),
                    6002
                ),
                fontSize = 16.sp,
                color = colorResource(id = R.color.hintColorDefault),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconId = if (value.isNotEmpty()) {
                R.drawable.ic_close
            } else {
                R.drawable.ic_search
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle.Default.copy(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus(force = true)
                    }
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .align(Alignment.CenterVertically)
                    .padding(start = 7.dp)
                    .weight(1f)
            )

            Image(
                painter = painterResource(id = iconId),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.Black),
                modifier = Modifier
                    .padding(end = 7.dp, start = 7.dp)
                    .size(30.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (value.isNotEmpty()) {
                            onValueChange("")
                            activateSearch()
                        } else {
                            activateSearch()
                        }
                    }
            )
        }
    }
}