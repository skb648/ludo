package com.ludolegends.game.engine

object BoardGeometry {
    const val GRID = 15
    data class Cell(val col: Int, val row: Int)
    val MAIN_TRACK = buildList {
        (1..5).forEach { add(Cell(it, 6)) }; (5 downTo 0).forEach { add(Cell(6, it)) }
        add(Cell(7,0)); (0..5).forEach { add(Cell(8,it)) }; (9..14).forEach { add(Cell(it,6)) }
        add(Cell(14,7)); (14 downTo 9).forEach { add(Cell(it,8)) }; (9..14).forEach { add(Cell(8,it)) }
        add(Cell(7,14)); (14 downTo 9).forEach { add(Cell(6,it)) }; (5 downTo 0).forEach { add(Cell(it,8)) }
        add(Cell(0,7)); add(Cell(0,6))
    }
    val STAR_CELLS = setOf(MAIN_TRACK[8], MAIN_TRACK[21], MAIN_TRACK[34], MAIN_TRACK[47])
    val START_CELLS = PlayerColor.entries.associateWith { MAIN_TRACK[it.startOffset] }
    val HOME_COLUMNS = mapOf(
        PlayerColor.RED to (1..5).map { Cell(it,7) }, PlayerColor.GREEN to (1..5).map { Cell(7,it) },
        PlayerColor.YELLOW to (13 downTo 9).map { Cell(it,7) }, PlayerColor.BLUE to (13 downTo 9).map { Cell(7,it) }
    )
    val CENTER = Cell(7,7)
    val BASE_ORIGIN = mapOf(PlayerColor.RED to Cell(0,0), PlayerColor.GREEN to Cell(9,0), PlayerColor.BLUE to Cell(0,9), PlayerColor.YELLOW to Cell(9,9))
    val BASE_SLOTS = listOf(Cell(2,2), Cell(4,2), Cell(2,4), Cell(4,4))
    fun cellFor(color: PlayerColor, relativePos: Int, tokenIndexInColor: Int): Cell = when {
        relativePos == Token.POS_BASE -> { val o=BASE_ORIGIN.getValue(color); val s=BASE_SLOTS[tokenIndexInColor.coerceIn(0,3)]; Cell(o.col+s.col,o.row+s.row) }
        relativePos == Token.POS_HOME -> CENTER
        relativePos in 1..51 -> MAIN_TRACK[LudoRules.toAbsolute(color, relativePos)]
        relativePos in 52..56 -> HOME_COLUMNS.getValue(color)[relativePos-52]
        else -> CENTER
    }
}
