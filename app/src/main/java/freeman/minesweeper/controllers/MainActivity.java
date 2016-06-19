package freeman.minesweeper.controllers;

import android.os.*;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

import freeman.minesweeper.R;
import freeman.minesweeper.models.Block;
import freeman.minesweeper.models.Location;
import freeman.minesweeper.views.BlockView;

// TODO Add fast uncover option.
// TODO Add menu to let the user decide the rows cols number.
// TODO Add scrolling and zooming functionality.
// TODO Add record saver (via Sqlite).
// TODO Generate new icons for each number between 1 - 8 instead of print text on the buttons.
// TODO Add stop/continue options.

/**
 * An activity that starts a MineSweeper game.
 */
public class MainActivity extends AppCompatActivity {

    // region Constants

    private static final int   EASY_BOARD_ROWS    = 8;
    private static final int   EASY_BOARD_COLS    = 8;
    private static final int   EASY_MINES_COUNT   = 10;
    private static final int   MEDIUM_BOARD_ROWS  = 11;
    private static final int   MEDIUM_BOARD_COLS  = 9;
    private static final int   MEDIUM_MINES_COUNT = 15;
    private static final int   HARD_BOARD_ROWS    = 14;
    private static final int   HARD_BOARD_COLS    = 9;
    private static final int   HARD_MINES_COUNT   = 20;
    private static final float BLOCK_WIDTH        = 34;
    private static final float BLOCK_HEIGHT       = 34;
    //private static final int[] EASY_GAME_PROPS    = { EASY_BOARD_ROWS, EASY_BOARD_COLS, EASY_MINES_COUNT};
    //private static final int[] MEDIUM_GAME_PROPS  = { MEDIUM_BOARD_ROWS, MEDIUM_BOARD_COLS, MEDIUM_MINES_COUNT};
    //private static final int[] HARD_GAME_PROPS    = { HARD_BOARD_ROWS, HARD_BOARD_COLS, HARD_MINES_COUNT};

    // endregion

    // region Data Members

    private TableLayout         gameBoardView;
    private Block[][]           gameBoard;
    private BlockView[][]       blockViews;
    private float               dp;
    private float sp;
    private OnClickListener     blockClickListener;
    private OnLongClickListener blockLongClickListener;
    private Location[]          mineLocations;
    private @DrawableRes int[]  blockBackgrounds;
    private TextView            minesCountTxt;
    private ImageButton         newGameButton;
    private int                 minesCount;
    private int                 rows;
    private int                 cols;
    private Chronometer         gameChronometer;
    private int                 coveredBlocksCount;
    private int                 flagsCount;

    // endregion

    // region Life Cycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views references.
        this.minesCountTxt = (TextView) findViewById(R.id.mines_count_txt);
        this.gameChronometer = (Chronometer) findViewById(R.id.game_chronometer);
        this.gameBoardView = (TableLayout) findViewById(R.id.game_board);
        this.newGameButton = (ImageButton) findViewById(R.id.new_game_btn);

        // Initialize dp and sp sizes.
        this.dp = getResources().getDimension(R.dimen.dp);
        this.sp = getResources().getDimension(R.dimen.sp);

        // Initialize the block background drawable resources.
        this.blockBackgrounds = new int[]{
                R.drawable.zero,
                R.drawable.one,
                R.drawable.two,
                R.drawable.three,
                R.drawable.four,
                R.drawable.five,
                R.drawable.six,
                R.drawable.seven,
                R.drawable.eight,
        };

        // Initialize the block click listener.
        this.blockClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cast the view to a BlockView.
                BlockView blockView = (BlockView) v;

                // Get reference to the block.
                Block block = MainActivity.this.gameBoard
                        [blockView.getRow()][blockView.getColumn()];

                // If this block is covered and not flagged.
                if (block.isCovered() && !block.isFlagged()) {
                    // Uncover it!.
                    MainActivity.this.uncover(blockView.getRow(), blockView.getColumn());
                }
            }
        };

        // Initialize the block long click listener.
        this.blockLongClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Cast the view to a BlockView.
                BlockView blockView = (BlockView) v;

                // Get reference to the block.
                Block block = MainActivity.this.gameBoard
                        [blockView.getRow()][blockView.getColumn()];

                // If this block is flagged,
                if (block.isFlagged()) {
                    // Unflag it.
                    MainActivity.this.unflag(blockView.getRow(), blockView.getColumn());
                } else if (block.isCovered()) { // Else, if this block is covered,
                    // Flag it!.
                    MainActivity.this.flag(blockView.getRow(), blockView.getColumn());
                }

                // We handled the the event, no need to pass it.
                return true;
            }
        };

        // Initialize the new game button.
        this.newGameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.newGame();
            }
        });

        // TODO Pass to menu page (let the user decide the game parameters).
        this.rows = HARD_BOARD_ROWS;
        this.cols = HARD_BOARD_COLS;
        this.minesCount = HARD_MINES_COUNT;

        // Set up the game board.
        this.setupGameBoard();

        // Start new game.
        this.newGame();
    }

    // endregion

    // region Other Methods

    /**
     * Unflags the specified block.
     * @param row - The block's row.
     * @param col - The block's column.
     */
    private void unflag(int row, int col) {
        // Update the mine count and the text view.
        this.flagsCount--;
        this.minesCountTxt.setText(Integer.toString(this.minesCount - this.flagsCount));

        // Mark it as not flagged.
        this.gameBoard[row][col].setFlagged(false);

        // Change it's background to a flag background.
        this.setBackground(blockViews[row][col], R.drawable.block);
    }

    /**
     * Flags the block on the specified location.
     * @param row - The block's row.
     * @param col - The block's column.
     */
    private void flag(int row, int col) {
        // TODO add detection to winning cases.

        // Update the mine count and the text view.
        this.flagsCount++;
        this.minesCountTxt.setText(Integer.toString(this.minesCount - this.flagsCount));

        // Mark it as flagged.
        this.gameBoard[row][col].setFlagged(true);

        // Change it's background to a block background.
        this.setBackground(blockViews[row][col], R.drawable.flag);
    }

    /**
     * Sets the background of the view.
     * @param view - The view.
     * @param res - The resource to the wanted background.
     */
    private void setBackground(View view, @DrawableRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setBackground(getDrawable(res));
        } else {
            view.setBackground(ContextCompat.getDrawable(this, res));
        }
    }

    /**
     * Starts new game.
     */
    private void newGame() {
        // Reset board and board view.
        for (int row = 1; row < this.rows + 1; row++) {
            for (int col = 1; col < this.cols + 1; col++) {
                this.gameBoard[row][col].reset();
                this.setBackground(this.blockViews[row][col], R.drawable.block);
                this.blockViews[row][col].setText("");
            }
        }

        // Start the game chronometer.
        this.gameChronometer.setBase(SystemClock.elapsedRealtime());
        this.gameChronometer.start();

        // Update the new game button image.
        this.newGameButton.setImageResource(R.drawable.playing);

        // Update mine count text view and the discovered mines count.
        this.flagsCount = 0;
        this.minesCountTxt.setText(Integer.toString(this.minesCount - this.flagsCount));

        // Update covered blocks count.
        this.coveredBlocksCount = rows * cols;

        // Debug
        printBoardToConsole();

        // Initialize the mines location array.
        this.mineLocations = new Location[minesCount];

        // Generate random locations and put mines in these locations.
        //
        // For each mine.
        for (int currMineIndex = 0; currMineIndex < minesCount; currMineIndex++) {
            // Generate new random row, col, make sure that the block on this location
            // isn't marked already, and put the mine.
            int row = new Random().nextInt(this.rows - 1) + 1;
            int col = new Random().nextInt(this.cols - 1) + 1;
            while (this.gameBoard[row][col].containsMine()) {
                row = new Random().nextInt(this.rows - 1) + 1;
                col = new Random().nextInt(this.cols - 1) + 1;
            }
            this.gameBoard[row][col].setContainsMine(true);

            // Update neighbors.
            for (Location loc : getNeighbors(row, col)) {
                this.gameBoard[loc.row][loc.col].increaseNeighborMines();
            }

            // Update the array.
            this.mineLocations[currMineIndex] = new Location(row, col);
        }

        // Debug
        printBoardToConsole();
    }

    /**
     * Prints the state of the game board model to the console for debugging.
     */
    private void printBoardToConsole() {
        for (int row = 0; row < this.rows + 2; row++) {
            for (int col = 0; col < this.cols + 2; col++) {
                if (this.gameBoard[row][col].containsMine()) {
                    System.out.print("* ");
                } else {
                    System.out.print("" + this.gameBoard[row][col].getNeighborMinesCount() + " ");
                }
            }

            System.out.print("\n\n");
        }
        System.out.print("\n\n");
    }

    /**
     * Uncovers a block.
     * @param row - The block's row.
     * @param col - The block's column.
     */
    private void uncover(int row, int col) {
        // If the block contains mine.
        if (this.gameBoard[row][col].containsMine()) {
            // Change it's background to a mine background.
            this.setBackground(blockViews[row][col], R.drawable.mine);

            // The game is lost, end the game.
            this.endGame(false);
        } else {
            recursiveUncover(row, col);
        }
    }

    /**
     * Recursively uncover the block at the specified location, and if it's empty then keeps
     * uncovering, until the non-empty neighbors uncovered.
     * @param row - The block's row.
     * @param col - The block's column.
     */
    private void recursiveUncover(int row, int col) {
        // TODO add detection to winning cases.

        // Debug.
        if (!this.gameBoard[row][col].isEmpty()) {
            this.blockViews[row][col].setText(Integer.toString(this.gameBoard[row][col].getNeighborMinesCount()));
        }

        // Mark it uncovered.
        this.gameBoard[row][col].setCovered(false);

        // Update covered blocks count
        this.coveredBlocksCount--;

        // If all the non-mine blocks have uncovered.
        if (this.coveredBlocksCount == this.minesCount) {
            // The game is won, end the game
            this.endGame(true);
        }

        // Change it's background to the matched block background.
        @DrawableRes int bgId = blockBackgrounds[this.gameBoard[row][col].getNeighborMinesCount()];
        this.setBackground(blockViews[row][col], bgId);

        // If this block is empty.
        if (this.gameBoard[row][col].isEmpty()) {
            // For each neighbor location.
            for (Location loc : this.getNeighbors(row, col)) {
                // Recursively uncover all it's empty neighbors until the non-empty neighbors.
                if (this.gameBoard[loc.row][loc.col].isCovered()) {
                    this.recursiveUncover(loc.row, loc.col);
                }
            }
        }
    }

    /**
     * Get the neighbor locations of the block at the specified location.
     * @param row - The block's row.
     * @param col - The block's column.
     * @return - An array of locations (of the neighbors).
     */
    private Location[] getNeighbors(int row, int col) {
        return new Location[] {
                new Location(row - 1, col - 1),
                new Location(row - 1, col    ),
                new Location(row - 1, col + 1),
                new Location(row    , col - 1),
                new Location(row    , col + 1),
                new Location(row + 1, col - 1),
                new Location(row + 1, col    ),
                new Location(row + 1, col + 1),
        };
    }

    /**
     * Uncovers all the mines and ends the game.
     */
    private void endGame(boolean won) {
        // Stop the game chronometer.
        this.gameChronometer.stop();

        // For each mine location.
        for (Location loc : mineLocations) {
            // Get reference to the block view at this location.
            BlockView blockView = (BlockView)
                    ((ViewGroup) this.gameBoardView.getChildAt(loc.row - 1)).getChildAt(loc.col - 1);

            // If the game has won then mark it as flag, else, mark it as mine.
            this.setBackground(blockView, (won) ? R.drawable.flag : R.drawable.mine);
        }

        // If the game has won change the image of the new game button to winner image, else, to loser.
        this.newGameButton.setImageResource((won) ? R.drawable.winner: R.drawable.loser);
    }

    /**
     * Sets up the game board view and the game board model.
     */
    private void setupGameBoard() {
        // Initialize the GameBoard model and the block views
        // according to the rows and cols params + 'security frame'.
        this.gameBoard = new Block[this.rows + 2][this.cols + 2];
        this.blockViews = new BlockView[this.rows + 2][this.cols + 2];

        // Add to the game board view the block views according to the rows and cols params,
        // and initialize the game board model without the security frame.
        //
        // For each row without the frame.
        for (int row = 1; row < this.rows + 1; row++) {
            // Create new row and add it to the game board view.
            TableRow tableRow = new TableRow(this);
            this.gameBoardView.addView(tableRow);

            // For each column without the frame.
            for (int col = 1; col < this.cols + 1; col++) {
                // Initialize current block model and block view.
                this.gameBoard[row][col] = new Block();
                this.blockViews[row][col] = new BlockView(this);

                // Add the block view to the game board view.
                this.blockViews[row][col].setTextSize(3 * sp);
                this.blockViews[row][col].setRow(row);
                this.blockViews[row][col].setColumn(col);
                this.blockViews[row][col].setClickable(true);
                this.blockViews[row][col].setOnClickListener(this.blockClickListener);
                this.blockViews[row][col].setOnLongClickListener(this.blockLongClickListener);
                tableRow.addView(this.blockViews[row][col], (int) (BLOCK_WIDTH * dp), (int) (BLOCK_HEIGHT * dp));
            }
        }

        // Initialize the frame blocks.
        //
        // For each block in the top and bottom rows.
        for (int col = 0; col < (this.cols + 2); col++) {
            // Initialize the current top and bottom block and block view.
            this.gameBoard[0][col] = new Block();
            this.gameBoard[0][col].setContainsMine(true);
            this.gameBoard[0][col].setCovered(false);
            this.gameBoard[this.rows + 1][col] = new Block();
            this.gameBoard[this.rows + 1][col].setContainsMine(true);
            this.gameBoard[this.rows + 1][col].setCovered(false);
        }
        // For each block in the left and bottom right rows.
        for (int row = 1; row < (this.rows + 1); row++) {
            // Initialize the current left and right block and block view.
            this.gameBoard[row][0] = new Block();
            this.gameBoard[row][0].setContainsMine(true);
            this.gameBoard[row][0].setCovered(false);
            this.gameBoard[row][this.cols + 1] = new Block();
            this.gameBoard[row][this.cols + 1].setContainsMine(true);
            this.gameBoard[row][this.cols + 1].setCovered(false);
        }
    }

    // endregion
}