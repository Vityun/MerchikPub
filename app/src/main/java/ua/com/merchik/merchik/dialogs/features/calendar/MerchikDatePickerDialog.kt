package ua.com.merchik.merchik.dialogs.features.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Translate
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CalendarCorner = 8.dp
private const val CalendarAnimationMs = 220
private const val CalendarYearPageSize = 12
private const val CalendarMinYear = 1
private const val CalendarMaxYear = 9999
private const val CalendarMonthPageCount = CalendarMaxYear * 12
private val CalendarContentMaxWidth = 360.dp
private val CalendarContentMaxHeight = 360.dp
private val CalendarDayRowHeight = 42.dp
private val CalendarDaysGridHeight = 252.dp

private enum class CalendarPickerMode {
    Days,
    Months,
    Years
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MerchikDatePickerDialog(
    visible: Boolean,
    initialDate: LocalDate,
    title: String,
    allowedDateValidator: (LocalDate) -> Boolean = { true },
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var pickerMode by remember(initialDate) { mutableStateOf(CalendarPickerMode.Days) }
    var yearSlideDirection by remember { mutableIntStateOf(1) }
    var visibleYearStart by remember(initialDate) {
        mutableIntStateOf(calendarCenteredYearPageStart(initialDate.year))
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val initialMonthPage = remember(initialDate) {
        calendarPageForMonth(YearMonth.from(initialDate))
    }
    val monthPagerState = rememberPagerState(
        initialPage = initialMonthPage,
        pageCount = { CalendarMonthPageCount }
    )
    val visibleMonth = calendarMonthForPage(monthPagerState.currentPage)
    val today = remember { LocalDate.now() }
    val locale = remember(context) { calendarLocale(Translate.getAppLanguage(context)) }
    val monthFormatter = remember(locale) { calendarMonthFormatter(locale) }
    val selectedDateFormatter = remember(locale) { calendarSelectedDateFormatter(locale) }
    val weekDays = remember(locale) { calendarWeekDays(locale) }
    val monthNames = remember(locale) { calendarMonthNames(locale) }
    val accentColor = Color(0xFFB1B1B1)
    val headerColor = Color(0xFF5F5F5F)
    val futureDateColor = Color(0xFF424242)
    val pastDateColor = Color(0xFF8C8C8C)
    val outsideMonthDateColor = Color(0xFFC6C6C6)
    val disabledDateColor = Color(0xFFD8D8D8)
    val selectedDateAllowed = allowedDateValidator(selectedDate)
    var pendingMonthPage by remember { mutableStateOf<Int?>(null) }
    val focusSelectedDateMonth = {
        pendingMonthPage = calendarPageForMonth(YearMonth.from(selectedDate))
        pickerMode = CalendarPickerMode.Days
    }
    val selectVisibleMonth = { targetMonth: YearMonth ->
        val targetDate = selectedDate.withClampedYearMonth(targetMonth)
        selectedDate = targetDate
            .takeIf(allowedDateValidator)
            ?: firstSelectableDateInMonth(targetMonth, allowedDateValidator)
            ?: selectedDate
        pendingMonthPage = calendarPageForMonth(targetMonth)
        pickerMode = CalendarPickerMode.Days
    }
    val toggleMonthPicker = {
        pickerMode = if (pickerMode == CalendarPickerMode.Months) {
            CalendarPickerMode.Days
        } else {
            CalendarPickerMode.Months
        }
    }
    val toggleYearPicker = {
        if (pickerMode == CalendarPickerMode.Years) {
            pickerMode = CalendarPickerMode.Days
        } else {
            visibleYearStart = calendarCenteredYearPageStart(visibleMonth.year)
            pickerMode = CalendarPickerMode.Years
        }
    }
    val changeMonth = { direction: Int ->
        val nextPage = (monthPagerState.currentPage + direction)
            .coerceIn(0, CalendarMonthPageCount - 1)
        if (nextPage != monthPagerState.currentPage) {
            pickerMode = CalendarPickerMode.Days
            coroutineScope.launch {
                monthPagerState.animateScrollToPage(nextPage)
            }
        }
    }
    val selectDate = { date: LocalDate ->
        if (allowedDateValidator(date)) {
            selectedDate = date
        }
        val dateMonth = YearMonth.from(date)
        if (dateMonth != visibleMonth) {
            pendingMonthPage = calendarPageForMonth(dateMonth)
            pickerMode = CalendarPickerMode.Days
        }
    }

    LaunchedEffect(pendingMonthPage, pickerMode) {
        val targetPage = pendingMonthPage ?: return@LaunchedEffect
        if (pickerMode == CalendarPickerMode.Days) {
            if (monthPagerState.currentPage != targetPage) {
                monthPagerState.animateScrollToPage(targetPage)
            }
            pendingMonthPage = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .widthIn(max = 430.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 10.dp)
                ) {
                    ImageButton(
                        id = R.drawable.ic_letter_x,
                        shape = CircleShape,
                        colorImage = ColorFilter.tint(color = Color.Gray),
                        sizeButton = 40.dp,
                        sizeImage = 25.dp,
                        modifier = Modifier.padding(start = 15.dp),
                        onClick = onDismiss
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(CalendarCorner))
                        .clip(RoundedCornerShape(CalendarCorner))
                        .background(Color.White)
                        .padding(10.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(start = 10.dp, bottom = 7.dp, end = 10.dp),
                        fontWeight = FontWeight.Bold,
                        text = title
                    )

                    CalendarSelectedDateHeader(
                        selectedDate = selectedDate,
                        formatter = selectedDateFormatter,
                        onClick = focusSelectedDateMonth
                    )
                    Box(
                        modifier = Modifier
                            .heightIn(max = CalendarContentMaxHeight)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color.White)
                    ) {
                        AnimatedContent(
                            targetState = pickerMode,
                            modifier = Modifier.padding(6.dp),
                            transitionSpec = {
                                fadeIn(animationSpec = tween(CalendarAnimationMs)) togetherWith
                                        fadeOut(animationSpec = tween(CalendarAnimationMs))
                            },
                            label = "calendar_picker_mode"
                        ) { mode ->
                            when (mode) {
                                CalendarPickerMode.Days -> {
                                    Column {
                                        CalendarMonthHeader(
                                            visibleMonth = visibleMonth,
                                            monthFormatter = monthFormatter,
                                            headerColor = headerColor,
                                            expandedMode = pickerMode,
                                            yearSuffix = calendarYearSuffix(locale),
                                            onMonthClick = toggleMonthPicker,
                                            onYearClick = toggleYearPicker,
                                            onPreviousMonth = { changeMonth(-1) },
                                            onNextMonth = { changeMonth(1) }
                                        )

                                        Column(
                                            modifier = Modifier
                                                .border(
                                                    BorderStroke(
                                                        1.dp,
                                                        colorResource(id = R.color.borderContextMenu)
                                                    ), RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            CalendarWeekHeader(
                                                weekDays = weekDays,
                                                headerColor = headerColor
                                            )

                                            HorizontalDivider(
                                                thickness = 1.dp,
                                                color = colorResource(id = R.color.borderContextMenu)
                                            )

                                            HorizontalPager(
                                                state = monthPagerState,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(CalendarDaysGridHeight)
                                            ) { page ->
                                                val month = calendarMonthForPage(page)
                                                CalendarDaysGrid(
                                                    visibleMonth = month,
                                                    selectedDate = selectedDate,
                                                    today = today,
                                                    accentColor = accentColor,
                                                    futureDateColor = futureDateColor,
                                                    pastDateColor = pastDateColor,
                                                    outsideMonthDateColor = outsideMonthDateColor,
                                                    disabledDateColor = disabledDateColor,
                                                    allowedDateValidator = allowedDateValidator,
                                                    onDateClick = selectDate
                                                )
                                            }
                                        }
                                    }
                                }

                                CalendarPickerMode.Months -> {
                                    Column {
                                        CalendarMonthHeader(
                                            visibleMonth = visibleMonth,
                                            monthFormatter = monthFormatter,
                                            headerColor = headerColor,
                                            expandedMode = pickerMode,
                                            yearSuffix = calendarYearSuffix(locale),
                                            onMonthClick = toggleMonthPicker,
                                            onYearClick = toggleYearPicker,
                                            onPreviousMonth = { changeMonth(-1) },
                                            onNextMonth = { changeMonth(1) }
                                        )

                                        CalendarMonthPicker(
                                            visibleMonth = visibleMonth,
                                            monthNames = monthNames,
                                            today = today,
                                            headerColor = headerColor,
                                            accentColor = accentColor,
                                            futureDateColor = futureDateColor,
                                            pastDateColor = pastDateColor,
                                            disabledDateColor = disabledDateColor,
                                            allowedDateValidator = allowedDateValidator,
                                            onMonthSelected = { month ->
                                                selectVisibleMonth(
                                                    YearMonth.of(visibleMonth.year, month)
                                                )
                                            }
                                        )
                                    }
                                }

                                CalendarPickerMode.Years -> {
                                    Column {
                                        CalendarMonthHeader(
                                            visibleMonth = visibleMonth,
                                            monthFormatter = monthFormatter,
                                            headerColor = headerColor,
                                            expandedMode = pickerMode,
                                            yearSuffix = calendarYearSuffix(locale),
                                            onMonthClick = toggleMonthPicker,
                                            onYearClick = toggleYearPicker,
                                            onPreviousMonth = { changeMonth(-1) },
                                            onNextMonth = { changeMonth(1) }
                                        )

                                        CalendarYearPicker(
                                            visibleYearStart = visibleYearStart,
                                            selectedYear = visibleMonth.year,
                                            headerColor = headerColor,
                                            accentColor = accentColor,
                                            futureDateColor = futureDateColor,
                                            pastDateColor = pastDateColor,
                                            disabledDateColor = disabledDateColor,
                                            allowedDateValidator = allowedDateValidator,
                                            slideDirection = yearSlideDirection,
                                            onPreviousYears = {
                                                yearSlideDirection = -1
                                                visibleYearStart =
                                                    (visibleYearStart - CalendarYearPageSize)
                                                        .coerceAtLeast(CalendarMinYear)
                                            },
                                            onNextYears = {
                                                yearSlideDirection = 1
                                                visibleYearStart =
                                                    (visibleYearStart + CalendarYearPageSize)
                                                        .coerceAtMost(
                                                            calendarCenteredYearPageStart(
                                                                CalendarMaxYear
                                                            )
                                                        )
                                            },
                                            onYearSelected = { year ->
                                                selectVisibleMonth(
                                                    YearMonth.of(year, visibleMonth.month)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Скасувати",
                                style = TextStyle(
                                    color = colorResource(R.color.orange),
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }

                        TextButton(
                            enabled = selectedDateAllowed,
                            onClick = {
                                if (selectedDateAllowed) {
                                    onDateSelected(selectedDate)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text(
                                text = "ОК",
                                style = TextStyle(
                                    color = if (selectedDateAllowed) {
                                        colorResource(R.color.blue)
                                    } else {
                                        disabledDateColor
                                    },
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarSelectedDateHeader(
    selectedDate: LocalDate,
    formatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = selectedDate.format(formatter),
            color = Color.Black,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun CalendarMonthHeader(
    visibleMonth: YearMonth,
    monthFormatter: DateTimeFormatter,
    headerColor: Color,
    expandedMode: CalendarPickerMode,
    yearSuffix: String,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarHeaderToggle(
            text = visibleMonth.atDay(1).format(monthFormatter),
            expanded = expandedMode == CalendarPickerMode.Months,
            headerColor = headerColor,
            contentDescription = "Выбор месяца",
            modifier = Modifier.padding(start = 2.dp, end = 12.dp),
            onClick = onMonthClick
        )

        CalendarHeaderToggle(
            text = "${visibleMonth.year} $yearSuffix",
            expanded = expandedMode == CalendarPickerMode.Years,
            headerColor = headerColor,
            contentDescription = "Выбор года",
            onClick = onYearClick
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Предыдущий месяц",
                tint = headerColor
            )
        }

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Следующий месяц",
                tint = headerColor
            )
        }
    }
}

@Composable
private fun CalendarHeaderToggle(
    text: String,
    expanded: Boolean,
    headerColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = headerColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Icon(
            imageVector = if (expanded) {
                Icons.Filled.KeyboardArrowUp
            } else {
                Icons.Filled.KeyboardArrowDown
            },
            contentDescription = contentDescription,
            tint = headerColor,
            modifier = Modifier
                .padding(start = 2.dp)
                .size(20.dp)
        )
    }
}

@Composable
private fun CalendarWeekHeader(
    weekDays: List<String>,
    headerColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()
        .clip(
            RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp
            )
        )
        .background(color = colorResource(id = R.color.background_item_filter))
    ) {
        weekDays.forEach { day ->
            Text(
                text = day,
                color = headerColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CalendarDaysGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    accentColor: Color,
    futureDateColor: Color,
    pastDateColor: Color,
    outsideMonthDateColor: Color,
    disabledDateColor: Color,
    allowedDateValidator: (LocalDate) -> Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = visibleMonth.atDay(1)
    val startDate = firstDay.minusDays((firstDay.dayOfWeek.value - 1).toLong())

    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(6) { weekIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CalendarDayRowHeight)
            ) {
                repeat(7) { dayIndex ->
                    val date = startDate.plusDays((weekIndex * 7 + dayIndex).toLong())
                    CalendarDayCell(
                        date = date,
                        visibleMonth = visibleMonth,
                        selectedDate = selectedDate,
                        today = today,
                        accentColor = accentColor,
                        futureDateColor = futureDateColor,
                        pastDateColor = pastDateColor,
                        outsideMonthDateColor = outsideMonthDateColor,
                        disabledDateColor = disabledDateColor,
                        enabled = allowedDateValidator(date),
                        onDateClick = onDateClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthPicker(
    visibleMonth: YearMonth,
    monthNames: List<String>,
    today: LocalDate,
    headerColor: Color,
    accentColor: Color,
    futureDateColor: Color,
    pastDateColor: Color,
    disabledDateColor: Color,
    allowedDateValidator: (LocalDate) -> Boolean,
    onMonthSelected: (Int) -> Unit
) {
    val currentMonth = YearMonth.from(today)

    Column {
        repeat(4) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(3) { columnIndex ->
                    val month = rowIndex * 3 + columnIndex + 1
                    val targetMonth = YearMonth.of(visibleMonth.year, month)
                    CalendarMonthCell(
                        monthName = monthNames.getOrElse(month - 1) { month.toString() },
                        targetMonth = targetMonth,
                        selectedMonth = visibleMonth,
                        currentMonth = currentMonth,
                        headerColor = headerColor,
                        accentColor = accentColor,
                        futureDateColor = futureDateColor,
                        pastDateColor = pastDateColor,
                        disabledDateColor = disabledDateColor,
                        enabled = firstSelectableDateInMonth(
                            targetMonth,
                            allowedDateValidator
                        ) != null,
                        onMonthSelected = onMonthSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarYearPicker(
    visibleYearStart: Int,
    selectedYear: Int,
    headerColor: Color,
    accentColor: Color,
    futureDateColor: Color,
    pastDateColor: Color,
    disabledDateColor: Color,
    allowedDateValidator: (LocalDate) -> Boolean,
    slideDirection: Int,
    onPreviousYears: () -> Unit,
    onNextYears: () -> Unit,
    onYearSelected: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$visibleYearStart - ${
                    (visibleYearStart + CalendarYearPageSize - 1)
                        .coerceAtMost(CalendarMaxYear)
                }",
                color = headerColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onPreviousYears,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Предыдущие годы",
                    tint = headerColor
                )
            }

            IconButton(
                onClick = onNextYears,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Следующие годы",
                    tint = headerColor
                )
            }
        }

        AnimatedContent(
            targetState = visibleYearStart,
            transitionSpec = {
                (slideInHorizontally(
                    animationSpec = tween(CalendarAnimationMs),
                    initialOffsetX = { width -> width * slideDirection }
                ) + fadeIn(animationSpec = tween(CalendarAnimationMs))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(CalendarAnimationMs),
                            targetOffsetX = { width -> -width * slideDirection }
                        ) + fadeOut(animationSpec = tween(CalendarAnimationMs)))
            },
            label = "calendar_year_page"
        ) { startYear ->
            Column {
                repeat(4) { rowIndex ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(3) { columnIndex ->
                            val year = startYear + rowIndex * 3 + columnIndex
                            CalendarYearCell(
                                year = year,
                                selectedYear = selectedYear,
                                headerColor = headerColor,
                                accentColor = accentColor,
                                futureDateColor = futureDateColor,
                                pastDateColor = pastDateColor,
                                disabledDateColor = disabledDateColor,
                                enabled = year in CalendarMinYear..CalendarMaxYear &&
                                        hasSelectableDateInYear(year, allowedDateValidator),
                                onYearSelected = onYearSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CalendarMonthCell(
    monthName: String,
    targetMonth: YearMonth,
    selectedMonth: YearMonth,
    currentMonth: YearMonth,
    headerColor: Color,
    accentColor: Color,
    futureDateColor: Color,
    pastDateColor: Color,
    disabledDateColor: Color,
    enabled: Boolean,
    onMonthSelected: (Int) -> Unit
) {
    val selected = targetMonth == selectedMonth
    val currentUnselected = targetMonth == currentMonth && !selected
    val textColor = when {
        selected -> Color.White
        !enabled -> disabledDateColor
        currentUnselected -> headerColor
        targetMonth.isAfter(currentMonth) -> futureDateColor
        else -> pastDateColor
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) accentColor else Color.Transparent)
                .then(
                    if (currentUnselected) {
                        Modifier.border(BorderStroke(1.dp, accentColor), RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (enabled) {
                        Modifier.clickable { onMonthSelected(targetMonth.monthValue) }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthName,
                color = textColor,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RowScope.CalendarYearCell(
    year: Int,
    selectedYear: Int,
    headerColor: Color,
    accentColor: Color,
    futureDateColor: Color,
    pastDateColor: Color,
    disabledDateColor: Color,
    enabled: Boolean,
    onYearSelected: (Int) -> Unit
) {
    val selected = year == selectedYear
    val textColor = when {
        selected -> Color.White
        !enabled -> disabledDateColor
        year > selectedYear -> futureDateColor
        else -> pastDateColor
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) accentColor else Color.Transparent)
                .then(
                    if (enabled) {
                        Modifier.clickable { onYearSelected(year) }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = year.toString(),
                color = textColor,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RowScope.CalendarDayCell(
    date: LocalDate,
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    accentColor: Color,
    futureDateColor: Color,
    pastDateColor: Color,
    outsideMonthDateColor: Color,
    disabledDateColor: Color,
    enabled: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val inVisibleMonth = YearMonth.from(date) == visibleMonth
    val selected = date == selectedDate
    val todayUnselected = inVisibleMonth && date == today && !selected
    val dayTextColor = when {
        selected -> Color.White
        !enabled -> disabledDateColor
        !inVisibleMonth -> outsideMonthDateColor
        todayUnselected -> futureDateColor
        date.isAfter(today) -> futureDateColor
        else -> pastDateColor
    }

    val cellModifier = Modifier
        .weight(1f)
        .height(CalendarDayRowHeight)
        .padding(3.dp)

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) accentColor else Color.Transparent)
                .then(
                    if (todayUnselected) {
                        Modifier.border(BorderStroke(1.dp, accentColor), CircleShape)
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (enabled || !inVisibleMonth) {
                        Modifier.clickable { onDateClick(date) }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = dayTextColor,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun calendarLocale(appLanguage: String?): Locale {
    return when (appLanguage?.uppercase(Locale.US)) {
        "UA", "UK" -> Locale("uk", "UA")
        "RU" -> Locale("ru", "RU")
        "PL" -> Locale("pl", "PL")
        "GB", "EN" -> Locale.ENGLISH
        else -> Locale.getDefault()
    }
}

private fun calendarWeekDays(locale: Locale): List<String> {
    return if (locale.language.equals("uk", ignoreCase = true)) {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    } else {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    }
}

private fun calendarMonthFormatter(locale: Locale): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("LLLL", locale)
}

private fun calendarSelectedDateFormatter(locale: Locale): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("d MMMM yyyy '${calendarYearSuffix(locale)}'", locale)
}

private fun calendarMonthNames(locale: Locale): List<String> {
    val formatter = calendarMonthFormatter(locale)
    return (1..12).map { month ->
        YearMonth.of(2000, month).atDay(1).format(formatter)
    }
}

private fun calendarYearSuffix(locale: Locale): String {
    return if (locale.language.equals("uk", ignoreCase = true)) "р." else "г."
}

private fun calendarCenteredYearPageStart(year: Int): Int {
    val maxStart = (CalendarMaxYear - CalendarYearPageSize + 1)
        .coerceAtLeast(CalendarMinYear)
    return (year.coerceIn(CalendarMinYear, CalendarMaxYear) - 4)
        .coerceIn(CalendarMinYear, maxStart)
}

private fun calendarPageForMonth(yearMonth: YearMonth): Int {
    return ((yearMonth.year - CalendarMinYear) * 12 + yearMonth.monthValue - 1)
        .coerceIn(0, CalendarMonthPageCount - 1)
}

private fun calendarMonthForPage(page: Int): YearMonth {
    val normalizedPage = page.coerceIn(0, CalendarMonthPageCount - 1)
    return YearMonth.of(CalendarMinYear, 1).plusMonths(normalizedPage.toLong())
}

private fun LocalDate.withClampedYearMonth(yearMonth: YearMonth): LocalDate {
    return yearMonth.atDay(dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()))
}

private fun firstSelectableDateInMonth(
    yearMonth: YearMonth,
    allowedDateValidator: (LocalDate) -> Boolean
): LocalDate? {
    for (day in 1..yearMonth.lengthOfMonth()) {
        val date = yearMonth.atDay(day)
        if (allowedDateValidator(date)) return date
    }

    return null
}

private fun hasSelectableDateInYear(
    year: Int,
    allowedDateValidator: (LocalDate) -> Boolean
): Boolean {
    for (month in 1..12) {
        val yearMonth = YearMonth.of(year, month)
        if (firstSelectableDateInMonth(yearMonth, allowedDateValidator) != null) {
            return true
        }
    }

    return false
}
