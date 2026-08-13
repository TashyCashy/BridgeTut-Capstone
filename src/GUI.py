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
        super().__init__(parent, background="#0f4d3f")
        self.grid_columnconfigure(0, weight=1)

        welcome_label= Label(self, 
              text="Welcome!", 
              font=("Georgia", 36, "bold"), 
              fg="#d4af37",
              bg="#0f4d3f",
              pady=20)
        
        username_label=Label(self, 
                             text="Username: ",
                             font=("Segoe UI", 12))
        
        username_entry=Entry(self, 
                             width=30,
                             font=("Segoe UI", 12))
        
        password_entry=Entry(self,
                             show="*", 
                             width=30,
                             font=("Segoe UI", 12))

        password_label=Label(self, 
                             text="Password: ",
                             font=("Segoe UI", 12))
        
        login_btn= Button(self, 
                         text="Sign in",
                         font=("Segoe UI", 12),
                         padx=10,
                         pady=5,
                         command=lambda: controller.show_frame(HomePage))

        welcome_label.grid(row=0, column=0, sticky="ew", pady=(20, 40))
        username_label.grid(row=1, column=0, pady=(10, 5))
        username_entry.grid(row=2, column=0)
        password_label.grid(row=3, column=0, pady=(20, 5) )
        password_entry.grid(row=4, column=0)
        login_btn.grid(row=5, column=0, pady=30)


class HomePage(Frame):
    def __init__(self, parent, controller):
            super().__init__(parent)
            Label(self, text="").pack()

class ResultPage(Frame):
    def __init__(self, parent, controller):
            super().__init__(parent)
            Label(self, text="").pack()


    

GUI().mainloop()