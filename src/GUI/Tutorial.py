from tkinter import *
from tkinter import messagebox
from PIL import Image, ImageTk
from py4j.java_gateway import JavaGateway

class TutorialPage(Frame):
    """Class which displays the tutorial version of the game"""
    def __init__(self, parent, controller):
        super().__init__(parent,      bg="#0f4d3f")
        self.controller = controller

        Label(self,
              text="Bridge Tutorial",
              font=("Georgia", 34, "bold"),
              bg="#0f4d3f",
              fg="#C9A42C").pack(pady=(70, 10))

        Label(self,
              text="Choose tutorial mode",
              font=("Arial", 16, "bold"),
              bg="#0f4d3f",
              fg="white").pack(pady=(0, 40))

        mode_frame = Frame(self,
                           bg="#055341")

        mode_frame.pack(padx=100,
                        pady=20,
                        ipadx=50,
                        ipady=40)

        #Buttons which allows user to choose desired tutorial mode
        Button(mode_frame,
               text="Bidding and playing cards tutorial",
               font=("Arial", 16, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=30,
               command=lambda: self.open_tut("Bidding")).pack(
                   pady=12,
                   ipady=10)

        Button(mode_frame,
               text="Playing cards tutorial",
               font=("Arial", 16, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=30,
               command=lambda: self.open_tut("Playing Cards")).pack(
                   pady=12,
                   ipady=10)

        Button(mode_frame,
               text="Close",
               font=("Arial", 11, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=30,
               command=self.controller.show_home).pack(
                   pady=30,
                   ipadx=20,
                   ipady=8)

    def open_tut(self, mode):
        """Sets tutorial mode"""
        tut_page = self.controller.frames[TutorialGamePage]
        tut_page.set_mode(mode)
        self.controller.show_frame(TutorialGamePage)


class TutorialGamePage(Frame):
    """Class which displays actual tutorial lessons"""
    def __init__(self, parent, controller):
        super().__init__(parent,bg="#0f4d3f")
        self.controller = controller

        #Tutorial gateway added to integrate lesson backend code to frontend
        self.gateway = JavaGateway()
        self.entry_point = self.gateway.entry_point
        self.tutorial_gateway = self.entry_point.getTutorialGateway()

        #sets tutorial states
        self.mode = None
        self.tutorial_phase = None
        self.lesson_loaded = False

        #sets card states
        self.current_card = None
        self.north_cards = []
        self.south_cards = []
        self.east_cards = []
        self.west_cards = []

        #stores tutorial progress
        self.tutorial_bids = []
        self.tutorial_plays = []
        self.tut_bid_idx = 0
        self.tut_play_idx = 0

        #lesson states stored
        self.dealer = None
        self.vulnerability = None
        self.tutorial_note = ""

        self.card_images =[]
        self.players = ["North", "West", "East", "South"]

        self.grid_rowconfigure(1, weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.header_display()
        self.player_table()
        self.tutorial_feedback()
        self.bidding_panel()

class TutorialGamePage(Frame):
    """Class which displays actual tutorial lessons"""
    def __init__(self, parent, controller):
        super().__init__(parent,bg="#0f4d3f")
        self.controller = controller

        #Tutorial gateway added to integrate lesson backend code to frontend
        self.gateway = JavaGateway()
        self.entry_point = self.gateway.entry_point
        self.tutorial_gateway = self.entry_point.getTutorialGateway()

        #sets tutorial states
        self.mode = None
        self.tutorial_phase = None
        self.lesson_loaded = False

        #sets card states
        self.current_card = None
        self.north_cards = []
        self.south_cards = []
        self.east_cards = []
        self.west_cards = []

        #stores tutorial progress
        self.tutorial_bids = []
        self.tutorial_plays = []
        self.tut_bid_idx = 0
        self.tut_play_idx = 0

        #lesson states stored
        self.dealer = None
        self.vulnerability = None
        self.tutorial_note = ""

        self.card_images =[]
        self.players = ["North", "West", "East", "South"]

        self.grid_rowconfigure(1, weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.header_display()
        self.player_table()
        self.tutorial_feedback()
        self.bidding_panel()

    def set_mode(self, mode):
        """Sets game to certain mode chosen"""
        self.mode = mode

        self.tutorial_bids = []
        self.tutorial_plays = []

        self.tut_bid_idx = 0
        self.tut_play_idx = 0

        self.cards_held = None
        self.north_cards = None
        self.south_cards = None
        self.east_cards = None
        self.west_cards = None

        self.dealer = None
        self.vulnerability = None
        self.tutorial_note = ""

        self.tutorial_phase = None
        self.lesson_loaded = False

        self.bid_label.config(text="Bid: -")
        self.contract_label.config(text="Contract: -")
        self.contract.config(text="Current contract: None")

        self.feedback_label.config(text="")
        self.note_label.config(text="")

        self.claim_button.config(state="disabled")
        self.concede_button.config(state="disabled")

        self.bid_history_data = []
        self.clear_bids()

        if mode == "Bidding":
            self.bidding.grid()
        else:
            self.bidding.grid_remove()

        self.load_tutorial()

    def header_display(self):
        header = Frame(self,
                       bg="#055341",
                       height=60)
        header.grid(row=0,
                    column=0,
                    sticky="ew")
        header.grid_propagate(False)

        self.bid_label = Label(header,
                               text="Bid: -",
                               font=("Arial", 12, "bold"),
                               bg="darkgreen",
                               fg="white")
        self.bid_label.pack(side="left",
                            padx=30)

        self.contract_label = Label(header,
                                    text="Contract: -",
                                    font=("Arial", 12, "bold"),
                                    bg="darkgreen",
                                    fg="white")
        self.contract_label.pack(side="left",
                                 padx=20)

        self.claim_button = Button(header,
                                   text="Claim",
                                   font=("Arial", 13, "bold"),
                                   bg="#055341",
                                   fg="white",
                                   relief="flat",
                                   command=self.claim_hand)
        self.claim_button.pack(side="right",
                               padx=5)

        self.concede_button = Button(header,
                                     text="Concede",
                                     font=("Arial", 13, "bold"),
                                     bg="#055341",
                                     fg="white",
                                     relief="flat",
                                     command=self.concede_hand)
        self.concede_button.pack(side="right",
                                 padx=5)

        menu_button = Menubutton(header,
                                 text="Menu",
                                 font=("Arial", 13, "bold"),
                                 bg="#055341",
                                 fg="white",
                                 relief="flat")
        menu_button.pack(side="right",
                         padx=30)

        drop_down = Menu(menu_button,
                         tearoff=0,
                         bg="white",
                         fg="#055341",
                         font=("Arial", 11))

        drop_down.add_command(label="Home",
                              command=self.controller.show_home)

        drop_down.add_command(label="Tutorials",
                              command=self.controller.show_tutorials)

        menu_button.config(menu=drop_down)

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

        for col in range(4):
              self.players_frame.grid_columnconfigure(col, weight=1, uniform="playercol")

        for col, p in enumerate(self.players):
             Label(self.players_frame,
                   text=p,
                   font=("Arial", 11, "bold"),
                   bg="#7B7D7E",
                   fg="#055341").grid(row=0, column=col, sticky="w", padx=10)

        self.bid_history= Frame(self.bidding, bg="#7B7D7E")
        self.bid_history.pack(fill="both", expand=True, padx=20, pady=5)

        for col in range(4):
              self.bid_history.grid_columnconfigure(col, weight=1, uniform="bidcol")

        self.contract = Label(self.bidding,
                              text="Current contract: None",
                              font=("Arial", 14, "bold"),
                              bg="#7B7D7E",
                              fg="#055341")
        self.contract.pack(pady=5)

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

        calls_frame = Frame(btn_frame, bg="#7B7D7E")
        calls_frame.pack(pady=(20, 5))

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
        if self.mode == "Bidding":

            seat = self.tutorial_gateway.getCurrentBidSeatIndex()

            if seat != 0:
                self.show_feedback("It is not your turn.")
                return

            accepted = self.tutorial_gateway.submitBid(bid)

            if not accepted:
                self.show_feedback("This is not an expected bid. Try again.")
                return

            tutorial_players = ["South", "West", "North", "East"]
            player = tutorial_players[seat]

            self.bid_history_data.append((player, bid))
            self.display_bid(player, bid)

            self.show_feedback(f"Correct bid: {bid}")

            if not self.tutorial_gateway.isBiddingPhase():
                self.finish_tutorial_bidding()
            else:
                self.play_computer_bid()

            return

        # user plays normally
        if bid == "Pass":
            accepted = self.entry_point.submitPass()
        elif bid == "Double":
            accepted = self.entry_point.submitDouble()
        elif bid == "Redouble":
            accepted = self.entry_point.submitRedouble()
        else:
            level = int(bid[0])
            suit_symbol = bid[1:]
            if suit_symbol not in SUIT_SYMBOL_TO_STRAIN:
                print(f"Unknown bid symbol: {suit_symbol}")
                return
            strain_name = SUIT_SYMBOL_TO_STRAIN[suit_symbol]
            accepted = self.entry_point.submitBid(level,strain_name)

        if not accepted:
            messagebox.showinfo("Illegal bid", "Bid is not legal right now")
            return

        player= self.players[self.current_player]
        self.bid_history_data.append((player, bid))
        save_bid(self.game_id, player, bid)
        self.display_bid(player, bid)

        if bid != "Pass":
              declarer = self.entry_point.getCurrentDeclarerSeatIndex()
              if declarer:
                  self.contract.config(text=f"Current contract: {bid} - Declarer: {declarer}")
              else:
                  self.contract.config(text=f"Current contract: {bid}")

        if bid not in ("Pass", "Double", "Redouble"):
              self.current_level= int(bid[0])
              self.update_lvl()

        self.current_player = self.entry_point.getCurrentSeatIndex()

        if self.entry_point.checkBiddingOver():
            if self.entry_point.isPassedOut():
                new_seat = self.entry_point.resetAfterPassedOut()
                self.current_player = new_seat
                messagebox.showinfo("Passed out", "No bids made — redealing.")
            else:
                self.finish_bidding()
                declarer = self.entry_point.getDeclarerName()
                contract_str = self.entry_point.getWinningContractString()
                self.contract.config(text=f"Final: {contract_str} - Declarer: {declarer}")

    def play_computer_bid(self):
         """Automatically makes computer bids during tutorial."""
         if self.mode != "Bidding":
            return

         if not self.tutorial_gateway.isBiddingPhase():
            self.finish_tutorial_bidding()
            return

         seat = self.tutorial_gateway.getCurrentBidSeatIndex()

         # South is the learner
         if seat == 0:
            return

         expected_bid = self.tutorial_gateway.getExpectedBid()

         if expected_bid is None:
            self.finish_tutorial_bidding()
            return

         tutorial_players = ["South", "West", "North", "East"]
         player = tutorial_players[seat]

         accepted = self.tutorial_gateway.submitBid(expected_bid)

         if accepted:
            self.bid_history_data.append((player, expected_bid))
            self.display_bid(player, expected_bid)

            self.show_feedback(f"{player} bids {expected_bid}")

            if not self.tutorial_gateway.isBiddingPhase():
                self.finish_tutorial_bidding()
            else:
                self.after(500,self.play_computer_bid)

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
        if self.mode == "Bidding":
            self.show_feedback("Undo is not available in the tutorial.")
            return

        if not self.bid_history_data:
              messagebox.showinfo("Undo", "There are no bids to undo")
              return

        if not self.entry_point.undoLastBid():
              messagebox.showinfo("Undo", "Nothing to undo on the backend")
              return

        self.bid_history_data.pop()
        self.clear_bids()

        for player, bid in self.bid_history_data:
              self.display_bid(player, bid)

        self.current_player = self.entry_point.getCurrentSeatIndex()

        last_bid = None
        for player, bid in reversed(self.bid_history_data):
              if bid != "Pass":
                    last_bid = (player, bid)
                    break

        if last_bid:
         player, bid = last_bid
         declarer = self.entry_point.getCurrentDeclarerSeatIndex()

         if declarer:
             self.contract.config(text=f"Current contract: {bid} - Declarer: {declarer}")
         else:
             self.contract.config(text=f"Current contract: {bid}")

         if bid not in ("Pass", "Double", "Redouble"):
             self.current_level = int(bid[0])
        else:
         self.contract.config( text="Current contract: None" )
         self.current_level = 0

        self.update_lvl()
        self.bidding_phase = True
        self.bidding.grid()

    def finish_tutorial_bidding(self):
        """Starts card play after tutorial bidding is complete."""
        self.tutorial_phase = "PLAYING"

        self.show_feedback("Bidding complete. Now play the cards.")

        self.contract.config(text="Contract: Bidding complete")

        self.claim_button.config(state="normal")
        self.concede_button.config(state="normal")

        self.play_computer_card()

    def clear_bids(self):
        """Removes displayed bid labels"""
        for widget in self.bid_history.winfo_children():
             widget.destroy()

    def start_tut(self, lesson_file):
        """Loads tutorial lesson."""
        success = self.tutorial_gateway.loadLessonText(lesson_file)

        if not success:
            self.show_feedback("Could not load tutorial lesson.")
            return

        self.lesson_loaded = True

        self.south_cards = list(self.tutorial_gateway.getHand(0))
        self.west_cards = list(self.tutorial_gateway.getHand(1))
        self.north_cards = list(self.tutorial_gateway.getHand(2))
        self.east_cards = list(self.tutorial_gateway.getHand(3))

        self.player_hands(["South", "North"])

        self.show_feedback("Tutorial loaded.")
        self.show_note(self.tutorial_gateway.getLessonNote())

        if self.mode == "Bidding":
            self.tutorial_phase = "BIDDING"
            self.show_feedback("Bidding tutorial started.")
            self.bidding.grid()
        else:
            self.tutorial_phase = "PLAYING"
            self.show_feedback("Playing tutorial started.")
            self.bidding.grid_remove()
            self.play_computer_card()

    def show_note(self, note):
        """Shows note wanted by user"""
        self.note_label.config(text=note)

    def player_hands(self, visible_players=None):
          """Displays player hands"""
          for frame in [self.south_frame, self.west_frame, self.north_frame, self.east_frame]:
               for widget in frame.winfo_children():
                    widget.destroy()

          if visible_players is None:
               visible_players = ["South", "North"]

          self.card_images=[]

          seat_positions = [(0, self.south_frame, "South", "left"),
          (1, self.west_frame,  "West",  "place"),
          (2, self.north_frame, "North", "left"),
          (3, self.east_frame,  "East",  "place"),
          ]

          for seat_index, frame, name, layout in seat_positions:
               if name not in visible_players:
                    continue

               if name == "South":
                   hand = self.south_cards
               elif name == "North":
                   hand = self.north_cards
               elif name == "East":
                   hand = self.east_cards
               else:
                   hand = self.west_cards

               if hand is None:
                   continue

               for i, card_code in enumerate(hand):
                   img = self.resize_cards(f"png/{card_code}.png")
                   self.card_images.append(img)

                   if name == "South":
                       btn = Button(frame,image=img, borderwidth=0)
                       btn.config(command=lambda image=img,
                                  b=btn,
                                  n=name,
                                  c=card_code:self.select_card(image,c,b))
                       btn.pack(side="left",  padx=3)
                   else:
                       card = Label(frame,
                             image=img,
                             borderwidth=0,
                             bg="#055341")
                       card.pack(side="left",padx=3)

    def select_card(self, image, card_code, button):
        """Plays a card in the tutorial"""
        if self.tutorial_phase != "PLAYING":
            self.show_feedback("You cannot play a card during bidding")
            return

        seat = self.tutorial_gateway.getCurrentTurnSeatIndex()

        if seat != 0:
            self.show_feedback("It is not your turn.")
            return

        correct = self.tutorial_gateway.playCard(seat, card_code)

        if correct:
            self.show_feedback(f"Correct play: {card_code}")
            button.destroy()
            self.update_tutorial()
        else:
            self.show_feedback("That is not the expected play. Try again.")

    def play_computer_card(self):
        """Automatically plays cards for computer players during tutorial."""
        if self.tutorial_phase != "PLAYING":
            return

        if self.tutorial_gateway.isTutorialComplete():
            self.update_tutorial()
            return

        seat = self.tutorial_gateway.getCurrentTurnSeatIndex()

        # South is the learner
        if seat == 0:
            return

        expected_card = self.tutorial_gateway.getExpectedCardCode()

        if expected_card is None:
            self.update_tutorial()
            return

        correct = self.tutorial_gateway.playCard(seat, expected_card)

        if correct:
            tutorial_players = ["South", "West", "North", "East"]
            player = tutorial_players[seat]

            self.show_feedback(f"{player} plays {expected_card}")

            self.after(500,self.play_computer_card)

    def update_tutorial(self):
        if self.tutorial_gateway.isTutorialComplete():
            outcome = self.tutorial_gateway.getFinalOutcome()
            self.show_feedback(f"Tutorial complete: {outcome}")

            self.claim_button.config(state="disabled")
            self.concede_button.config(state="disabled")

            return

        self.show_note(self.tutorial_gateway.getLessonNote())

        seat = self.tutorial_gateway.getCurrentTurnSeatIndex()

        if seat != 0:
            self.play_computer_card()

    def resize_cards(self, card):
       """Ensures cards are shaped in a way that it can be displayed by player hands and on the board"""
       card_image=Image.open(card)
       resized_card= card_image.resize((70,100))
       return ImageTk.PhotoImage(resized_card)

    def claim_hand(self):
        """Displays if user claimed"""
        success = self.tutorial_gateway.claimTricks()

        if success:
            self.show_feedback( "Claim selected.")
            self.update_tutorial()
        else:
            self.show_feedback( "Not the correct move to claim now.")

    def concede_hand(self):
        """Displays if user concedes"""
        success = self.tutorial_gateway.concedeTricks()

        if success:
            self.show_feedback( "Concede selected.")
            self.update_tutorial()
        else:
            self.show_feedback( "Not the correct move to concede now.")