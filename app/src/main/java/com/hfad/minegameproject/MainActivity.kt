package com.hfad.minegameproject

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
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

        //Skapar bräde med celler, lista med listor rows*columns, där varje cell består
        //av objekt av typen Tile
        gameBoardCells = List(rows){ List(columns) { Tile()}}

        setUpGame()

    }

    fun setText(text: String){
        binding.testText.text = text
    }

    fun checkTile(clickedTile: Tile) {
        if(!clickedTile.isRevealed) {
            clickedTile.reveal()
        }
           // if(clickedTile.state == Tile.State.MINE){
           //     // Game over
            // } else {
                //clickedTile.reveal()
                // Kalla på metod som visar vad som är under rutorna
        //updateBoard(gameboard)

    }

    /** Skapar en ny ImageView för varje Tile-objekt i spelbrädet
     *
     */
    @SuppressLint("SuspiciousIndentation")
    fun setUpGame() {
        plantMines()
        calculateNumbers()
        var test = ""
        for (array in gameBoardCells)
            for (elements in array) {
                elements.row = gameBoardCells.indexOf(array)
                elements.col = array.indexOf(elements)

                //Test för att se om rätt bild visas
                //elements.reveal()
                var newView: ImageView = ImageView(this)

                elements.tileView = newView
                gameboard.addView(newView)
                newView.layoutParams.height = 100
                newView.layoutParams.width = 100
                //kalla på metod som sätter drawable beroende på isRevealed och state?
                setDrawables(newView, elements)
                //newView.setImageDrawable(setDrawables(elements))

                /** sätta lyssnare för "långt klick" för att flagga?
                 * Kalla på funktion som kollar vilket state en tile befinner sig i,
                 * för att sedan kalla på andra funktioner beroende på vilket state
                 * varje tile har?
                 */
                newView.setOnClickListener(View.OnClickListener {
                    //visa ruta/rutor om den/de är numbered, om det är en mina = Game Over
                    // om rutan är 0 kolla detta:
                    var row = elements.row
                    var col = elements.col
                        revealCell(row, col)
                            // titta efter vinst.
                })
                newView.setOnLongClickListener(View.OnLongClickListener {
                    //Placera ut flagga på den ruta som klickats på
                    true
                })
            }
    }
    // Kallar på funktion i Tile.kt som ändrar isFlagged till true/false
    fun toggleFlag(currentTile: Tile) {
        currentTile.toggleFlag()

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
                Tile.State.NUMBERED -> image = numberedTile(currentTile.numberOfMinedNeighbours)
            }
        currentView.setImageDrawable(image)
    }
    fun updateBoard(currentTile : Tile) {
        var imView = currentTile.tileView
        setDrawables(imView, currentTile)
    }
    private fun revealCell(row : Int, col : Int) {
        var currentTile = gameBoardCells[row][col]
        if (!currentTile.isRevealed && !currentTile.isFlagged) {
            currentTile.reveal()
            // if currentTile.isMine {lose}
        }

        if (currentTile.numberOfMinedNeighbours == 0) {
            revealAdjacentCells(row, col)
        }
        updateBoard(currentTile)
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
                // Ta bort reveal, endast för att testa om olika tiles blir minor
                //randomTile.reveal()
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
}