import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import java.util.ArrayList;
import java.lang.InterruptedException;
import java.util.Random;

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
    ArrayList<GASolution> population;
	int fittestIndex = 0;
	int currentEpoch = 0;
	int crossoverCount = 0;
	int mutationCount = 0;

    // Adjust these parameters as necessary for your simulation
	static final int POPULATION_SIZE = 50;
    static final double MUTATION_RATE = 0.005;
    static final double CROSSOVER_RATE = 0.8;
    static final int MAX_POLYGON_POINTS = 4;
    static final int MAX_POLYGONS = 10;
	static final int MAX_EPOCHS = 5000;
	static final double MAX_FITNESS = Math.sqrt((255*255)*3);
	static final int FITNESS_SAMPLE_SIZE = 100; // how many points to sample?

    public GA(JFrame _frame, GACanvas _canvas, BufferedImage _realPicture) {
		frame = _frame;
        canvas = _canvas;
        realPicture = _realPicture;
        width = realPicture.getWidth();
        height = realPicture.getHeight();
        population = new ArrayList<GASolution>(POPULATION_SIZE);

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
			population.add(solution);
		}
	}

    public void runSimulation() {
		currentEpoch = 0;
		float populationFitness = 0;
		while(population.get(fittestIndex).getFitness() < 0.9f && currentEpoch < MAX_EPOCHS) {
			fittestIndex = 0;
			crossoverCount = 0;
			mutationCount = 0;
			ArrayList<GASolution> newPopulation = new ArrayList<GASolution>(POPULATION_SIZE);
			Random rand = new Random();
			for(int i=0; i<POPULATION_SIZE; i++) {
				float r = rand.nextFloat();
				GASolution solution = population.get(i);
				populationFitness += solution.getFitness();
				if(solution.getFitness() > population.get(fittestIndex).getFitness()) {
					fittestIndex = i;
				}
				if(r <= CROSSOVER_RATE*solution.getFitness()) {
					// possibly crossover
					newPopulation.add(maybeCrossover(solution, i));
				} else {
					// possibly mutate
					newPopulation.add(maybeMutate(solution));
				}
			}
			populationFitness /= POPULATION_SIZE;
			if(currentEpoch % 10 == 0) {
				System.out.printf("Epoch %d => Avg Fitness: %f, Crossovers: %d, Mutations: %d\n",
						currentEpoch, populationFitness, crossoverCount, mutationCount);
				canvas.setImage(population.get(fittestIndex));
				frame.repaint();
			}
			population = newPopulation;
			currentEpoch++;
		}
    }

	public GASolution maybeCrossover(GASolution solution, int i) {
		Random rand = new Random();
		for(int j=0; j<POPULATION_SIZE; j++) {
			if(j==i)
				continue;
			float r = rand.nextFloat();
			GASolution otherSolution = population.get(j);
			if(otherSolution.getFitness() > r*CROSSOVER_RATE) {
				// crossover!
				crossoverCount++;
				return cross(solution, otherSolution);
			}
		}
		return solution;
	}

	public GASolution cross(GASolution s1, GASolution s2) {
		GASolution newSolution = new GASolution(s1.width, s1.height);
		Random rand = new Random();
		ArrayList<MyPolygon> s1Shapes = s1.getShapes();
		ArrayList<MyPolygon> s2Shapes = s2.getShapes();
		for(int j=0; j<MAX_POLYGONS; j++) {
			MyPolygon p1 = s1Shapes.get(j);
			MyPolygon p2 = s2Shapes.get(j);
			Color color = p1.getColor();
			float r = rand.nextFloat();
			if(r <= 0.5f) {
				// use the second soln's color!
				color = s2Shapes.get(j).getColor();
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
				if(r <= 0.5f) {
					// use the first soln's vertex!
					s1Iter.currentSegment(coords);
				} else {
					// use the second soln's vertex!
					s2Iter.currentSegment(coords);
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

	public GASolution maybeMutate(GASolution solution) {
		GASolution newSolution = new GASolution(solution.width, solution.height);
		Random rand = new Random();
		ArrayList<MyPolygon> oldShapes = solution.getShapes();
		for(int j=0; j<MAX_POLYGONS; j++) {
			Color color = oldShapes.get(j).getColor();
			float r = rand.nextFloat();
			if(r <= MUTATION_RATE) {
				// mutate this polygon's color!
				mutationCount++;
				color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
			}
			Polygon oldPoly = oldShapes.get(j).getPolygon();
			Polygon newPoly = new Polygon();
			PathIterator iter = oldPoly.getPathIterator(null);
			int ptIdx = 0;
			while(!iter.isDone() && ptIdx < oldPoly.npoints) {
				float[] coords = new float[6];
				iter.currentSegment(coords);
				r = rand.nextFloat();
				if(r <= MUTATION_RATE) {
					// mutate this vertex!
					mutationCount++;
					coords[0] = rand.nextInt(width);
					coords[0] = rand.nextInt(height);
				}
				newPoly.addPoint((int)coords[0], (int)coords[1]);
				iter.next();
				ptIdx++;
			}
			MyPolygon myNewPoly = new MyPolygon(newPoly, color);
			newSolution.addPolygon(myNewPoly);
		}
		newSolution.setFitness(calcFitness(newSolution));
		return newSolution;
	}

	// percent (0.0 to 1.0) where 1.0 = perfect match
	public float calcFitness(GASolution solution) {
		Random rand = new Random();
		BufferedImage genPicture = solution.getImage();
		float avgFitness = 0;
		for(int i=0; i<FITNESS_SAMPLE_SIZE; i++) {
			int x = rand.nextInt(solution.width);
			int y = rand.nextInt(solution.height);
			Color a = new Color(realPicture.getRGB(x,y));
			Color b = new Color(genPicture.getRGB(x,y));
			// calculate euclidean distance
			avgFitness += Math.sqrt(Math.pow(a.getRed() - b.getRed(), 2) +
								Math.pow(a.getBlue() - b.getBlue(), 2) +
								Math.pow(a.getGreen() - b.getGreen(), 2));
		}
		avgFitness /= FITNESS_SAMPLE_SIZE;
		return (float)(avgFitness / MAX_FITNESS);
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




