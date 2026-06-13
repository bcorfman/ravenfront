from kivy import require as kivy_require

kivy_require("1.11.0")

from kivy.uix.screenmanager import ScreenManager, Screen, FadeTransition
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.widget import Widget
from kivy.uix.button import Button
from kivy.uix.image import Image
from kivy.uix.floatlayout import FloatLayout
from kivy.uix.popup import Popup
from kivy.properties import StringProperty, ObjectProperty


# ScreenManagement
class WelcomeScreen(Screen):
    pass


class MainScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.ml = MainLayout()
        self.add_widget(self.ml)


class SettingsScreen(Screen):
    pass


class ScreenManagement(ScreenManager):
    pass


class MainLayout(FloatLayout):
    game = ObjectProperty(None)


# Table Configuration
class SquareImage(Image):
    square_number = StringProperty("")

    def __init__(self, color: str, real_pos, **kwargs):
        super().__init__(**kwargs)
        self.col = color
        self.source = "data/white.png" if color == "white" else "data/black.png"
        self.real_pos = real_pos
        self.square_number = str(real_pos)


class Table(GridLayout):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        color = ("white", "black")
        square_number = 1
        for row in range(8):
            for col in range(8):
                if (row + col) % 2 == 1:  # Valid squares for checkers
                    self.add_widget(SquareImage(color[(row + col) % 2], square_number))
                    square_number += 1
                else:
                    self.add_widget(SquareImage(color[(row + col) % 2], None))


class StateAlert(Popup):
    def __init__(self, message, **kwargs):
        super().__init__(**kwargs)
        self.title = "Game State"
        self.content = Button(text=message, on_release=self.dismiss)
        self.size_hint = (None, None)
        self.size = (400, 200)
