/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nanotech;
import com.thoughtworks.xstream.XStream;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;





/**
 *
 * @author Artem
 */

class Station {
   String Name;
   int x,y;
   List<String> Date = new ArrayList<String>();
   List<String> Types = new ArrayList<String>();
   ArrayList<ArrayList<Double>> Data = new ArrayList<ArrayList<Double>>();
   Station(String Name)
   {
       this.Name = Name;      
   }
   
}

class WeightedPoint {

    int x;
    int y;
    double value;

    WeightedPoint(int x, int y, double value) {

        this.x = x;
        this.y = y;
        this.value = value;
    }
}

class ColorRange {

    double min;
    double max;
    Color color;

    ColorRange(double min, double max, Color color) {

        this.min = min;
        this.max = max;
        this.color = color;
    }
}



public class Converter {
    
    static List<WeightedPoint> weightedPoints = new ArrayList<WeightedPoint>();
    static List<ColorRange> colorRanges = new ArrayList<ColorRange>();
    static int power = 4;
    
    static List<String> Folders = new ArrayList<String>();
    static List<Station> Stations = new ArrayList<Station>();
    static List<String> UniqTypes = new ArrayList<String>();
    
    static DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
    static DateFormat dff = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    static Calendar c = Calendar.getInstance();
    static Calendar cc = Calendar.getInstance();
    

    
    public static void main(String[] args) {

        
        String start = "01.01.2099 00:00";
        String finish = "01.01.2000 00:00";
        
        File configFile = new File("converter.cfg");
        Properties config = new Properties();
        try {
            FileInputStream is= new FileInputStream(configFile);
            config.loadFromXML(is);
        } catch (IOException ignored) {
        }
        /*
        try {
            FileOutputStream os= new FileOutputStream(configFile);
            config.setProperty("data-file", "/Users/evilcrab/Dropbox/data.xml");
            config.setProperty("map-file", "/Users/evilcrab/Dropbox/map.png");
            config.setProperty("output-folder", "/Users/evilcrab/Dropbox/output");
            config.storeToXML(os, "Converter Settings");
            System.exit(0);
        } catch (IOException ex) {
        } */
        

        
        
        if(args.length == 0)
            System.exit(0);
        
        if(args[0].equals("setProperty"))
        {
            
            try {
                FileOutputStream os = new FileOutputStream(configFile);
                config.setProperty(args[1], args[2]);
                config.storeToXML(os, "Converter Settings");
                System.exit(0);
            } catch (IOException ignored) {
            }
            System.exit(0);
        }
        
        if(args[0].equals("convertData"))
        {
            convertToXml(args[1]);
            XStream xstream = new XStream();
            String xml = xstream.toXML(Stations);
            try {
                PrintWriter writer = new PrintWriter(args[2], "UTF-8");
                writer.println(xml);
                writer.close();
            } catch (FileNotFoundException ignored) {
            } catch (UnsupportedEncodingException ignored) {
            }
            System.exit(0);
        }
        
        if(args[0].equals("dataInfo"))
        {
            String data = "";
            try {
                data = FileUtils.readFileToString(new File(config.getProperty("data-file")), "UTF-8");
            } catch (IOException ignored) {
            }
            
            XStream xstream = new XStream();
            Stations = (List<Station>) xstream.fromXML(data);

            for (Station Station : Stations) {
                for (int j = 0; j < Station.Types.size(); j++) {
                    if (!UniqTypes.contains(Station.Types.get(j))) {
                        UniqTypes.add(Station.Types.get(j));
                    }
                }
            }


            System.out.println("Data file: " + config.getProperty("data-file"));
            System.out.println("Chemicals: " + UniqTypes);
            System.out.println();

            for (String UniqType : UniqTypes) {
                boolean exists = false;

                start = "01.01.2099 00:00";
                finish = "01.01.2000 00:00";

                for (Station Station : Stations) {

                    for (int j = 0; j < Station.Types.size(); j++) {
                        if (Station.Types.get(j).equals(UniqType)) {
                            exists = true;
                        }
                    }

                    if (exists) {
                        for (int j = 0; j < Station.Date.size(); j++) {
                            try {
                                c.setTime(df.parse(Station.Date.get(j)));
                                cc.setTime(dff.parse(start));
                                if (c.before(cc)) {
                                    start = Station.Date.get(j);
                                }
                                cc.setTime(dff.parse(finish));
                                if (c.after(cc)) {
                                    finish = Station.Date.get(j);
                                }
                            } catch (ParseException ignored) {
                            }
                        }
                    }

                    exists = false;
                }
                System.out.println(UniqType);
                System.out.println("Earliest records: " + start);
                System.out.println("Most recent records: " + finish);
                System.out.println();

            }
                
            System.out.println("Earliest records: " + start);
            System.out.println("Most recent records: " + finish);

        }

        
        if(args[0].equals("generatePng"))
        {
            String data = "";
            try {
                data = FileUtils.readFileToString(new File(config.getProperty("data-file")), "UTF-8");
            } catch (IOException ignored) {
            }
            
            XStream xstream = new XStream();
            Stations = (List<Station>) xstream.fromXML(data);
            
            String date;
            String type;
            int typeIndex;
            int dateIndex;
            double max = 0.0;
            
            date = args[2];
            type = args[3];
            
            boolean b = true;

            for (Station Station : Stations)
                if (Station.Types.contains(type))
                    if (Station.Date.contains(date)) {
                        typeIndex = Station.Types.indexOf(type);
                        dateIndex = Station.Date.indexOf(date);
                        if (max < Station.Data.get(typeIndex).get(dateIndex)) {
                            max = Station.Data.get(typeIndex).get(dateIndex);
                        }
                        if(Station.Data.get(typeIndex).get(dateIndex) != -1.0)
                            b = false;
                    }

            if (b) {
                System.out.println("No data about this chemical on this date");
                System.exit(0);
            }
            
            initializeColors(max);
            
            if(args[1].equals("layer"))
            {
                BufferedImage image = generateLayer(type, date);
                String path = "/layer " + type + " " + date + ".png";
                path = path.replaceAll(":", ".");
                path = config.getProperty("output-folder") + path;
                File imageFile = new File(path);
                try {
                    ImageIO.write(image, "png", imageFile);
                } catch (IOException ignored) {
                }
                System.exit(0);
            }
            
            if(args[1].equals("image"))
            {
                try {
                    BufferedImage image = generateLayer(type, date);
                    BufferedImage map = ImageIO.read(new File(config.getProperty("map-file"))); 
                    BufferedImage combination = combineLayers(map, image, type, date);
                    
                    String path = "/image " + type + " " + date + ".png";
                    path = path.replaceAll(":", ".");
                    path = config.getProperty("output-folder") + path;
                    File imageFile = new File(path);
                    ImageIO.write(combination, "png", imageFile);
                    System.exit(0);
                } catch (IOException ignored) {
                }
            }   
        }
        
        if(args[0].equals("generateGif"))
        {
            String data = "";
            try {
                data = FileUtils.readFileToString(new File(config.getProperty("data-file")), "UTF-8");
            } catch (IOException ignored) {
            }
            
            XStream xstream = new XStream();
            Stations = (List<Station>) xstream.fromXML(data);
            
            String dateStart;
            String dateFinish;
            int intervalTime;
            int intervalSlides;
            String type;
            int typeIndex;
            int dateIndex;
            double max = 0.0;
            
            dateStart = args[2];
            dateFinish = args[3];
            intervalTime = Integer.parseInt(args[4]);
            type = args[5];
            intervalSlides = Integer.parseInt(args[6]);
            
            boolean b;
            
            List<BufferedImage> layers = new ArrayList<BufferedImage>();
            List<String> dates = new ArrayList<String>();
            
            
            SimpleDateFormat parser=new SimpleDateFormat("dd.MM.yyyy HH:mm");
            try {
                Calendar c = Calendar.getInstance();
                Calendar ce = Calendar.getInstance();
                c.setTime(parser.parse(dateStart));
                ce.setTime(parser.parse(dateFinish)); 
                while(c.before(ce) || c.equals(ce))
                {
                    String tmp = parser.format(c.getTime());
                    b = true;
                    for (com.nanotech.Station Station : Stations) {
                        if (Station.Types.contains(type)) {
                            if (Station.Date.contains(tmp)) {
                                typeIndex = Station.Types.indexOf(type);
                                dateIndex = Station.Date.indexOf(tmp);
                                if (max < Station.Data.get(typeIndex).get(dateIndex))
                                    max = Station.Data.get(typeIndex).get(dateIndex);
                                if(Station.Data.get(typeIndex).get(dateIndex) != -1.0)
                                    b = false;
                            }
                        }
                    }

                    if (!b) {
                        dates.add(tmp);
                    }   
                    c.add(Calendar.HOUR, intervalTime);
                }
                
            } catch (ParseException ignored) {
            }

            if (dates.isEmpty()) {
                System.out.println("No data about this chemical on this dates");
                System.exit(0);
            }
            
            initializeColors(max);
            
            for (int i = 0; i < dates.size(); i++) {
                System.out.println("Generating layer " + i);
                layers.add(generateLayer(type, dates.get(i)));
                /*String path = "\\layer " + type + " " + dates.get(i) + ".png";
                path = path.replaceAll(":", ".");
                path = config.getProperty("output-folder") + path;
                File imageFile = new File(path);
                try {
                    ImageIO.write(layers.get(i), "png", imageFile);
                } catch (IOException ignored) {
                }   */
            }

            
            
            if(args[1].equals("layer"))
            {
                
                
                String path = "/layer " + type + " " + dateStart + "-" + dateFinish + ".gif";
                path = path.replaceAll(":", ".");
                path = config.getProperty("output-folder") + path;
                
                AnimatedGifEncoder gif = new AnimatedGifEncoder();
                gif.start(path);
                gif.setDelay(intervalSlides);
                gif.setRepeat(0);
                for( int i = 0; i < layers.size(); i++){
                    System.out.println("Adding layer " + i);
                    gif.addFrame(layers.get(i));
                }
                    
                gif.finish();
 
                System.exit(0);
            }
            
            if(args[1].equals("image"))
            {
                try {
                    String path = "/image " + type + " " + dateStart + "-" + dateFinish + ".gif";
                    path = path.replaceAll(":", ".");
                    path = config.getProperty("output-folder") + path;
                    
                    BufferedImage map = ImageIO.read(new File(config.getProperty("map-file")));
                    AnimatedGifEncoder gif = new AnimatedGifEncoder();
                    gif.start(path);
                    gif.setDelay(intervalSlides);
                    gif.setRepeat(0);
                    for( int i = 0; i < layers.size(); i++){
                        System.out.println("Adding layer " + i);
                        gif.addFrame(combineLayers(map, layers.get(i), type, dates.get(i)));
                    }
                        
                    gif.finish();
     
                    System.exit(0);
                } catch (IOException ignored) {
                }
            }
             
             
        }
        
    }
    
    static BufferedImage generateLayer(String type, String date)
    {
        int typeIndex;
        int dateIndex;

        weightedPoints.clear();

        for (Station Station : Stations) {
            if (Station.Types.contains(type)) {
                if (Station.Date.contains(date)) {
                    typeIndex = Station.Types.indexOf(type);
                    dateIndex = Station.Date.indexOf(date);
                    if(Station.Data.get(typeIndex).get(dateIndex) != -1.0)
                        weightedPoints.add(new WeightedPoint(Station.x, Station.y, Station.Data.get(typeIndex).get(dateIndex)));
                }
            }
        }


        BufferedImage image = getImage(600, 600);

        Graphics2D g2d = image.createGraphics();
        g2d.setFont(new Font("Calibri", Font.PLAIN, 20));
        g2d.setColor(Color.black);
        g2d.drawString(date + " " + type, 10, 30);
        g2d.drawString(colorRanges.get(0).min + "", 35, 375);
        g2d.drawString(colorRanges.get(colorRanges.size() - 1).max + "", 35, 560);
        g2d.drawRect(19, 359, 11, 201);

        for (Station Station : Stations)
            if (Station.Types.contains(type))
                if (Station.Date.contains(date)) {
                    typeIndex = Station.Types.indexOf(type);
                    dateIndex = Station.Date.indexOf(date);
                    if(Station.Data.get(typeIndex).get(dateIndex) != -1.0)
                        g2d.drawOval(Station.x, Station.y, 2, 2);
                }


        int counter = 0;
        for (ColorRange r : colorRanges) {
            g2d.setColor(r.color);
            g2d.fillRect(20, 360 + counter * 2, 10, 2);
            counter++;
        }

        return image;
    }
    
    static BufferedImage combineLayers(BufferedImage back, BufferedImage image, String type, String date)
    {
        int typeIndex;
        int dateIndex;

        BufferedImage combination = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combination.createGraphics();

        g2d.drawImage(back, 0, 0, null);
        
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
        g2d.setComposite(ac);
        g2d.drawImage(image, 0, 0, null);

        g2d.setFont(new Font("Calibri", Font.PLAIN, 20));
        g2d.setColor(Color.black);
        ac = AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f);
        g2d.setComposite(ac);
        g2d.drawString(date + " " + type, 10, 30);
        g2d.drawString(colorRanges.get(0).min + "", 35, 375);
        g2d.drawString(colorRanges.get(colorRanges.size() - 1).max + "", 35, 560);
        g2d.drawRect(19, 359, 11, 201);

        for (Station Station : Stations)
            if (Station.Types.contains(type))
                if (Station.Date.contains(date)) {
                    typeIndex = Station.Types.indexOf(type);
                    dateIndex = Station.Date.indexOf(date);
                    if(Station.Data.get(typeIndex).get(dateIndex) != -1.0)
                        g2d.drawOval(Station.x, Station.y, 2, 2);
                }
        
        return combination;
    }
    
    private static void convertToXml(String path)
    {
        
        File fname = new File(path);
        File[] fileNames;
        fileNames = fname.listFiles();
        for (File fileName1 : fileNames) {
            if (fileName1.isDirectory()) {
                Folders.add(fileName1.getPath());
                Stations.add(new Station(fileName1.getName()));
            }
        }
       
       
        
        for(int i = 0; i < Folders.size(); i++)
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader(Folders.get(i)+"/coord.txt"));
                String line = br.readLine();
                Stations.get(i).x = Integer.parseInt(line.split(" ")[0]);
                Stations.get(i).y = Integer.parseInt(line.split(" ")[1]);
                br.close();
            } catch (Exception ignored) {
            }
          
        }
        
 
        String d, M, y, h, m, line, curDate;
        String[] ss;
        BufferedReader br;

        for(int i = 0; i < Folders.size(); i++)
        {
            fname = new File(Folders.get(i));
            fileNames = fname.listFiles();
            for (File fileName : fileNames) {
                if (fileName.getName().equals("coord.txt"))
                    continue;

                System.out.println("Filename: " + fileName.getName());
                try {
                    br = new BufferedReader(new FileReader(fileName));
                    line = br.readLine();
                    line = line.trim();
                    ss = line.split(" ");
                    if (Stations.get(i).Types.isEmpty()) {
                        for (int ii = 2; ii < ss.length; ii++) {
                            Stations.get(i).Types.add(ss[ii]);
                            Stations.get(i).Data.add(new ArrayList<Double>());
                        }
                    }

                    line = br.readLine();
                    line = line.trim();
                    ss = line.split(" ");

                    c.setTime(df.parse(ss[0] + " " + ss[1]));
                    d = (c.get(Calendar.DATE) < 10) ? ("0" + String.valueOf(c.get(Calendar.DATE))) : (String.valueOf(c.get(Calendar.DATE)));
                    M = (c.get(Calendar.MONTH) + 1 < 10) ? ("0" + String.valueOf(c.get(Calendar.MONTH) + 1)) : (String.valueOf(c.get(Calendar.MONTH) + 1));
                    y = String.valueOf(c.get(Calendar.YEAR));
                    h = (c.get(Calendar.HOUR_OF_DAY) < 10) ? ("0" + String.valueOf(c.get(Calendar.HOUR_OF_DAY))) : (String.valueOf(c.get(Calendar.HOUR_OF_DAY)));
                    m = (c.get(Calendar.MINUTE) < 10) ? ("0" + String.valueOf(c.get(Calendar.MINUTE))) : (String.valueOf(c.get(Calendar.MINUTE)));
                    curDate = d + "." + M + "." + y + " " + h + ":" + m;


                    if (!Stations.get(i).Date.contains(curDate)) {
                        Stations.get(i).Date.add(curDate);
                        for (int ii = 2; ii < ss.length; ii++) {
                            Stations.get(i).Data.get(ii - 2).add((ss[ii].equals("----")) ? (-1.0) : (Double.parseDouble(ss[ii])));
                        }

                    }

                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        ss = line.split(" ");
                        c.add(Calendar.HOUR_OF_DAY, 1);
                        d = (c.get(Calendar.DATE) < 10) ? ("0" + String.valueOf(c.get(Calendar.DATE))) : (String.valueOf(c.get(Calendar.DATE)));
                        M = (c.get(Calendar.MONTH) + 1 < 10) ? ("0" + String.valueOf(c.get(Calendar.MONTH) + 1)) : (String.valueOf(c.get(Calendar.MONTH) + 1));
                        y = String.valueOf(c.get(Calendar.YEAR));
                        h = (c.get(Calendar.HOUR_OF_DAY) < 10) ? ("0" + String.valueOf(c.get(Calendar.HOUR_OF_DAY))) : (String.valueOf(c.get(Calendar.HOUR_OF_DAY)));
                        m = (c.get(Calendar.MINUTE) < 10) ? ("0" + String.valueOf(c.get(Calendar.MINUTE))) : (String.valueOf(c.get(Calendar.MINUTE)));
                        curDate = d + "." + M + "." + y + " " + h + ":" + m;

                        if (!Stations.get(i).Date.contains(curDate)) {
                            Stations.get(i).Date.add(curDate);
                            for (int ii = 2; ii < ss.length; ii++) {
                                Stations.get(i).Data.get(ii - 2).add((ss[ii].equals("----")) ? (-1.0) : (Double.parseDouble(ss[ii])));
                            }

                        }
                    }
                    br.close();
                } catch (Exception ignored) {
                }
            }
        }
        
    }


    static void initializeColors(double max) {
 
        float r = 0.33f;
        int n = 100;
 
        for (int i = 0; i < n; i++) {
            r += 0.7222f / n;
 
            colorRanges.add(new ColorRange(i * (max / n), i * (max / n)
                    + (max / n), Color.getHSBColor(r, 1f, 1f)));
        }
 
    }
    
    static BufferedImage getImage(int width, int height) {
 
        BufferedImage bufferedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        double[][] imgArray;
        imgArray = interpolateImage(width, height);
        
        double max = 0.0;
         for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(imgArray[i][j]>max) {
                    max = imgArray[i][j];
                }
            }
         }

        
        drawImage(bufferedImage);
 
        return bufferedImage;
    }
    
     static double[][] interpolateImage(int width, int height) {

        double[][] imgArray = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imgArray[i][j] =  getValueShepard(i, j);
            }
        }
        return imgArray;
    }
    
     private static void drawImage(BufferedImage bufferedImage) {

        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                bufferedImage.setRGB(i, j, getColor(getValueShepard(i, j)));
            }
        }
    }
 
    static double getValueShepard(int i, int j) {
 
        double dTotal = 0.0;
        double result = 0.0;
 
        for (WeightedPoint p : weightedPoints) {
 
            double d = distance(p.x,p.y, i, j);
            if (power != 1) {
                d = Math.pow(d, power);
            }
            if (d > 0.0) {
                d = 1.0 / d;
            } else { // if d is real small set the inverse to a large number
                     // to avoid INF
                d = 1.e20;
            }
            result += p.value * d;
            dTotal += d;
        }
 
        if (dTotal > 0) {
            return result / dTotal;
        } else {
            return 0;
        }
 
    }
 
    static int getColor(double val) {
        for (ColorRange r : colorRanges) {
            if (val >= r.min && val < r.max) {
                return r.color.getRGB();
            }
        }
        return 0;
    }
    
    static double distance(double xDataPt, double yDataPt, double xGrdPt,
            double yGrdPt) {
        double dx = xDataPt - xGrdPt;
        double dy = yDataPt - yGrdPt;
        return Math.sqrt(dx * dx + dy * dy);
    }
 
    // bufferedImage.setRGB(i, j, new Random(100).nextInt());
 
    
    
    
 
}
