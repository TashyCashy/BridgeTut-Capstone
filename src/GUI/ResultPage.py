from tkinter import *
from tkinter import ttk

from db import (
    get_user_id,
    get_game_dates,
    get_games_by_date,
    get_bidding_hist,
    get_game_tricks,
    get_game_cards
)

# ---- shared palette ----
BG_DARK = "#0f4d3f"
BG_PANEL = "#123f35"
BG_CARD = "#16564a"
ACCENT = "#C9A42C"
ACCENT_DARK = "#8a701d"
TEXT_LIGHT = "#F4F1E8"
TEXT_MUTED = "#B7C9C2"
ROW_EVEN = "#123f35"
ROW_ODD = "#0d3b30"
SUIT_COLOURS = {
    "♥": "#E05252",
    "♦": "#E05252",
    "♣": TEXT_LIGHT,
    "♠": TEXT_LIGHT
}
PLAYER_NAMES = [
    "South",
    "West",
    "North",
    "East"
]

class ResultPage(Frame):
    """Page which displays users previous games"""
    def __init__(self, parent, controller):
        super().__init__(parent, bg=BG_DARK)

        self.controller = controller
        self.selected_game_id = None
        self.games = []

        self._setup_style()

        #creating the header for the results page
        Label(self,
              text="Game History",
              font=("Georgia", 30, "bold"),
              bg=BG_DARK,
              fg=ACCENT).pack(pady=(20, 2))

        Label(self,
              text="Review your previous Bridge games",
              font=("Arial", 13, "italic"),
              bg=BG_DARK,
              fg=TEXT_MUTED).pack(pady=(0, 12))

        #frame which date and game selection will be stored on
        selection_frame = Frame( self,
                                bg=BG_PANEL,
                                highlightbackground=ACCENT_DARK,
                                highlightthickness=1)

        selection_frame.pack(padx=80,pady=6,ipadx=20,ipady=12,fill="x" )

        picker_row = Frame(selection_frame,bg=BG_PANEL)
        picker_row.pack()

        #displays date games were played
        date_col = Frame(picker_row,bg=BG_PANEL)
        date_col.grid(row=0,column=0,padx=20)

        Label(date_col,
              text="DATE",
              font=("Arial", 10, "bold"),
              bg=BG_PANEL,
              fg=ACCENT).pack(anchor="w")

        self.date_var = StringVar()

        #combo box which stores the dates
        self.date_combo = ttk.Combobox(
            date_col,
            textvariable=self.date_var,
            state="readonly",
            width=18,
            style="Classy.TCombobox"
        )

        self.date_combo.pack(pady=(3, 0))

        self.date_combo.bind( "<<ComboboxSelected>>",
            lambda e: self.date_selected(self.date_var.get()))

        #displays the games played on certain dates
        game_col = Frame(picker_row,bg=BG_PANEL)
        game_col.grid(row=0,column=1,padx=20)

        Label(game_col,
              text="GAME",
              font=("Arial", 10, "bold"),
              bg=BG_PANEL,
              fg=ACCENT).pack(anchor="w")

        self.game_var = StringVar()

        #combo box which stores all the games of a certain date
        self.game_combo = ttk.Combobox(
            game_col,
            textvariable=self.game_var,
            state="readonly",
            width=28,
            style="Classy.TCombobox"
        )

        self.game_combo.pack(pady=(3, 0))

        self.game_combo.bind(
            "<<ComboboxSelected>>",
            self._on_game_combo
        )

        #Button frame which holds various game history buttons
        button_frame = Frame(self,bg=BG_DARK)
        button_frame.pack(pady=(10, 4))

        self.tab_buttons = {}

        # Direct commands instead of lambda and the button chosen
        buttons = [
            ("Summary", self.show_summary),
            ("Bidding History", self.show_bidding),
            ("Tricks", self.show_tricks),
            ("Cards Played", self.show_cards)
        ]

        for i, (label, command) in enumerate(buttons):
            btn = ttk.Button(button_frame,
                             text=label,
                             width=18,
                             style="Tab.TButton",
                             command=command)

            btn.grid(row=0,column=i,padx=3)

            self.tab_buttons[label] = btn

        #Button which returns to home page
        ttk.Button(self,
                   text="Back to Home",
                   style="Gold.TButton",
                   command=self.controller.show_home).pack(pady=(6, 4))

        #Frame which shows chosen game history
        self.results_frame = Frame(self,
                                   bg=BG_PANEL,
                                   highlightbackground=ACCENT_DARK,
                                   highlightthickness=1)

        self.results_frame.pack(fill="both",expand=True,padx=35,pady=(8, 15))

        #Inserted a scrollbar so all results can be seen
        self.results_canvas = Canvas(
            self.results_frame,
            bg=BG_PANEL,
            highlightthickness=0 )

        self.results_scrollbar = ttk.Scrollbar(
            self.results_frame,
            orient="vertical",
            command=self.results_canvas.yview)

        self.results_content = Frame(self.results_canvas,bg=BG_PANEL)

        self.results_window = self.results_canvas.create_window(
            (0, 0),
            window=self.results_content,
            anchor="nw")

        self.results_canvas.configure(yscrollcommand=self.results_scrollbar.set )

        self.results_canvas.pack(side="left",fill="both",expand=True)

        self.results_scrollbar.pack(side="right",fill="y")

        self.results_content.bind(
            "<Configure>",
            lambda e: self.results_canvas.configure(
                scrollregion=self.results_canvas.bbox("all")
            ))

        self.results_canvas.bind(
            "<Configure>",
            lambda e: self.results_canvas.itemconfigure(
                self.results_window,
                width=e.width
            )
        )

        # Mouse wheel scrolling
        self.results_canvas.bind_all(
            "<MouseWheel>",
            self._scroll_results
        )
    #AI generated styling to make the results page more aesthetic
    def _setup_style(self):

        style = ttk.Style()
        style.theme_use("clam")

        style.configure(
            "Classy.TCombobox",
            fieldbackground=BG_CARD,
            background=BG_CARD,
            foreground=TEXT_LIGHT,
            arrowcolor=ACCENT,
            bordercolor=ACCENT_DARK,
            padding=6)

        style.configure(
            "Tab.TButton",
            font=("Arial", 11, "bold"),
            background=BG_PANEL,
            foreground=TEXT_LIGHT,
            padding=8,
            borderwidth=0 )

        style.map(
            "Tab.TButton",
            background=[
                ("active", BG_CARD),
                ("pressed", ACCENT_DARK)
            ])

        style.configure(
            "Gold.TButton",
            font=("Arial", 11, "bold"),
            background=ACCENT,
            foreground=BG_DARK,
            padding=(20, 8),
            borderwidth=0)

        style.map(
            "Gold.TButton",
            background=[
                ("active", ACCENT_DARK)
            ])

        style.configure(
            "Classy.Treeview",
            background=BG_PANEL,
            fieldbackground=BG_PANEL,
            foreground=TEXT_LIGHT,
            rowheight=30,
            borderwidth=0,
            font=("Arial", 11))

        style.configure(
            "Classy.Treeview.Heading",
            background=BG_CARD,
            foreground=ACCENT,
            font=("Arial", 11, "bold"),
            borderwidth=0)

        style.map(
            "Classy.Treeview.Heading",
            background=[
                ("active", BG_CARD)
            ])

        style.map(
            "Classy.Treeview",
            background=[
                ("selected", ACCENT_DARK)],
            foreground=[
                ("selected", TEXT_LIGHT)
            ])

    def _scroll_results(self, event):
        """Always user to scroll through results"""
        self.results_canvas.yview_scroll(
            int(-1 * (event.delta / 120)),
            "units")

    def _make_treeview(self, columns):
        """Helper method which enables smooth display"""
        wrapper = Frame(self.results_content,bg=BG_PANEL)
        wrapper.pack(fill="both",expand=True,padx=25,pady=10)

        tree = ttk.Treeview(
            wrapper,
            columns=columns,
            show="headings",
            style="Classy.Treeview",
            height=12)

        for col in columns:
            tree.heading(col,text=col )
            tree.column(col,anchor="center",width=160)

        scrollbar = ttk.Scrollbar(
            wrapper,
            orient="vertical",
            command=tree.yview)

        tree.configure(yscrollcommand=scrollbar.set)
        tree.pack(side="left",fill="both",expand=True)
        scrollbar.pack(side="right",fill="y")

        tree.tag_configure("even",background=ROW_EVEN)
        tree.tag_configure("odd",background=ROW_ODD)

        return tree

    def _insert_rows(self, tree, rows):
        """Inserts rows of tables to display game history"""
        for i, row in enumerate(rows):
            tag = "even" if i % 2 == 0 else "odd"
            tree.insert("","end",values=row,tags=(tag,))

    def _empty_state(self, message):
        """Clears """
        Label(self.results_content,
              text=message,
              font=("Arial", 14, "italic"),
              bg=BG_PANEL,
              fg=TEXT_MUTED).pack( pady=50)

    def _trick_totals(self, game_id):
        """Displays amount of tricks won"""
        tricks = get_game_tricks(game_id)
        ns_tricks = sum( 1 for _, _, winner in tricks
            if winner in ("North", "South"))

        ew_tricks = sum(1 for _, _, winner in tricks
            if winner in ("East", "West"))

        return ns_tricks, ew_tricks
    
    def _section_title(self, text):
        Label(self.results_content,
              text=text,
              font=("Georgia", 20, "bold"),
              bg=BG_PANEL,
              fg=ACCENT).pack( pady=(12, 4))

    def load_dates(self):
        """Displays the dates of games played"""
        self.clear_results()
        username = self.controller.current_username

        if username is None:
            self.date_combo["values"] = ["No user logged in"]

            self.date_var.set( "No user logged in")
            return

        user_id = get_user_id(username)
        if user_id is None:
            self.date_combo["values"] = ["No user found"]
            self.date_var.set("No user found")
            return

        dates = get_game_dates(user_id)
        if not dates:
            self.date_combo["values"] = [ "No games available"]
            self.date_var.set( "No games available")
            return

        date_strings = [str(d) for d in dates ]
        self.date_combo["values"] = date_strings
        self.date_var.set(date_strings[0] )

        self.date_selected( date_strings[0])

    def date_selected(self, selected_date):
        """Allows user to choose game choice after a date is selected"""
        self.date_var.set(selected_date)

        username = self.controller.current_username

        if username is None:
            return

        user_id = get_user_id(username)

        if user_id is None:
            return

        self.games = get_games_by_date(user_id, selected_date)

        if not self.games:
            self.game_combo["values"] = ["No games available" ]
            self.game_var.set( "No games available")
            self.selected_game_id = None
            self.clear_results()
            return

        game_texts = [f"Game {g[3]} (Dealer: {g[1]})"
            for g in self.games]
        self.game_combo["values"] = game_texts
        self.game_combo.current(0)
        first_game = self.games[0]
        self.game_var.set( game_texts[0])

        self.selected_game_id = first_game[0]

        self.show_summary()

    def game_selected(self, selected_game, game_id=None):
        """Displays summary automatically when date and game selected"""
        self.game_var.set(selected_game)

        if game_id is not None:
            self.selected_game_id = game_id
        self.show_summary()

    def _on_game_combo(self, event):
        """Idk what you do"""
        idx = self.game_combo.current()
        if 0 <= idx < len(self.games):
            game = self.games[idx]
            self.game_selected(self.game_var.get(),game[0])

    def clear_results(self):
        """Clears results once a different result button clicked"""
        for widget in self.results_content.winfo_children():
            widget.destroy()
        self.results_canvas.yview_moveto(0)

    def show_summary(self):
        """Displays game results from game table"""
        self.clear_results()

        if self.selected_game_id is None:
            self._empty_state("Please select a game.")
            return
        selected_game = next((g for g in self.games
                              if g[0] == self.selected_game_id), None)
        if selected_game is None:
            return

        # game_id, dealer, declarer, seq_num, attempt_num
        (game_id,
        dealer,
        declarer,
        seq_num,
        attempt_num) = selected_game

        ns_tricks, ew_tricks = self._trick_totals(self.selected_game_id)

        if dealer is not None and 0 <= dealer < len(PLAYER_NAMES):
            dealer_name = PLAYER_NAMES[dealer]
        else:
            dealer_name = "-"

        if declarer is not None and 0 <= declarer < len(PLAYER_NAMES):
            declarer_name = PLAYER_NAMES[declarer]
        else:
            declarer_name = "-"

        self._section_title("Game Summary")
        stats = [("Game ID", game_id),
                 ("Game Number", seq_num),
                 ("Attempt", attempt_num),
                 ("Dealer", dealer_name),
                 ("Declarer", declarer_name),
                 ("North/South Tricks", ns_tricks),
                 ("East/West Tricks", ew_tricks)]

        grid = Frame(self.results_content,bg=BG_PANEL)
        grid.pack(pady=8,padx=30,fill="x")

        for i, (label, value) in enumerate(stats):
            row, col = divmod(i, 2)
            card = Frame(grid,
                         bg=BG_CARD,
                         highlightbackground=ACCENT_DARK,
                         highlightthickness=1)
            card.grid(row=row,
                     column=col,
                     padx=8,
                     pady=5,
                     sticky="ew",
                     ipady=4)
            grid.grid_columnconfigure(col,weight=1)

            Label(card,
                  text=label.upper(),
                  font=("Arial", 9, "bold"),
                  bg=BG_CARD,
                  fg=ACCENT).pack(anchor="w",padx=12,pady=(5, 0))

            Label(card,
                  text=str(value),
                  font=("Arial", 14, "bold"),
                  bg=BG_CARD,
                  fg=TEXT_LIGHT).pack(anchor="w",padx=12,pady=(0, 5))

    def show_bidding(self):
        """Displays bidding history from bidding table"""
        self.clear_results()
        if self.selected_game_id is None:
            self._empty_state("Please select a game.")
            return

        self._section_title("Bidding History")

        bids = get_bidding_hist( self.selected_game_id)

        if not bids:
            self._empty_state(
                "No bids recorded for this game.")
            return

        tree = self._make_treeview( ("Position", "Bid"))

        self._insert_rows(tree,
            [(position, bid_value)
             for bid_value, position in bids])

    def show_tricks(self):
        """Displays tricks made from tricks table"""
        self.clear_results()
        if self.selected_game_id is None:
            self._empty_state("Please select a game.")
            return

        self._section_title( "Tricks")

        tricks = get_game_tricks(self.selected_game_id)
        if not tricks:
            self._empty_state("No tricks recorded for this game.")
            return

        tree = self._make_treeview( ("Trick", "Winner"))

        self._insert_rows(tree,
            [(number, winner)
             for _, number, winner in tricks])

    def show_cards(self):
        """Displayed cards played during a game"""
        self.clear_results()
        if self.selected_game_id is None:
            self._empty_state("Please select a game.")
            return

        self._section_title( "Cards Played")
        cards = get_game_cards(self.selected_game_id)

        if not cards:
            self._empty_state("No cards recorded for this game.")
            return

        tree = self._make_treeview(("Trick", "South", "West", "North", "East", "Winner"))
        #Added so that trick winner can get displayed
        tricks = {}

        for _, suit, card_rank, trick_id, play_order in cards:
            if trick_id not in tricks:
                tricks[trick_id] = {"South": "",
                                    "West": "",
                                    "North": "",
                                    "East": "",
                                    "Winner": ""}
            card = f"{suit}{card_rank}"

            if play_order == 1:
                tricks[trick_id]["South"] = card
            elif play_order == 2:
                tricks[trick_id]["West"] = card
            elif play_order == 3:
                tricks[trick_id]["North"] = card
            elif play_order == 4:
                tricks[trick_id]["East"] = card

        rows = []
        for trick_id, trick in sorted(tricks.items()):
            rows.append((trick_id,
                         trick["South"],
                         trick["West"],
                         trick["North"],
                         trick["East"],
                         trick["Winner"]))
        self._insert_rows(tree, rows)