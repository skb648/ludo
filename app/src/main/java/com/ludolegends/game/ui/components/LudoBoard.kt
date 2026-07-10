// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ludolegends.game.engine.BoardMap
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.Player
import com.ludolegends.game.engine.SafeCells
import com.ludolegends.game.engine.Token
import com.ludolegends.game.ui.theme.BoardFrameGradient
import com.ludolegends.game.ui.theme.CellPath
import com.ludolegends.game.ui.theme.CellWhite
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireBase
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.viewmodel.LudoViewModel

/**
 * === SECTION 1.1 — BOARD CONTAINER SCALING & SPACING ===
 *
 * The 15x15 Ludo Board canvas expands dynamically to occupy the maximum
 * available width of the device screen. Use [fillMaxWidth] + [aspectRatio(1f)]
 * from the caller. No dead space surrounding the board — the gold trim sits
 * directly on the screen edge with a thin 4dp safety margin.
 *
 * === GLITCH FIXES (Veteran pass) ===
 *   • Removed the floating yellow circle artifact that was drawn at the
 *     center-vertex of the home triangles (the "gold center jewel" circle).
 *     The four colored home triangles now meet cleanly at a single point
 *     with no overlay dot.
 *   • Token rendering rewritten as solid premium high-gloss 3D pawns —
 *     discarded the transparent bubble/flat-globe wrapper. Each pawn has:
 *       - A solid base disc with player-color radial gradient
 *       - A solid domed body with sharp radial highlight on the upper bulb
 *       - A distinct soft blurred drop-shadow offset below the base
 *       - No transparent halo, no jewel, no outline-only bubble.
 *   • Safe-zone multi-stacking implemented per spec: when 2+ tokens share
 *     a safe cell, each is scaled to 0.65f of its normal radius and laid
 *     out in an inner 2×2 mini-grid so they remain individual click targets.
 */
@Composable
fun LudoBoard(
    state: LudoGameState,
    hopState: LudoViewModel.HopState,
    onTokenTapped: (Player, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val paddingPx = with(density) { 6.dp.toPx() }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        val canvasW = with(density) { maxWidth.toPx() }
        val canvasH = with(density) { maxHeight.toPx() }
        val boardSize = minOf(canvasW, canvasH) - paddingPx * 2
        val boardLeft = (canvasW - boardSize) / 2f
        val boardTop = (canvasH - boardSize) / 2f
        val cellSize = boardSize / BoardMap.GRID_SIZE

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Sapphire radial background (luxury dark navy)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(SapphireMid, SapphireBase, SapphireDeep),
                    center = Offset(boardLeft + boardSize / 2f, boardTop + boardSize / 2f),
                    radius = boardSize * 0.75f
                ),
                topLeft = Offset(boardLeft, boardTop),
                size = Size(boardSize, boardSize)
            )

            // 2. Outer gold trim frame — double layered
            drawGoldFrame(boardLeft, boardTop, boardSize)

            // 3. Home-base quadrants
            drawHomeQuadrants(boardLeft, boardTop, cellSize)

            // 4. The path (ring + home columns + center)
            drawPath(boardLeft, boardTop, cellSize)

            // 5. Safe-zone stars + exit arrow tiles
            drawSafeMarkers(boardLeft, boardTop, cellSize)

            // 6. Center home triangle (4 colored sub-triangles meeting at a point)
            drawCenterTriangle(boardLeft, boardTop, cellSize)

            // 7. Tokens (solid 3D pawns with safe-zone stacking at 0.65f)
            drawTokens(state, hopState, boardLeft, boardTop, cellSize)
        }

        // === SECTION 2.1 — DOTTED SPINNING SELECTION RING OVERLAY ===
        // Drawn ON TOP of the canvas (so it's visible above tokens) but
        // UNDER the touch overlay (so taps still reach the right handler).
        // Only renders when there are selectable tokens (AWAITING_TOKEN_PICK phase).
        if (state.selectableTokenIds.isNotEmpty()) {
            val tokenRadiusPx = cellSize * 0.32f
            com.ludolegends.game.jui.DottedSpinningSelectionRing(
                selectableTokens = state.selectableTokenIds,
                cellCenters = computeSelectableCenters(state, boardLeft, boardTop, cellSize),
                tokenRadiusPx = tokenRadiusPx,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 8. Token-tap hit-testing on top of the canvas
        TokenTouchOverlay(
            state = state,
            cellSizeFraction = 1f / BoardMap.GRID_SIZE,
            onTokenTapped = onTokenTapped,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Compute the canvas-pixel centers for all selectable tokens — used by
 * [DottedSpinningSelectionRing] so it knows where to draw the spinning
 * rings without re-running the BoardMap lookup logic.
 */
private fun computeSelectableCenters(
    state: LudoGameState,
    boardLeft: Float,
    boardTop: Float,
    cellSize: Float
): Map<Pair<Player, Int>, Offset> {
    val out = mutableMapOf<Pair<Player, Int>, Offset>()
    for ((player, tokenId) in state.selectableTokenIds) {
        val token = state.tokens[player]?.getOrNull(tokenId) ?: continue
        val cell = BoardMap.tokenToCell(token) ?: continue
        val (row, col) = cell
        val cx = boardLeft + col * cellSize + cellSize / 2f
        val cy = boardTop + row * cellSize + cellSize / 2f
        out[Pair(player, tokenId)] = Offset(cx, cy)
    }
    return out
}

/**
 * Holder for the 4 colored home-triangle sub-paths drawn at the board center.
 */
internal data class HomeTriangle(
    val p1: Offset,
    val p2: Offset,
    val p3: Offset,
    val player: Player
)

// ============================================================
// Canvas drawing helpers
// ============================================================

private fun DrawScope.drawGoldFrame(left: Float, top: Float, size: Float) {
    drawRoundRect(
        brush = BoardFrameGradient,
        topLeft = Offset(left - 4f, top - 4f),
        size = Size(size + 8f, size + 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(width = 6f)
    )
    drawRoundRect(
        color = GoldDeep,
        topLeft = Offset(left - 1f, top - 1f),
        size = Size(size + 2f, size + 2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 1.2f)
    )
}

private fun DrawScope.drawHomeQuadrants(left: Float, top: Float, cellSize: Float) {
    for (player in Player.values()) {
        val (rowRange, colRange) = BoardMap.QUADRANTS[player]!!
        val r0 = rowRange.first; val r1 = rowRange.last
        val c0 = colRange.first; val c1 = colRange.last
        val qLeft = left + c0 * cellSize
        val qTop = top + r0 * cellSize
        val qW = (c1 - c0 + 1) * cellSize
        val qH = (r1 - r0 + 1) * cellSize

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(player.light.copy(alpha = 0.85f), player.primary, player.dark),
                start = Offset(qLeft, qTop),
                end = Offset(qLeft + qW, qTop + qH)
            ),
            topLeft = Offset(qLeft, qTop),
            size = Size(qW, qH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        drawRoundRect(
            brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
            topLeft = Offset(qLeft, qTop),
            size = Size(qW, qH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = Stroke(width = 2f)
        )

        for (slot in BoardMap.BASE_SLOTS[player]!!) {
            val (sr, sc) = slot
            val sx = left + sc * cellSize + cellSize / 2f
            val sy = top + sr * cellSize + cellSize / 2f
            val slotR = cellSize * 0.42f
            // === SECTION 2.3 — EMBOSSED SECURE QUADRANT HOME BASES ===
            // Multi-layered inner shadow / embossed vector style — looks
            // distinctively recessed/sunken, matching professional game-board standards.
            // Layer 1: outer rim (lighter — catches the imaginary light from top).
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = slotR,
                center = Offset(sx, sy)
            )
            // Layer 2: inner disc (darker — the sunken interior).
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        player.dark.copy(alpha = 0.95f),
                        player.primary.copy(alpha = 0.85f),
                        SapphireMid
                    ),
                    center = Offset(sx, sy - slotR * 0.25f),
                    radius = slotR * 0.95f
                ),
                radius = slotR * 0.92f,
                center = Offset(sx, sy)
            )
            // Layer 3: top inner-shadow ring (gives the recessed look).
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = slotR * 0.88f,
                center = Offset(sx, sy - slotR * 0.06f),
                style = Stroke(width = 1.5f)
            )
            // Layer 4: bottom inner-highlight arc (the embossed bevel).
            drawCircle(
                color = Color.White.copy(alpha = 0.30f),
                radius = slotR * 0.78f,
                center = Offset(sx, sy + slotR * 0.10f),
                style = Stroke(width = 1.2f)
            )
            // Layer 5: gold outline trim (premium finish).
            drawCircle(
                color = GoldDeep,
                radius = slotR,
                center = Offset(sx, sy),
                style = Stroke(width = 1.5f)
            )
            // Layer 6: center dimple (the deepest point of the recess).
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.55f),
                        Color.Black.copy(alpha = 0.0f)
                    ),
                    center = Offset(sx, sy),
                    radius = slotR * 0.45f
                ),
                radius = slotR * 0.45f,
                center = Offset(sx, sy)
            )
        }
    }
}

private fun DrawScope.drawPath(left: Float, top: Float, cellSize: Float) {
    for ((index, cell) in BoardMap.RING_COORDS.withIndex()) {
        val (r, c) = cell
        val x = left + c * cellSize
        val y = top + r * cellSize
        val isExit = SafeCells.isExit(index)
        val exitOwner = Player.values().firstOrNull { it.startIndex == index }
        val cellColor = if (isExit && exitOwner != null) exitOwner.primary.copy(alpha = 0.7f) else CellPath
        drawRoundRect(
            color = cellColor,
            topLeft = Offset(x + 1f, y + 1f),
            size = Size(cellSize - 2f, cellSize - 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = GoldDeep.copy(alpha = 0.55f),
            topLeft = Offset(x + 1f, y + 1f),
            size = Size(cellSize - 2f, cellSize - 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
            style = Stroke(width = 0.6f)
        )
    }
    for (player in Player.values()) {
        for ((homeIdx, cell) in BoardMap.HOME_COLUMN_COORDS[player]!!.withIndex()) {
            val (r, c) = cell
            val x = left + c * cellSize
            val y = top + r * cellSize
            val intensity = 0.4f + homeIdx * 0.12f
            drawRoundRect(
                color = player.primary.copy(alpha = intensity),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(cellSize - 2f, cellSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = GoldDeep.copy(alpha = 0.55f),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(cellSize - 2f, cellSize - 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                style = Stroke(width = 0.6f)
            )
        }
    }
}

private fun DrawScope.drawSafeMarkers(left: Float, top: Float, cellSize: Float) {
    for (ringIdx in SafeCells.STAR_TILES) {
        val (r, c) = BoardMap.RING_COORDS[ringIdx]
        val cx = left + c * cellSize + cellSize / 2f
        val cy = top + r * cellSize + cellSize / 2f
        val starR = cellSize * 0.32f
        val star = makeStarPath(cx, cy, starR, starR * 0.42f)
        drawPath(star, GoldBright)
        drawPath(star, GoldDeep, style = Stroke(width = 1.2f))
    }
    for (ringIdx in SafeCells.EXIT_TILES) {
        val (r, c) = BoardMap.RING_COORDS[ringIdx]
        val cx = left + c * cellSize + cellSize / 2f
        val cy = top + r * cellSize + cellSize / 2f
        drawExitArrow(cx, cy, cellSize, ringIdx)
    }
}

private fun DrawScope.drawExitArrow(cx: Float, cy: Float, cellSize: Float, ringIdx: Int) {
    val arrowR = cellSize * 0.22f
    val arrow = Path().apply {
        when (ringIdx) {
            0    -> { moveTo(cx - arrowR, cy - arrowR); lineTo(cx + arrowR, cy)
                      lineTo(cx - arrowR, cy + arrowR); close() }
            13   -> { moveTo(cx - arrowR, cy - arrowR); lineTo(cx, cy + arrowR)
                      lineTo(cx + arrowR, cy - arrowR); close() }
            36   -> { moveTo(cx + arrowR, cy - arrowR); lineTo(cx - arrowR, cy)
                      lineTo(cx + arrowR, cy + arrowR); close() }
            39   -> { moveTo(cx - arrowR, cy + arrowR); lineTo(cx, cy - arrowR)
                      lineTo(cx + arrowR, cy + arrowR); close() }
        }
    }
    drawPath(arrow, Color.White.copy(alpha = 0.8f))
    drawPath(arrow, GoldDeep, style = Stroke(width = 1f))
}

private fun makeStarPath(cx: Float, cy: Float, outerR: Float, innerR: Float): Path {
    return Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (Math.PI / 2) + i * (Math.PI / 5)
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cy - (r * Math.sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

/**
 * Center home triangle — four colored sub-triangles meeting at the exact
 * center point. NO gold jewel circle overlay (this was the floating yellow
 * dot artifact in the previous build).
 */
private fun DrawScope.drawCenterTriangle(left: Float, top: Float, cellSize: Float) {
    val centerX = left + 7.5f * cellSize
    val centerY = top + 7.5f * cellSize
    val half = cellSize * 1.5f

    val triangles = listOf(
        HomeTriangle(
            Offset(centerX - half, centerY - half),
            Offset(centerX + half, centerY - half),
            Offset(centerX, centerY),
            Player.RED
        ),
        HomeTriangle(
            Offset(centerX + half, centerY - half),
            Offset(centerX + half, centerY + half),
            Offset(centerX, centerY),
            Player.GREEN
        ),
        HomeTriangle(
            Offset(centerX + half, centerY + half),
            Offset(centerX - half, centerY + half),
            Offset(centerX, centerY),
            Player.YELLOW
        ),
        HomeTriangle(
            Offset(centerX - half, centerY + half),
            Offset(centerX - half, centerY - half),
            Offset(centerX, centerY),
            Player.BLUE
        )
    )

    for (t in triangles) {
        val path = Path().apply {
            moveTo(t.p1.x, t.p1.y); lineTo(t.p2.x, t.p2.y); lineTo(t.p3.x, t.p3.y); close()
        }
        drawPath(path, t.player.primary.copy(alpha = 0.92f))
        drawPath(path, GoldBright, style = Stroke(width = 1.5f))
    }
    // NOTE: No center jewel circle is drawn here. The four triangles meet
    // cleanly at (centerX, centerY) and that vertex is the visual home target.
}

// ============================================================
// Token rendering — solid premium 3D pawns
// ============================================================

private fun DrawScope.drawTokens(
    state: LudoGameState,
    hopState: LudoViewModel.HopState,
    left: Float,
    top: Float,
    cellSize: Float
) {
    // Group tokens by cell — used for safe-zone multi-stacking logic.
    val tokensByCell = mutableMapOf<Pair<Int, Int>, MutableList<Triple<Player, Int, Token>>>()
    for (player in state.turnOrder) {
        for ((idx, token) in state.tokens[player].orEmpty().withIndex()) {
            val isHopping = hopState is LudoViewModel.HopState.Hopping &&
                hopState.playerId == player && hopState.tokenId == idx
            if (isHopping) continue
            val cell = BoardMap.tokenToCell(token) ?: continue
            tokensByCell.getOrPut(cell) { mutableListOf() }.add(Triple(player, idx, token))
        }
    }

    val baseTokenR = cellSize * 0.32f

    for ((cell, tokens) in tokensByCell) {
        val (row, col) = cell
        val cellCenterX = left + col * cellSize + cellSize / 2f
        val cellCenterY = top + row * cellSize + cellSize / 2f

        if (tokens.size == 1) {
            // === Single token — full size, solid 3D pawn ===
            val (player, idx, _) = tokens.first()
            drawSolidPawn3D(
                player = player,
                tokenId = idx,
                state = state,
                cx = cellCenterX,
                cy = cellCenterY,
                r = baseTokenR,
                scale = 1.0f
            )
        } else {
            // === RULE 3 — Safe-zone multi-stacking ===
            // Multiple tokens on the same cell: scale each to 0.65f and lay
            // them out in a 2×2 inner mini-grid so they remain individual
            // click targets and don't overlap blindly.
            val stackScale = 0.65f
            val stackR = baseTokenR * stackScale
            val offsets = listOf(
                Offset(-stackR * 0.75f, -stackR * 0.75f),
                Offset( stackR * 0.75f, -stackR * 0.75f),
                Offset(-stackR * 0.75f,  stackR * 0.75f),
                Offset( stackR * 0.75f,  stackR * 0.75f)
            )
            for ((i, triple) in tokens.withIndex()) {
                val (player, idx, _) = triple
                val off = offsets[i.coerceAtMost(3)]
                drawSolidPawn3D(
                    player = player,
                    tokenId = idx,
                    state = state,
                    cx = cellCenterX + off.x,
                    cy = cellCenterY + off.y,
                    r = stackR,
                    scale = stackScale
                )
            }
        }
    }

    // Render the hopping token at its interpolated position with parabolic arc.
    if (hopState is LudoViewModel.HopState.Hopping) {
        val fromCell = stepToCell(hopState.playerId, hopState.fromStep, left, top, cellSize)
        val toCell = stepToCell(hopState.playerId, hopState.toStep, left, top, cellSize)
        val t = hopState.progress
        val x = fromCell.first + (toCell.first - fromCell.first) * t
        val y = fromCell.second + (toCell.second - fromCell.second) * t
        // Parabolic arc — lift the token upward at midpoint (4*t*(1-t) = max at t=0.5)
        val arcLift = cellSize * 0.55f * (4f * t * (1f - t))
        drawSolidPawn3D(
            player = hopState.playerId,
            tokenId = hopState.tokenId,
            state = state,
            cx = x,
            cy = y - arcLift,
            r = baseTokenR,
            scale = 1.0f
        )
    }
}

private fun DrawScope.stepToCell(
    player: Player,
    step: Int,
    left: Float,
    top: Float,
    cellSize: Float
): Pair<Float, Float> {
    return when {
        step == Token.BASE -> {
            val slot = BoardMap.BASE_SLOTS[player]!![0]
            Pair(left + slot.second * cellSize + cellSize / 2f,
                 top + slot.first * cellSize + cellSize / 2f)
        }
        step in 0..51 -> {
            val absolute = (player.startIndex + step) % 52
            val (r, c) = BoardMap.RING_COORDS[absolute]
            Pair(left + c * cellSize + cellSize / 2f, top + r * cellSize + cellSize / 2f)
        }
        step in 52..56 -> {
            val homeIdx = step - 52
            val (r, c) = BoardMap.HOME_COLUMN_COORDS[player]!![homeIdx]
            Pair(left + c * cellSize + cellSize / 2f, top + r * cellSize + cellSize / 2f)
        }
        else -> Pair(left + 7.5f * cellSize, top + 7.5f * cellSize)
    }
}

/**
 * Solid premium high-gloss 3D pawn — drawn entirely on the Canvas.
 *
 * Anatomy (bottom → top), all SOLID (no transparent bubble):
 *   1. Soft blurred drop-shadow ellipse — offset below the base, gives the
 *      piece weight and premium depth on the board grid.
 *   2. Solid base disc — player color radial gradient (light edge → dark center),
 *      with a thin dark rim stroke for definition.
 *   3. Gold neck ring — thin horizontal gold band where the dome meets the base.
 *   4. Solid domed body — same player gradient, drawn as a Path with two
 *      cubic curves. NO transparency, NO bubble.
 *   5. Sharp radial highlight — a small bright white ellipse on the UPPER
 *      BULB area, offset to the upper-left, simulating a gloss specular.
 *   6. Selection ring — only when the pawn is in [selectableTokenIds].
 *
 * Removed from previous build:
 *   - Top jewel (was contributing to the bubble look)
 *   - Transparent dome outline
 *   - Dim overlay (replaced with smaller scale for non-current tokens)
 */
private fun DrawScope.drawSolidPawn3D(
    player: Player,
    tokenId: Int,
    state: LudoGameState,
    cx: Float,
    cy: Float,
    r: Float,
    scale: Float
) {
    val isSelectable = Pair(player, tokenId) in state.selectableTokenIds
    val isCurrentPlayer = player == state.currentPlayer
    // Dim non-current-player tokens during the pick phase by reducing alpha
    // (NOT by drawing a transparent overlay rectangle — that caused the bubble look).
    val baseAlpha = if (!isCurrentPlayer &&
                        state.phase == com.ludolegends.game.engine.TurnPhase.AWAITING_TOKEN_PICK) 0.45f
                    else 1.0f

    // === 1. Soft blurred drop-shadow ===
    // Radial gradient from dark to transparent simulates a Gaussian blur.
    val shadowCenterY = cy + r * 0.55f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.55f * baseAlpha),
                Color.Black.copy(alpha = 0.30f * baseAlpha),
                Color.Black.copy(alpha = 0.0f)
            ),
            center = Offset(cx, shadowCenterY),
            radius = r * 1.6f
        ),
        topLeft = Offset(cx - r * 1.3f, shadowCenterY - r * 0.35f),
        size = Size(r * 2.6f, r * 1.1f)
    )

    // === 2. Solid base disc ===
    val baseCenterY = cy + r * 0.10f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                player.light.copy(alpha = baseAlpha),
                player.primary.copy(alpha = baseAlpha),
                player.dark.copy(alpha = baseAlpha)
            ),
            center = Offset(cx - r * 0.3f, baseCenterY - r * 0.2f),
            radius = r * 1.3f
        ),
        topLeft = Offset(cx - r, baseCenterY - r * 0.5f),
        size = Size(r * 2f, r * 1.1f)
    )
    // Base rim — dark stroke for definition
    drawOval(
        color = Color.Black.copy(alpha = 0.65f * baseAlpha),
        topLeft = Offset(cx - r, baseCenterY - r * 0.5f),
        size = Size(r * 2f, r * 1.1f),
        style = Stroke(width = 1.2f)
    )

    // === 3. Gold neck ring ===
    val neckY = baseCenterY - r * 0.30f
    drawLine(
        brush = Brush.horizontalGradient(
            listOf(
                GoldBright.copy(alpha = baseAlpha),
                GoldDeep.copy(alpha = baseAlpha),
                GoldBright.copy(alpha = baseAlpha)
            )
        ),
        start = Offset(cx - r * 0.62f, neckY),
        end   = Offset(cx + r * 0.62f, neckY),
        strokeWidth = 2.2f
    )

    // === 4. Solid domed body ===
    val domeTopY = cy - r * 1.55f
    val domeBottomY = baseCenterY - r * 0.20f
    val domePath = Path().apply {
        moveTo(cx - r * 0.62f, domeBottomY)
        cubicTo(
            cx - r * 0.62f, (domeBottomY + domeTopY) * 0.5f,
            cx - r * 0.35f, domeTopY,
            cx, domeTopY
        )
        cubicTo(
            cx + r * 0.35f, domeTopY,
            cx + r * 0.62f, (domeBottomY + domeTopY) * 0.5f,
            cx + r * 0.62f, domeBottomY
        )
        // Close back to the base — solid bottom edge, no transparency.
        lineTo(cx - r * 0.62f, domeBottomY)
        close()
    }
    drawPath(
        path = domePath,
        brush = Brush.radialGradient(
            colors = listOf(
                player.light.copy(alpha = baseAlpha),
                player.primary.copy(alpha = baseAlpha),
                player.dark.copy(alpha = baseAlpha)
            ),
            // Gradient center is offset to the upper-left to suggest a light source.
            center = Offset(cx - r * 0.25f, domeTopY + (domeBottomY - domeTopY) * 0.30f),
            radius = r * 1.5f
        )
    )
    // Dome rim — subtle dark stroke so the dome reads as a 3D solid.
    drawPath(
        path = domePath,
        color = Color.Black.copy(alpha = 0.55f * baseAlpha),
        style = Stroke(width = 1.0f)
    )

    // === 5. Sharp radial highlight on the upper bulb ===
    // A bright white ellipse offset to the upper-left of the dome top.
    // This is the key specular that makes the pawn read as glossy 3D.
    val highlightCx = cx - r * 0.22f
    val highlightCy = domeTopY + (domeBottomY - domeTopY) * 0.30f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * baseAlpha),
                Color.White.copy(alpha = 0.55f * baseAlpha),
                Color.White.copy(alpha = 0.0f)
            ),
            center = Offset(highlightCx, highlightCy),
            radius = r * 0.55f
        ),
        topLeft = Offset(highlightCx - r * 0.35f, highlightCy - r * 0.45f),
        size = Size(r * 0.7f, r * 0.9f)
    )

    // === 6. Selection ring (only when this pawn is a legal pick target) ===
    if (isSelectable) {
        // Outer pulsing gold ring
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    GoldBright,
                    GoldBright.copy(alpha = 0.10f),
                    GoldBright,
                    GoldBright.copy(alpha = 0.10f)
                ),
                center = Offset(cx, cy - r * 0.3f)
            ),
            radius = r * 1.55f,
            center = Offset(cx, cy - r * 0.3f),
            style = Stroke(width = 3.5f)
        )
        // Inner subtle gold dot at center to draw the eye
        drawCircle(
            color = GoldBright.copy(alpha = 0.30f),
            radius = r * 0.18f,
            center = Offset(cx, cy - r * 0.3f)
        )
    }
}
