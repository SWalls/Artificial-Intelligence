import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import java.util.ArrayList;
import java.lang.InterruptedException;

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

	public GASolution(int _width, int _height) {
		shapes = new ArrayList<MyPolygon>();
		width = _width;
		height = _height;
	}

	public void addPolygon(MyPolygon p) {
		shapes.add(p);
	}	

	public ArrayList<MyPolygon> getShapes() {
		return shapes;
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
		BufferedImage image = solution.getImage();
		g.drawImage(image, 0, 0, null);
    }
}


public class GA extends JComponent{
	
    GACanvas canvas;
    int width, height;
    BufferedImage realPicture;
    ArrayList<GASolution> population;

    // Adjust these parameters as necessary for your simulation
    double MUTATION_RATE = 0.01;
    double CROSSOVER_RATE = 0.6;
    int MAX_POLYGON_POINTS = 5;
    int MAX_POLYGONS = 10;

    public GA(GACanvas _canvas, BufferedImage _realPicture) {
        canvas = _canvas;
        realPicture = _realPicture;
        width = realPicture.getWidth();
        height = realPicture.getHeight();
        population = new ArrayList<GASolution>();

        // You'll need to define the following functions
        createPopulation(50);	// Make 50 new, random chromosomes
    }

    // YOUR CODE GOES HERE!
	

    public void runSimulation() {

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

        GA pt = new GA(theCanvas, realPicture);
            pt.runSimulation();
    }
}




