from tkinter import *
from PIL import Image, ImageTk
import os 

class GUI(Tk):

    def __init__(self):
        super().__init__()
        self.title("Bridge: NiteMeh Edition")
        self.geometry("900x500")

        container= Frame(self) #Creating frames so different app pages can be displayed
        container.pack(fill="both", expand=True)
        container.grid_rowconfigure(0, weight=1)
        container.grid_columnconfigure(0, weight=1)

        self.frames={}
        for F in (LoginPage, HomePage, ResultPage):
            frame= F(container, self)
            self.frames[F] = frame
            frame.grid(row=0, column=0, sticky="nsew")
        self.show_frame(LoginPage)

    def show_frame(self, page_class):
        """Allows different pages to be displayed"""
        frame= self.frames[page_class]
        frame.tkraise()

class LoginPage(Frame):
    def __init__(self, parent, controller):
        super().__init__(parent, background="pink")
        Label(self, text="").pack(pady=20)
        Button(self, text="Sign in", command=lambda: controller.show_frame(HomePage)).pack()

class HomePage(Frame):
    def __init__(self, parent, controller):
            super().__init__(parent)
            Label(self, text="").pack()

class ResultPage(Frame):
    def __init__(self, parent, controller):
            super().__init__(parent)
            Label(self, text="").pack()


    

GUI().mainloop()