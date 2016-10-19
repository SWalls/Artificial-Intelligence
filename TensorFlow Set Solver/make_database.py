from __future__ import print_function
from os import listdir
from scipy import misc
import numpy as np
import h5py

def get_group_names(root_dir):
    dirs = [f for f in listdir(root_dir) if f[0] != "."]
    for i in range(len(dirs)):
        dirs[i] = dirs[i].replace("-", "/")
    return dirs

def load_image(filename):
    return misc.imread(filename)

def get_image_files(dir):
    image_files = [f for f in listdir(dir) if f[0] != "."]
    print ("%d images in dir: %s" % (len(image_files), dir))
    return image_files

def make_h5_db(db_filename):
    root_dir = "cards/"
    with h5py.File(db_filename, 'w') as hf:
        # load recognition data
        group_names = get_group_names(root_dir)
        for name in group_names:
            # load training images
            print ("Creating hdf5 group: train/" + name)
            train_group = hf.create_group("train/" + name)
            train_dir = root_dir + name.replace("/", "-") + "/train/"
            train_image_files = get_image_files(train_dir)
            for i in range(len(train_image_files)):
                file_path = train_dir+train_image_files[i]
                train_group.create_dataset(("img-%d"%i), data=load_image(file_path))
            # load validation images
            print ("Creating hdf5 group: validation/" + name)
            validation_group = hf.create_group("validation/" + name)
            validation_dir = root_dir + name.replace("/", "-") + "/validation/"
            validation_image_files = get_image_files(validation_dir)
            for i in range(len(validation_image_files)):
                file_path = validation_dir+validation_image_files[i]
                validation_group.create_dataset(("img-%d"%i), data=load_image(file_path))
        # load visibility data
        root_dir = "visibility/"
        for type in range(0,2):
            for visibility in range(0,2):
                data_type = "train"
                if type == 1:
                    data_type = "validation"
                visibility_type = "visible"
                if visibility == 1:
                    visibility_type = "invisible"
                name = data_type + "/" + visibility_type
                print ("Creating hdf5 group: " + name)
                group = hf.create_group(name)
                dir = root_dir + visibility_type + "/" + data_type + "/"
                image_files = get_image_files(dir)
                for i in range(len(image_files)):
                    file_path = dir+image_files[i]
                    group.create_dataset(("img-%d"%i), data=load_image(file_path))

make_h5_db("cards.h5")