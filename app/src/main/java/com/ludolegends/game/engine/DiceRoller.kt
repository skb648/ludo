package com.ludolegends.game.engine

import kotlin.random.Random

/**
 * Dice roller abstraction. Two production implementations:
 *
 *  • [FairRandomDiceRoller] — used by MODE 1 (Local Pass & Play).
 *    Driven by `Random.nextInt(1, 7)` with uniform probability
 *    distribution (zero bias, zero rigging).
 *
 *  • [ManualDiceRoller] — used by MODE 2 (Play With Friends).
 *    Holds a value set externally via [setValue] and returns it on
 *    [roll]. Used when players tap the on-screen 1–6 buttons after
 *    rolling a real wooden dice.
 *
 * The [LudoEngine] doesn't care which one it holds — it just calls
 * [roll] when a turn needs a value. This keeps the rule engine
 * perfectly mode-agnostic.
 */
interface DiceRoller {
    /** Returns a uniformly distributed integer in 1..6. */
    fun roll(): Int
}

/**
 * === MODE 1 — Fair Random Math Engine ===
 *
 * Backed by Kotlin's [Random.Default] which uses a thread-safe
 * xorshift generator. `nextInt(1, 7)` produces a uniform integer
 * in [1, 6] with exactly 1/6 probability per face — zero bias,
 * zero rigging, suitable for tournament play.
 */
class FairRandomDiceRoller(
    private val random: Random = Random.Default
) : DiceRoller {
    override fun roll(): Int = random.nextInt(1, 7)
}

/**
 * === MODE 2 — Manual Dice Injector ===
 *
 * Used by the manual dice-button panel. The value is set externally
 * via [setValue] (typically by a UI button tap) and then consumed by
 * [roll]. Thread-safe via `@Volatile`.
 */
class ManualDiceRoller : DiceRoller {
    @Volatile private var pending: Int = 1

    fun setValue(value: Int) {
        require(value in 1..6) { "Dice value must be 1..6, got $value" }
        pending = value
    }

    override fun roll(): Int = pending
}
