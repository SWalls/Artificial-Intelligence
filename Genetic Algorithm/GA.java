import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import java.lang.InterruptedException;
import java.util.*;

// Each MyPolygon has a color and a Polygon object
class MyPolygon {

	Polygon polygon;
	Color color;

	public MyPolygon(Polygon _p, Color _c) {
		polygon = _p;
		color = _c;
	}

	public Color getColor() {
		return color;
	}

	public Polygon getPolygon() {
		return polygon;
	}

}


// Each GASolution has a list of MyPolygon objects
class GASolution {

	ArrayList<MyPolygon> shapes;

	// width and height are for the full resulting image
	int width, height;

	float fitness = 0;
	float normalizedFitness = 0;

	public GASolution(int _width, int _height) {
		shapes = new ArrayList<MyPolygon>();
		width = _width;
		height = _height;
	}

	public void setFitness(float _fitness) {
		fitness = _fitness;
	}

	public float getFitness() {
		return fitness;
	}

	public void setNormalizedFitness(float _normalizedFitness) {
		normalizedFitness = _normalizedFitness;
	}

	public float getNormalizedFitness() {
		return normalizedFitness;
	}

	public void addPolygon(MyPolygon p) {
		shapes.add(p);
	}

	public ArrayList<MyPolygon> getShapes() {
		return shapes;
	}

	public void clearShapes() {
		shapes.clear();
	}

	public int size() {
		return shapes.size();
	}

	// Create a BufferedImage of this solution
	// Use this to compare an evolved solution with 
	// a BufferedImage of the target image
	//
	// This is almost surely NOT the fastest way to do this...
	public BufferedImage getImage() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (MyPolygon p : shapes) {
			Graphics g2 = image.getGraphics();			
			g2.setColor(p.getColor());
			Polygon poly = p.getPolygon();
			if (poly.npoints > 0) {
				g2.fillPolygon(poly);
			}
		}
		return image;
	}

	public String toString() {
		return "" + shapes;
	}
}


// A Canvas to draw the highest ranked solution each epoch
class GACanvas extends JComponent{

    int width, height;
    GASolution solution;

    public GACanvas(int WINDOW_WIDTH, int WINDOW_HEIGHT) {
    	width = WINDOW_WIDTH;
    	height = WINDOW_HEIGHT;
    }
 
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setImage(GASolution sol) {
  	    solution = sol;
    }

    public void paintComponent(Graphics g) {
		if(solution != null) {
			BufferedImage image = solution.getImage();
			g.drawImage(image, 0, 0, null);
		}
    }
}


public class GA extends JComponent{
	
	JFrame frame;
    GACanvas canvas;
    int width, height;
    BufferedImage realPicture;
    GASolution[] population;
	int fittestIndex = 0;
	int currentEpoch = 0;
	int crossoverCount = 0;
	int mutationCount = 0;
	int pickedFittestCount = 0;

    // Adjust these parameters as necessary for your simulation
	static final int POPULATION_SIZE = 50;
	static final int POPULATION_BRANCHES = 3;
	static final boolean USE_WEIGHTED_MUTATION = false;
    static final double MUTATION_RATE = 0.01;
    static final double CROSSOVER_RATE = 0.6;
    static final int MAX_POLYGON_POINTS = 5;
    static final int MAX_POLYGONS = 10;
	static final int MAX_EPOCHS = 10000;
	static final int FITNESS_SAMPLE_SIZE = 100; // how many points to sample?
	static final float MAX_DISTANCE = (float)Math.sqrt((255*255)*3);

    public GA(JFrame _frame, GACanvas _canvas, BufferedImage _realPicture) {
		frame = _frame;
        canvas = _canvas;
        realPicture = _realPicture;
        width = realPicture.getWidth();
        height = realPicture.getHeight();
        population = new GASolution[POPULATION_SIZE];

        // You'll need to define the following functions
        createPopulation(POPULATION_SIZE);	// Make new, random chromosomes
    }

	public void createPopulation(int size) {
		Random rand = new Random();
		for(int i=0; i<size; i++) {
			GASolution solution = new GASolution(width, height);
			for(int j=0; j<MAX_POLYGONS; j++) {
				int[] xpoints = new int[MAX_POLYGON_POINTS];
				int[] ypoints = new int[MAX_POLYGON_POINTS];
				for(int k=0; k<MAX_POLYGON_POINTS; k++) {
					xpoints[k] = rand.nextInt(width);
					ypoints[k] = rand.nextInt(height);
				}
				Polygon p = new Polygon(xpoints, ypoints, MAX_POLYGON_POINTS);
				Color c = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				MyPolygon poly = new MyPolygon(p, c);
				solution.addPolygon(poly);
			}
			solution.setFitness(calcFitness(solution));
			population[i] = solution;
		}
	}

    public void runSimulation() {
		currentEpoch = 0;
		while(normalizeFitnesses() && 
				population[fittestIndex].getFitness() < 0.95f && 
				currentEpoch < MAX_EPOCHS) {
			crossoverCount = 0;
			mutationCount = 0;
			pickedFittestCount = 0;
			GASolution[] bestNextEpoch = null;
			float maxPopulationFitness = 0;
			for(int n=0; n<POPULATION_BRANCHES; n++) {
				GASolution[] newPopulation = new GASolution[POPULATION_SIZE];
				Random rand = new Random();
				// create new population
				float avgPopulationFitness = 0;
				for(int i=0; i<POPULATION_SIZE; i++) {
					float r = rand.nextFloat();
					if(r <= CROSSOVER_RATE) {
						// crossover
						newPopulation[i] = newCrossover();
					} else {
						// just add the same individual
						newPopulation[i] = deepCopy(population[i]);
					}
					avgPopulationFitness += newPopulation[i].getFitness();
				}
				avgPopulationFitness /= POPULATION_SIZE;
				if(avgPopulationFitness > maxPopulationFitness) {
					bestNextEpoch = newPopulation;
					maxPopulationFitness = avgPopulationFitness;
				}
			}
			if(currentEpoch % 10 == 0) {
				// float percentTimesFittestSelected = (((float)pickedFittestCount)/(crossoverCount*2))*100;
				System.out.printf("Epoch %d => Avg Fitness: %.2f, "+
						"Crossovers: %d, Mutations: %d\n", //+ ", Fittest Selection: %.1f%% (weight: %.1f%%)\n",
						currentEpoch, maxPopulationFitness,
						crossoverCount/POPULATION_BRANCHES,
						mutationCount/POPULATION_BRANCHES
						//, percentTimesFittestSelected, population[fittestIndex].getNormalizedFitness()*100
						);
				canvas.setImage(population[fittestIndex]);
				frame.repaint();
			}
			population = bestNextEpoch;
			currentEpoch++;
		}
		System.out.printf("Finished! Solution fitness: %f\n", population[fittestIndex].getFitness());
    }

	public boolean normalizeFitnesses() {
		fittestIndex = 0;
		// normalize fitnesses
		float sumFitness = 0;
		for(int i=0; i<POPULATION_SIZE; i++) {
			sumFitness += population[i].getFitness();
			if(population[i].getFitness() > population[fittestIndex].getFitness()) {
				fittestIndex = i;
			}
		}
		for(int i=0; i<POPULATION_SIZE; i++) {
			float fitness = population[i].getFitness();
			population[i].setNormalizedFitness(fitness/sumFitness);
		}
		return true;
	}

	public GASolution getFitIndividual() {
		// spin wheel to randomly select individuals, so that fitter individuals
		// have a higher chance of being selected
		Random rand = new Random();
		float r = rand.nextFloat();
		float sum = 0;
		int i = -1;
		while(sum <= r && i < POPULATION_SIZE) {
			i++;
			sum += population[i].getNormalizedFitness();
			/*
			if(i == POPULATION_SIZE-1) {
				System.out.printf("Sum: %f, r: %f\n", sum, r);
			}
			*/
		}
		if(i == fittestIndex) {
			/*
			System.out.printf("Picked fittest individual (weight: %f)\n", 
				population[fittestIndex].getNormalizedFitness());
				*/
			pickedFittestCount++;
		}
		return population[i];
	}

	public GASolution newCrossover() {
		crossoverCount++;
		GASolution parent1 = getFitIndividual();
		GASolution parent2 = getFitIndividual();
		return cross(parent1, parent2);
	}

	public GASolution cross(GASolution s1, GASolution s2) {
		GASolution newSolution = new GASolution(s1.width, s1.height);
		// calculate new mutation rate, so more fit solutions are less likely to mutate
		float s1RelativeFitness = s1.getFitness()/population[fittestIndex].getFitness();
		float s2RelativeFitness = s2.getFitness()/population[fittestIndex].getFitness();
		float avgInverseFitness = (((1.0f/s1RelativeFitness)+(1.0f/s2RelativeFitness))/2);
		double weightedMutateRate = MUTATION_RATE*avgInverseFitness;
		Random rand = new Random();
		ArrayList<MyPolygon> s1Shapes = s1.getShapes();
		ArrayList<MyPolygon> s2Shapes = s2.getShapes();
		for(int j=0; j<MAX_POLYGONS; j++) {
			MyPolygon p1 = s1Shapes.get(j);
			MyPolygon p2 = s2Shapes.get(j);
			Color color = p1.getColor();
			float r = rand.nextFloat();
			if(r <= (USE_WEIGHTED_MUTATION ? weightedMutateRate : MUTATION_RATE)) {
				// randomly mutate the color!
				mutationCount++;
				color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
			} else {
				r = rand.nextFloat();
				if(r >= 0.5f*(s1.getFitness()/s2.getFitness())) {
					// use the second soln's color!
					color = p2.getColor();
				}
			}
			Polygon s1Poly = p1.getPolygon();
			Polygon s2Poly = p2.getPolygon();
			Polygon newPoly = new Polygon();
			PathIterator s1Iter = s1Poly.getPathIterator(null);
			PathIterator s2Iter = s2Poly.getPathIterator(null);
			int ptIdx = 0;
			while(!s1Iter.isDone() && !s2Iter.isDone() && ptIdx < s1Poly.npoints) {
				float[] coords = new float[6];
				r = rand.nextFloat();
				if(r <= (USE_WEIGHTED_MUTATION ? weightedMutateRate : MUTATION_RATE)) {
					// mutate this vertex!
					mutationCount++;
					coords[0] = rand.nextInt(width);
					coords[1] = rand.nextInt(height);
				} else {
					r = rand.nextFloat();
					if(r <= 0.5f*(s1.getFitness()/s2.getFitness())) {
						// use the first soln's vertex!
						s1Iter.currentSegment(coords);
					} else {
						// use the second soln's vertex!
						s2Iter.currentSegment(coords);
					}
				}
				newPoly.addPoint((int)coords[0], (int)coords[1]);
				s1Iter.next();
				s2Iter.next();
				ptIdx++;
			}
			MyPolygon myNewPoly = new MyPolygon(newPoly, color);
			newSolution.addPolygon(myNewPoly);
		}
		newSolution.setFitness(calcFitness(newSolution));
		return newSolution;
	}

	public GASolution deepCopy(GASolution solution) {
		GASolution newSolution = new GASolution(solution.width, solution.height);
		ArrayList<MyPolygon> shapes = solution.getShapes();
		for(int j=0; j<MAX_POLYGONS; j++) {
			MyPolygon myPoly = shapes.get(j);
			Color color = myPoly.getColor();
			Polygon poly = myPoly.getPolygon();
			Polygon newPoly = new Polygon();
			PathIterator iter = poly.getPathIterator(null);
			int ptIdx = 0;
			while(!iter.isDone() && ptIdx < poly.npoints) {
				float[] coords = new float[6];
				iter.currentSegment(coords);
				newPoly.addPoint((int)coords[0], (int)coords[1]);
				iter.next();
				ptIdx++;
			}
			MyPolygon myNewPoly = new MyPolygon(newPoly, color);
			newSolution.addPolygon(myNewPoly);
		}
		newSolution.setFitness(solution.getFitness());
		return newSolution;
	}

	// value (0.0 to 1.0) where 1.0 = perfect match
	public float calcFitness(GASolution solution) {
		Random rand = new Random();
		BufferedImage genPicture = solution.getImage();
		float avgDistance = 0;
		for(int i=0; i<FITNESS_SAMPLE_SIZE; i++) {
			int x = rand.nextInt(solution.width);
			int y = rand.nextInt(solution.height);
			Color a = new Color(realPicture.getRGB(x,y));
			Color b = new Color(genPicture.getRGB(x,y));
			// calculate euclidean distance
			float distance = (float) Math.sqrt(Math.pow(a.getRed() - b.getRed(), 2) +
										Math.pow(a.getBlue() - b.getBlue(), 2) +
										Math.pow(a.getGreen() - b.getGreen(), 2));
			avgDistance += (distance / MAX_DISTANCE);
		}
		avgDistance /= FITNESS_SAMPLE_SIZE;
		return (1 - avgDistance);
	}

    public static void main(String[] args) throws IOException {
        String realPictureFilename = "test.jpg";
        BufferedImage realPicture = ImageIO.read(new File(realPictureFilename));

        JFrame frame = new JFrame();
        frame.setSize(realPicture.getWidth(), realPicture.getHeight());
        frame.setTitle("GA Simulation of Art");
	
        GACanvas theCanvas = new GACanvas(realPicture.getWidth(), realPicture.getHeight());
        frame.add(theCanvas);
        frame.setVisible(true);

        GA pt = new GA(frame, theCanvas, realPicture);
		pt.runSimulation();
    }
}




