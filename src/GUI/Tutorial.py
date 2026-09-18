from tkinter import *
from tkinter import messagebox
from PIL import Image, ImageTk
import os
from py4j.java_gateway import JavaGateway

#gets the textfile
LESSON_DIR = os.path.dirname(os.path.abspath(__file__))
##for conversion
SUIT_SYMBOL_TO_STRAIN = {
    "♣": "CLUBS",
    "♦": "DIAMONDS",
    "♥": "HEARTS",
    "♠": "SPADES",
    "NT": "NO_TRUMP",
}
STRAIN_TO_SUIT_SYMBOL = {v: k for k, v in SUIT_SYMBOL_TO_STRAIN.items()}

class TutorialPage(Frame):
    """Class which displays the tutorial version of the game"""
    def __init__(self, parent, controller):
        super().__init__(parent, bg="#0f4d3f")
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

        mode_frame = Frame(self, bg="#055341")
        mode_frame.pack(padx=100, pady=20, ipadx=50, ipady=40)

        # Buttons which allow the user to choose the desired tutorial mode
        Button(mode_frame,
               text="Bidding and playing cards tutorial",
               font=("Arial", 16, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=30,
               command=lambda: self.open_tut("Bidding")).pack(pady=12, ipady=10)

        Button(mode_frame,
               text="Playing cards tutorial",
               font=("Arial", 16, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=30,
               command=lambda: self.open_tut("Playing Cards")).pack(pady=12, ipady=10)

        Button(mode_frame,
               text="Close",
               font=("Arial", 11, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=30,
               command=self.controller.show_home).pack(pady=30, ipadx=20, ipady=8)

    def open_tut(self, mode):
        """Sets tutorial mode"""
        tut_page = self.controller.frames[TutorialGamePage]
        tut_page.set_mode(mode)
        self.controller.show_frame(TutorialGamePage)


class TutorialGamePage(Frame):
    """Class which displays actual tutorial lessons (bidding and/or card play)."""

    def __init__(self, parent, controller):
        super().__init__(parent, bg="#0f4d3f")
        self.controller = controller

        # Tutorial gateway - integrates lesson backend code to the frontend
        self.gateway = JavaGateway()
        self.entry_point = self.gateway.entry_point
        self.tutorial_gateway = self.entry_point.getTutorialGateway()

        # tutorial state
        self.mode = None                 # "Bidding" or "Playing Cards"
        self.tutorial_phase = None       # "BIDDING" or "PLAYING"
        self.lesson_loaded = False

        # card state
        self.north_cards = []
        self.south_cards = []
        self.east_cards = []
        self.west_cards = []

        self.trick_play_count = 0
        self.bid_history_data = []

        # bidding-panel UI state
        self.selected_level = None
        self.current_level = 0

        self.card_images = []
        self.players = ["North", "West", "East", "South"]

        self.grid_rowconfigure(1, weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.header_display()
        self.player_table()
        self.tutorial_feedback()
        self.bidding_panel()

    def set_mode(self, mode):
        """Resets all tutorial state and loads the lesson for the chosen mode."""
        self.mode = mode

        self.north_cards = []
        self.south_cards = []
        self.east_cards = []
        self.west_cards = []

        self.tutorial_phase = None
        self.lesson_loaded = False

        self.selected_level = None
        self.current_level = 0

        self.bid_label.config(text="Bid: -")
        self.contract_label.config(text="Contract: -")
        self.contract.config(text="Current contract: None")

        self.feedback_label.config(text="")
        self.note_label.config(text="")

        self.claim_button.config(state="disabled")
        self.concede_button.config(state="disabled")

        self.bid_history_data = []
        self.clear_bids()

        # FIX: reset the trick display BEFORE loading the lesson - load_tutorial()
        # can immediately trigger play_computer_card(), which would otherwise get
        # wiped out by a clear_trick() that used to run after it
        self.trick_play_count = 0
        self.clear_trick()

        if mode == "Bidding":
            self.bidding.grid()
        else:
            self.bidding.grid_remove()

        self.load_tutorial()

    def load_tutorial(self):
        """Loads the tutorial lesson file for the selected mode."""
        # Replace these with the actual paths to your lesson files.
        if self.mode == "Bidding":
            lesson_file = os.path.join(LESSON_DIR, "BiddingAndPlayTutorial.txt")
        else:
            lesson_file = os.path.join(LESSON_DIR, "PlayTutorial.txt")

        self.start_tut(lesson_file)

    def start_tut(self, lesson_file):
        """Loads the tutorial lesson and initializes the board for it."""
        success = self.tutorial_gateway.loadLessonText(lesson_file)

        if not success:
            self.show_feedback("Could not load tutorial lesson.")
            return

        self.lesson_loaded = True

        self.south_cards = self.sort_cards(list(self.tutorial_gateway.getHandForSeat(0)))
        self.west_cards = self.sort_cards(list(self.tutorial_gateway.getHandForSeat(1)))
        self.north_cards = self.sort_cards(list(self.tutorial_gateway.getHandForSeat(2)))
        self.east_cards = self.sort_cards(list(self.tutorial_gateway.getHandForSeat(3)))

        self.show_note(self.tutorial_gateway.getLessonNote())

        if self.mode == "Bidding":
            #only display south's cards when bidding takes place
            self.player_hands(["South"])
            self.tutorial_phase = "BIDDING"
            self.show_feedback("Bidding tutorial started.")
            self.bidding.grid()
            self.play_computer_bid()
        else:
            self.player_hands(["South", "North"])
            self.tutorial_phase = "PLAYING"
            self.show_feedback("Playing tutorial started.")
            self.bidding.grid_remove()
            self.claim_button.config(state="normal")
            self.concede_button.config(state="normal")
            self.play_computer_card()

    def sort_cards(self, hand):
        """Sorts cards by suit and then by rank."""
        suit_order = {
            "S": 0,
            "H": 1,
            "D": 2,
            "C": 3
        }

        rank_order = {
            "A": 0,
            "K": 1,
            "Q": 2,
            "J": 3,
            "10": 4,
            "9": 5,
            "8": 6,
            "7": 7,
            "6": 8,
            "5": 9,
            "4": 10,
            "3": 11,
            "2": 12
        }

        return sorted(
            hand,
            key=lambda card: (
                suit_order.get(card[0], 99),
                rank_order.get(card[1:], 99)
            )
        )

    def header_display(self):
        header = Frame(self, bg="#055341", height=60)
        header.grid(row=0, column=0, sticky="ew")
        header.grid_propagate(False)

        self.bid_label = Label(header, text="Bid: -", font=("Arial", 12, "bold"),
                                bg="darkgreen", fg="white")
        self.bid_label.pack(side="left", padx=30)

        self.contract_label = Label(header, text="Contract: -", font=("Arial", 12, "bold"),
                                     bg="darkgreen", fg="white")
        self.contract_label.pack(side="left", padx=20)

        self.claim_button = Button(header, text="Claim", font=("Arial", 13, "bold"),
                                    bg="#055341", fg="white", relief="flat",
                                    command=self.claim_hand)
        self.claim_button.pack(side="right", padx=5)

        self.concede_button = Button(header, text="Concede", font=("Arial", 13, "bold"),
                                      bg="#055341", fg="white", relief="flat",
                                      command=self.concede_hand)
        self.concede_button.pack(side="right", padx=5)

        menu_button = Menubutton(header, text="Menu", font=("Arial", 13, "bold"),
                                  bg="#055341", fg="white", relief="flat")
        menu_button.pack(side="right", padx=30)

        drop_down = Menu(menu_button, tearoff=0, bg="white", fg="#055341", font=("Arial", 11))
        drop_down.add_command(label="Home", command=self.controller.show_home)
        drop_down.add_command(label="Tutorials", command=self.controller.show_tutorials)
        menu_button.config(menu=drop_down)

    def player_table(self):
        """Creates the table where the game takes place."""
        self.table = Frame(self, bg="#055341", bd=5, relief="ridge")
        self.table.grid(row=1, column=0, sticky="nsew")
        self.table.grid_propagate(False)

        self.table.grid_rowconfigure(0, minsize=90)
        self.table.grid_rowconfigure(1, minsize=520)
        self.table.grid_rowconfigure(2, minsize=90)

        self.table.grid_columnconfigure(0, minsize=80, weight=0)
        self.table.grid_columnconfigure(1, weight=1)
        self.table.grid_columnconfigure(2, minsize=80, weight=0)

        self.north_frame = Frame(self.table, bg="#055341")
        self.west_frame = Frame(self.table, bg="#055341", width=120)
        self.centre_frame = Frame(self.table, bg="darkgreen", bd=3, relief="ridge")
        self.east_frame = Frame(self.table, bg="#055341", width=120)
        self.south_frame = Frame(self.table, bg="#055341")

        self.west_frame.grid_propagate(False)
        self.east_frame.grid_propagate(False)

        self.north_frame.grid(row=0, column=0, columnspan=3, sticky="n")
        self.west_frame.grid(row=1, column=0, sticky="ns")
        self.centre_frame.grid(row=1, column=1, sticky="nsew", padx=15, pady=10)
        self.east_frame.grid(row=1, column=2, sticky="ns")
        self.south_frame.grid(row=2, column=0, columnspan=3, sticky="s")

        self.centre_frame.grid_rowconfigure(0, weight=1)
        self.centre_frame.grid_columnconfigure(0, weight=1)

        self.trick_labels = {}
        offsets = {"North": (0, -110),"East": (90, 0),"South": (0, 110),"West": (-90, 0)}
        self.trick_offsets = offsets
        for player in self.players:
            lbl = Label(self.centre_frame, bd=0, bg="darkgreen")
            lbl.place(relx=0.5, rely=0.5, anchor="center", x=offsets[player][0], y=offsets[player][1])
            self.trick_labels[player] = lbl

    def tutorial_feedback(self):
        """Creates the tutorial feedback and note labels."""
        feedback_frame = Frame(self.centre_frame, bg="#055341")
        feedback_frame.grid(row=1, column=0, sticky="ew", padx=10, pady=5)

        self.feedback_label = Label(feedback_frame, text="", font=("Arial", 12, "bold"),
                                     bg="#055341", fg="white")
        self.feedback_label.pack(pady=5)

        self.note_label = Label(feedback_frame, text="", font=("Arial", 11),
                                 bg="#055341", fg="#C9A42C", wraplength=500)
        self.note_label.pack(pady=5)

    def show_feedback(self, message):
        """Displays tutorial feedback."""
        self.feedback_label.config(text=message)

    def show_note(self, note):
        """Displays the current lesson note."""
        self.note_label.config(text=note)

    def bidding_panel(self):
        """Creates the bidding panel used during the bidding tutorial."""
        self.bidding = Frame(self.centre_frame, bg="#7B7D7E", bd=2, relief="ridge")
        self.bidding.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)

        Label(self.bidding, text="Bidding", font=("Arial", 16, "bold"),
              bg="#7B7D7E", fg="#055341").pack(pady=(8, 5))

        self.players_frame = Frame(self.bidding, bg="#7B7D7E")
        self.players_frame.pack(fill="x", padx=20)

        for col in range(4):
            self.players_frame.grid_columnconfigure(col, weight=1, uniform="playercol")

        for col, p in enumerate(self.players):
            Label(self.players_frame, text=p, font=("Arial", 11, "bold"),
                  bg="#7B7D7E", fg="#055341").grid(row=0, column=col, sticky="w", padx=10)

        self.bid_history = Frame(self.bidding, bg="#7B7D7E")
        self.bid_history.pack(fill="both", expand=True, padx=20, pady=5)

        for col in range(4):
            self.bid_history.grid_columnconfigure(col, weight=1, uniform="bidcol")

        self.contract = Label(self.bidding, text="Current contract: None", font=("Arial", 14, "bold"),
                               bg="#7B7D7E", fg="#055341")
        self.contract.pack(pady=5)

        self.create_bid_btns()

    def create_bid_btns(self):
        """Creates the bidding buttons."""
        btn_frame = Frame(self.bidding, bg="#7B7D7E")
        btn_frame.pack(fill="x", pady=5)

        numbers = Frame(btn_frame, bg="#7B7D7E")
        numbers.pack(pady=3)

        Label(numbers, text="Level: ", font=("Arial", 10, "bold"),
              bg="#7B7D7E", fg="#055341", width=6).pack(side="left", padx=5)

        self.level_btns = []
        for num in range(1, 8):
            l_btn = Button(numbers, text=str(num), font=("Arial", 10, "bold"),
                            width=2, height=1, padx=2, pady=2,
                            command=lambda n=num: self.select_level(n))
            l_btn.pack(side="left", padx=3, pady=3)
            self.level_btns.append(l_btn)

        suits = Frame(btn_frame, bg="#7B7D7E")
        suits.pack(pady=3)

        Label(suits, text="Suits: ", font=("Arial", 10, "bold"),
              bg="#7B7D7E", fg="#055341", width=6).pack(side="left", padx=5)

        suit = ["♣", "♦", "♥", "♠", "NT"]
        self.suits_btn = []
        for s in suit:
            suit_colour = "red" if s in ["♦", "♥"] else "black"
            s_btn = Button(suits, text=s, font=("Arial", 14, "bold"),
                            width=2, height=1, padx=2, pady=2, fg=suit_colour,
                            command=lambda st=s: self.select_suit(st))
            s_btn.pack(side="left", padx=2)
            self.suits_btn.append(s_btn)

        calls_frame = Frame(btn_frame, bg="#7B7D7E")
        calls_frame.pack(pady=(20, 5))

        Button(calls_frame, text="Pass", font=("Arial", 12, "bold"), width=8,
               command=lambda: self.make_bid("Pass")).pack(side="left", padx=4)
        Button(calls_frame, text="Double", font=("Arial", 12, "bold"), width=8,
               command=lambda: self.make_bid("Double")).pack(side="left", padx=4)
        Button(calls_frame, text="Redouble", font=("Arial", 12, "bold"), width=8,
               command=lambda: self.make_bid("Redouble")).pack(side="left", padx=4)

    def select_level(self, level):
        """Highlights the clicked level button."""
        self.selected_level = level
        for i, btn in enumerate(self.level_btns, start=1):
            btn.config(relief="sunken" if i == level else "raised")

    def select_suit(self, suit):
        """Requires a level to be chosen first, then submits the bid."""
        if self.selected_level is None:
            self.show_feedback("Select a level first.")
            return

        bid = f"{self.selected_level}{suit}"
        self.make_bid(bid)
        self.selected_level = None

        for btn in self.level_btns:
            btn.config(relief="raised")

    def make_bid(self, bid):
        if self.tutorial_phase != "BIDDING":
            self.show_feedback("Bidding is not active right now.")
            return

        seat = self.tutorial_gateway.getCurrentBidTurnSeatIndex()
        if seat != 0:
            self.show_feedback("It is not your turn.")
            return

        if bid == "Pass":
            accepted = self.tutorial_gateway.submitPass(seat)
        elif bid == "Double":
            accepted = self.tutorial_gateway.submitDouble(seat)
        elif bid == "Redouble":
            accepted = self.tutorial_gateway.submitRedouble(seat)
        else:
            level = int(bid[0])
            suit_symbol = bid[1:]
            strain_name = SUIT_SYMBOL_TO_STRAIN.get(suit_symbol)
            if strain_name is None:
                self.show_feedback(f"Unkown bid: {bid}")
                return

            accepted = self.tutorial_gateway.submitBid(seat, level, strain_name)

        if not accepted:
            self.show_feedback("This is not the expected bid. Try again.")

        tutorial_players = ["South", "West", "North", "East"]
        player = tutorial_players[seat]

        self.bid_history_data.append((player, bid))
        self.display_bid(player, bid)
        self.show_feedback(f"Correct bid: {bid}")

        if not self.tutorial_gateway.isBiddingPhase():
            self.finish_tutorial_bidding()
        else:
            self.after(300, self.play_computer_bid)

    def play_computer_bid(self):
        """Automatically makes computer bids during the bidding tutorial."""
        if self.tutorial_phase != "BIDDING":
            return

        if not self.tutorial_gateway.isBiddingPhase():
            self.finish_tutorial_bidding()
            return

        seat = self.tutorial_gateway.getCurrentBidTurnSeatIndex()

        # South is the learner - stop and wait for their input
        if seat == 0:
            return

        bid_type = self.tutorial_gateway.getExpectedBidType()
        if bid_type == "":
            self.finish_tutorial_bidding()
            return

        level = None
        strain_name = None

        if bid_type == "PASS":
            accepted = self.tutorial_gateway.submitPass(seat)
        elif bid_type == "DOUBLE":
            accepted = self.tutorial_gateway.submitDouble(seat)
        elif bid_type == "REDOUBLE":
            accepted = self.tutorial_gateway.submitRedouble(seat)
        else:  # "CONTRACT"
            level = self.tutorial_gateway.getExpectedBidLevel()
            strain_name = self.tutorial_gateway.getExpectedBidStrain()
            accepted = self.tutorial_gateway.submitBid(seat, level, strain_name)

        if accepted:
            display = self._format_bid_display(bid_type, level, strain_name)
            tutorial_players = ["South", "West", "North", "East"]
            player = tutorial_players[seat]

            self.bid_history_data.append((player, display))
            self.display_bid(player, display)
            self.show_feedback(f"{player} bids {display}")

            if not self.tutorial_gateway.isBiddingPhase():
                self.finish_tutorial_bidding()
            else:
                self.after(500, self.play_computer_bid)

    def _format_bid_display(self, bid_type, level=None, strain_name=None):
        if bid_type == "PASS":
            return "Pass"
        if bid_type == "DOUBLE":
            return "Double"
        if bid_type == "REDOUBLE":
            return "Redouble"
        if bid_type == "CONTRACT":
            symbol = STRAIN_TO_SUIT_SYMBOL.get(strain_name, strain_name)
            return f"{level}{symbol}"
        return ""

    def finish_tutorial_bidding(self):
        """Transitions from the bidding tutorial into card play."""
        self.tutorial_phase = "PLAYING"
        self.bidding.grid_remove()

        # reveal all four hands now that bidding has determined the contract
        self.player_hands(["South", "North", "East", "West"])

        self.show_feedback("Bidding complete. Now play the cards.")
        self.contract.config(text="Contract: Bidding complete")

        self.claim_button.config(state="normal")
        self.concede_button.config(state="normal")

        self.play_computer_card()

    def display_bid(self, player, bid):
        """Displays a bid in the on-screen bidding history grid."""
        player_idx = self.players.index(player)
        bid_num = len(self.bid_history_data) - 1
        row = (bid_num // 4) + 1

        Label(self.bid_history, text=bid, font=("Arial", 18, "bold"),
              bg="#99C0D3", fg="white").grid(row=row, column=player_idx, sticky="w", padx=10, pady=4)

    def clear_bids(self):
        """Removes displayed bid labels."""
        for widget in self.bid_history.winfo_children():
            widget.destroy()

    def display_played_card(self, player, card_code):
        """Shows the card a player just played in the centre of the table."""
        card_path = os.path.join(LESSON_DIR, "png", f"{card_code}.png")
        img = self.resize_cards(card_path)

        # FIX: resize_cards returns None if the file is missing - guard against
        # it here the same way player_hands already does, instead of trying to
        # set a Label's image to None
        if img is None:
            return

        self.card_images.append(img)

        lbl = self.trick_labels[player]
        lbl.config(image=img)
        lbl.image = img

    def clear_trick(self):
        """board gets cleared once all 4 players have played"""
        for lbl in self.trick_labels.values():
            lbl.config(image="")
            lbl.image = None

    def _advance_trick_display(self):
        """Displays tricks on the board and clears the trick when done"""
        self.trick_play_count += 1
        if self.trick_play_count >= 4:
            self.trick_play_count = 0
            self.after(1200, self.clear_trick)

    def player_hands(self, visible_players=None):
        """Displays player hands. South's cards are clickable; others aren't."""

        for frame in (self.south_frame, self.west_frame, self.north_frame, self.east_frame):
            for widget in frame.winfo_children():
                widget.destroy()

        if visible_players is None:
            visible_players = ["South", "North"]

        self.card_images = []

        seat_positions = [
            (self.south_frame, "South"),
            (self.west_frame, "West"),
            (self.north_frame, "North"),
            (self.east_frame, "East"),
        ]

        hands_by_name = {
            "South": self.south_cards,
            "North": self.north_cards,
            "East": self.east_cards,
            "West": self.west_cards,
        }

        for frame, name in seat_positions:
            if name not in visible_players:
                continue

            hand = hands_by_name[name]
            if not hand:
                continue

            # Sort again before displaying the hand
            hand = self.sort_cards(hand)

            for card_code in hand:
                card_path = os.path.join(LESSON_DIR, "png", f"{card_code}.png")
                img = self.resize_cards(card_path)

                if img is None:
                    continue

                self.card_images.append(img)

                if name == "South":
                    btn = Button(frame, image=img, borderwidth=0)
                    btn.config(command=lambda c=card_code, b=btn: self.select_card(c, b))
                    btn.pack(side="left", padx=3)
                else:
                    Label(frame, image=img, borderwidth=0, bg="#055341").pack(side="left", padx=3)

            print(name, "hand:", hand)

    def select_card(self, card_code, button):
        """Plays South's (the learner's) chosen card in the tutorial."""
        if self.tutorial_phase != "PLAYING":
            self.show_feedback("You cannot play a card during bidding.")
            return

        seat = self.tutorial_gateway.getCurrentTurnSeatIndex()
        if seat != 0:
            self.show_feedback("It is not your turn.")
            return

        correct = self.tutorial_gateway.playCard(seat, card_code)

        if correct:
            self.show_feedback(f"Correct play: {card_code}")
            button.destroy()
            self.display_played_card("South", card_code)
            self._advance_trick_display()
            self.update_tutorial()
        else:
            self.show_feedback("That is not the expected play. Try again.")

    def play_computer_card(self):
        """Automatically plays cards for computer players during the tutorial."""
        if self.tutorial_phase != "PLAYING":
            return

        if self.tutorial_gateway.isTutorialComplete():
            self.update_tutorial()
            return

        seat = self.tutorial_gateway.getCurrentTurnSeatIndex()

        # South is the learner - stop and wait for their input
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
            self.display_played_card(player, expected_card)
            self._advance_trick_display()

            self.after(1200, self.play_computer_card)

    def update_tutorial(self):
        """Advances tutorial state after a card is played, or ends the lesson."""

        if self.tutorial_gateway.isTutorialComplete():
            outcome = self.tutorial_gateway.getFinalOutcome()
            self.show_feedback(f"Tutorial complete: {outcome}")
            self.claim_button.config(state="disabled")
            self.concede_button.config(state="disabled")
            return

        self.show_note(
            self.tutorial_gateway.getLessonNote()
        )

        seat = self.tutorial_gateway.getCurrentTurnSeatIndex()

        if seat != 0:
            self.play_computer_card()

    def claim_hand(self):
        """Handles the learner claiming the remaining tricks."""
        success = self.tutorial_gateway.claimTricks()

        if success:
            self.show_feedback("Claim selected.")
            self.update_tutorial()
        else:
            self.show_feedback("Not the correct move to claim now.")

    def concede_hand(self):
        """Handles the learner conceding the remaining tricks."""
        success = self.tutorial_gateway.concedeTricks()

        if success:
            self.show_feedback("Concede selected.")
            self.update_tutorial()
        else:
            self.show_feedback("Not the correct move to concede now.")

    def resize_cards(self, card_path):
        """Resizes a card image for display in hands and on the board."""

        if not os.path.exists(card_path):
            print("Card image not found:", card_path)
            return None

        card_image = Image.open(card_path)
        resized_card = card_image.resize((70, 100))

        return ImageTk.PhotoImage(resized_card)