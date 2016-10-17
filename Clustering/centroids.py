from math import sqrt
import numpy as np
import csv
np.random.seed(46)

class Cluster(object):
    
    def __init__(self, low, high, k, dim):
        self.centroids = np.random.random_integers(low,high,(k,dim))

    def train(self, data):
        count = 0
        changed = True
        self.centroid_assignments = np.zeros(len(data))
        while(changed and count < 12):
            print "ITERATION %d" % count
            count += 1
            changed = False
            # assign each vector in data to its closest centroid
            for i in range(0, len(data)):
                min_dist = float("inf")
                closest_centroid = 0
                for j in range(0, len(self.centroids)):
                    dist = calc_distance(data[i], self.centroids[j])
                    if(dist < min_dist):
                        # print "Found smaller dist: %f (centroid %d)" % (dist, j)
                        min_dist = dist
                        closest_centroid = j
                if(self.centroid_assignments[i] != closest_centroid):
                    self.centroid_assignments[i] = closest_centroid
                    changed = True
            # recalculate centroids by averaging features of all their member vectors
            for i in range(0, len(self.centroids)):
                sum = 0
                num_members = 0
                for j in range(0, len(data)):
                    if(self.centroid_assignments[j] == i):
                        sum = sum + data[j]
                        num_members += 1
                if num_members > 0:
                    avg = sum / num_members
                else:
                    avg = np.zeros(len(data[0]))
                # print avg
                print "Avg dist from centroid %d: %.2f (%d members)" % (i, calc_distance(self.centroids[i], avg), num_members)
                self.centroids[i] = avg
    
    def print_movies(self, number):
        for i in range(0, len(self.centroids)):
            print ""
            movies_assigned = np.where(self.centroid_assignments == i)[0]
            if(len(movies_assigned) == 0):
                print "No movies in Centroid %d" %(i)
                continue
            print "Centroid %d:" % (i)
            genres_count = np.zeros(len(genres))
            for movie in movies_assigned:
                for j in range(1, len(titles[movie])):
                    genres_count[titles[movie][j]] += 1
            print "* Genres:"
            genre_primary = np.argmax(genres_count)
            print "  - MAIN GENRE is %s: %.1f%%" % (genres[genre_primary], 100*genres_count[genre_primary]/len(movies_assigned))
            print "  - OTHER GENRES:",
            for j in range(0, len(genres)):
                if(genres_count[j] > 0):
                    print ("%s (%.1f%%)," % (genres[j], 100*genres_count[j]/len(movies_assigned))),
            print ""
            print "* 10 Random Movies:"
            found = []
            while(len(found) < number):
                j = np.random.randint(0, len(movies_assigned))
                if(j not in found):
                    found.append(j)
                    print "  - %s =>" % titles[movies_assigned[j]][0],
                    for k in range(1, len(titles[movies_assigned[j]])):
                        print genres[titles[movies_assigned[j]][k]] + ",",
                    print ""
            
def calc_distance(v1, v2):
    if(len(v1) != len(v2)):
        print "Cannot compute distance between differently shaped vectors!"
        return -1
    sum = 0
    num_votes = 1
    for i in range(0, len(v1)):
        if(v2[i] > 0 and v1[i] > 0):
            sum += pow(v2[i] - v1[i], 2)
            num_votes += 1
    distance = sqrt(sum) + len(v1)/num_votes
    return distance
        
def load_dataset(filename):
    data = np.genfromtxt(filename, delimiter='\t')[:,[0,1,2]]
    # 943 users, 1682 movies
    movies = np.zeros((1683, 944))
    for i in data:
        user_id = int(i[0])
        movie_id = int(i[1])
        rating = int(i[2])
        movies[movie_id][user_id] = rating
    return movies
        
def load_titles(titles_file, genres_file):
    genres = []
    titles = [""]
    with open(genres_file, 'rb') as csvfile:
        reader = csv.reader(csvfile, delimiter='|')
        for row in reader:
            genres.append(row[0])
    with open(titles_file, 'rb') as csvfile:
        reader = csv.reader(csvfile, delimiter='|')
        for row in reader:
            title = [row[1]]
            for i in range(0, len(genres)):
                if(row[i+5] == '1'):
                    title.append(i)
            titles.append(title)
    # print titles[1]
    return titles, genres

np.set_printoptions(precision=2)
movies = load_dataset("ml-100k/u.data")
titles, genres = load_titles("ml-100k/u.item","ml-100k/u.genre")
cluster = Cluster(1,5,15,944)
cluster.train(movies)
cluster.print_movies(10)