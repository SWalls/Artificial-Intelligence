import numpy as np
np.random.seed(42)

class Cluster(object):
    
    def __init__():
        print "Hi"

def load_dataset(filename):
    data = np.genfromtxt(filename, delimiter='\t')[:,[0,1,2]]
    dt = np.dtype([('user_id','int'),('rating','int')])
    movies = dict()
    for i in data:
        user_id = i[0]
        movie_id = i[1]
        rating = i[2]
        if movies.has_key(movie_id):
            movies[movie_id] = np.append(movies[movie_id], np.array([(user_id, rating)], dtype=dt))
        else:
            movies[movie_id] = np.array([(user_id, rating)], dtype=dt)
    print movies[242][0]['user_id']

np.set_printoptions(precision=2)
load_dataset("ml-100k/u.data")