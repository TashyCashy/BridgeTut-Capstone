from tkinter import *

from db import (
    get_user_id,
    get_game_dates,
    get_games_by_date,
    get_bidding_hist,
    get_game_tricks,
    get_game_cards
)


class ResultPage(Frame):

    def __init__(self, parent, controller):

        super().__init__(parent,
                         bg="#0f4d3f")

        self.controller = controller
        self.selected_game_id = None
        self.games = []

        Label(self,
              text="Game History",
              font=("Georgia", 30, "bold"),
              bg="#0f4d3f",
              fg="#C9A42C").pack(pady=(35, 10))

        Label(self,
              text="Review your previous Bridge games",
              font=("Arial", 14),
              bg="#0f4d3f",
              fg="white").pack(pady=(0, 20))

        selection_frame = Frame(self,
                                bg="#055341")

        selection_frame.pack(padx=100,
                             pady=10,
                             ipadx=40,
                             ipady=20)

        Label(selection_frame,
              text="Select a date:",
              font=("Arial", 14, "bold"),
              bg="#055341",
              fg="white").pack(pady=(5, 5))

        self.date_var = StringVar()

        self.date_menu = OptionMenu(
            selection_frame,
            self.date_var,
            "No dates available",
            command=self.date_selected
        )

        self.date_menu.config(
            width=25,
            font=("Arial", 12),
            bg="#C9A42C",
            fg="#055341",
            activebackground="#C9A42C",
            activeforeground="#055341"
        )

        self.date_menu["menu"].config(
            bg="white",
            fg="#055341",
            font=("Arial", 11)
        )

        self.date_menu.pack(pady=5)

        Label(selection_frame,
              text="Select a game:",
              font=("Arial", 14, "bold"),
              bg="#055341",
              fg="white").pack(pady=(15, 5))

        self.game_var = StringVar()

        self.game_menu = OptionMenu(
            selection_frame,
            self.game_var,
            "Select a date first",
            command=self.game_selected
        )

        self.game_menu.config(
            width=35,
            font=("Arial", 12),
            bg="#C9A42C",
            fg="#055341",
            activebackground="#C9A42C",
            activeforeground="#055341"
        )

        self.game_menu["menu"].config(
            bg="white",
            fg="#055341",
            font=("Arial", 11)
        )

        self.game_menu.pack(pady=5)

        button_frame = Frame(self,
                             bg="#0f4d3f")

        button_frame.pack(pady=15)

        Button(button_frame,
               text="Summary",
               font=("Arial", 12, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=18,
               command=self.show_summary).grid(
                   row=0,
                   column=0,
                   padx=5)

        Button(button_frame,
               text="Bidding History",
               font=("Arial", 12, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=18,
               command=self.show_bidding).grid(
                   row=0,
                   column=1,
                   padx=5)

        Button(button_frame,
               text="Tricks",
               font=("Arial", 12, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=18,
               command=self.show_tricks).grid(
                   row=0,
                   column=2,
                   padx=5)

        Button(button_frame,
               text="Cards Played",
               font=("Arial", 12, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               width=18,
               command=self.show_cards).grid(
                   row=0,
                   column=3,
                   padx=5)

        Button(self,
               text="Back to Home",
               font=("Arial", 12, "bold"),
               bg="#C9A42C",
               fg="#055341",
               relief="flat",
               command=self.controller.show_home).pack(
                   pady=8,
                   ipadx=20,
                   ipady=5)

        self.results_frame = Frame(self,
                                   bg="#055341")

        self.results_frame.pack(
            fill="both",
            expand=True,
            padx=40,
            pady=10
        )

    def load_dates(self):

        self.clear_results()

        username = self.controller.current_username

        if username is None:

            self.date_var.set("No user logged in")

            return

        user_id = get_user_id(username)

        if user_id is None:

            self.date_var.set("No user found")

            return

        dates = get_game_dates(user_id)

        menu = self.date_menu["menu"]

        menu.delete(0,
                    "end")

        if not dates:

            self.date_var.set("No games available")

            menu.add_command(
                label="No games available",
                command=lambda:
                self.date_var.set("No games available")
            )

            return

        for game_date in dates:

            date_string = str(game_date)

            menu.add_command(
                label=date_string,
                command=lambda value=date_string:
                self.date_selected(value)
            )

        first_date = str(dates[0])

        self.date_var.set(first_date)

        self.date_selected(first_date)

    def date_selected(self, selected_date):

        self.date_var.set(selected_date)

        username = self.controller.current_username

        if username is None:

            return

        user_id = get_user_id(username)

        if user_id is None:

            return

        self.games = get_games_by_date(
            user_id,
            selected_date
        )

        menu = self.game_menu["menu"]

        menu.delete(0,
                    "end")

        if not self.games:

            self.game_var.set("No games available")

            menu.add_command(
                label="No games available",
                command=lambda:
                self.game_var.set("No games available")
            )

            self.selected_game_id = None

            self.clear_results()

            return

        for game in self.games:

            game_id = game[0]
            dealer = game[1]
            seq_num = game[6]

            game_text = (
                f"Game {seq_num} "
                f"(Dealer: {dealer})"
            )

            menu.add_command(
                label=game_text,
                command=lambda value=game_text,
                gid=game_id:
                self.game_selected(
                    value,
                    gid
                )
            )

        first_game = self.games[0]

        first_game_id = first_game[0]

        first_game_text = (
            f"Game {first_game[6]} "
            f"(Dealer: {first_game[1]})"
        )

        self.game_var.set(first_game_text)

        self.selected_game_id = first_game_id

        self.show_summary()

    def game_selected(self, selected_game, game_id=None):

        self.game_var.set(selected_game)

        if game_id is not None:

            self.selected_game_id = game_id

        self.show_summary()

    def clear_results(self):

        for widget in self.results_frame.winfo_children():

            widget.destroy()

    def show_summary(self):

        self.clear_results()

        if self.selected_game_id is None:

            Label(self.results_frame,
                  text="Please select a game.",
                  font=("Arial", 16),
                  bg="#055341",
                  fg="white").pack(pady=30)

            return

        selected_game = None

        for game in self.games:

            if game[0] == self.selected_game_id:

                selected_game = game

                break

        if selected_game is None:

            return

        game_id = selected_game[0]
        dealer = selected_game[1]
        declarer = selected_game[2]
        vulnerability = selected_game[3]
        ns_score = selected_game[4]
        ew_score = selected_game[5]
        seq_num = selected_game[6]
        attempt_num = selected_game[7]

        Label(self.results_frame,
              text="Game Summary",
              font=("Georgia", 20, "bold"),
              bg="#055341",
              fg="#C9A42C").pack(pady=8)

        info_frame = Frame(self.results_frame,
                           bg="#055341")

        info_frame.pack(pady=5)

        Label(info_frame,
              text=f"Game ID: {game_id}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=0,
                  column=0,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"Game Number: {seq_num}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=0,
                  column=1,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"Attempt: {attempt_num}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=1,
                  column=0,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"Dealer: {dealer}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=1,
                  column=1,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"Declarer: {declarer}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=2,
                  column=0,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"Vulnerability: {vulnerability}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=2,
                  column=1,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"North/South Score: {ns_score}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=3,
                  column=0,
                  padx=20,
                  pady=5)

        Label(info_frame,
              text=f"East/West Score: {ew_score}",
              font=("Arial", 13),
              bg="#055341",
              fg="white",
              width=25,
              anchor="w").grid(
                  row=3,
                  column=1,
                  padx=20,
                  pady=5)

    def show_bidding(self):

        self.clear_results()

        if self.selected_game_id is None:

            Label(self.results_frame,
                  text="Please select a game.",
                  font=("Arial", 16),
                  bg="#055341",
                  fg="white").pack(pady=30)

            return

        Label(self.results_frame,
              text="Bidding History",
              font=("Georgia", 20, "bold"),
              bg="#055341",
              fg="#C9A42C").pack(pady=8)

        bids = get_bidding_hist(
            self.selected_game_id
        )

        if not bids:

            Label(self.results_frame,
                  text="No bids recorded for this game.",
                  font=("Arial", 14),
                  bg="#055341",
                  fg="white").pack(pady=20)

            return

        heading = Frame(self.results_frame,
                        bg="#055341")

        heading.pack(fill="x",
                     padx=50)

        Label(heading,
              text="Position",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=20).grid(
                  row=0,
                  column=0)

        Label(heading,
              text="Bid",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=20).grid(
                  row=0,
                  column=1)

        for bid_value, position in bids:

            row = Frame(self.results_frame,
                        bg="#055341")

            row.pack(fill="x",
                     padx=50)

            Label(row,
                  text=position,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=20).grid(
                      row=0,
                      column=0)

            Label(row,
                  text=bid_value,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=20).grid(
                      row=0,
                      column=1)

    def show_tricks(self):

        self.clear_results()

        if self.selected_game_id is None:

            Label(self.results_frame,
                  text="Please select a game.",
                  font=("Arial", 16),
                  bg="#055341",
                  fg="white").pack(pady=30)

            return

        Label(self.results_frame,
              text="Tricks",
              font=("Georgia", 20, "bold"),
              bg="#055341",
              fg="#C9A42C").pack(pady=8)

        tricks = get_game_tricks(
            self.selected_game_id
        )

        if not tricks:

            Label(self.results_frame,
                  text="No tricks recorded for this game.",
                  font=("Arial", 14),
                  bg="#055341",
                  fg="white").pack(pady=20)

            return

        heading = Frame(self.results_frame,
                        bg="#055341")

        heading.pack(fill="x",
                     padx=50)

        Label(heading,
              text="Trick",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=20).grid(
                  row=0,
                  column=0)

        Label(heading,
              text="Winner",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=20).grid(
                  row=0,
                  column=1)

        for trick_id, trick_number, winner in tricks:

            row = Frame(self.results_frame,
                        bg="#055341")

            row.pack(fill="x",
                     padx=50)

            Label(row,
                  text=trick_number,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=20).grid(
                      row=0,
                      column=0)

            Label(row,
                  text=winner,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=20).grid(
                      row=0,
                      column=1)

    def show_cards(self):

        self.clear_results()

        if self.selected_game_id is None:

            Label(self.results_frame,
                  text="Please select a game.",
                  font=("Arial", 16),
                  bg="#055341",
                  fg="white").pack(pady=30)

            return

        Label(self.results_frame,
              text="Cards Played",
              font=("Georgia", 20, "bold"),
              bg="#055341",
              fg="#C9A42C").pack(pady=8)

        cards = get_game_cards(
            self.selected_game_id
        )

        if not cards:

            Label(self.results_frame,
                  text="No cards recorded for this game.",
                  font=("Arial", 14),
                  bg="#055341",
                  fg="white").pack(pady=20)

            return

        heading = Frame(self.results_frame,
                        bg="#055341")

        heading.pack(fill="x",
                     padx=30)

        Label(heading,
              text="Trick",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=15).grid(
                  row=0,
                  column=0)

        Label(heading,
              text="Suit",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=15).grid(
                  row=0,
                  column=1)

        Label(heading,
              text="Card",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=15).grid(
                  row=0,
                  column=2)

        Label(heading,
              text="Play Order",
              font=("Arial", 13, "bold"),
              bg="#055341",
              fg="#C9A42C",
              width=15).grid(
                  row=0,
                  column=3)

        for cards_id, suit, card_rank, trick_id, play_order in cards:

            row = Frame(self.results_frame,
                        bg="#055341")

            row.pack(fill="x",
                     padx=30)

            Label(row,
                  text=trick_id,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=15).grid(
                      row=0,
                      column=0)

            Label(row,
                  text=suit,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=15).grid(
                      row=0,
                      column=1)

            Label(row,
                  text=card_rank,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=15).grid(
                      row=0,
                      column=2)

            Label(row,
                  text=play_order,
                  font=("Arial", 12),
                  bg="#055341",
                  fg="white",
                  width=15).grid(
                      row=0,
                      column=3)