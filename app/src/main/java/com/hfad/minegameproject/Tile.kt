package com.hfad.minegameproject

import android.widget.ImageView

class Tile {
    var row : Int = 0
    var col : Int = 0
    lateinit var tileView : ImageView
    enum class State {
        HIDDEN,
        FLAGGED,
        MINE,
        NUMBERED
    }

    var isMine = false
    fun plantMine() {
        isMine = true
    }

    fun reveal() {
        if(isRevealed) return
        isRevealed = true
    }

    fun toggleFlag() {
        isFlagged = !isFlagged
    }
    fun removeMine() {
        isMine = false
    }

    fun isEmpty() = !isMine && numberOfMinedNeighbours == 0

    var numberOfMinedNeighbours = 0
    var isFlagged = false
    var isRevealed = false
    val state: State
        get() = when (isRevealed) {
            true -> when(isMine) {
                true -> State.MINE
                false -> State.NUMBERED
            }
            false -> when (isFlagged) {
                true -> State.FLAGGED
                false -> State.HIDDEN
            }
        }

    fun isFalslyFlagged() = isFlagged and isMine

    override fun toString(): String {
        return when (state) {
            State.MINE -> "Mine"
            State.HIDDEN -> "Hidden"
            State.FLAGGED -> "Flagged"
            State.NUMBERED -> numberOfMinedNeighbours.toString()
        }
    }
}