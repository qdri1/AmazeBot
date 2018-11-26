

/**
 * class Cell to store position of something such as Player, or Garbage
 *
 * @author andrey
 */
public class Cell implements Comparable<Cell> {

    public int row;
    public int column;

    public Cell(int r, int c) {
        row = r;
        column = c;
    }

    Cell(Cell cell) {
        row = cell.row;
        column = cell.column;
    }

    public int compareTo(Cell a) {
        if (row > a.row) {
            return +1;
        } else if (row < a.row) {
            return -1;
        } else if (column > a.column) {
            return +1;
        } else if (column < a.column) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Cell{" + "row=" + row + ", column=" + column + '}';
    }
}
