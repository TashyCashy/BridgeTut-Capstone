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
        Check that each player initially has 13 cards.
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

        north = len(
            self.game.north_frame.winfo_children()
        )

        south = len(
            self.game.south_frame.winfo_children()
        )

        east = len(
            self.game.east_frame.winfo_children()
        )

        west = len(
            self.game.west_frame.winfo_children()
        )

        total_cards = north + south + east + west

        self.assertEqual(total_cards, 52)

    # ---------------------------------------------------------
    # TEST 5: Bidding phase starts correctly
    # ---------------------------------------------------------

    def test_bidding_phase_starts(self):
        """
        Check that the game starts in the bidding phase.
        """

        self.assertTrue(
            self.game.bidding_phase
        )

    # ---------------------------------------------------------
    # TEST 6: Bidding panel exists
    # ---------------------------------------------------------

    def test_bidding_panel_exists(self):
        """
        Check that the bidding panel exists when
        the game starts.
        """

        self.assertIsNotNone(
            self.game.bidding
        )

    # ---------------------------------------------------------
    # TEST 7: Selected level starts empty
    # ---------------------------------------------------------

    def test_selected_level_starts_empty(self):
        """
        Check that no bidding level is selected
        when the game starts.
        """

        self.assertIsNone(
            self.game.selected_level
        )

    # ---------------------------------------------------------
    # TEST 8: Selecting a bid level
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
    # TEST 9: Selecting another bid level
    # ---------------------------------------------------------

    def test_change_bid_level(self):
        """
        Check that selecting another level updates
        the selected level.
        """

        self.game.select_level(2)

        self.assertEqual(
            self.game.selected_level,
            2
        )

        self.game.select_level(5)

        self.assertEqual(
            self.game.selected_level,
            5
        )

    # ---------------------------------------------------------
    # TEST 10: Selecting a suit creates a bid
    # ---------------------------------------------------------

    @patch.object(GamePage, "make_bid")
    def test_select_suit_creates_bid(self, mock_make_bid):
        """
        Check that selecting a suit after selecting
        a level creates the correct bid.
        """

        self.game.select_level(2)

        self.game.select_suit("♥")

        mock_make_bid.assert_called_once_with(
            "2♥"
        )

    # ---------------------------------------------------------
    # TEST 11: Suit cannot be selected without a level
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
    # TEST 12: Bid history starts empty
    # ---------------------------------------------------------

    def test_bid_history_starts_empty(self):
        """
        Check that the bidding history is empty
        when the game starts.
        """

        self.assertEqual(
            self.game.bid_history_data,
            []
        )

    # ---------------------------------------------------------
    # TEST 13: Undo history starts empty
    # ---------------------------------------------------------

    def test_undo_history_starts_empty(self):
        """
        Check that the undo history is empty
        when the game starts.
        """

        self.assertEqual(
            self.game.undo_hist,
            []
        )

    # ---------------------------------------------------------
    # TEST 14: Current player starts at the first player
    # ---------------------------------------------------------

    def test_current_player_starts_at_zero(self):
        """
        Check that the current player starts at
        the first player.
        """

        self.assertEqual(
            self.game.current_player,
            0
        )

    # ---------------------------------------------------------
    # TEST 15: Current bidding level starts at zero
    # ---------------------------------------------------------

    def test_current_level_starts_at_zero(self):
        """
        Check that the current bidding level starts at zero.
        """

        self.assertEqual(
            self.game.current_level,
            0
        )

    # ---------------------------------------------------------
    # TEST 16: Trick count starts at zero
    # ---------------------------------------------------------

    def test_trick_count_starts_at_zero(self):
        """
        Check that no cards have been played when
        the game starts.
        """

        self.assertEqual(
            self.game.trick_count,
            0
        )

    # ---------------------------------------------------------
    # TEST 17: Trick labels exist
    # ---------------------------------------------------------

    def test_trick_labels_exist(self):
        """
        Check that the GUI has a centre label
        for each player.
        """

        self.assertIn(
            "North",
            self.game.trick_labels
        )

        self.assertIn(
            "South",
            self.game.trick_labels
        )

        self.assertIn(
            "East",
            self.game.trick_labels
        )

        self.assertIn(
            "West",
            self.game.trick_labels
        )

    # ---------------------------------------------------------
    # TEST 18: Finishing bidding changes the phase
    # ---------------------------------------------------------

    def test_finish_bidding_changes_phase(self):
        """
        Check that finishing bidding changes the game
        from the bidding phase.
        """

        self.game.finish_bidding()

        self.assertFalse(
            self.game.bidding_phase
        )

    # ---------------------------------------------------------
    # TEST 19: Finishing bidding hides the panel
    # ---------------------------------------------------------

    def test_finish_bidding_hides_panel(self):
        """
        Check that the bidding panel is hidden after
        bidding has finished.
        """

        self.game.finish_bidding()

        self.assertEqual(
            self.game.bidding.winfo_viewable(),
            0
        )

    # ---------------------------------------------------------
    # TEST 20: Player frames contain cards
    # ---------------------------------------------------------

    def test_player_frames_contain_cards(self):
        """
        Check that each player frame contains
        card widgets when the game starts.
        """

        self.assertGreater(
            len(self.game.north_frame.winfo_children()),
            0
        )

        self.assertGreater(
            len(self.game.south_frame.winfo_children()),
            0
        )

        self.assertGreater(
            len(self.game.east_frame.winfo_children()),
            0
        )

        self.assertGreater(
            len(self.game.west_frame.winfo_children()),
            0
        )


if __name__ == "__main__":
    unittest.main()