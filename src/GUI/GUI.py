from tkinter import *
from tkinter import messagebox
import random
from PIL import Image, ImageTk
import os
from db import create_user, verify_user
##this is needed for the py4j gateway to be able to be used for translation
from py4j.java_gateway import JavaGateway
gateway = JavaGateway()
##where python code is going through
entry_point = gateway.entry_point
##for conversion
SUIT_SYMBOL_TO_STRAIN = {
    "♣": "CLUBS",
    "♦": "DIAMONDS",
    "♥": "HEARTS",
    "♠": "SPADES",
    "NT": "NO_TRUMP",
}

class GUI(Tk):
    def __init__(self):
        super().__init__()
        self.title("Bridge: NiteMeh Edition")
        self.geometry("1400x900")
        self.minsize(1100, 750) #fixed minimum size which accomodates the playing table.

        #Image storage and fixed image measurements
        self.card_images={}
        self.card_width=70
        self.card_height=100

        #Creating container whi
        #ch holds frames so different app pages can be displayed
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
        super().__init__(parent, background="#055341")
        self.controller= controller

        Label(self,
              text="♣♦♥♠ BRIDGE: NiteMeh Edition ♣♦♥♠",
              font=("Georgia", 32, "bold"),
              bg="#055341",
              fg="#C9A42C").pack(pady=(80,5))

        Label(self,
              text="Becoming a pro, one game at a time",
              font=("Georgia", 26, "italic"),
              fg="#C9A42C").pack(pady=(0,35))

        #Creating the login panel
        login= Frame(self, background="#123f35")
        login.pack(ipadx=50, ipady=40)

        Label(login,
              text="Username",
              font=("Arial", 11, "bold")).pack(anchor="w", padx=30)

        self.username=Entry(login, font=("Arial",13), width=35)
        self.username.pack(fill="x", padx=60, ipady=8, pady=(5,20))

        Label(login,
              text="Password",
              font=("Arial", 11, "bold")).pack(anchor="w", padx=30)

        self.password=Entry(login, font=("Arial",13),width=35, show="*")
        self.password.pack(fill="x", padx=60, ipady=8, pady=(5,30))

        Button(login,
               text="Sign up",
               font=("Arial",12,"bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               command=self.sign_up).pack(side="left", padx=10, ipady=10, ipadx=20)

        Button(login,
               text="Log in",
               font=("Arial",12,"bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               command=self.log_in).pack(side="left", padx=10, ipady=10, ipadx=20)

    def sign_up(self):
          username= self.username.get()
          password= self.password.get()
          if create_user(username, password):
                messagebox.showinfo("Success","Account added.")
                self.controller.show_frame(GamePage)
          else:
                messagebox.showinfo("Sign up failed", "Try again. Username may be already taken")



    def log_in(self):
          username= self.username.get()
          password= self.password.get()
          if verify_user(username, password):
                messagebox.showinfo("Login Successful!", "Welcome!")
                self.controller.show_frame(GamePage)
          else:
                messagebox.showinfo("Login failed","Incorrect username or password")



class HomePage(Frame):
     def __init__(self, parent, controller):
          super().__init__(parent, background="#0f4d3f")
          self.controller= controller

          #creating a menu frame, where user can choose mode
          menu= Frame(self, bg="#055341")
          menu.place(relx=0.5, rely=0.5, anchor="center")

          Label(menu,
                text="Welcome!",
                font=("Georgia", 38, "italic"),
                bg="#055341",
                fg="white").pack(pady=(0,40))

          Button(menu,
                 text="Play Game",
                 font=("Arial",13,"bold"),
                 bg="#055341",
                 width=40,
                 fg="white",
                 relief="flat", #Creates no border effect on the frame
                 command=lambda: controller.show_frame(GamePage)).pack(fill="x",pady=18, ipady=10)

          Button(menu,
                 text="Tutorial",
                 font=("Arial",13,"bold"),
                 width=40,
                 bg="#055341",
                 fg="white",
                 relief="flat",
                 command=lambda: controller.show_frame(TutorialPage)).pack(fill="x",pady=18, ipady=10)

          Button(menu,
                 text="Game History",
                 font=("Arial",13,"bold"),
                 bg="#055341",
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
             self.current_player=0
             self.selected_level=None
             ##changed to reflect the java ordering.
             self.players=["South","West","North","East"]
             self.bid_history_data=[]
             self.bidding_phase=True

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
             header= Frame(self, bg="#055341", height=60)
             header.grid(row=0, column=0, sticky="ew")
             header.grid_propagate(False) #Ensuring fixed size of the header

             Label(header,
                   text="North/South tricks: 0   East/West tricks:0",
                   font=("Arial",12,"bold"),
                   bg="darkgreen",
                   fg="white").pack(side="left", padx=30)

             Button(header,
                    text="View Bids",
                     font=("Arial",13,"bold"),
                     bg="#055341",
                     fg="white",
                     relief="flat",
                     command= self.show_bids).pack(side="right", padx=10)

             menu_button= Menubutton(header,
                                     text="Menu",
                                     font=("Arial",13,"bold"),
                                     bg="#055341",
                                     fg="white",
                                     relief="flat")
             menu_button.pack(side="right", padx=30)

             drop_down= Menu(menu_button,
                             tearoff=0,
                             bg="white",
                             fg="#055341",
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

     def player_table(self):
             """Creates player table where games take place"""
             self.table= Frame(self,
                          bg="#055341",
                          bd=5,  #setting border width of the table frame
                          relief="ridge")
             self.table.grid(row=1, column=0, sticky="nsew")
             self.table.grid_propagate(False)

             #Creating desired table grid
             self.table.grid_rowconfigure(0, minsize=90)
             self.table.grid_rowconfigure(1, minsize=520)
             self.table.grid_rowconfigure(2, minsize=90)

             self.table.grid_columnconfigure(0, minsize=80, weight=0)
             self.table.grid_columnconfigure(1, weight=1)
             self.table.grid_columnconfigure(2, minsize=80, weight=0)

             #creating 4 frames for the 4 hand display and the centre where cards get played
             self.north_frame= Frame(self.table, bg="#055341")
             self.west_frame= Frame(self.table, bg="#055341", width=120)
             self.centre_frame= Frame(self.table, bg="darkgreen", bd=3, relief="ridge")
             self.east_frame= Frame(self.table, bg="#055341", width=120)
             self.south_frame= Frame(self.table, bg="#055341")

             #Ensuring west and east frames stay fixed for card displays
             self.west_frame.grid_propagate(False)
             self.east_frame.grid_propagate(False)

             #Placing frames in desired spots
             self.north_frame.grid(row=0, column=0, columnspan=3, sticky="n")
             self.west_frame.grid(row=1, column=0, sticky="ns")
             self.centre_frame.grid(row=1, column=1, sticky="nsew", padx=15, pady=10)
             self.east_frame.grid(row=1, column=2, sticky="ns")
             self.south_frame.grid(row=2, column=0, columnspan=3, sticky="s")

             self.centre_frame.grid_rowconfigure(0, weight=1)
             self.centre_frame.grid_columnconfigure(0, weight=1)

             #Creating card area
             self.trick_labels={}
             offsets = { "North" : (0, -25),
                        "East": (25,0),
                        "South": (0,25),
                        "West": (-25,0)}
             self.trick_offsets= offsets
             for player in self.players:
                   lbl=Label(self.centre_frame, bd=0, bg="darkgreen")
                   lbl.place(relx=0.5, rely=0.5, anchor="center",
                             x=offsets[player][0], y= offsets[player][1])
                   self.trick_labels[player]=lbl


     def bidding_panel(self):
             """ Creating bidding panel where bids take place"""
             self.bidding=Frame(self.centre_frame,
                           bg="#7B7D7E",
                           bd=2,
                           relief="ridge")
             self.bidding.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)

             Label(self.bidding,
                   text="Bidding",
                   font=("Arial", 16, "bold"),
                   bg="#7B7D7E",
                   fg="white").pack(pady=(8,5))

             self.players_frame=Frame(self.bidding, bg="#7B7D7E")
             self.players_frame.pack(fill="x", padx=20)

             #Ensuring that bids display as desired
             for col in range(4):
                   self.players_frame.grid_columnconfigure(col, weight=1, uniform="playercol")

             #Adding player names to display which player made which bid
             for col, p in enumerate(self.players):
                  Label(self.players_frame,
                        text=p,
                        font=("Arial", 11, "bold"),
                        bg="#7B7D7E",
                        fg="white").grid(row=0, column=col, sticky="w", padx=10)

             #storing the bidding history
             self.bid_history= Frame(self.bidding, bg="#7B7D7E")
             self.bid_history.pack(fill="both", expand=True, padx=20, pady=5)

             for col in range(4):
                   self.bid_history.grid_columnconfigure(col, weight=1, uniform="bidcol")

             #Displays current contract of each player
             self.contract = Label(self.bidding,
                                   text="Current contract: None",
                                   font=("Arial", 14, "bold"),
                                   bg="#7B7D7E",
                                   fg="#055341")
             self.contract.pack(pady=5)

             #calling create_bid_btns which displays the various button options
             self.create_bid_btns()

             #Added close button for testing purposes
             Button(self.bidding,
                    text="Close Bidding",
                    font=("Arial", 11, "bold"),
                    bg="#055341",
                    fg="white",
                    relief="flat",
                    command=lambda: self.bidding.grid_remove()).pack(pady=10)

     def create_bid_btns(self):
             """Creates bidding buttons"""
             btn_frame= Frame(self.bidding, bg="#7B7D7E")
             btn_frame.pack(fill="x", pady=5)

             numbers= Frame(btn_frame, bg="#7B7D7E")
             numbers.pack(pady=3)

             Label(numbers,
                   text= "Level: ",
                   font=("Arial", 11, "bold"),
                   bg="#7B7D7E",
                   fg="white",
                   width=7).pack(side="left", padx=5)

             #Creating different contract level buttons
             self.level_btns=[]
             for num in range(1,8):
                   l_btn=Button(numbers,
                          text= str(num),
                          font=("Arial", 12, "bold"),
                          width=3,
                          padx=4,
                          pady=4,
                          command=lambda n=num: self.select_level(n))
                   l_btn.pack(side="left", padx=3)
                   self.level_btns.append(l_btn)

             suits= Frame(btn_frame, bg="#7B7D7E")
             suits.pack(pady=3)

             Label(suits,
                   text="Suits: ",
                   font=("Arial", 11, "bold"),
                   bg="#7B7D7E",
                   fg="white",
                   width=7).pack(side="left", padx=5)

             #Creating different suit and game logic buttons
             suit = ["♣","♦","♥","♠","NT", "Dbl"]
             self.suits_btn=[]
             for s in suit:
                  s_btn=Button(suits,
                        text= s,
                        font=("Arial", 12, "bold"),
                        width=3,
                        padx=4,
                        pady=4,
                        fg= "#055341",
                        command=lambda st=s: self.select_suit(st))
                  s_btn.pack(side="left", padx=2)
                  self.suits_btn.append(s_btn)

             #Pass button for when player does not want to make a contract
             Button(btn_frame,
                  text= "Pass",
                  font=("Arial", 12, "bold"),
                  width=5,
                  command=lambda: self.make_bid("Pass")).pack(pady=4)

     def select_level(self, level):
           """Highligts the clicked level button"""
           self.selected_level=level
           for i, btn in enumerate(self.level_btns, start=1):
                 btn.config(relief="sunken" if i==level else "raised")

     def select_suit(self, suit):
           """Ensures that a level is chosen first and highlights clicked button"""
           if self.selected_level is None:
                 print("Select a level first.")
                 return
           bid= f"{self.selected_level}{suit}"
           self.make_bid(bid)
           self.selected_level=None
           for btn in self.level_btns:
                 btn.config(relief="raised")


     def make_bid(self, bid):
             """Adds and displays bid made by user"""
             if bid == "Pass":
                 accepted = entry_point.submitPass()
             else:
                 level = int(bid[0])
                 suit_symbol = bid[1:]
                 if suit_symbol not in SUIT_SYMBOL_TO_STRAIN:
                     print(f"Unknown bid symbol: {suit_symbol}")  # catches "Dbl" for now
                     return
                 strain_name = SUIT_SYMBOL_TO_STRAIN[suit_symbol]
                 accepted = entry_point.submitBid(level,strain_name)
             if not accepted:
                 messagebox.showinfo("Illegal bid", "Bid is not legal right now")
                 return

             #Mostly backend but added for testing purposes
             player= self.players[self.current_player]
             self.bid_history_data.append((player, bid))
             self.display_bid(player, bid)
             if bid != "Pass":
                   self.contract.config(text= f"Current contract: {bid} by {player}")
                #removed joyes's logic to have java be the single source of truth
                   self.current_level= int(bid[0])
                   self.update_lvl()

             self.current_player = entry_point.getCurrentSeatIndex()

             if entry_point.checkBiddingOver():
                 if entry_point.isPassedOut():
                     new_seat = entry_point.resetAfterPassedOut()
                     self.current_player = new_seat
                     messagebox.showinfo("Passed out", "No bids made — redealing.")
                     #passed out so reset game -- later
                 else:
                     self.finish_bidding()
                     declarer = entry_point.getDeclarerName()
                     contract_str = entry_point.getWinningContractString()
                     self.contract.config(text=f"Final: {contract_str} - Declarer: {declarer}")



     def update_lvl(self):
           """Displays lower contract level buttons once clicked and a round of bidding passed"""
           min_lvl= getattr(self, "current_level", 0)
           for i, btn in enumerate(self.level_btns, start=1):
                 btn.config(state="disabled" if i < min_lvl else "normal")


     def display_bid(self, player, bid):
           """Displays and stores bids made by players"""
           player_idx=self.players.index(player)
           bid_num= len(self.bid_history_data)-1
           row= (bid_num // 4)+1

           Label(self.bid_history,
                 text=bid,
                 font=("Arial", 11, "bold"),
                 bg="#7B7D7E",
                 fg="white").grid(row=row, column=player_idx, sticky="w", padx=10, pady=4)

     def show_bids(self):
           """Displays bidding history whenever user clicks 'view bids' button"""
           bid_window= Toplevel(self)
           bid_window.title("Bidding History")
           bid_window.geometry("650x450")
           bid_window.configure(bg="#055341")

           Label(bid_window,
                 text="Bidding History",
                 font=("Georgia", 22, "bold"),
                 bg="#055341",
                 fg="#C9A42C").pack(pady=15)

           hist= Frame(bid_window,
                       bd=2,
                       bg="#7B7D7E",
                       relief="ridge")
           hist.pack(fill="both", expand=True, padx=30, pady=10)

           for column, player in enumerate(self.players):
                 Label(hist,
                       text= player,
                       font=("Arial", 11, "bold"),
                       bg="#7B7D7E",
                       fg="white",
                       width=7).grid(row=0, column=column, padx=20, pady=10)

           for i, (player, bid) in enumerate(self.bid_history_data):
                 row=(i//4)+1
                 col= self.players.index(player)
                 Label(hist,
                       text= bid,
                       font=("Arial", 11, "bold"),
                       bg="#7B7D7E",
                       fg="white").grid(row=row, column=col, padx=20, pady=5)

           Label(bid_window,
                  text= self.contract.cget("text"),
                  font=("Arial", 14, "bold"),
                  bg="#055341",
                  fg="white").pack(pady=5)

           Button(bid_window,
                  text="Close",
                  font=("Arial",11, "bold"),
                  bg="#C9A42C",
                  fg="#123f35",
                  relief="flat",
                  command=bid_window.destroy).pack(pady=10)


     def player_hands(self):
           """Displays player hands"""
           #calculation for west and east hands which ensures that all 13 cards are displayed
           frame_height = 520
           card_height = 100
           n = 13
           step = (frame_height - card_height) / (n - 1)
           self.card_images=[]
            #seats
           seat_positions = [(0, self.south_frame, "South", "left"),
           (1, self.west_frame,  "West",  "place"),
           (2, self.north_frame, "North", "left"),
           (3, self.east_frame,  "East",  "place"),
           ]
           # seat indices: SOUTH=0, WEST=1, NORTH=2, EAST=3 pretty sure this is how it's ordered on playerposition
           for seat_index, frame, name, layout in seat_positions:
               #runs the java gateway method
                hand = entry_point.getHandForSeat(seat_index)
                for i, card_code in enumerate(hand):
                    img = self.resize_cards(f"png/{card_code}.png")
                    #add the relevant card image for the card in cardcodes
                    self.card_images.append(img)

                    btn = Button(frame, image=img, borderwidth=0)
                    btn.config(command=lambda image=img, b=btn, n=name: self.play_card(image, n, b))

                    if layout == "left":
                         btn.pack(side="left", padx=3)
                    else:
                         btn.place(x=15 if name == "East" else 0, y=i * step)

     def play_card(self, image, player, btn):
        """moves card to playing board and removes it from player hand"""
        if self.bidding_phase:
              self.finish_bidding()
        if player is None:
              player= self.players[self.current_player]

        lbl= self.trick_labels[player]
        lbl.config(image=image)
        lbl.lift()

        btn.destroy()

        #board gets cleared once all 4 players have played
        self.trick_count= getattr(self, "trick_count",0)+1
        if self.trick_count==4:
              self.after(1200, self.clear_trick)

     def clear_trick(self):
           """board gets cleared once all 4 players have played """
           for lbl in self.trick_labels.values():
                 lbl.config(image="")
                 lbl.image=None
           self.trick_count=0

     def finish_bidding(self):
           """removes bidding panel once bidding has been completed"""
           self.bidding_phase=False
           self.bidding.grid_remove()

     def resize_cards(self, card):
        """Ensures cards are shaped in a way that it can be displayed by player hands and on the board"""
        card_image=Image.open(card)
        resized_card= card_image.resize((70,100))
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
