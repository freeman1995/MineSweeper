package freeman.minesweeper.models;

/**
 * Represents a location of a block.
 */
public class Location {

    public int row;
    public int col;

    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }
    public Location() {}
}