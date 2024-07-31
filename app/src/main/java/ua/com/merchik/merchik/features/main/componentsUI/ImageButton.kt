package ua.com.merchik.merchik.features.main.componentsUI

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.R

@Composable
fun ImageButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    @DrawableRes id: Int,
    sizeButton: Dp,
    sizeImage: Dp,
    backGround: ButtonColors = ButtonDefaults.buttonColors(containerColor = Color.White),
    colorImage: ColorFilter = ColorFilter.tint(color = colorResource(id = R.color.black)),
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick.invoke() },
        shape = shape,
        colors = backGround,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .size(sizeButton)
            .shadow(4.dp, shape = shape)
            .clip(shape)
    ) {
        Image(
            painter = painterResource(id),
            contentDescription = "",
            contentScale = ContentScale.Inside,
            colorFilter = colorImage,
            modifier = Modifier
                .clip(shape)
                .size(sizeImage)
        )
    }
}