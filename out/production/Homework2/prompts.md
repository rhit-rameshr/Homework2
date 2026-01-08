## Prompt 1

Please create a java program following the design of the following PlantUML.

enum ChipType{
    RED
    GREEN 
    BLUE 
    BLACK 
    WHITE 
}



package presentationLayer{

    class Main{
        -board: Card[]
        -startNewGame()
        -updateBoard()
        -nextTurn()
        -saveGameState()
        -checkIfGameOver()
    }

}

package DomainLayer{

    class Player{
        -chips: ArrayList<ChipType>
        -cards: ArrayList<Card>
        +buyCard(card: Card): boolean
        +takeSameChips(chip1: ChipType, chip2: ChipType): void
        +takeDifferentChips(chip1: ChipType, chip2: ChipType, chip3: ChipType): void
    }

    class Card{
    +cost: ArrayList<ChipType>
    +pointValue: int
    }  
}

package DataStorageLayer{

    class DataLogger{
        +fileName: String
        +logData(players: Player[], availableCards: Card[])
    }

}

Main -d-> DataLogger
Main -d->"2" Player

Player -d->"*" Card



## Prompt 2

Please add functionality to Main.java to display the game state as a 3x5 array of cards with 2 text boxes to show player1 and player2 chips and victory points. Also allow for taking chips and restarting the game.

## Prompt 3

Please also expand main to allow players to buy a card by clicking on it in the UI. Also add some indication of which player's turn it is.

## Prompt 4

Please add some variety to the costs of the cards. Cards worth more victory points should cost more chips. Cards should cost at least 2 one color and can cost a maximum of 3 different colors.

## Prompt 5

When a player is taking chips as their action they should have two options: 1: they take two of the same chip 2: they take 3 unique chips Currently the game only allows the first type of actions. Please create functionality to allow the player to do either

## Prompt 6

There is a bug that allows players to buy cards even if they do not have enough chips, as long as they have one of each of the types of chips the cards costs. The game should check if they have enough chips of each type. When all cards are bought please display the winner of the game as well as allowing the user to restart the game.