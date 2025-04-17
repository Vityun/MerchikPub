package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.dataLayer.model.DataItemUI

@Composable
fun ImageWithText(
    item: DataItemUI,
    index: Int,
    painter: Painter,
    imageText: String,
    onMultipleClickItemImage: (DataItemUI, Int) -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onMultipleClickItemImage(item, index) },
            contentScale = ContentScale.Crop
        )
        // Затемнение с градиентом под текстом
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        ) {

            Text(
                text = imageText,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge.copy(
                    shadow = Shadow(
                        color = Color.Black, offset = Offset(0f, 0f), blurRadius = 1f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }
//        Text(text = imageText,
//            color = Color.White,
//            fontWeight = FontWeight.SemiBold,
//            style = MaterialTheme.typography.bodyLarge.copy(
//                shadow = Shadow(
//                    color = Color.Black, offset = Offset(0f, 0f), blurRadius = 1f
//                )
//            ),
//            modifier = Modifier
//                .graphicsLayer {
////                    shadowElevation = 10.dp.toPx()
//                    shape = RoundedCornerShape(8f)
//                    clip = false
//                }
//                .drawWithContent {
//                    drawContent()
//                    drawIntoCanvas { canvas ->
//                        val paint = Paint()
//                            .asFrameworkPaint()
//                            .apply {
//                                isAntiAlias = true
//                                color = android.graphics.Color.BLACK
//                                setShadowLayer(
//                                    8f,  // blurRadius
//                                    1f,  // offsetX
//                                    1f,  // offsetY
//                                    android.graphics.Color.BLACK
//                                )
//                            }
//                        canvas.nativeCanvas.drawText(
//                            "",
//                            0f,
//                            0f,
//                            paint
//                        )
//                    }
//                }
//                .align(alignment = Alignment.TopCenter)
//                .padding(horizontal = 6.dp)
//        )

    }
}