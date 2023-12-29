package com.hfad.minegameproject

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.hfad.minegameproject.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var rootView : LinearLayout
    lateinit var gameboard : GridLayout
    lateinit var gameBoardCells : List<List<Tile>>
    lateinit var resetBtn : Button
    lateinit var timer : Chronometer
    var rows = 8
    var columns = 8
    var mines = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        rootView = binding.rootLayout
        gameboard = binding.gameBoard
        resetBtn = binding.resetButton
        timer = binding.timer

        resetBtn.setOnClickListener(){
            setUpGame()
        }
        //Skapar bräde med celler, lista med listor rows*columns, där varje cell består
        //av objekt av typen Tile
        gameBoardCells = List(rows){ List(columns) { Tile()}}

        setUpGame()
    }

    fun setText(text: String){
        binding.testText.text = text
    }


    /** Skapar en ny ImageView för varje Tile-objekt i spelbrädet
     *
     */
    @SuppressLint("SuspiciousIndentation")
    fun setUpGame() {
        var firstClick = true
        resetBoard()
        plantMines()
        calculateNumbers()
        for (array in gameBoardCells)
            for (elements in array) {
                elements.row = gameBoardCells.indexOf(array)
                elements.col = array.indexOf(elements)

                //elements.reveal()
                var newView: ImageView = ImageView(this)

                elements.tileView = newView
                gameboard.addView(newView)
                newView.layoutParams.height = 80
                newView.layoutParams.width = 80
                //kalla på metod som sätter drawable beroende på isRevealed och state?
                setDrawables(newView, elements)

                newView.setOnClickListener(View.OnClickListener {
                    if(!elements.isRevealed && !elements.isFlagged){
                        revealCell(elements.row, elements.col)
                        gameWon()
                    }
                    if (firstClick){
                        setBaseTime()
                        timer.start()
                        firstClick = false
                    }
                })
                newView.setOnLongClickListener(View.OnLongClickListener {
                    toggleFlag(elements)
                    true
                })
            }
    }
    // Kallar på funktion i Tile.kt som ändrar isFlagged till true/false
    // Skapa knapp som ändrar till flaggningstryck.
    fun toggleFlag(currentTile: Tile) {
        currentTile.toggleFlag()
        updateBoard(currentTile)
    }

    /** Kontrollerar hur många childviews gameBoard har och ska ändra vilken bild
     * som visas beroende på state. (fungerar ej helt?)
     * Redundant? varför har jag lagt till childviews? För att hitta och ändra view och
     * dess drawable?
     */
    fun setDrawables(currentView : ImageView, currentTile : Tile){
        lateinit var image : Drawable
        when(currentTile.state) {
            Tile.State.MINE -> image = resources.getDrawable(R.drawable.mine_tile)
            Tile.State.FLAGGED -> image = resources.getDrawable(R.drawable.flag_tile)
            Tile.State.HIDDEN -> image = resources.getDrawable(R.drawable.tile_hidden)
            // tilestate för detonerad bomb för alla eller de som ej flaggats?
            Tile.State.NUMBERED -> image = numberedTile(currentTile.numberOfMinedNeighbours)
        }
        currentView.setImageDrawable(image)
    }
    fun updateBoard(currentTile : Tile) {
        var imView = currentTile.tileView
        setDrawables(imView, currentTile)
    }

    fun revealBoard(){
        var revealAll = gameBoardCells.flatten()
        for (tile in revealAll) {
            if (!tile.isRevealed) {
                tile.reveal()
            }
            updateBoard(tile)
        }
    }

    private fun gameOver(currentTile : Tile) {
        revealBoard()
        currentTile.tileView.setImageDrawable(resources.getDrawable(R.drawable.mine_detonated))
        timer.stop()
        var elapsedTime = elapsedTime()
        setText("You lost! Your time was $elapsedTime seconds")
    }

    private fun gameWon(){
        // Kollar hur många tiles som är revealed och adderar reavealedTiles
        var revealedTiles : Int = 0
        var totalAmountOfTiles : Int = rows*columns
        for(array in gameBoardCells){
            for (elements in array){
                if (elements.isRevealed)
                    revealedTiles++
            }
        }
        if (revealedTiles == totalAmountOfTiles - mines){
            revealBoard()
            timer.stop()
            var elapsedTime = elapsedTime()
            setText("You won! Your time was $elapsedTime seconds")
            // game won!
            // avslöja alla tiles.
        }
    }

    fun elapsedTime() : Double {
        var elapsedTime = SystemClock.elapsedRealtime() - timer.base
        // Omvandlar tid från millisekunder till sekunder m 2 decimaler
        return (elapsedTime/10)/100.0
    }

    private fun resetBoard() {
        // om tile är mine, ta bort från gameboard.
        gameBoardCells.flatten().filter { it.isMine }.forEach { tile -> tile.removeMine() }
        // om tile är avslöjad, göm den igen.
        gameBoardCells.flatten().filter { it.isRevealed }.forEach { tile -> tile.hide() }
        // ta bort view.
        gameboard.removeAllViews()
    }

    private fun revealCell(row : Int, col : Int) {
        var currentTile = gameBoardCells[row][col]
        if (!currentTile.isRevealed && !currentTile.isFlagged) {
            currentTile.reveal()
            if (currentTile.numberOfMinedNeighbours == 0) {
                revealAdjacentCells(row, col)
            }
            updateBoard(currentTile)
        }
        if (currentTile.isMine) {
            gameOver(currentTile)
        }


    }

    private fun revealAdjacentCells(row: Int, col: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until rows && newCol in 0 until columns && !gameBoardCells[newRow][newCol].isRevealed) {
                    revealCell(newRow, newCol)
                }
            }
        }
    }

    /** Tanken är att funktionen ska sätta rätt numbered tile baserat på hur många
     * minor som finns i närheten (med hjälp av annan funktion?)
     */
    fun numberedTile(number: Int): Drawable = when (number) {
        0 -> resources.getDrawable(R.drawable.numbered_tile_0)
        1 -> resources.getDrawable(R.drawable.numbered_tile_1)
        2 -> resources.getDrawable(R.drawable.numbered_tile_2)
        3 -> resources.getDrawable(R.drawable.numbered_tile_3)
        4 -> resources.getDrawable(R.drawable.numbered_tile_4)
        5 -> resources.getDrawable(R.drawable.numbered_tile_5)
        6 -> resources.getDrawable(R.drawable.numbered_tile_6)
        7 -> resources.getDrawable(R.drawable.numbered_tile_7)
        8 -> resources.getDrawable(R.drawable.numbered_tile_8)
        else -> {
            resources.getDrawable(R.mipmap.ic_launcher)
        }
    }

    // Ändrar state på Tile till mina? Returnerar lista så att jag ska kunna
    // kontrollera om det ändras, kan tar bort returtyp sen?
    fun plantMines() : List<Tile>{
        var allTiles = gameBoardCells.flatten()
        var counter = 0
        while(counter < mines) {
            var randomTile = allTiles.random()
            if(!randomTile.isMine) {
                randomTile.plantMine()
                counter++
            }
        }
        return allTiles
    }

    private fun calculateNumbers() {
        var testNum : String = ""
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (!gameBoardCells[row][col].isMine) {
                    val count = countAdjacentMines(row, col)
                    testNum += count
                    gameBoardCells[row][col].numberOfMinedNeighbours = count
                }
            }
        }
        binding.checkNumbers.text = testNum
    }

    private fun countAdjacentMines(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until rows && newCol in 0 until columns && gameBoardCells[newRow][newCol].isMine) {
                    count++
                }
            }
        }
        return count
    }
    fun setBaseTime() {
        binding.timer.base = SystemClock.elapsedRealtime() - 0
    }
}