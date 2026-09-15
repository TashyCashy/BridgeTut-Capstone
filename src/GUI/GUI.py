from tkinter import *
from tkinter import messagebox
import random
from PIL import Image, ImageTk
import os
from Tutorial import TutorialPage, TutorialGamePage
from ResultPage import ResultPage
from db import (create_user, verify_user, create_game, get_user_id)
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
        self.current_username = None

        #Creating container whi
        #ch holds frames so different app pages can be displayed
        container= Frame(self)
        container.pack(fill="both", expand=True)
        container.grid_rowconfigure(0, weight=1)
        container.grid_columnconfigure(0, weight=1)

        #Creating the different pages
        self.frames={}
        for F in (LoginPage, HomePage, GamePage, TutorialPage, TutorialGamePage, ResultPage):
            frame= F(container, self)
            self.frames[F] = frame
            frame.grid(row=0, column=0, sticky="nsew")
        self.show_frame(LoginPage)

    def show_frame(self, page_class):
        """Allows different pages to be displayed"""
        frame= self.frames[page_class]
        frame.tkraise()

        if page_class == ResultPage:
             frame.load_dates()

    def show_home(self):
         self.show_frame(HomePage)

    def show_tutorials(self):
         self.show_frame(TutorialPage)
         
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
                self.controller.current_username = username
                messagebox.showinfo("Success","Account added.")
                self.controller.show_frame(HomePage)
          else:
                messagebox.showinfo("Sign up failed", "Try again. Username may be already taken")



    def log_in(self):
          username= self.username.get()
          password= self.password.get()
          if verify_user(username, password):
                self.controller.current_username = username
                messagebox.showinfo("Login Successful!", "Welcome!")
                self.controller.show_frame(HomePage)
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
                 command=lambda: controller.frames[GamePage].start_game()).pack(fill="x",pady=18, ipady=10)

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
             self.current_level=0
             self.selected_level=None
             self.dummy = None

             ##changed to reflect the java ordering.
             self.players=["South","West","North","East"]
             self.bid_history_data=[]
             self.undo_hist=[]
             self.bidding_phase=True
             self.game_id = None

             #variables to display in the header
             self.ns_tricks = 0
             self.ew_tricks = 0
             self.declarer = None
             self.trick_count = 0

             #grid layout for the board
             self.grid_rowconfigure(0, weight=0)
             self.grid_rowconfigure(1, weight=1)
             self.grid_rowconfigure(2, weight=0)
             self.grid_columnconfigure(0, weight=1)

             #calling different functions of the game
             self.header_display()
             self.player_table()
             self.bidding_panel()
             # adding a field to hold the play gateway once bidding ends
             self.play_gateway = None

             self.player_hands()

     def start_game(self):
          if self.controller.current_username is None:
               messagebox.showerror("Error", "Please log in first.")
               return
          user_id = get_user_id(self.controller.current_username)

          if user_id is None:
               messagebox.showerror("Error", "Could not find user.")
               return

          dealer = entry_point.getCurrentSeatIndex()
          self.game_id = create_game(user_id, dealer)

          if self.game_id is None:
               messagebox.showerror("Error", "Could not create game.")
               return

          #resetting game states
          self.bidding_phase = True
          self.current_level = 0
          self.selected_level = None
          self.current_player = entry_point.getCurrentSeatIndex()
          self.trick_count = 0
          self.ns_tricks = 0
          self.ew_tricks = 0

          self.dummy = None
          self.declarer = None
          self.play_gateway = None

          self.bid_history_data = []
          self.undo_hist = []

          # Reset header
          self.trick_label.config(text="North/South tricks: 0   East/West tricks: 0")
          self.declarer_label.config(text="Declarer: -")
          self.bid_label.config(text="Bid: -")

          # Claim and concede are not available during bidding
          self.claim_button.config(state="disabled")
          self.concede_button.config(state="disabled")

          self.clear_bids()

          self.contract.config( text="Current contract: None" )
          # Reset level buttons
          for btn in self.level_btns:
               btn.config( relief="raised", state="normal" )
          # Show the GamePage
          self.controller.show_frame(GamePage)
          # Show bidding panel
          self.bidding.grid( row=0, column=0, sticky="nsew", padx=10, pady=10 )
          self.bidding.lift()

          print("Game created:", self.game_id)
          print("Bidding phase:", self.bidding_phase)


     def header_display(self):
             #Creating header which has the option to go back to menu and clues
             header= Frame(self, bg="#055341", height=60)
             header.grid(row=0, column=0, sticky="ew")
             header.grid_propagate(False) #Ensuring fixed size of the header

             self.trick_label =Label(header,
                   text="North/South tricks: 0   East/West tricks:0",
                   font=("Arial",12,"bold"),
                   bg="darkgreen",
                   fg="white")
             self.trick_label.pack(side="left", padx=30)

             self.declarer_label=Label(header,
                   text="Declarer: -",
                   font=("Arial",12,"bold"),
                   bg="darkgreen",
                   fg="white")
             self.declarer_label.pack(side="left", padx=20)

             self.bid_label = Label(header,
                                    text="Bid: -",
                                    font=("Arial", 12, "bold"),
                                    bg="darkgreen",
                                    fg="white")
             self.bid_label.pack(side="left", padx=20)

             Button(header,
                    text="View Bids",
                     font=("Arial",13,"bold"),
                     bg="#055341",
                     fg="white",
                     relief="flat",
                     command= self.show_bids).pack(side="right", padx=10)
             
             self.claim_button=Button(header,
                    text="Claim",
                     font=("Arial",13,"bold"),
                     bg="#055341",
                     fg="white",
                     relief="flat",
                     command= self.claim_hand)
             self.claim_button.pack(side="right", padx=5)
             
             self.concede_button=Button(header,
                    text="Concede",
                     font=("Arial",13,"bold"),
                     bg="#055341",
                     fg="white",
                     relief="flat",
                     command= self.concede_hand)
             self.concede_button.pack(side="right", padx=5)

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

     def claim_hand(self):
          """Allows player to claim remaining tricks"""

          if self.bidding_phase:
               messagebox.showinfo("Claim", "You cannot claim during bidding.")
               return

          if self.play_gateway is None:
               return

          current_seat = self.play_gateway.getCurrentTurnSeatIndex()
          confirm = messagebox.askyesno("Claim", "Are you want to claim remaining tricks?")

          if not confirm:
               return
          
          #need to add claim backend code

          self.claim_button.config(state="disabled")
          self.concede_button.config(state="disabled")

     def concede_hand(self):
          if self.bidding_phase:
               messagebox.showinfo("Concede", "You cannot concede during bidding.")
               return
          
          if self.play_gateway is None:
               return
          
          current_seat = self.play_gateway.getCurrentTurnSeatIndex()
          confirm = messagebox.askyesno("Concede", "Are you want to concede the hand?")
          
          if not confirm:
               return
          
          #need to add concede backend code
          
          self.claim_button.config(state="disabled")
          self.concede_button.config(state="disabled")
          


     def update_trick_score(self):
          self.ns_tricks = self.play_gateway.getNorthSouthTricks()
          self.ew_tricks = self.play_gateway.getEastWestTricks()
          self.trick_label.config(text= f"North/South tricks: {self.ns_tricks}  "
                                         f"East/West tricks: {self.ew_tricks}")

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
                   fg="#055341").pack(pady=(8,5))

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
                        fg="#055341").grid(row=0, column=col, sticky="w", padx=10)

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


     def create_bid_btns(self):
             """Creates bidding buttons"""
             btn_frame= Frame(self.bidding, bg="#7B7D7E")
             btn_frame.pack(fill="x", pady=5)

             numbers= Frame(btn_frame, bg="#7B7D7E")
             numbers.pack(pady=3)

             Label(numbers,
                   text= "Level: ",
                   font=("Arial", 10, "bold"),
                   bg="#7B7D7E",
                   fg="#055341",
                   width=6).pack(side="left", padx=5)

             #Creating different contract level buttons
             self.level_btns=[]
             for num in range(1,8):
                   l_btn=Button(numbers,
                          text= str(num),
                          font=("Arial", 10, "bold"),
                          width=2,
                          height=1,
                          padx=2,
                          pady=2,
                          command=lambda n=num: self.select_level(n))
                   l_btn.pack(side="left", padx=3, pady=3)
                   self.level_btns.append(l_btn)

             suits= Frame(btn_frame, bg="#7B7D7E")
             suits.pack(pady=3)

             Label(suits,
                   text="Suits: ",
                   font=("Arial", 10, "bold"),
                   bg="#7B7D7E",
                   fg="#055341",
                   width=6).pack(side="left", padx=5)

             #Creating different suit and game logic buttons
             suit = ["♣","♦","♥","♠","NT"]
             self.suits_btn=[]
             for s in suit:
                  if s in ["♦", "♥"]:
                        suit_colour="red"
                  else:
                        suit_colour= "black"
                  s_btn=Button(suits,
                        text= s,
                        font=("Arial", 14, "bold"),
                        width=2,
                        height=1,
                        padx=2,
                        pady=2,
                        fg= suit_colour,
                        command=lambda st=s: self.select_suit(st))
                  s_btn.pack(side="left", padx=2)
                  self.suits_btn.append(s_btn)

             #creating a frame where pass, double and redouble button will be displayed
             calls_frame = Frame(btn_frame, bg="#7B7D7E")
             calls_frame.pack(pady=(20, 5))

             #Pass button for when player does not want to make a contract
             Button(calls_frame,
                  text= "Pass",
                  font=("Arial", 12, "bold"),
                  width=8,
                  command=lambda: self.make_bid("Pass")).pack(side="left", padx=4)

             Button(calls_frame,
                  text= "Double",
                  font=("Arial", 12, "bold"),
                  width=8,
                  command=lambda: self.make_bid("Double")).pack(side="left", padx=4)

             Button(calls_frame,
                  text= "Redouble",
                  font=("Arial", 12, "bold"),
                  width=8,
                  command=lambda: self.make_bid("Redouble")).pack(side="left", padx=4)

             Button(btn_frame,
                  text= "Undo",
                  font=("Arial", 11, "bold"),
                  width=6,
                  command=self.undo_bid).pack(side="right", anchor="se",padx=8,pady=8)

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
             elif bid == "Double":
                 accepted = entry_point.submitDouble()
             elif bid == "Redouble":
                 accepted = entry_point.submitRedouble()
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
                   declarer = entry_point.getCurrentDeclarerSeatIndex()
                   if declarer:
                       self.contract.config(text=f"Current contract: {bid} - Declarer: {declarer}")
                   else:
                       self.contract.config(text=f"Current contract: {bid}")
                #removed joyes's logic to have java be the single source of truth
             if bid not in ("Pass", "Double", "Redouble"):
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
                 font=("Arial", 18, "bold"),
                 bg="#99C0D3",
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
                       bg="#99C0D3",
                       relief="ridge")
           hist.pack(fill="both", expand=True, padx=30, pady=10)

           for column, player in enumerate(self.players):
                 Label(hist,
                       text= player,
                       font=("Arial", 11, "bold"),
                       bg="#99C0D3",
                       fg="white",
                       width=7).grid(row=0, column=column, padx=20, pady=10)

           for i, (player, bid) in enumerate(self.bid_history_data):
                 row=(i//4)+1
                 col= self.players.index(player)
                 Label(hist,
                       text= bid,
                       font=("Arial", 18, "bold"),
                       bg="#99C0D3",
                       fg="white").grid(row=row, column=col, padx=20, pady=5)

           Label(bid_window,
                  text= self.contract.cget("text"),
                  font=("Arial", 18, "bold"),
                  bg="#055341",
                  fg="white").pack(pady=5)

           Button(bid_window,
                  text="Close",
                  font=("Arial",11, "bold"),
                  bg="#C9A42C",
                  fg="#123f35",
                  relief="flat",
                  command=bid_window.destroy).pack(pady=10)

     def undo_bid(self):
           #Undo button which removes previous bids made
           if not self.undo_hist:
                 messagebox.showinfo("Undo", "There are no bids to undo")
                 return

           self.bid_history_data.pop()
           self.clear_bids()

           self.clear_bids()
           for player, bid in self.bid_history_data:
                 self.display_bid(player, bid)

           last_bid = None
           for player, bid in reversed(self.bid_history_data):
                 if bid != "Pass":
                       last_bid = (player, bid)
                       break
           if last_bid:
            player, bid = last_bid
            self.contract.config( text=f"Current contract: {bid} by {player}" )
            self.current_level = int(bid[0])
           else:
            self.contract.config( text="Current contract: None" )
            self.current_level = 0

           self.update_lvl()
           self.bidding_phase = True
           self.bidding.grid()

     def clear_bids(self):
          """Removes displayed bid labels"""
          for widget in self.bid_history.winfo_children():
               widget.destroy()

     def update_visible_hands(self):
          current_player_name = self.players[self.current_player]
          visible_players = [self.dummy, current_player_name]

          visible_players = list (dict.fromkeys(visible_players))
          self.player_hands(visible_players)

     def player_hands(self, visible_players=None):
           """Displays player hands"""
           #Clear cards being displayed
           for frame in [self.south_frame, self.west_frame, self.north_frame, self.east_frame]:
                for widget in frame.winfo_children():
                     widget.destroy()

           if visible_players is None:
                visible_players = ["South"]

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
                #only displaying player and dummys hands
                if name not in visible_players:
                     continue
               #runs the java gateway method
                if self.play_gateway is not None:
                  hand = self.play_gateway.getRemainingHandForSeat(seat_index)
                else:
                     hand = entry_point.getHandForSeat(seat_index)

                for i, card_code in enumerate(hand):
                    img = self.resize_cards(f"png/{card_code}.png")
                    #add the relevant card image for the card in cardcodes
                    self.card_images.append(img)

                    btn = Button(frame, image=img, borderwidth=0)
                    btn.config(command=lambda image=img, b=btn, n=name, s=seat_index, c=card_code: self.play_card(image, n, s, c, b))

                    if layout == "left":
                         btn.pack(side="left", padx=3)
                    else:
                         btn.place(x=15 if name == "East" else 0, y=i * step)

     def play_card(self, image, player, seat_index, card_code, btn):
        """moves card to playing board and removes it from player hand"""
        if self.bidding_phase:
              messagebox.showinfo("Bidding", "The bidding is not finished yet.")
              return

        # ensuring that it is this seat's turn in Java
        current_turn = self.play_gateway.getCurrentTurnSeatIndex()
        if seat_index != current_turn:
             messagebox.showinfo("Out of turn", f"It is currently {self.players[current_turn]}'s turn to play")
             return

        accepted = self.play_gateway.playCard(seat_index, card_code)
        if not accepted:
             messagebox.showinfo("Illegal play", "That card cannot be played right now")
             return

        lbl = self.trick_labels[player]
        lbl.config(image = image)
        lbl.image = image # keep a reference so Tkinter does not garbage collect it

        btn.destroy()

        # update current player index directly from Java backend
        self.current_player = self.play_gateway.getCurrentTurnSeatIndex()
        self.update_visible_hands()

        #checks if trick has been completed
        completed_tricks = self.play_gateway.getCompletedTricksCount()

        if completed_tricks > self.trick_count:
             self.trick_count = completed_tricks
             self.update_trick_score()
             #board gets cleared once all 4 players have played
             self.after(1200, self.clear_trick)

        if self.play_gateway.isHandComplete():
             messagebox.showinfo("Hand complete", "All 13 tricks played")
        # game history goes here later

     def clear_trick(self):
           """board gets cleared once all 4 players have played """
           for lbl in self.trick_labels.values():
                 lbl.config(image="")
                 lbl.image=None

     def finish_bidding(self):
           """removes bidding panel once bidding has been completed"""
           self.bidding_phase=False
           self.bidding.grid_remove()

           declarer = entry_point.getDeclarerName()
           winningBid = entry_point.getWinningContractString()
           declarer_idx = self.players.index(declarer)

           dummy_idx = (declarer_idx + 2) % 4
           self.dummy = self.players[dummy_idx]

           self.declarer_label.config(text=f"Declarer: {declarer}")
           self.bid_label.config( text=f"Bid: {winningBid}" )

           # Reset trick scores for the playing phase
           self.trick_count = 0
           self.ns_tricks = 0
           self.ew_tricks = 0

           self.trick_label.config(text="North/South tricks: 0   East/West tricks: 0")

           self.play_gateway = entry_point.startPlayPhase()
           self.current_player = (self.play_gateway.getCurrentTurnSeatIndex())

           self.update_visible_hands()
           self.claim_button.config(state="normal")
           self.concede_button.config(state="normal")

     def resize_cards(self, card):
        """Ensures cards are shaped in a way that it can be displayed by player hands and on the board"""
        card_image=Image.open(card)
        resized_card= card_image.resize((70,100))
        return ImageTk.PhotoImage(resized_card)

GUI().mainloop()
