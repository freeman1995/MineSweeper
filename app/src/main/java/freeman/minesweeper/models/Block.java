package freeman.minesweeper.models;

/**
 * Represents a block in  a Mine Sweeper game board.
 */
public class Block {

    // region Data Members

    private boolean covered;
    private int     neighborMinesCount;
    private boolean containsMine;
    private boolean flagged;

    // endregion

    // region Ctors

    public Block() {
        this.reset();
    }

    // endregion

    // region Accessors

    public boolean isCovered() {
        return covered;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public int getNeighborMinesCount() {
        return neighborMinesCount;
    }

    public void setNeighborMinesCount(int neighborMinesCount) {
        this.neighborMinesCount = neighborMinesCount;
    }

    /**
     * Indicates whether this block contains mine or not.
     * @return true if so, else, false.
     */
    public boolean containsMine() {
        return containsMine;
    }

    public void setContainsMine(boolean containsMine) {
        this.containsMine = containsMine;
    }

    // endregion

    // region Other Methods

    /**
     * Increases the amount of neighbor mines around this block.
     */
    public void increaseNeighborMines() {
        this.neighborMinesCount++;
    }

    /**
     * Indicates whther this block has no neighbor mines.
     * @return true if so, else, false.
     */
    public boolean isEmpty() {
        return (this.neighborMinesCount == 0);
    }

    /**
     * Indicated whether this block is flagged or not.
     * @return - true if so, else. false.
     */
    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    /**
     * Reset this block too the original state.
     */
    public void reset() {
        this.containsMine = false;
        this.covered = true;
        this.flagged = false;
        this.neighborMinesCount = 0;
    }

    // ednregion
}