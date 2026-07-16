package com.robotemi.go.feature.delivery.ui.others

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.robotemi.go.feature.delivery.model.Tray
import com.robotemi.go.feature.mymodel.R

@Composable
fun TemiGo(
    onSelect: (tray: Tray) -> Unit,
    onCancel: (tray: Tray) -> Unit,
    map: Map<Tray, String?>,
    currentSelectedTray: Tray?,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxHeight(.75f)
            .offset(x = 100.dp, y = 100.dp)
            .aspectRatio(379 / 726f)
    ) {
        val (
            temiGo,
            topTray, middleTray, bottomTray,
            topButtonX, middleButtonX, bottomButtonX,
        ) = createRefs()
        val topTrayGuideLine = createGuidelineFromTop(114 / 726f)
        val middleTrayGuideLine = createGuidelineFromTop(245 / 726f)
        val bottomTrayGuideLine = createGuidelineFromTop(397 / 726f)
        val rightBodyGuidLine = createGuidelineFromStart(329 / 379f)

        Image(
            modifier = Modifier
                .aspectRatio(329 / 726f)
                .constrainAs(temiGo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(rightBodyGuidLine)
                    width = Dimension.fillToConstraints
                },
            painter = painterResource(id = R.drawable.body),
            contentScale = ContentScale.Fit,
            contentDescription = null
        )

        TrayLayer(
            modifier = Modifier.constrainAs(topTray) {
                top.linkTo(topTrayGuideLine)
                start.linkTo(parent.start)
                end.linkTo(rightBodyGuidLine)
                width = Dimension.fillToConstraints
            },
            modifierX = Modifier.constrainAs(topButtonX) {
                top.linkTo(topTray.top)
                bottom.linkTo(topTray.bottom)
                end.linkTo(parent.end)
            },
            highlightResource = R.drawable.top_highlight,
            dimResource = R.drawable.top_dim,
            onTrayClicked = { onSelect(Tray.TOP) },
            onXClicked = { onCancel(Tray.TOP) },
            destination = map[Tray.TOP],
            isSelected = currentSelectedTray == Tray.TOP,
            boxOffsetBottom = 0.dp,
        )

        TrayLayer(
            modifier = Modifier.constrainAs(middleTray) {
                top.linkTo(middleTrayGuideLine)
                start.linkTo(parent.start)
                end.linkTo(rightBodyGuidLine)
                width = Dimension.fillToConstraints
            },
            modifierX = Modifier.constrainAs(middleButtonX) {
                top.linkTo(middleTray.top)
                bottom.linkTo(middleTray.bottom)
                end.linkTo(parent.end)
            },
            highlightResource = R.drawable.middle_highlight,
            dimResource = R.drawable.middle_dim,
            onTrayClicked = { onSelect(Tray.MIDDLE) },
            onXClicked = { onCancel(Tray.MIDDLE) },
            destination = map[Tray.MIDDLE],
            isSelected = currentSelectedTray == Tray.MIDDLE,
            boxOffsetBottom = 0.dp
        )

        TrayLayer(
            modifier = Modifier.constrainAs(bottomTray) {
                top.linkTo(bottomTrayGuideLine)
                start.linkTo(parent.start)
                end.linkTo(rightBodyGuidLine)
                width = Dimension.fillToConstraints
            },
            modifierX = Modifier.constrainAs(bottomButtonX) {
                top.linkTo(bottomTray.top)
                bottom.linkTo(bottomTray.bottom)
                end.linkTo(parent.end)
            },
            highlightResource = R.drawable.bottom_highlight,
            dimResource = R.drawable.bottom_dim,
            onTrayClicked = { onSelect(Tray.BOTTOM) },
            onXClicked = { onCancel(Tray.BOTTOM) },
            destination = map[Tray.BOTTOM],
            isSelected = currentSelectedTray == Tray.BOTTOM,
            boxOffsetBottom = (-20).dp
        )
    }

}

@Composable
private fun TrayLayer(
    modifier: Modifier,
    modifierX: Modifier,
    @DrawableRes highlightResource: Int,
    @DrawableRes dimResource: Int,
    onTrayClicked: () -> Unit,
    onXClicked: () -> Unit,
    destination: String?,
    isSelected: Boolean = false,
    boxOffsetBottom: Dp
) {

    val imageResource = if (destination != null || isSelected) highlightResource else dimResource


    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = destination == null, indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    onTrayClicked()
                },
            contentScale = ContentScale.FillWidth,
            painter = painterResource(imageResource),
            contentDescription = null
        )
        if (destination != null) {
            Box(
                modifier = Modifier
                    .offset(x = 5.dp, y = boxOffsetBottom)
                    .clip(
                        RoundedCornerShape(19.dp)
                    )
                    .background(Color(0xFF20D199))
                    .align(Alignment.Center)
                    .width(203.dp)
                    .height(49.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .align(Alignment.Center),
                    text = destination, color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = when (destination.length) {
                        in 1..5 -> 30.sp
                        in 6..10 -> 25.sp
                        else -> 20.sp
                    },
                )
            }
        }
    }

    if (destination != null) {
        ButtonX(modifier = modifierX, onXClicked = onXClicked, boxOffsetBottom = boxOffsetBottom)
    }
}

@Composable
private fun ButtonX(modifier: Modifier, onXClicked: () -> Unit, boxOffsetBottom: Dp) {
    Image(
        modifier = modifier
            .offset(x = 15.dp, y = boxOffsetBottom)
            .clip(CircleShape)
            .clickable {
                onXClicked()
            }
            .padding(15.dp),
        painter = painterResource(id = R.drawable.button_x),
        contentDescription = null
    )
}