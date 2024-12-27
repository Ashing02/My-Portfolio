import tkinter as tk
import random

class SnakeGame:
    def __init__(self, root):
        self.root = root
        self.root.title("Snake Game")

        # Game settings
        self.window_width = 400
        self.window_height = 400
        self.cell_size = 20
        self.snake = [(100, 100)]  # Initial snake position
        self.food = (200, 200)  # Initial food position
        self.direction = "Right"
        self.running = False

        # Canvas for the game
        self.canvas = tk.Canvas(root, width=self.window_width, height=self.window_height, bg="black")
        self.canvas.pack()

        # Buttons for control
        self.button_frame = tk.Frame(root)
        self.button_frame.pack()

        self.start_button = tk.Button(self.button_frame, text="Start", command=self.start_game)
        self.start_button.pack(side="left")

        self.restart_button = tk.Button(self.button_frame, text="Restart", command=self.restart_game)
        self.restart_button.pack(side="left")

        self.quit_button = tk.Button(self.button_frame, text="Quit", command=self.root.quit)
        self.quit_button.pack(side="left")

        # Bind keys for movement
        self.root.bind("<Up>", lambda event: self.change_direction("Up"))
        self.root.bind("<Down>", lambda event: self.change_direction("Down"))
        self.root.bind("<Left>", lambda event: self.change_direction("Left"))
        self.root.bind("<Right>", lambda event: self.change_direction("Right"))

    def start_game(self):
        if not self.running:
            self.running = True
            self.update_game()

    def restart_game(self):
        self.running = False
        self.snake = [(100, 100)]
        self.food = (200, 200)
        self.direction = "Right"
        self.canvas.delete("all")
        self.running = True
        self.update_game()

    def change_direction(self, new_direction):
        opposite_directions = {"Up": "Down", "Down": "Up", "Left": "Right", "Right": "Left"}
        if new_direction != opposite_directions.get(self.direction):
            self.direction = new_direction

    def move_snake(self):
        head_x, head_y = self.snake[0]

        if self.direction == "Up":
            head_y -= self.cell_size
        elif self.direction == "Down":
            head_y += self.cell_size
        elif self.direction == "Left":
            head_x -= self.cell_size
        elif self.direction == "Right":
            head_x += self.cell_size

        new_head = (head_x, head_y)

        # Check for collisions
        if (new_head in self.snake or
            head_x < 0 or head_x >= self.window_width or
            head_y < 0 or head_y >= self.window_height):
            self.running = False
            return

        self.snake.insert(0, new_head)

        # Check if food is eaten
        if new_head == self.food:
            self.place_food()
        else:
            self.snake.pop()

    def place_food(self):
        while True:
            food_x = random.randint(0, (self.window_width // self.cell_size) - 1) * self.cell_size
            food_y = random.randint(0, (self.window_height // self.cell_size) - 1) * self.cell_size
            self.food = (food_x, food_y)
            if self.food not in self.snake:
                break

    def draw_elements(self):
        self.canvas.delete("all")
        for segment in self.snake:
            x, y = segment
            self.canvas.create_rectangle(x, y, x + self.cell_size, y + self.cell_size, fill="green")

        food_x, food_y = self.food
        self.canvas.create_oval(food_x, food_y, food_x + self.cell_size, food_y + self.cell_size, fill="red")

    def update_game(self):
        if self.running:
            self.move_snake()
            self.draw_elements()
            self.root.after(100, self.update_game)
        else:
            self.canvas.create_text(
                self.window_width // 2,
                self.window_height // 2,
                text="Game Over",
                fill="white",
                font=("Arial", 24)
            )

# Run the game
if __name__ == "__main__":
    root = tk.Tk()
    game = SnakeGame(root)
    root.mainloop()
