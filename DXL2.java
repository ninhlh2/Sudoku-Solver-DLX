package site.vpop.testcapp;

import java.util.ArrayList;
import java.util.Scanner;


class DLXSolver2 {
    private static  byte SIZE ;
    public static  short SIZE2 ;
    private static byte BLOCK_SIZE;
    byte num_solution;
    byte[] grid;
    byte[] Solution;
    short[] solution_pos;
    Node root;
    ArrayList<boolean[]> matrix;
    ArrayList<Index> listIndex;

    static class Node {
        int   row, index, size;
        Node left, right, up, down, column;

        public Node(short col) {
            left = right = up = down = column = this;
            index = col;
        }

        public Node() {
            left = right = up = down = column = this;
        }
    }

    static class Index {
        byte row, col, digit;

        public Index(byte row, byte col, byte digit) {
            this.row = row;
            this.col = col;
            this.digit = digit;
        }
    }

    public static void main(String[] args) {

        DLXSolver2 s = new DLXSolver2((byte) 3);
        while (true) {
            Scanner input = new Scanner(System.in);
            String a= input.nextLine().trim();
            if(a.length() == SIZE2){
                long startTime = System.currentTimeMillis(); // test
                s.setGrid(a);
                s.solve();
                System.out.println(System.currentTimeMillis() - startTime); // test
                System.out.println(s.getSolution());
            }

        }
    }

    public DLXSolver2(byte size) {
        BLOCK_SIZE =size;
        SIZE= (byte) (BLOCK_SIZE*BLOCK_SIZE);
        SIZE2 = (short) (SIZE*SIZE);
        root = new Node();
        grid = new byte[SIZE2];
        Solution = new byte[SIZE2];
        matrix = new ArrayList<>();
        listIndex = new ArrayList<>();
    }

    public void solve() {
        setMatrixAndIndex();
        createDoubleLinkedLists();
        search(new short[SIZE2], 0);
        if (solution_pos != null) {
            for (int j : solution_pos) {
                int index = listIndex.get(j).row * SIZE + listIndex.get(j).col;
                Solution[index] = listIndex.get(j).digit;
            }
        }
    }

    public String getSolution(){
        StringBuilder s = new StringBuilder();
        for (byte f: Solution){
            s.append(f);
        }
        return s.toString();
    }

    private void cover(Node t) {
        Node e, r;
        t.right.left = t.left;
        t.left.right = t.right;//--remove node t
        for (e = t.down; e != t; e = e.down) {
            for (r = e.right; r != e; r = r.right) {
                r.down.up = r.up;
                r.up.down = r.down;
                r.column.size--;
            }
        }
    }

    private void uncover(Node t) {
        Node e, r;
        for (e = t.up; e != t; e = e.up) {
            for (r = e.left; r != e; r = r.left) {
                r.column.size++;
                r.down.up = r;
                r.up.down = r;
            }
        }
        t.right.left = t;
        t.left.right = t;
    }

    private boolean search(short[] e, int step) {
        Node d, l, selected_node = null;
        int a = Integer.MAX_VALUE;

        if (root.right == root) {
            num_solution++;
            solution_pos = e.clone();
            return num_solution > 1;
        }

        for (l = root.right; l != root; l = l.right) {
            if (0 == l.size) {
                return false;
            }
            if (l.size < a) {
                a = l.size;
                selected_node = l;
            }
            if(a==1)
                break;
        }

        this.cover(selected_node);
        for (d = selected_node.down; d != selected_node; d = d.down) {
            e[step] = (short) d.row;
            for (l = d.right; l != d; l = l.right) {
                cover(l.column);
            }
            if (search(e, step + 1)) {
                return true;
            }
            for (l = d.left; l != d; l = l.left) {
                uncover(l.column);
            }
        }
        uncover(selected_node);
        return false;
    }

    void createDoubleLinkedLists() {//e column
        Node[] header = new Node[matrix.get(0).length];
        Node node;

        int i, j, size =  header.length;

        for (i = 0; i < size; i++) {
            header[i] = new Node((short) i);
        }

        for (i = 0; i < size; i++) {
            if (i >= 0) {
                if (i - 1 >= 0) {
                    header[i].left = header[i - 1];
                }
                if (i + 1 < size) {
                    header[i].right = header[i + 1];
                }
            }
        }

        for (i = 0; i < matrix.size(); i++) {
            Node a = null;
            for (j = 0; j < matrix.get(i).length; j++) {
                if (matrix.get(i)[j]) {// co gia tri = 1
                    node = new Node();
                    node.row = i;
                    node.column = header[j];
                    node.up = header[j].up;
                    node.down = header[j];//ok
                    if (a != null) {
                        node.left = a;
                        node.right = a.right;
                        a.right.left = node;
                        a.right = node;
                    } else {
                        node.left = node;
                        node.right = node;
                    }
                    header[j].up.down = node;
                    header[j].up = node;//ok
                    header[j].size++;
                    a = node;
                }
            }
        }

        root.right  = header[0];
        root.left   = header[size - 1];
        header[0].left = root;
        header[size - 1].right = root;
    }

    public void setGrid(String a) {
        if (a.length() == SIZE2) {
            for (int i = 0; i < SIZE * SIZE; i++) {
                try {
                    grid[i] = Byte.parseByte(a.charAt(i) + "");
                } catch (NumberFormatException e) {
                    grid[i] = 0;
                }
            }
        }
    }

    private void setMatrixAndIndex() {
        byte s, col, row;
        boolean[] row_val;
        matrix.clear();
        listIndex.clear();

        for (int index = 0; index < SIZE2; index++) {
            col = (byte) (index%SIZE);
            row = (byte) (index/SIZE);
            s = (byte) (grid[index] - 1);
            if (s >= 0) {
                row_val = new boolean[4*SIZE2];
                row_val[index] = true;          //pos
                row_val[SIZE2 + SIZE * row + s] = true;     //row
                row_val[SIZE2*2 + SIZE * col + s] = true;    //col
                row_val[SIZE2*3 + SIZE * (3 * (row / 3) + (col / 3)) + s] = true;//b
                matrix.add(row_val);
                listIndex.add(new Index(row, col, (byte) (s + 1)));
            } else {
                for (byte a : get_cadidate(index)) {
                    row_val = new boolean[324];
                    row_val[index] = true;
                    row_val[SIZE2 + SIZE * row + a-1] = true;
                    row_val[SIZE2*2 + SIZE * col + a-1] = true;
                    row_val[SIZE2*3+ SIZE * (3 * (row / 3) + (col/ 3)) + a-1] = true;
                    matrix.add(row_val);
                    listIndex.add(new Index(row, col, a));
                }
            }

        }
    }

    private ArrayList<Byte> get_cadidate(int index){
        byte row, col, start, end, k;
        int[]  unavailable;
        ArrayList<Byte> a = new ArrayList<>();

        if (grid[index] < 1) {
            row = (byte) (index / SIZE);
            col = (byte) (index % SIZE);
            unavailable = new int[10];

            start = (byte) (row * SIZE);
            end = (byte) (start + SIZE);
            for (k = start; k < end; k++) {//Search Rows
                if (grid[k] > 0)
                    unavailable[grid[k]]++;
            }
            for (k = col; k < SIZE2; k += SIZE) {//Search Columns
                if (grid[k] > 0)
                    unavailable[grid[k]]++;
            }

            int xStart = (col / BLOCK_SIZE) * BLOCK_SIZE;
            int yStart = (row / BLOCK_SIZE) * BLOCK_SIZE;
            for (int ii = yStart; ii < yStart + BLOCK_SIZE; ii++)
                for (int jj = xStart; jj < xStart + BLOCK_SIZE; jj++)
                    if (grid[ii * SIZE + jj] > 0)
                        unavailable[grid[ii * SIZE + jj]]++;

            for (k = 1; k < 10; k++) {
                if (unavailable[k] < 1) {
                    a.add(k);
                }
            }
        }
        return a;
    }
}
