package freeman.minesweeper.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Represents a block view, a view with row and column properties,
 * that indicates the location of this block on a the board it may contained in.
 */
public class BlockView extends Button {

    // region Data Members

    private int row;
    private int column;

    // endregion

    // region Ctors

    public BlockView(Context context) {
        super(context);
    }

    public BlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // endregion

    // region Accessors

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    // endregion
}