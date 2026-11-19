package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R

@Composable
fun SectionToolbarTitle(title: String, count: Long, modifier: Modifier = Modifier) {
    val headerHeight = dimensionResource(R.dimen.section_header_height)
    val badgeRaise = dimensionResource(R.dimen.counter_badge_text_raise)
    Row(
        modifier.fillMaxWidth().heightIn(min = headerHeight).padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(title, Modifier.weight(1f, fill = false).padding(top = badgeRaise),
                fontSize = 18.sp, color = Color.Black)
            if (count > 0) {
                CounterBadge(count, Modifier.padding(start = 3.dp), maxCount = null,
                    background = Color.LightGray, borderAndTextColor = Color.DarkGray)
            }
        }
    }
}
