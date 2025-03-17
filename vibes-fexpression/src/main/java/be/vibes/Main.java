package be.vibes;

import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.io.xml.XmlLoaders;
import be.vibes.solver.io.xml.XmlSavers;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, DimacsFormatException {
        test();
    }

    public static void test() throws IOException {
        /*
        String dirPath = "vibes-fexpression/src/main/resources/";
        Path filePath = Paths.get(dirPath + "robot.uvl");
        String content = new String(Files.readAllBytes(filePath));
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        FeatureModel fm = uvlModelFactory.parse(content);
        XmlSavers.save(fm, dirPath + "new.uvl.xml");
*/

        String dirPath = "vibes-fexpression/src/main/resources/";
        File file = new File(dirPath + "robot.xml");
        FeatureModel fm = XmlLoaders.loadFeatureModel(file);
        XmlSavers.save(fm, dirPath + "new.xml");

    }
}
