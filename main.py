import asyncio
import aiohttp
from kivy.app import App
from kivy.uix.button import Button
from kivy.uix.widget import Widget
from kivy.properties import BooleanProperty, ListProperty, StringProperty
from kivy.clock import Clock
from kivy.animation import Animation
from kivy.config import Config
from vdesign import MainLayout, ScreenManagement
import asynckivy as ak

Config.set("graphics", "resizable", "0")
Config.set("graphics", "width", 800)
Config.set("graphics", "height", 640)

API_BASE_URL = "https://raven-1-j8079958.deta.app"


class NewGameButton(Button):
    def on_release(self):
        App.get_running_app().run_coroutine(self.start_new_game())

    async def start_new_game(self):
        await App.get_running_app().main_layout.game.new_game()


class VChecker(Widget):
    move_flag = BooleanProperty(False)
    start_touch = ListProperty([None, None])
    delta = ListProperty([None, None])
    border = ListProperty([None, None, None, None])
    move = ListProperty([None, None])
    chcolor = StringProperty()

    def __init__(self, color, border_pos_list, game, queen=False, **kwargs):
        self.border = border_pos_list
        self.game = game
        self.real_pos = kwargs.pop("real_pos")
        super().__init__(**kwargs)
        self.size_hint = (None, None)
        self.size = (60, 60)
        self.pos = kwargs.get("pos", (0, 0))
        self.chcolor = color
        self.is_queen = queen
        self.update_source()

    def update_source(self):
        self.source = f"data/{self.chcolor}_ch{'_q' if self.is_queen else ''}.png"

    def become_queen(self):
        self.is_queen = True
        self.update_source()

    def on_touch_down(self, touch):
        if self.collide_point(*touch.pos) and self.game.is_player_turn(self.chcolor):
            self.move = [self.real_pos]
            self.move_flag = True
            self.start_touch = touch.pos
            self.delta = (
                self.start_touch[0] - self.center[0],
                self.start_touch[1] - self.center[1],
            )
            return True
        return super().on_touch_down(touch)

    def on_touch_move(self, touch):
        if self.move_flag:
            new_center = (touch.x - self.delta[0], touch.y - self.delta[1])
            self.center = (
                max(
                    self.border[0] + self.width / 2,
                    min(new_center[0], self.border[2] - self.width / 2),
                ),
                max(
                    self.border[3] + self.height / 2,
                    min(new_center[1], self.border[1] - self.height / 2),
                ),
            )
            return True
        return super().on_touch_move(touch)

    def on_touch_up(self, touch):
        if self.move_flag:
            self.move_flag = False
            end_square = self.game.get_square_at_pos(self.center)

            if end_square and end_square != self.real_pos:
                self.move.append(end_square)
                App.get_running_app().run_coroutine(self.handle_move(end_square))
            else:
                self.center = self.game.get_square_center(self.real_pos)
            return True
        return super().on_touch_up(touch)

    async def handle_move(self, end_square):
        valid_move = await self.game.make_move(self.move[0], end_square)
        if valid_move:
            new_center = self.game.get_square_center(end_square)
            Animation(center=new_center, duration=0.3).start(self)
            self.real_pos = end_square
        else:
            original_center = self.game.get_square_center(self.real_pos)
            Animation(center=original_center, duration=0.3).start(self)


class MainGame:
    def __init__(self, layout):
        self.layout = layout
        self.session = None
        self.checkers = {}
        self.current_player = "black"
        self.game_mode = "2player"
        self.ai_thinking = False
        self.square_to_pos = {}
        self.pos_to_square = {}
        self._init_square_mappings()

    def _init_square_mappings(self):
        square_number = 1
        for row in range(8):
            for col in range(8):
                if (row + col) % 2 == 1:
                    self.square_to_pos[square_number] = (col, row)
                    self.pos_to_square[(col, row)] = square_number
                    square_number += 1

    def get_square_at_pos(self, pos):
        square_width = self.layout.width / 8
        square_height = self.layout.height / 8
        col = int(pos[0] // square_width)
        row = int(pos[1] // square_height)
        return self.pos_to_square.get((col, row))

    async def create_session(self):
        self.session = aiohttp.ClientSession()
        async with self.session.post(f"{API_BASE_URL}/create_session") as response:
            if response.status == 200:
                await self.update_board_state()
            else:
                print("Failed to create session")

    async def end_session(self):
        if self.session:
            await self.session.post(f"{API_BASE_URL}/end_session")
            await self.session.close()
            self.session = None

    async def update_board_state(self):
        async with self.session.get(f"{API_BASE_URL}/cb_state") as response:
            if response.status == 200:
                data = await response.json()
                fen = data["fen"]
                self.update_board_from_fen(fen)
            else:
                print("Failed to get board state")

    def update_board_from_fen(self, fen):
        parts = fen.split(":")
        self.current_player = "black" if parts[0] == "B" else "white"

        new_checkers = {}
        for part in parts[1:]:
            color = "white" if part[0] == "W" else "black"
            pieces = part[1:].split(",")
            for piece in pieces:
                if piece.startswith("K"):
                    square = int(piece[1:])
                    new_checkers[square] = (color, True)
                else:
                    square = int(piece)
                    new_checkers[square] = (color, False)

        for square in list(self.checkers.keys()):
            if square not in new_checkers:
                self.layout.remove_widget(self.checkers[square])
                del self.checkers[square]

        for square, (color, is_queen) in new_checkers.items():
            if square in self.checkers:
                checker = self.checkers[square]
                if checker.chcolor != color or checker.is_queen != is_queen:
                    checker.chcolor = color
                    checker.is_queen = is_queen
                    checker.update_source()
            else:
                self.create_checker(color, square, is_queen)

    def create_checker(self, color, square, queen=False):
        border = [0, self.layout.height, self.layout.width, 0]
        center = self.get_square_center(square)
        checker = VChecker(
            color, border, self, queen=queen, pos=center, real_pos=square
        )
        self.layout.add_widget(checker)
        self.checkers[square] = checker

    def get_square_center(self, square):
        col, row = self.square_to_pos[square]
        square_width = self.layout.width / 8
        square_height = self.layout.height / 8
        return ((col + 0.5) * square_width, (7 - row + 0.5) * square_height)

    def is_player_turn(self, color):
        return color == self.current_player and (
            self.game_mode == "2player" or color == "black"
        )

    async def make_move(self, start_sq, end_sq):
        if self.ai_thinking:
            return False

        async with self.session.post(
            f"{API_BASE_URL}/make_move", params={"start_sq": start_sq, "end_sq": end_sq}
        ) as response:
            if response.status == 200:
                await self.update_board_state()
                if self.game_mode == "1player" and self.current_player == "white":
                    App.get_running_app().run_coroutine(self.ai_move())
                return True
            else:
                print("Invalid move")
                return False

    async def ai_move(self):
        self.ai_thinking = True
        async with self.session.post(
            f"{API_BASE_URL}/calc_move/", params={"search_time": 5}
        ) as response:
            if response.status == 200:
                data = await response.json()
                start_sq, end_sq = data["start_sq"], data["end_sq"]
                await self.make_move(start_sq, end_sq)
            else:
                print("AI failed to make a move")
        self.ai_thinking = False

    async def new_game(self, mode="2player"):
        if self.session:
            await self.session.close()
        await self.create_session()
        self.game_mode = mode

    def run(self):
        ak.start(self.new_game(self.game_mode))


class MainApp(App):
    def build(self):
        self.sm = ScreenManagement()
        main_screen = self.sm.get_screen("main")
        if main_screen is None:
            raise ValueError("Main screen not found in ScreenManagement")
        self.main_layout = main_screen.ml
        self.main_layout.game = MainGame(self.main_layout)
        return self.sm

    def on_start(self):
        ak.start(self.main_layout.game.run())


if __name__ == "__main__":
    MainApp().run()
