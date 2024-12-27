import tkinter as tk
from tkinter import messagebox

class ToDoListApp:
    def __init__(self, root):
        self.root = root
        self.root.title("To-Do List")

        # Input for new task
        self.task_entry = tk.Entry(root, width=40, font=("Arial", 14))
        self.task_entry.pack(pady=10)

        # Add task button
        self.add_button = tk.Button(root, text="Add Task", command=self.add_task, font=("Arial", 14))
        self.add_button.pack(pady=5)

        # Listbox to display tasks
        self.task_listbox = tk.Listbox(root, width=50, height=15, font=("Arial", 14))
        self.task_listbox.pack(pady=10)

        # Buttons to mark as done or delete
        self.done_button = tk.Button(root, text="Mark as Done", command=self.mark_done, font=("Arial", 14))
        self.done_button.pack(side="left", padx=20, pady=10)

        self.delete_button = tk.Button(root, text="Delete Task", command=self.delete_task, font=("Arial", 14))
        self.delete_button.pack(side="left", padx=20, pady=10)

    def add_task(self):
        task = self.task_entry.get()
        if task:
            self.task_listbox.insert(tk.END, task)
            self.task_entry.delete(0, tk.END)
        else:
            messagebox.showwarning("Input Error", "Please enter a task.")

    def mark_done(self):
        selected_task = self.task_listbox.curselection()
        if selected_task:
            task = self.task_listbox.get(selected_task)
            self.task_listbox.delete(selected_task)
            self.task_listbox.insert(tk.END, f"{task} (Done)")
        else:
            messagebox.showwarning("Selection Error", "Please select a task to mark as done.")

    def delete_task(self):
        selected_task = self.task_listbox.curselection()
        if selected_task:
            self.task_listbox.delete(selected_task)
        else:
            messagebox.showwarning("Selection Error", "Please select a task to delete.")

# Run the To-Do List app
if __name__ == "__main__":
    root = tk.Tk()
    app = ToDoListApp(root)
    root.mainloop()
