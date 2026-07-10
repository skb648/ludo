package com.ludolegends.game

import com.ludolegends.game.engine.*
import org.junit.Assert.*
import org.junit.Test

class LudoRulesTest {
    private fun state(mode:GameMode=GameMode.STANDARD)=GameState.newMatch(mode,listOf(
        PlayerSeat(PlayerColor.RED,name="Red",teamId=0),PlayerSeat(PlayerColor.GREEN,name="Green",teamId=1),
        PlayerSeat(PlayerColor.YELLOW,name="Yellow",teamId=0),PlayerSeat(PlayerColor.BLUE,name="Blue",teamId=1)))
    @Test fun baseNeedsSix(){assertNull(LudoRules.destinationFor(Token(0,PlayerColor.RED),5));assertEquals(1,LudoRules.destinationFor(Token(0,PlayerColor.RED),6))}
    @Test fun exactHomeRequired(){assertEquals(57,LudoRules.destinationFor(Token(0,PlayerColor.RED,55),2));assertNull(LudoRules.destinationFor(Token(0,PlayerColor.RED,56),2))}
    @Test fun safeCellsCanonical(){assertEquals(setOf(0,8,13,21,26,34,39,47),LudoRules.SAFE_ABSOLUTE_CELLS)}
    @Test fun sixCreatesMove(){val r=LudoRules.applyDice(state(),6);assertTrue(r.outcome is DiceOutcome.ChooseMove);assertEquals(4,r.state.movableTokenIds.size)}
    @Test fun noMovePassesTurn(){val r=LudoRules.applyDice(state(),3);assertTrue(r.outcome is DiceOutcome.NoMoves);assertEquals(1,r.state.currentSeatIndex)}
    @Test fun thirdSixIsVoided(){val s=state().copy(consecutiveSixes=2);val r=LudoRules.applyDice(s,6);assertTrue(r.outcome is DiceOutcome.TripleSixPenalty);assertEquals(1,r.state.currentSeatIndex)}
    @Test fun captureResetsOpponent(){var s=state();s=s.copy(tokens=s.tokens.map{when(it.id){0->it.copy(pos=2);4->it.copy(pos=42);else->it}},phase=TurnPhase.AWAIT_MOVE,diceValue=1,movableTokenIds=listOf(0));val r=LudoRules.applyMove(s,0);assertEquals(-1,r.state.token(4).pos);assertEquals(listOf(4),r.capturedTokenIds)}
    @Test fun homeGivesBonus(){var s=state();s=s.copy(tokens=s.tokens.map{if(it.id==0)it.copy(pos=56)else it},phase=TurnPhase.AWAIT_MOVE,diceValue=1,movableTokenIds=listOf(0));val r=LudoRules.applyMove(s,0);assertTrue(r.reachedHome);assertTrue(r.grantsExtraTurn)}
}
class TeamRedirectionTest {
    private fun teamState():GameState=GameState.newMatch(GameMode.TEAM_2V2,listOf(PlayerSeat(PlayerColor.RED,name="R",teamId=0),PlayerSeat(PlayerColor.GREEN,name="G",teamId=1),PlayerSeat(PlayerColor.YELLOW,name="Y",teamId=0),PlayerSeat(PlayerColor.BLUE,name="B",teamId=1)))
    @Test fun redAndYellowAreMates(){assertTrue(LudoRules.isTeammate(teamState(),PlayerColor.RED,PlayerColor.YELLOW))}
    @Test fun redCannotCaptureYellow(){val s=teamState();assertTrue(LudoRules.isTeammate(s,PlayerColor.RED,PlayerColor.YELLOW));assertFalse(LudoRules.isTeammate(s,PlayerColor.RED,PlayerColor.GREEN))}
    @Test fun completedColorDrivesMate(){var s=teamState();s=s.copy(tokens=s.tokens.map{if(it.color==PlayerColor.RED)it.copy(pos=57)else it});assertEquals(PlayerColor.YELLOW,LudoRules.effectiveColorFor(s,0))}
    @Test fun unfinishedColorDrivesSelf(){assertEquals(PlayerColor.RED,LudoRules.effectiveColorFor(teamState(),0))}
}
