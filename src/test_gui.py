import unittest
import tkinter as tk
from unittest.mock import patch

from GUI import GUI, GamePage


class TestBridgeGUI(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        """
        Create the Tkinter application once.
        """
        cls.root = GUI()
        cls.root.withdraw()

    @classmethod
    def tearDownClass(cls):
        """
        Close the application after all tests.
        """
        cls.root.destroy()

    def setUp(self):
        """
        Create a fresh GamePage before every test.
        """

        old_game = self.root.frames[GamePage]
        parent = old_game.master

        old_game.destroy()

        self.game = GamePage(parent, self.root)
        self.game.grid(row=0, column=0, sticky="nsew")

        self.root.frames[GamePage] = self.game

        self.root.update()

    # ---------------------------------------------------------
    # TEST 1: Game page exists
    # ---------------------------------------------------------

    def test_game_page_exists(self):
        """
        Check that the GamePage was successfully created.
        """
        self.assertIsNotNone(self.game)

    # ---------------------------------------------------------
    # TEST 2: Four player frames exist
    # ---------------------------------------------------------

    def test_four_player_frames_exist(self):
        """
        Check that North, South, East and West hand areas exist.
        """
        self.assertIsNotNone(self.game.north_frame)
        self.assertIsNotNone(self.game.south_frame)
        self.assertIsNotNone(self.game.east_frame)
        self.assertIsNotNone(self.game.west_frame)

    # ---------------------------------------------------------
    # TEST 3: Each player initially has 13 cards
    # ---------------------------------------------------------

    def test_each_player_has_13_cards(self):
        """
        Check that 13 card buttons are displayed for
        each player.
        """

        north_cards = self.game.north_frame.winfo_children()
        south_cards = self.game.south_frame.winfo_children()
        east_cards = self.game.east_frame.winfo_children()
        west_cards = self.game.west_frame.winfo_children()

        self.assertEqual(len(north_cards), 13)
        self.assertEqual(len(south_cards), 13)
        self.assertEqual(len(east_cards), 13)
        self.assertEqual(len(west_cards), 13)

    # ---------------------------------------------------------
    # TEST 4: There are 52 cards displayed
    # ---------------------------------------------------------

    def test_total_number_of_cards(self):
        """
        Check that the GUI displays 52 cards in total.
        """

        north = len(self.game.north_frame.winfo_children())
        south = len(self.game.south_frame.winfo_children())
        east = len(self.game.east_frame.winfo_children())
        west = len(self.game.west_frame.winfo_children())

        total_cards = north + south + east + west

        self.assertEqual(total_cards, 52)

    # ---------------------------------------------------------
    # TEST 5: Clicking a South card removes it
    # ---------------------------------------------------------

    @patch.object(GamePage, "finish_bidding")
    def test_click_south_card_removes_card(self, mock_finish_bidding):
        """
        Check that clicking a South card removes it
        from the South hand.
        """

        south_cards_before = len(
            self.game.south_frame.winfo_children()
        )

        # Get the first South card
        card = self.game.south_frame.winfo_children()[0]

        # Simulate clicking the card
        card.invoke()

        # Process Tkinter events
        self.root.update()

        south_cards_after = len(
            self.game.south_frame.winfo_children()
        )

        self.assertEqual(
            south_cards_after,
            south_cards_before - 1
        )

    # ---------------------------------------------------------
    # TEST 6: Clicking a North card removes it
    # ---------------------------------------------------------

    @patch.object(GamePage, "finish_bidding")
    def test_click_north_card_removes_card(self, mock_finish_bidding):
        """
        Check that clicking a North card removes it
        from the North hand.
        """

        north_cards_before = len(
            self.game.north_frame.winfo_children()
        )

        card = self.game.north_frame.winfo_children()[0]

        card.invoke()

        self.root.update()

        north_cards_after = len(
            self.game.north_frame.winfo_children()
        )

        self.assertEqual(
            north_cards_after,
            north_cards_before - 1
        )

    # ---------------------------------------------------------
    # TEST 7: Clicking an East card removes it
    # ---------------------------------------------------------

    @patch.object(GamePage, "finish_bidding")
    def test_click_east_card_removes_card(self, mock_finish_bidding):
        """
        Check that clicking an East card removes it
        from the East hand.
        """

        east_cards_before = len(
            self.game.east_frame.winfo_children()
        )

        card = self.game.east_frame.winfo_children()[0]

        card.invoke()

        self.root.update()

        east_cards_after = len(
            self.game.east_frame.winfo_children()
        )

        self.assertEqual(
            east_cards_after,
            east_cards_before - 1
        )

    # ---------------------------------------------------------
    # TEST 8: Clicking a West card removes it
    # ---------------------------------------------------------

    @patch.object(GamePage, "finish_bidding")
    def test_click_west_card_removes_card(self, mock_finish_bidding):
        """
        Check that clicking a West card removes it
        from the West hand.
        """

        west_cards_before = len(
            self.game.west_frame.winfo_children()
        )

        card = self.game.west_frame.winfo_children()[0]

        card.invoke()

        self.root.update()

        west_cards_after = len(
            self.game.west_frame.winfo_children()
        )

        self.assertEqual(
            west_cards_after,
            west_cards_before - 1
        )

    # ---------------------------------------------------------
    # TEST 9: Played card appears in centre
    # ---------------------------------------------------------

    @patch.object(GamePage, "finish_bidding")
    def test_card_moves_to_centre(self, mock_finish_bidding):
        """
        Check that a played card is displayed in the
        centre of the table.
        """

        card = self.game.south_frame.winfo_children()[0]

        card.invoke()

        self.root.update()

        south_label = self.game.trick_labels["South"]

        image = south_label.cget("image")

        self.assertNotEqual(image, "")

    # ---------------------------------------------------------
    # TEST 10: Four played cards are tracked
    # ---------------------------------------------------------

    @patch.object(GamePage, "finish_bidding")
    def test_four_cards_create_trick(self, mock_finish_bidding):
        """
        Check that four cards can be played and that
        trick_count reaches 4.
        """

        players = [
            self.game.south_frame,
            self.game.north_frame,
            self.game.east_frame,
            self.game.west_frame
        ]

        for frame in players:
            card = frame.winfo_children()[0]
            card.invoke()
            self.root.update()

        self.assertEqual(self.game.trick_count, 4)

    # ---------------------------------------------------------
    # TEST 11: Bidding starts correctly
    # ---------------------------------------------------------

    def test_bidding_phase_starts(self):
        """
        Check that the game starts in the bidding phase.
        """

        self.assertTrue(self.game.bidding_phase)

    # ---------------------------------------------------------
    # TEST 12: Selecting a bid level
    # ---------------------------------------------------------

    def test_select_bid_level(self):
        """
        Check that selecting a bid level stores the level.
        """

        self.game.select_level(3)

        self.assertEqual(
            self.game.selected_level,
            3
        )

    # ---------------------------------------------------------
    # TEST 13: Selecting a suit creates a bid
    # ---------------------------------------------------------

    @patch.object(GamePage, "make_bid")
    def test_select_suit_creates_bid(self, mock_make_bid):
        """
        Check that selecting a suit after selecting
        a level creates the correct bid.
        """

        self.game.select_level(2)

        self.game.select_suit("♥")

        mock_make_bid.assert_called_once_with("2♥")

    # ---------------------------------------------------------
    # TEST 14: Suit cannot be selected without a level
    # ---------------------------------------------------------

    @patch.object(GamePage, "make_bid")
    def test_suit_requires_level(self, mock_make_bid):
        """
        Check that a suit cannot be selected before
        choosing a bidding level.
        """

        self.game.selected_level = None

        self.game.select_suit("♠")

        mock_make_bid.assert_not_called()

    # ---------------------------------------------------------
    # TEST 15: Finishing bidding hides bidding panel
    # ---------------------------------------------------------

    def test_finish_bidding(self):
        """
        Check that the bidding phase ends and the
        bidding panel is removed.
        """

        self.game.finish_bidding()

        self.assertFalse(self.game.bidding_phase)

        self.assertEqual(
            self.game.bidding.winfo_viewable(),
            0
        )


if __name__ == "__main__":
    unittest.main()