import tkinter as tk
from tkinter import messagebox

def on_click(button_text):
    if button_text == "C":
        entry.delete(0, tk.END)
    elif button_text == "=":
        try:
            result = eval(entry.get())
            entry.delete(0, tk.END)
            entry.insert(tk.END, str(result))
        except Exception as e:
            messagebox.showerror("Error", f"Invalid input: {e}")
    else:
        entry.insert(tk.END, button_text)

# Create the main application window
root = tk.Tk()
root.title("Simple Calculator")

# Create an entry widget for input
entry = tk.Entry(root, font=("Arial", 20), borderwidth=5, relief=tk.RIDGE, justify="right")
entry.grid(row=0, column=0, columnspan=4, padx=10, pady=10)

# Define the calculator buttons
buttons = [
    ("7", 1, 0), ("8", 1, 1), ("9", 1, 2), ("/", 1, 3),
    ("4", 2, 0), ("5", 2, 1), ("6", 2, 2), ("*", 2, 3),
    ("1", 3, 0), ("2", 3, 1), ("3", 3, 2), ("-", 3, 3),
    ("C", 4, 0), ("0", 4, 1), ("=", 4, 2), ("+", 4, 3)
]

# Add buttons to the grid
for text, row, col in buttons:
    button = tk.Button(root, text=text, font=("Arial", 18), command=lambda t=text: on_click(t), width=5, height=2)
    button.grid(row=row, column=col, padx=5, pady=5)

# Run the application
root.mainloop()
