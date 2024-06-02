# Group 0 - 159261 Assignment 2

### Members
### Caspar Rollo - 21010371
### Tiernan De Lacy - 22008375
### Mitchell Hartland - 22007285
###

### Resources

Utilising sprite sheet from:
https://craftpix.net/freebies/free-homeless-character-sprite-sheets-pixel-art/

Word lists generated from: https://people.sc.fsu.edu/~jburkardt/datasets/words/wordlist.txt
Utilised a python script to get these to 3 txt files with 4 letter, 5 letter and 6 letter words seperated.

### Overview
Wordle style game with some added features. Main menu screen has options to play 4, 5, or 6 letter words. Then down the 
bottom of the screen there's a little mini-game for if you get bored. This is a demonstration of using a nice sprite 
running and jumping. 

Then for the actual game play once you've selected your target length word you then get met with a familiar Wordle 
looking interface. A random word gets selected from the txt file. Then the user can make a guess. The guess gets 
validated against the text file to ensure it's a valid word if it isn't it notifies the user. If it's valid all the 
letters flip over in a nice animation with an appropriate sound to accompany it.

Then once the game is over by either winning or losing then you get given a results screen which gives you some 
performance results. After each guess the number of potential words the answer could be are calculated and these are 
displayed at the end. You can then see how your guesses narrowed down and how close you got.

