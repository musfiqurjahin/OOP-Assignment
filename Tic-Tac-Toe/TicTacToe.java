import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TicTacToe extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private char[][] board = new char[3][3];
    private boolean playerTurn = true;
    private JLabel statusLabel;

    public TicTacToe() {
        super("Tic Tac Toe - X vs Computer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel grid = new JPanel(new GridLayout(3, 3, 5, 5));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Font btnFont = new Font(Font.SANS_SERIF, Font.BOLD, 48);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttons[r][c] = new JButton("");
                buttons[r][c].setFont(btnFont);
                buttons[r][c].setFocusPainted(false);
                buttons[r][c].addActionListener(this);
                grid.add(buttons[r][c]);
                board[r][c] = '\0';
            }
        }

        add(grid, BorderLayout.CENTER);

        statusLabel = new JLabel("Your turn (X)");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 0));
        add(statusLabel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!playerTurn) return;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (e.getSource() == buttons[r][c]) {
                    if (board[r][c] == '\0') {
                        makeMove(r, c, 'X');
                        if (checkGameEnd()) return;

                        playerTurn = false;
                        statusLabel.setText("Computer thinking...");

                        Timer t = new Timer(400, ev -> {
                            aiMove();
                            playerTurn = true;
                            if (!checkGameEnd())
                                statusLabel.setText("Your turn (X)");
                        });
                        t.setRepeats(false);
                        t.start();
                    }
                }
            }
        }
    }

    private void makeMove(int r, int c, char p) {
        board[r][c] = p;
        buttons[r][c].setText(String.valueOf(p));
        buttons[r][c].setEnabled(false);
    }

    private void disableAll() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                buttons[r][c].setEnabled(false);
    }

    private boolean checkWin(char p) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == p && board[i][1] == p && board[i][2] == p) return true;
            if (board[0][i] == p && board[1][i] == p && board[2][i] == p) return true;
        }
        if (board[0][0] == p && board[1][1] == p && board[2][2] == p) return true;
        if (board[0][2] == p && board[1][1] == p && board[2][0] == p) return true;
        return false;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board[r][c] == '\0') return false;
        return true;
    }

    private boolean checkGameEnd() {
        if (checkWin('X')) {
            showWinnerPopup("🎉 You Win! (X)");
            return true;
        }
        if (checkWin('O')) {
            showWinnerPopup("🤖 Computer Wins! (O)");
            return true;
        }
        if (isBoardFull()) {
            showWinnerPopup("😐 Draw!");
            return true;
        }
        return false;
    }

    private void showWinnerPopup(String message) {
        disableAll();
        statusLabel.setText(message);

        Object[] options = {"Play Again", "Exit"};
        int result = JOptionPane.showOptionDialog(
                this,
                message + "\n\nDo you want to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == 0) resetGame();
        else System.exit(0);
    }

    private void resetGame() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c] = '\0';
                buttons[r][c].setText("");
                buttons[r][c].setEnabled(true);
            }
        }
        statusLabel.setText("Your turn (X)");
        playerTurn = true;
    }

    private void aiMove() {
        Move best = findBestMove();
        if (best != null) {
            makeMove(best.r, best.c, 'O');
            statusLabel.setText("Computer moved");
        }
    }

    private Move findBestMove() {
        int bestVal = Integer.MIN_VALUE;
        Move bestMove = null;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == '\0') {
                    board[r][c] = 'O';
                    int moveVal = minimax(0, false);
                    board[r][c] = '\0';

                    if (moveVal > bestVal) {
                        bestVal = moveVal;
                        bestMove = new Move(r, c);
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMax) {
        if (checkWin('O')) return 10 - depth;
        if (checkWin('X')) return depth - 10;
        if (isBoardFull()) return 0;

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    if (board[r][c] == '\0') {
                        board[r][c] = 'O';
                        best = Math.max(best, minimax(depth + 1, false));
                        board[r][c] = '\0';
                    }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    if (board[r][c] == '\0') {
                        board[r][c] = 'X';
                        best = Math.min(best, minimax(depth + 1, true));
                        board[r][c] = '\0';
                    }
            return best;
        }
    }

    private static class Move {
        int r, c;
        Move(int r, int c) { this.r = r; this.c = c; }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(TicTacToe::new);
    }
}
