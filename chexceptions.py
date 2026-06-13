# Exceptions
class CheckerException(Exception):
    def __str__(self):
        return "Base checker Exception"


class WrongColorError(CheckerException):
    def __str__(self):
        return "Wrong color - only black or white for choose"

    def __repr__(self):
        return "Wrong color"


class PositionError(CheckerException):
    def __str__(self):
        return "Unreal position"

    def __repr__(self):
        return "Unreal position"


class WrongMoveError(CheckerException):
    """
    move == 'from' or 'to'
    """

    def __init__(self, move):
        self.move = move

    def __str__(self):
        return 'Unreal "{}" move'.format(self.move)

    def __repr__(self):
        return 'Unreal "{}" move'.format(self.move)


class GameInternalError(CheckerException):
    def __str__(self):
        return "Wrong game playing"

    def __repr__(self):
        return "Wrong game playing"
