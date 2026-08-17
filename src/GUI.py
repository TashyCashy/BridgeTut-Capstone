from tkinter import *
import random
from PIL import Image, ImageTk
import os 

class GUI(Tk):

    def __init__(self):
        super().__init__()
        self.title("Bridge: NiteMeh Edition")
        self.geometry("1400x900")
        self.minsize(1100, 750)
        self.configure(bg="#10251D")

        #Image storage
        self.card_images={}
        self.card_width=70
        self.card_height=100

        #Creating container which holds frames so different app pages can be displayed
        container= Frame(self)
        container.pack(fill="both", expand=True)
        container.grid_rowconfigure(0, weight=1)
        container.grid_columnconfigure(0, weight=1)

        #Creating the different pages
        self.frames={}
        for F in (LoginPage, HomePage, GamePage, TutorialPage, ResultPage):
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
        self.controller= controller

        Label(self,
              text="♣♦♥♠ BRIDGE: NiteMeh Edition ♣♦♥♠",
              font=("Georgia", 32, "bold"),
              bg="#0f4d3f",
              fg="#D4AF37").pack(pady=(80,5))

        Label(self,
              text="Becoming a pro, one game at a time",
              font=("Georgia", 26, "italic"),
              fg="#D4AF37").pack(pady=(0,35))

        #Creating the login panel
        login= Frame(self, background="#123f35")
        login.pack(ipadx=50, ipady=40)

        Label(login,
              text="Username",
              font=("Arial", 11, "bold")).pack(anchor="w", padx=30)

        username=Entry(login, font=("Arial",13), width=35)
        username.pack(fill="x", padx=60, ipady=8, pady=(5,20))

        Label(login,
              text="Password",
              font=("Arial", 11, "bold")).pack(anchor="w", padx=30)
        
        password=Entry(login, font=("Arial",13),width=35, show="*")
        password.pack(fill="x", padx=60, ipady=8, pady=(5,30))

        Button(login,
               text="Sign in",
               font=("Arial",12,"bold"),
               bg="#D4AF37",
               fg="#123f35",
               relief="flat",
               command=lambda: controller.show_frame(HomePage)).pack(fill="x", padx=60, ipady=10)


class HomePage(Frame):
     def __init__(self, parent, controller):
          super().__init__(parent, background="#0f4d3f")
          self.controller= controller

          menu= Frame(self, bg="#0f4d3f")
          menu.place(relx=0.5, rely=0.5, anchor="center")

          Label(menu,
                text="Welcome!",
                font=("Georgia", 38, "italic"),
                bg="#0f4d3f",
                fg="white").pack(pady=(0,40))

          Button(menu,
                 text="Play Game",
                 font=("Arial",13,"bold"),
                 bg="#126B4F",
                 width=40,
                 fg="white",
                 relief="flat",
                 command=lambda: controller.show_frame(GamePage)).pack(fill="x",pady=18, ipady=10)

          Button(menu,
                 text="Tutorial",
                 font=("Arial",13,"bold"),
                 width=40,
                 bg="#126B4F",
                 fg="white",
                 relief="flat",
                 command=lambda: controller.show_frame(TutorialPage)).pack(fill="x",pady=18, ipady=10)

          Button(menu,
                 text="Game History",
                 font=("Arial",13,"bold"),
                 bg="#126B4F",
                 width=40,
                 fg="white",
                 relief="flat",
                 command=lambda: controller.show_frame(ResultPage)).pack(fill="x",pady=18, ipady=10)  


class GamePage(Frame):
     def __init__(self, parent, controller):
             super().__init__(parent, background="#0f4d3f")
             self.controller= controller

             #grid layout for the board
             self.grid_rowconfigure(0, weight=0)
             self.grid_rowconfigure(1, weight=1)
             self.grid_rowconfigure(2, weight=0)
             self.grid_columnconfigure(0, weight=1)

             #Creating header which has the option to go back to menu and clues
             header= Frame(self, bg="#123f35", height=60)
             header.grid(row=0, column=0, sticky="ew")
             header.grid_propagate(False)

             Label(header,
                   text="North/South tricks: 0   East/West tricks:0",
                   font=("Arial",12,"bold"),
                   bg="darkgreen",
                   fg="white").pack(side="left", padx=30)

             menu_button= Menubutton(header,
                                     text="Menu",
                                     font=("Arial",13,"bold"),
                                     bg="#126B4F",
                                     fg="white",
                                     relief="flat")
             menu_button.pack(side="right", padx=30)

             drop_down= Menu(menu_button,
                             tearoff=0,
                             bg="white",
                             fg="#126B4F",
                             font=("Arial",11))

             drop_down.add_command(label="Home",
                                   command= lambda: controller.show_frame(HomePage))

             drop_down.add_command(label="Hint",
                                    command= lambda: controller.show_hint)

             drop_down.add_command(label="History",
                                   command= lambda: controller.show_frame(ResultPage))

             drop_down.add_command(label="Logout",
                                   command= lambda: controller.show_frame(LoginPage))

             menu_button.config(menu=drop_down)

             #Creating game table
             table= Frame(self,
                          bg="#126B4F",
                          bd=5,
                          relief="ridge")
             table.grid(row=1, column=0, sticky="nsew")

             #Creating correct table grid
             table.grid_rowconfigure(0, minsize=70)
             table.grid_rowconfigure(1, weight=1)
             table.grid_rowconfigure(2, minsize=80)
             
             table.grid_columnconfigure(0, minsize=100)
             table.grid_columnconfigure(1, weight=1)
             table.grid_columnconfigure(2, minsize=100)

             north_frame= Frame(table, bg="darkgreen")
             west_frame= Frame(table, bg="darkgreen")
             centre_frame= Frame(table, bg="darkgreen", bd=3, relief="ridge")
             east_frame= Frame(table, bg="darkgreen")
             south_frame= Frame(table, bg="darkgreen")
             
             north_frame.grid(row=0, column=0, columnspan=3, sticky="n")
             west_frame.grid(row=1, column=0, sticky="ns")
             centre_frame.grid(row=1, column=1, sticky="nsew", padx=30, pady=20)
             east_frame.grid(row=1, column=2, sticky="ns")
             south_frame.grid(row=2, column=0, columnspan=3, sticky="s")

             centre_frame.grid_rowconfigure(0, weight=1)
             centre_frame.grid_columnconfigure(0, weight=1)

             self.centre_card_label=Label( centre_frame,
                                          text="",
                                          font=("Arial",16),
                                          bg="green",
                                          fg="white")
             self.centre_card_label.grid(row=1, column=0, padx=40, pady=20)
             #displaying cards on all sides
             #south
             self.card_images=[]
             
             for i in range(13):
                  img= self.resize_cards("png/C2.png", 40, 60)
                  self.card_images.append(img)
             
                  btn= Button(south_frame, 
                              image=img, 
                              borderwidth=0,
                              command=lambda image=img: self.play_card(image))
                  btn.pack(side="left", padx=1)
             
             #north
             for i in range(13):
                 img= self.resize_cards("png/S2.png", 40, 60)
                 self.card_images.append(img)
             
                 btn= Button(north_frame, 
                             image=img, 
                             borderwidth=0,
                             command=lambda image=img: self.play_card(image))
                 btn.pack(side="left", padx=1)
             
             #east
             for i in range(13):
                  img= self.resize_cards("png/D2.png", 40, 60)
                  self.card_images.append(img)
             
                  btn= Button(east_frame, 
                              image=img, 
                              borderwidth=0,
                              command=lambda image=img: self.play_card(image))
                  btn.pack(pady=0)
             
             #west
             for i in range(13):
                 img= self.resize_cards("png/H2.png", 40, 60)
                 self.card_images.append(img)
             
                 btn= Button(west_frame, 
                             image=img, 
                             borderwidth=0,
                             command=lambda image=img: self.play_card(image))
                 btn.pack(pady=0)

     def play_card(self, image):
        self.centre_card_label.config(image= image)
        self.centre_card_label.image=image
    
     def resize_cards(self, card, width=40, height=60):
        card_image=Image.open(card)
        resized_card= card_image.resize((width,height))   
        return ImageTk.PhotoImage(resized_card)

class TutorialPage(Frame):
     def __init__(self, parent, controller):
             super().__init__(parent)
             Label(self, text="").pack()

class ResultPage(Frame):
    def __init__(self, parent, controller):
            super().__init__(parent)
            Label(self, text="").pack()


    

GUI().mainloop()