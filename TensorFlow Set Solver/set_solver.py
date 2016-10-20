from keras.models import Sequential
from db_functions import *
from neural_network import *
from scipy import misc
import numpy as np
# np.random.seed(42)

def get_card_idx_from_window_bounds(left, right, top, bottom, width, height):
    center_x = left + ((right - left)/2)
    center_y = top + ((bottom - top)/2)
    is_top_third = center_y < height/3
    is_middle_third = (center_y >= height/3 and center_y < 2*(height/3))
    if(center_x < width/3):
        if(is_top_third): return 0
        elif(is_middle_third): return 1
        else: return 2
    elif(center_x >= width/3 and center_x < 2*(width/3)):
        if(is_top_third): return 3
        elif(is_middle_third): return 4
        else: return 5
    else:
        if(is_top_third): return 6
        elif(is_middle_third): return 7
        else: return 8

def solve_set_grid(db_file_path, grid_image_path):
    db = CardDatabase(db_file_path)
    recognition_nn = CardRecognitionNN(db)
    recognition_model = recognition_nn.train()
    visibility_nn = CardVisibilityNN(db)
    visibility_model = visibility_nn.train()

    grid_image = misc.imread(grid_image_path)
    WINDOW_WIDTH = 30
    WINDOW_HEIGHT = 40
    grid_width = len(grid_image[0])
    grid_height = len(grid_image)
    print "Grid width: %d, height: %d" % (grid_width, grid_height)
    print "Solving grid of set cards..."

    # there are 9 locations on the board, each with 9 possible votes
    card_votes = np.zeros((9,9))

    # start looping through image with a window
    for row in range(0, grid_height-WINDOW_HEIGHT, 2):
        for col in range(0, grid_width-WINDOW_WIDTH, 2):
            # build window from pixels
            window = []
            for i in range(WINDOW_HEIGHT):
                window.append([])
                for j in range(WINDOW_WIDTH):
                    window[i].append(grid_image[row+i][col+j])
            window = np.array(window)
            # show pixels collected in window
            # plt.imshow(window)
            # plt.show()
            # run window of pixels through nn model to decide if it contains a card
            window = window.reshape(1, 40, 30, 3)
            visible = visibility_model.predict(window)
            if(visible[0][1] == 1):
                # the window contains a card!
                # run the pixels through nn model to decide which card it is
                recognized = recognition_model.predict(window)[0]
                card_idx = get_card_idx_from_window_bounds(col, col+WINDOW_WIDTH,
                                row, row+WINDOW_HEIGHT, grid_width, grid_height)
                card_votes[card_idx] += recognized
    
    print "Made final decisions!"
    card_decisions = card_votes.argmax(axis=1)
    for i in range(len(card_decisions)):
        print "Card %d:" % i,
        print CardDatabase.get_description(card_decisions[i]+9)
        # set this max idx to zero to get secondary max next time
        card_votes[i][card_decisions[i]] = 0
    print "Secondary choices:"
    second_decisions = card_votes.argmax(axis=1)
    for i in range(len(second_decisions)):
        print "Card %d:" % i,
        print CardDatabase.get_description(second_decisions[i]+9)

solve_set_grid("cards.h5", "grid/grid-1.jpg")