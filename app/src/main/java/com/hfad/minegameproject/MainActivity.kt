package com.hfad.minegameproject

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

        setText(plantMines().toString())
        //binding.testText.text = gameBoardCells[0][3].toString()


        //val rootView = binding.rootLayout
        //val gameboard = binding.gameBoard
        //var newView: ImageView
        //newView = ImageView(this)
        //gameboard.addView(newView)
        
        //newView.setImageDrawable(resources.getDrawable(R.drawable.tile_hidden))
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
    fun setUpGame() {
        plantMines()
        calculateNumbers()
        for (array in gameBoardCells)
            for (elements in array) {
                //Test för att se om rätt bild visas
                elements.reveal()
                var newView: ImageView = ImageView(this)
                gameboard.addView(newView)
                newView.layoutParams.height = 100
                newView.layoutParams.width = 100
                //kalla på metod som sätter drawable beroende på isRevealed och state?
                newView.setImageDrawable(setDrawables(newView, elements))
                //newView.setImageDrawable(resources.getDrawable(R.drawable.tile_hidden))

                /** sätta lyssnare för "långt klick" för att flagga?
                 * Kalla på funktion som kollar vilket state en tile befinner sig i,
                 * för att sedan kalla på andra funktioner beroende på vilket state
                 * varje tile har?
                 */
                newView.setOnClickListener(View.OnClickListener {
                    //newView.setImageDrawable((resources.getDrawable(R.drawable.mine_tile)))
                    checkTile(elements)
                })
                newView.setOnLongClickListener(View.OnLongClickListener {
                    //newView.setImageDrawable((resources.getDrawable(R.drawable.flag_tile)))
                    toggleFlag(elements)
                    updateBoard(gameboard)
                    true
                })
            }
        //Uppdatera brädet
        updateBoard(gameboard)
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
    fun setDrawables(currentView : ImageView, currentTile : Tile) : Drawable {
        var image = resources.getDrawable(R.drawable.tile_hidden)
        return image
    }
    fun updateBoard(gameboard: GridLayout) {
        var childCount = gameboard.childCount
        var cells = gameBoardCells.flatten()
        var counter = 0
        while (counter < childCount) {
            for (tiles in cells) {
                var currentView = gameboard.getChildAt(counter)
                var image = when (tiles.state) {
                    Tile.State.HIDDEN -> resources.getDrawable(R.drawable.tile_hidden)
                    Tile.State.FLAGGED -> resources.getDrawable(R.drawable.flag_tile)
                    Tile.State.MINE -> resources.getDrawable(R.drawable.mine_tile)
                    Tile.State.NUMBERED -> numberedTile(tiles.numberOfMinedNeighbours)
                }
                currentView as ImageView
                currentView.setImageDrawable(image)
            }
            //test för att se om något ändras
            setText(gameBoardCells.flatten().toString())
            counter++

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
                randomTile.reveal()
            }
        }
        return allTiles
    }

    private fun calculateNumbers() {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (!gameBoardCells[row][col].isMine) {
                    val count = countAdjacentMines(row, col)
                    gameBoardCells[row][col].numberOfMinedNeighbours = count
                }
            }
        }
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