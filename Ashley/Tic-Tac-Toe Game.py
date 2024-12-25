import tkinter as tk
from tkinter import messagebox

class TicTacToe:
    def __init__(self, root):
        self.root = root
        self.root.title("Tic-Tac-Toe")
        self.turn = "X"  # X starts the game
        self.board = [["" for _ in range(3)] for _ in range(3)]

        # Create buttons for the grid
        self.buttons = [[None for _ in range(3)] for _ in range(3)]
        for i in range(3):
            for j in range(3):
                self.buttons[i][j] = tk.Button(root, text="", font=("Arial", 24), width=5, height=2,
                                               command=lambda row=i, col=j: self.make_move(row, col))
                self.buttons[i][j].grid(row=i, column=j)

    def make_move(self, row, col):
        if self.buttons[row][col]["text"] == "" and not self.check_winner():
            self.buttons[row][col]["text"] = self.turn
            self.board[row][col] = self.turn
            if self.check_winner():
                messagebox.showinfo("Game Over", f"Player {self.turn} wins!")
            elif all(all(cell != "" for cell in row) for row in self.board):
                messagebox.showinfo("Game Over", "It's a draw!")
            else:
                self.turn = "O" if self.turn == "X" else "X"

    def check_winner(self):
        # Check rows, columns, and diagonals
        for i in range(3):
            if self.board[i][0] == self.board[i][1] == self.board[i][2] != "":
                return True
            if self.board[0][i] == self.board[1][i] == self.board[2][i] != "":
                return True
        if self.board[0][0] == self.board[1][1] == self.board[2][2] != "":
            return True
        if self.board[0][2] == self.board[1][1] == self.board[2][0] != "":
            return True
        return False

# Run the game
if __name__ == "__main__":
    root = tk.Tk()
    game = TicTacToe(root)
    root.mainloop()
