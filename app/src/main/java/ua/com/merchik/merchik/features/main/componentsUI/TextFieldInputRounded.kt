package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R

@Composable
fun TextFieldInputRounded(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    var isFocusedSearchView by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .onFocusChanged { isFocusedSearchView = it.isFocused }
    ) {
        if (!isFocusedSearchView)
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 7.dp),
                text = "Пошук",
                fontSize = 16.sp,
                color = colorResource(id = R.color.hintColorDefault),
            )

        Row{
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle.Default.copy(color = Color.Black, fontSize = 16.sp),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 7.dp)
                    .weight(1f)
            )

            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 7.dp, start = 7.dp)
                    .fillMaxHeight(),
                painter = painterResource(id = R.drawable.ic_82),
//                painter = painterResource(id = com.google.android.material.R.drawable.ic_search_black_24),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.hintColorDefault))
            )
        }
    }
}