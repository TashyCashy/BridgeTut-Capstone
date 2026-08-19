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

             #Initialising game states
             self.card_images=[]
             self.bid_pos=0
             self.selected_level=None
             self.selected_suit=None
             self.players=["West","North","East","South"]
             self.bid_history_data=[]
             self.bidding=True

             #grid layout for the board
             self.grid_rowconfigure(0, weight=0)
             self.grid_rowconfigure(1, weight=1)
             self.grid_rowconfigure(2, weight=0)
             self.grid_columnconfigure(0, weight=1)

             #calling different functions of the game
             self.header_display()
             self.player_table()
             self.bidding_panel()
             self.player_hands()
     def header_display(self):
             #Creating header which has the option to go back to menu and clues
             header= Frame(self, bg="#123f35", height=60)
             header.grid(row=0, column=0, sticky="ew")
             header.grid_propagate(False)
             
             Label(header,
                   text="North/South tricks: 0   East/West tricks:0",
                   font=("Arial",12,"bold"),
                   bg="darkgreen",
                   fg="white").pack(side="left", padx=30)

             Button(header,
                    text="View Bids",
                     font=("Arial",13,"bold"),
                     bg="#126B4F",
                     fg="white",
                     relief="flat",
                     command= self.show_bids).pack(side="right", padx=10)
             
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
                                   command= lambda: self.controller.show_frame(HomePage))
             
             drop_down.add_command(label="Instructions",
                                    command= lambda: self.controller.show_instructions())
             
             drop_down.add_command(label="History",
                                   command= lambda: self.controller.show_frame(ResultPage))
             
             drop_down.add_command(label="Logout",
                                   command= lambda: self.controller.show_frame(LoginPage))
             
             menu_button.config(menu=drop_down)

             #Creating game table
     def player_table(self):
             self.table= Frame(self,
                          bg="#126B4F",
                          bd=5,
                          relief="ridge")
             self.table.grid(row=1, column=0, sticky="nsew")
             
             #Creating correct table grid
             self.table.grid_rowconfigure(0, minsize=120)
             self.table.grid_rowconfigure(1, weight=1)
             self.table.grid_rowconfigure(2, minsize=150)
             
             self.table.grid_columnconfigure(0, minsize=110)
             self.table.grid_columnconfigure(1, weight=1)
             self.table.grid_columnconfigure(2, minsize=110)

             #creating 4 frames for the 4 hand display and the centre where cards get played
             self.north_frame= Frame(self.table, bg="darkgreen")
             self.west_frame= Frame(self.table, bg="darkgreen")
             self.centre_frame= Frame(self.table, bg="darkgreen", bd=3, relief="ridge")
             self.east_frame= Frame(self.table, bg="darkgreen")
             self.south_frame= Frame(self.table, bg="darkgreen")

             #Placing frames in desired spots
             self.north_frame.grid(row=0, column=0, columnspan=3, sticky="n")
             self.west_frame.grid(row=1, column=0, sticky="ns")
             self.centre_frame.grid(row=1, column=1, sticky="nsew", padx=25, pady=20)
             self.east_frame.grid(row=1, column=2, sticky="ns")
             self.south_frame.grid(row=2, column=0, columnspan=3, sticky="s")
             
             self.centre_frame.grid_rowconfigure(0, weight=1)
             self.centre_frame.grid_columnconfigure(0, weight=1)

             #Creating card area
             self.centre_card_label=Label( self.centre_frame,
                                          text="",
                                          font=("Arial",16),
                                          bg="green",
                                          fg="white")
             self.centre_card_label.grid(row=1, column=0)
             

             #Creating bidding panel
     def bidding_panel(self):
             self.bidding=Frame(self.centre_frame,
                           bg="#a9cdf0",
                           bd=2,
                           relief="ridge")
             self.bidding.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)
             
             Label(self.bidding,
                   text="Bidding",
                   font=("Arial", 16, "bold"),
                   bg="#a9cdf0",
                   fg="white").pack(pady=(8,5))
             
             self.players_frame=Frame(self.bidding, bg="#a9cdf0")
             self.players_frame.pack(fill="x", padx=5)
             
             players=["West","North","East","South"]
             
             for i, player in enumerate(players):
                  self.players_frame.grid_columnconfigure(i, weight=1)
                  Label(self.players_frame,
                        text=player,
                        font=("Arial", 11, "bold"),
                        bg="#a9cdf0",
                        fg="white").grid(row=0, column=i, sticky="ew")

             #storing the bidding history
             self.bid_history= Frame(self.bidding, bg="#a9cdf0")
             self.bid_history.pack(fill="both", expand=True, padx=20, pady=5)

             for i in range(4):
                   self.bid_history.grid_columnconfigure(i, weight=1)
             #displaying the current contract
             self.contract=Label(self.bidding,
                   text="Current contract: ",
                   font=("Arial", 16, "bold"),
                   bg="#a9cdf0",
                   fg="#123f35").pack(pady=5)
             
             #calling create_bid_btns which displays the various button options
             self.create_bid_btns()

     def add_bid(self, bid):
             players=["West","North","East","South"]
             col= self.bid_pos%4
             row= self.bid_pos //4

             Label(self.bid_history,
                   text= bid,
                   font=("Arial", 11, "bold"),
                   bg="#a9cdf0",
                   fg="#123f35",
                   width=7).grid(row=row, column=col, padx=5, pady=3)
             if bid != "Pass":
                   self.contract.confiq(text= f"Current contract: {bid}")
             self.bid_pos+=1

     def create_bid_btns(self):
             btn_frame= Frame(self.bidding, bg="#a9cdf0")
             btn_frame.pack(fill="x", pady=5)

             numbers= Frame(btn_frame, bg="#a9cdf0")
             numbers.pack(pady=3)

             Label(numbers,
                   text= "Level: ",
                   font=("Arial", 11, "bold"),
                   bg="#a9cdf0",
                   fg="#123f35",
                   width=7).pack(side="left", padx=5)

             self.level_btns=[]
             for num in range(1,8):
                   l_btn=Button(numbers,
                          text= str(num),
                          font=("Arial", 10, "bold"),
                          command=lambda n=num: self.select_level(n))
                   l_btn.pack(side="left", padx=2)
             self.level_btns.append(l_btn)

             suits= Frame(btn_frame, bg="#a9cdf0")
             suits.pack(pady=3)

             Label(suits,
                   text="Suits: ",
                   font=("Arial", 11, "bold"),
                   bg="#a9cdf0",
                   fg="#123f35",
                   width=7).pack(side="left", padx=5)

             suit = ["♣","♦","♥","♠","NT" ]
             self.suits_btn=[]
             for s, in suit:
                  s_btn=Button(suits,
                        text= suits,
                        font=("Arial", 10, "bold"),
                        fg= "#123f35",
                        command=lambda st=s: self.select_suit(st))
                  s_btn.pack(side="left", padx=2)
                  self.suits_btn.append(s_btn)

             Button(btn_frame,
                  text= "Pass",
                  font=("Arial", 10, "bold"),
                  command=lambda: self.make_bid("Pass")).pack(pady=4)
             
     def select_level(self, level):
           self.selected_level=level
           print("Selected level: ", level)

     def select_suit(self, suit):
           if self.selected_level is None:
                 print("Select a level first.")
                 return
           bid= f"{self.selected_level}{suit}"
           self.make_bid(bid)
           self.selected_level=None
                         
                  
     def make_bid(self, bid):
             player= self.players[self.current_player]
             self.bid_history_data.append(player, bid)
             self.dispay_bid(player, bid)
             if bid != "Pass":
                   self.contract.confiq(text= f"Current contract: {bid}")
             self.current_player=(self.current_player+1)%4

     def player_hands(self):
           #south
           self.card_images=[]
           
           for i in range(13):
                img= self.resize_cards("png/C2.png")
                self.card_images.append(img)
           
                btn= Button(self.south_frame, 
                            image=img, 
                            borderwidth=0,
                            command=lambda image=img: self.play_card(image))
                btn.pack(side="left", padx=1)
           
           #north
           for i in range(13):
               img= self.resize_cards("png/S2.png")
               self.card_images.append(img)
           
               btn= Button(self.north_frame, 
                           image=img, 
                           borderwidth=0,
                           command=lambda image=img: self.play_card(image))
               btn.pack(side="left", padx=1)
           
           #east
           for i in range(13):
                img= self.resize_cards("png/D2.png")
                self.card_images.append(img)
           
                btn= Button(self.east_frame, 
                            image=img, 
                            borderwidth=0,
                            command=lambda image=img: self.play_card(image))
                btn.place(x=20, y=i*45)
           
           #west
           for i in range(13):
               img= self.resize_cards("png/H2.png")
               self.card_images.append(img)
           
               btn= Button(self.west_frame, 
                           image=img, 
                           borderwidth=0,
                           command=lambda image=img: self.play_card(image))
               btn.place(x=20, y=i*45)

     def play_card(self, image):
        self.centre_card_label.config(image= image)
        self.centre_card_label.image=image
    
     def resize_cards(self, card):
        card_image=Image.open(card)
        resized_card= card_image.resize((40,60))   
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