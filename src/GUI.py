from tkinter import *
import random
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
            super().__init__(parent, background="#0f4d3f")

            self.grid_rowconfigure(0, weight=0)
            self.grid_rowconfigure(1, weight=1)
            self.grid_columnconfigure(0, weight=1)

            header_frame= Frame(self, bg="darkgreen", height=80)
            header_frame.grid_propagate(False)
            table_frame= Frame(self, bg="darkgreen")

            header_frame.grid(row=0, column=0, sticky="ew")
            table_frame.grid(row=1, column=0, sticky="nsew")

            Label(header_frame,
                  text="North/South tricks: 0   East/West tricks:0",
                  bg="darkgreen",
                  fg="white"
                ).grid(row=0, column=0, pady=20)

            table_frame.grid_rowconfigure(0, weight=1)
            table_frame.grid_rowconfigure(1, weight=3)
            table_frame.grid_rowconfigure(2, weight=1)

            table_frame.grid_columnconfigure(0, weight=1)
            table_frame.grid_columnconfigure(1, weight=3)
            table_frame.grid_columnconfigure(2, weight=1)

            north_frame= Frame(table_frame, bg="red")
            west_frame= Frame(table_frame, bg="blue")
            centre_frame= Frame(table_frame, bg="darkgreen")
            east_frame= Frame(table_frame, bg="purple")
            south_frame= Frame(table_frame, bg="orange")

            north_frame.grid(row=0, column=1, sticky="nsew")
            west_frame.grid(row=1, column=0, sticky="nsew")
            centre_frame.grid(row=1, column=1, sticky="nsew")
            east_frame.grid(row=1, column=2, sticky="nsew")
            south_frame.grid(row=2, column=1, sticky="nsew")
         

    def resize_cards(card):
      card_image=Image.open(card)
      resized_card= card_image.resize((100,100))   
      global card_img 
      card_img= ImageTk.PhotoImage(resized_card)

      return card_img         

class ResultPage(Frame):
    def __init__(self, parent, controller):
            super().__init__(parent)
            Label(self, text="").pack()


    

GUI().mainloop()