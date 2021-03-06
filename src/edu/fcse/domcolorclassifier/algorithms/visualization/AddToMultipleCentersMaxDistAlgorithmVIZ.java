package edu.fcse.domcolorclassifier.algorithms.visualization;

import edu.fcse.domcolorclassifier.ClassificationResultWithVisualization;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import edu.fcse.domcolorclassifier.ImgData;
import edu.fcse.domcolorclassifier.MethodToApply;
import edu.fcse.domcolorclassifier.colorutils.CustColor;
import edu.fcse.domcolorclassifier.functions.distance.DistanceFunction;
import edu.fcse.domcolorclassifier.functions.weight.WeightFunction;
import java.awt.color.CMMException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * extension to AddToMultipleCenters algorithm, checks to see if a pixel's color
 * is within a certain radius of a grav. center if yes it is valued, otherwise
 * discarded
 *
 * @author Blagoj Atansovski
 *
 */
public class AddToMultipleCentersMaxDistAlgorithmVIZ implements AlgorithmToApplyWithVisualization {

    @Override
    public ClassificationResultWithVisualization classifyImage(File fileToClassify, MethodToApply method, List<CustColor> gravityCenters) throws IOException {
        Map<CustColor, Double> colorAppearance = new HashMap<>();
        Map<CustColor, List<int[]>> magic = new HashMap<>();
        for (CustColor cc : gravityCenters) {
            colorAppearance.put(cc, 0.0);
            magic.put(cc, new LinkedList<int[]>());
        }
        try {
            BufferedImage imageToClassify = ImageIO.read(fileToClassify);
            ImgData imgData = new ImgData(imageToClassify);
            DistanceFunction distanceF = method.getDistanceFunction();
            WeightFunction weiF = method.getWeightFunction();
            float[][][] pixels = method.getSmooth().smooth(imgData.getRgbdata());
            float[][][] pixelsD = method.convertToColorSpace(pixels);
            int width = imageToClassify.getWidth();
            int height = imageToClassify.getHeight();

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {

                    double weight = weiF.getWeight(i, j, height / 2, width / 2);
                    for (int k = 0; k < gravityCenters.size(); k++) {
                        CustColor curr = gravityCenters.get(k);
                        float[] valuesCurr = curr.getValues();
                        double currDistance = distanceF.getDistance(valuesCurr,
                                pixelsD[i][j]);

                        if (currDistance <= method.getDiscardDistance()) {
                            magic.get(curr).add(new int[]{j, i});
                            double R = 1 / currDistance;//TODO: proveri so znaci ovoa dali imam dvojno delenje t.e. delenje so nula koma neso 
                            colorAppearance.put(curr, colorAppearance.get(curr)
                                    + weight * R);
                        }
                    }

                }
            }

            CustColor max = gravityCenters.get(0);
            System.out.println(colorAppearance.toString());
            double maxAppearence = colorAppearance.get(max);
            for (CustColor cc : colorAppearance.keySet()) {
                if (colorAppearance.get(cc) > maxAppearence) {
                    max = cc;
                    maxAppearence = colorAppearance.get(cc);
                }
            }
            ClassificationResultWithVisualization rez = new ClassificationResultWithVisualization(fileToClassify.getName(), max, magic, colorAppearance,width, height);
            return rez;
        } catch (CMMException ex) {
            throw new IOException("Could not read file: " + fileToClassify);
        }
    }
}
