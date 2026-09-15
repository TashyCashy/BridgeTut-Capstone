from tkinter import *


class TutorialPage(Frame):

    def __init__(self, parent, controller):

        super().__init__(parent,
                         bg="#0f4d3f")

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

        tut_page = self.controller.frames[TutorialGamePage]

        tut_page.set_mode(mode)

        self.controller.show_frame(TutorialGamePage)


class TutorialGamePage(Frame):

    def __init__(self, parent, controller):

        super().__init__(parent,bg="#0f4d3f")

        self.controller = controller

        self.mode = None

        self.current_card = None

        self.tutorial_bids = []
        self.tutorial_plays = []

        self.tut_bid_idx = 0
        self.tut_play_idx = 0

        self.tutorial_phase = None

        self.cards_held = None
        self.north_cards = None
        self.south_cards = None
        self.east_cards = None
        self.west_cards = None

        self.dealer = None
        self.vulnerability = None
        self.tutorial_note = ""

        self.card_images = []
        self.players = ["North", "West", "East", "South"]
        self.grid_rowconfigure(1, weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.header_display()
        self.player_table()
        self.tutorial_feedback()

    def set_mode(self, mode):

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

        self.bid_label.config(text="Bid: -")
        self.contract_label.config(text="Contract: -")

        self.feedback_label.config(text="")
        self.note_label.config(text="")

        self.claim_button.config(state="disabled")
        self.concede_button.config(state="disabled")

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

    def tutorial_feedback(self):

        feedback_frame = Frame(self, bg="#0f4d3f")

        feedback_frame.grid(row=2,
                            column=0,
                            sticky="ew",
                            padx=20,
                            pady=10)

        self.feedback_label = Label(feedback_frame,
                                    text="",
                                    font=("Arial", 12, "bold"),
                                    bg="#0f4d3f",
                                    fg="white")

        self.feedback_label.pack(pady=3)

        self.note_label = Label(feedback_frame,
                                text="",
                                font=("Arial", 11),
                                bg="#0f4d3f",
                                fg="white",
                                wraplength=1100,
                                justify="center")

        self.note_label.pack(pady=3)

    def show_feedback(self, message):

        self.feedback_label.config(text=message)

    def show_note(self, note):

        self.note_label.config(text=note)

    def player_hands(self, visible_players=None):
          """Displays player hands"""
          #Clear cards being displayed
          for frame in [self.south_frame, self.west_frame, self.north_frame, self.east_frame]:
               for widget in frame.winfo_children():
                    widget.destroy()
    
          if visible_players is None:
               visible_players = ["South", "North"]
    
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

               hand=[]
    
               for i, card_code in enumerate(hand):
                   img = self.resize_cards(f"png/{card_code}.png")
                   #add the relevant card image for the card in cardcodes
                   self.card_images.append(img)
                   if name == "South":
                       btn = Button(frame,image=img, borderwidth=0)

                       btn.config(command=lambda image=img,
                                  b=btn,
                                  n=name,
                                  s=seat_index,
                                  c=card_code:self.select_card(image,c,b))
                       btn.pack(side="left",  padx=3)
                   else:
                       card = Label(frame,
                             image=img,
                             borderwidth=0,
                             bg="#055341")
                       card.pack(side="left",padx=3)

    def select_card(self, image, card_code, button):

        self.current_card = card_code

        self.show_feedback(
            f"Selected card: {card_code}"
        )

    def resize_cards(self, image_path):

        image = PhotoImage(
            file=image_path
        )

        return image

    def claim_hand(self):

        self.show_feedback(
            "Claim selected."
        )

    def concede_hand(self):

        self.show_feedback(
            "Concede selected."
        )