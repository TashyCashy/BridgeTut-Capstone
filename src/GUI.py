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
             self.current_player=0
             self.selected_level=None
             self.selected_suit=None
             self.players=["West","North","East","South"]
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
             self.table.grid_propagate(False)
             
             #Creating correct table grid
             self.table.grid_rowconfigure(0, minsize=90)
             self.table.grid_rowconfigure(1, minsize=520)
             self.table.grid_rowconfigure(2, minsize=90)
             
             self.table.grid_columnconfigure(0, minsize=80, weight=0)
             self.table.grid_columnconfigure(1, weight=1)
             self.table.grid_columnconfigure(2, minsize=80, weight=0)

             #creating 4 frames for the 4 hand display and the centre where cards get played
             self.north_frame= Frame(self.table, bg="#126B4F")
             self.west_frame= Frame(self.table, bg="#126B4F", width=120)
             self.centre_frame= Frame(self.table, bg="darkgreen", bd=3, relief="ridge")
             self.east_frame= Frame(self.table, bg="#126B4F", width=120)
             self.south_frame= Frame(self.table, bg="#126B4F")

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
                   lbl=Label(self.centre_frame, bd=0)
                   lbl.place(relx=0.5, rely=0.5, anchor="center",
                             x=offsets[player][0], y= offsets[player][1])
                   self.trick_labels[player]=lbl
             

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
             self.players_frame.pack(fill="x", padx=20)
             
             
             for col, p in enumerate(self.players):
                  Label(self.players_frame,
                        text=p,
                        font=("Arial", 11, "bold"),
                        bg="#a9cdf0",
                        fg="white").pack(side="left", expand=True)

             #storing the bidding history
             self.bid_history= Frame(self.bidding, bg="#a9cdf0")
             self.bid_history.pack(fill="both", expand=True, padx=20, pady=5)

             for col in range(4):
                   self.bid_history.grid_columnconfigure(col, weight=1, uniform="bidcol")

             self.contract = Label(self.bidding,
                                   text="Current contract: None",
                                   font=("Arial", 14, "bold"),
                                   bg="#a9cdf0",
                                   fg="#123f35")
             self.contract.pack(pady=5)
             #calling create_bid_btns which displays the various button options
             self.create_bid_btns()
             #Added close button for testing purposes
             Button(self.bidding,
                    text="Close Bidding",
                    font=("Arial", 11, "bold"),
                    bg="#126B4F",
                    fg="white",
                    relief="flat",
                    command=lambda: self.bidding.grid_remove()).pack(pady=10)

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
             for s in suit:
                  s_btn=Button(suits,
                        text= s,
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
           for i, btn in enumerate(self.level_btns, start=1):
                 btn.config(relief="sunken" if i==level else "raised")
           print("Selected level: ", level)

     def select_suit(self, suit):
           if self.selected_level is None:
                 print("Select a level first.")
                 return
           bid= f"{self.selected_level}{suit}"
           self.make_bid(bid)
           self.selected_level=None
           for btn in self.level_btns:
                 btn.config(relief="raised")
                         
                  
     def make_bid(self, bid):
             player= self.players[self.current_player]
             self.bid_history_data.append((player, bid))
             self.display_bid(player, bid)
             if bid != "Pass":
                   self.contract.config(text= f"Current contract: {bid} by {player}")
                   self.last_bidder= self.current_player
                   self.pass_count=0
                   self.current_level= int(bid[0])
                   self.update_lvl()
             else:
                   self.pass_count = getattr(self, "pass_count", 0)+1

             self.current_player=(self.current_player+1)%4
             #added for testing purposes
             if (self.pass_count == 3 and hasattr(self, "last_bidder")) or \
                  (self.pass_count == 4):
                   self.finish_bidding()

     def update_lvl(self):
           min_lvl= getattr(self, "current_level", 0)
           for i, btn in enumerate(self.level_btns, start=1):
                 btn.config(state="disabled" if i < min_lvl else "normal")


     def display_bid(self, player, bid):
           player_idx=self.players.index(player)
           bid_num= len(self.bid_history_data)-1
           row= (bid_num // 4)+1

           Label(self.bid_history,
                 text=bid,
                 font=("Arial", 11, "bold"),
                 bg="#a9cdf0",
                 fg="#123f35",
                 width=10,
                 anchor="w").grid(row=row, column=player_idx, sticky="w", padx=10, pady=4)          

     def show_bids(self):
           bid_window= Toplevel(self)
           bid_window.title("Bidding History")
           bid_window.geometry("650x450")
           bid_window.configure(bg="#0f4d3f")

           Label(bid_window,
                 text="Bidding History",
                 font=("Georgia", 22, "bold"),
                 bg="#0f4d3f",
                 fg="#D4AF37").pack(pady=15)

           hist= Frame(bid_window,
                       bd=2,
                       bg="#a9cdf0",
                       relief="ridge")
           hist.pack(fill="both", expand=True, padx=30, pady=10)

           for column, player in enumerate(self.players):
                 Label(hist,
                       text= player,
                       font=("Arial", 11, "bold"),
                       bg="#a9cdf0",
                       fg="#123f35",
                       width=7).grid(row=0, column=column, padx=20, pady=10)

           for i, (player, bid) in enumerate(self.bid_history_data):
                 row=(i//4)+1
                 col= self.players.index(player)
                 Label(hist,
                       text= bid,
                       font=("Arial", 11, "bold"),
                       bg="#a9cdf0",
                       fg="#123f35").grid(row=row, column=col, padx=20, pady=5)

           Label(bid_window,
                  text= self.contract.cget("text"),
                  font=("Arial", 14, "bold"),
                  bg="#0f4d3f",
                  fg="white").grid(pady=5)

           Button(bid_window,
                  text="Close",
                  font=("Arial",11, "bold"),
                  bg="#D4AF37",
                  fg="#123f35",
                  relief="flat",
                  command=bid_window.destroy).pack(pady=10)
                 

     def player_hands(self):
           frame_height = 520
           card_height = 100
           n = 13
           step = (frame_height - card_height) / (n - 1)
           #south
           self.card_images=[]
           
           for i in range(13):
                img= self.resize_cards("png/C2.png")
                self.card_images.append(img)
           
                btn= Button(self.south_frame, 
                            image=img, 
                            borderwidth=0)
                btn.config(command=lambda image=img, b=btn: self.play_card(image, "South", b))
                btn.pack(side="left", padx=3)
           
           #north
           for i in range(13):
               img= self.resize_cards("png/S2.png")
               self.card_images.append(img)
           
               btn= Button(self.north_frame, 
                           image=img, 
                           borderwidth=0)
               btn.config(command=lambda image=img, b=btn: self.play_card(image, "North", b))
               btn.pack(side="left", padx=3)
           
           #east
           for i in range(13):
                img= self.resize_cards("png/D2.png")
                self.card_images.append(img)
           
                btn= Button(self.east_frame, 
                            image=img, 
                            borderwidth=0)
                btn.config(command=lambda image=img, b=btn: self.play_card(image, "East", b))
                btn.place(x=15,y=i * step)
           
           #west
           for i in range(13):
               img= self.resize_cards("png/H2.png")
               self.card_images.append(img)
           
               btn= Button(self.west_frame, 
                           image=img, 
                           borderwidth=0)
               btn.config(command=lambda image=img, b=btn: self.play_card(image,"West", b))
               btn.place(x=0,y=i * step)

     def play_card(self, image, player, btn):
        if self.bidding_phase:
              self.finish_bidding()
        if player is None:
              player= self.player[self.current_player]

        lbl= self.trick_labels[player]
        lbl.config(image=image)
        lbl.lift()

        btn.destroy()

        self.trick_count= getattr(self, "trick_count",0)+1
        if self.trick_count==4:
              self.after(1200, self.clear_trick)

     def clear_trick(self):
           for lbl in self.trick_labels.values():
                 lbl.config(image="")
                 lbl.image=None
           self.trick_count=0

     def finish_bidding(self):
           self.bidding_phase=False
           self.bidding.grid_remove()
    
     def resize_cards(self, card):
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