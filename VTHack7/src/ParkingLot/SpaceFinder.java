package ParkingLot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.imageio.*;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;



/**
 * A program designed to load an image of a parking lot,
 * and identify which spots are empty.
 * @author Luke Wevley
 * @version 2020.02.14
 */

public class SpaceFinder{
    
    private static BufferedImage bigLot;
    private static BufferedImage smallLot;
    /**
     * POINT 1 OF ARRAYS ARE BOTTOM RIGHT CORNERS
     * AND SHOULD BE USED FOR SCALING
     * THEY ARE NOT PARKING SPACES!!!
     */
    private static Point[] blueLot = new Point[76];
    private static Point[] redLot = new Point[838];
    private static Point[] greenLot = new Point[853];
    private static Point[] orangeLot = new Point[97];
    private static Scanner kb;
    private static Color road = new Color(115, 116, 119);
    //supposedly allows for web searching?
    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    
    public static void main(String[]arg)
    {
        loadPictures();
        //fill(orangeLot);
        //locatePoints(blueLot);
        locatePoints(redLot);
        //locatePoints(greenLot);
        //locatePoints(orangeLot);
        boolean[] filled = checkSpots(redLot);
        
        //SpaceFinder obj = new SpaceFinder();
        //obj.sendGet();
        //obj.sendData();
        
        
        try {
            PrintWriter printWriter = new PrintWriter("orangeLot2.txt");
            for (int m = 0; m < orangeLot.length; m++)
            {
                printWriter.println(orangeLot[m].getX() + " " + orangeLot[m].getY() + " " + filled[m] + " orange");
            }
            printWriter.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        
        
    }
    /**
     * loads images of two parking lots into the class for later use
     * 
     * requires two files called parkingLotBig.png and parkingLotSmall.png
     */
    public static void loadPictures()
    {
        try {
            bigLot = ImageIO.read(new File("parkingLotBig.png"));
            smallLot = ImageIO.read(new File("parkingLotSmall.png"));
        }
        catch (IOException e) {
            System.out.println("file not found");
        }
    }
    /**
     * when run, prompts users to mouse over elements of images of
     * parkinglLotBig and parkingLotSmall while pressing enter
     * 
     * @param points The array of points w/length of available spots
     * @return Point[] if not created yet yet
     */
    public static Point[] locatePoints(Point[] points)
    {
        System.out.println("Press enter with mouse on top "+
            "left corner of image");
        kb = new Scanner(System.in);
        kb.reset();
        String dummy = kb.nextLine();
        Point origin = MouseInfo.getPointerInfo().getLocation();
        int dx = (int)origin.getX()*-1;
        int dy = (int)origin.getY()*-1;
        System.out.println("Press enter with mouse on bottom "+
            "right corner of image"+dummy);
        dummy = kb.nextLine();
        Point temp = MouseInfo.getPointerInfo().getLocation();
        temp.translate(dx, dy);
        points[0] = temp;
        System.out.println("Press enter when hovering the mouse"+
            "over a parking spot");
        for (int m = 1; m < points.length; m++)
        {
            dummy = kb.nextLine();
            temp = MouseInfo.getPointerInfo().getLocation();
            temp.translate(dx, dy);
            points[m] = temp;
        }
        PrintWriter printWriter;
        System.out.println("Lot completed!");
        try {
            printWriter = new PrintWriter(".txt");
            for (Point m: points)
            {
                printWriter.println(m.getX() + " " + m.getY());
            }
            printWriter.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        return points;
    }
    
    /**
     * if the location of points is already created, this can
     * be used instead of locatePoints to fill with given
     * locations
     * 
     * @param points the array to be filled
     */
    public static void fill(Point[] points)
    {
        try {
            Scanner input = new Scanner(new File("orangeLot.txt"));
            points[0] = new Point((int)input.nextDouble(), (int)input.nextDouble());
            double dx = smallLot.getWidth() / points[0].getX();
            double dy = smallLot.getHeight() / points[0].getY();
            points[0] = new Point((int)(points[0].getX() * dx), (int)(points[0].getY() * dy));
            for (int m = 1; m < points.length; m++)
            {
                //points[m] = new Point((int)input.nextDouble(), (int)input.nextDouble());
                double tempX = input.nextDouble();
                double tempY = input.nextDouble();
                points[m] = new Point((int)(tempX * dx), (int)(tempY * dy));
                //System.out.println(points[m]);
            }
            input.close();
            //System.out.println(points[0]);
            //System.out.println(smallLot.getWidth()+" "+smallLot.getHeight());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * identifies whether a car exists in a given spot by
     * comparing color to an existing road color
     * 
     * @param points set of points that will be checked for cars
     * @return boolean[] of whether a given spot is filled
     *          spot is true if available
     */
    public static boolean[] checkSpots(Point[] points)
    {
        boolean[] filled = new boolean[points.length];
        for (int m = 0; m < points.length; m++)
        {
            int range = 35;
            Color color = new Color(smallLot.getRGB((int)points[m].getX()-1, (int)points[m].getY()-1));
            filled[m] = Math.abs(color.getRed() - road.getRed()) <= range &&
                Math.abs(color.getGreen() - road.getGreen()) <=range &&
                Math.abs(color.getBlue() - road.getRed()) <= range;
        }
        return filled;
    }
    
    /**
     * receives the data in the firebase database and displays it
     */
    private void sendGet() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create("https://parking-helper-268303.firebaseio.com/data/lotAsked.json")).setHeader("User-Agent", "Space Finder").build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
    /*
    private void sendPost() throws Exception
    {
        Map<Object, Object> data = new HashMap<>();
        data.put("closestSpace", "23");
        HttpRequest request = HttpRequest.newBuilder().POST(buildFormDataFromMap(data)).uri(URI.create("https://parking-helper-268303.firebaseio.com/data/lotGiven/.json")).setHeader("User-Agent", "Space Finder").build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
    
    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<Object, Object> entry: data.entrySet())
        {
            if (builder.length() > 0)
            {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        System.out.println(builder.toString());
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
    */
    
    public void sendData()
    {
        /*
        FileInputStream serviceAccount =
            new FileInputStream("path/to/serviceAccountKey.json");

          FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://parking-helper-268303.firebaseio.com")
            .build();

          FirebaseApp.initializeApp(options);
          */
    }
}
