package ru.sedinkin.calculator_ui;

import ru.sedinkin.calculator_core.builders.HyperbolaExpressionBuilder;
import ru.sedinkin.calculator_core.builders.InterferogramExpressionBuilder;
import ru.sedinkin.calculator_core.builders.UnsignedInterferogramExpressionBuilder;
import ru.sedinkin.calculator_core.objects.hyperbola.Hyperbola;
import ru.sedinkin.calculator_core.objects.interferogram.Interferogram;
import ru.sedinkin.calculator_core.objects.spline.Spline;
import ru.sedinkin.calculator_core.utils.SplineEquationResolver;
import ru.sedinkin.calculator_core.sample_printer.HyperbolaSamplePrinter;
import ru.sedinkin.calculator_core.sample_printer.InterferogramSamplePrinter;
import ru.sedinkin.calculator_core.sample_printer.SplineSamplePrinter;
import ru.sedinkin.calculator_core.expression_printer.HyperbolaExpressionPrinter;
import ru.sedinkin.calculator_core.expression_printer.InterferogramExpressionPrinter;
import ru.sedinkin.calculator_core.samplers.HyperbolaSampler;
import ru.sedinkin.calculator_core.samplers.InterferogramSampler;
import ru.sedinkin.calculator_core.samplers.SplineSampler;
import ru.sedinkin.calculator_ui.environment.YamlParser;
import ru.sedinkin.calculator_ui.utils.Persistence;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.sedinkin.calculator_ui.environment.Environment.*;

public class Runner {

    public static void main(String[] args) throws Exception {
        YamlParser.setDefaultTargetDirectory();
        YamlParser.parseRuntimeTargetDirectorySelection();
        if ( Objects.equals(RUNTIME_TARGET_DIR_SELECTION, "always") ) {
            selectTargetDirectory();
        }
        System.out.println(TARGET_DIRECTORY);
        start( selectSourceFiles() );
    }

    private static void start( File[] filesToOpen ) throws IOException {
        System.out.println("Start generating...");
        for ( File file : filesToOpen ) {
            int fileNum = Integer.parseInt( file.getName().split("\\.")[0] );
            String targetPath = TARGET_DIRECTORY + SEP + String.format("%d", fileNum);

            if ( Files.exists(Paths.get(targetPath)) ) {
                continue;
            }

            String spectrumDiscreteFilename = String.format("spectrum_discrete%d.dat", fileNum);
            String interferogramDiscreteFilename = String.format("interferogram_discrete%d.dat", fileNum);
            String interferogramAnalyticalFilename = String.format("interferogram_analytical%d.txt", fileNum);
            String hyperbolaDiscreteFilename = String.format("hyperbola_discrete%d.dat", fileNum);
            String hyperbolaAnalyticalFilename = String.format("hyperbola_analytical%d.txt", fileNum);

            Spline sbf = getSplineBasedFunction( file, 20 );
            getDiscreteSpectrum( sbf, targetPath, spectrumDiscreteFilename );

            Interferogram interferogram = getInterferogram( sbf );
            getDiscreteInterferogram( interferogram, targetPath, interferogramDiscreteFilename );
            getAnalyticalInterferogram( interferogram, targetPath, interferogramAnalyticalFilename );

            Hyperbola hyperbola = getHyperbola( interferogram );
            getDiscreteHyperbola( hyperbola, targetPath, hyperbolaDiscreteFilename );
            getAnalyticalHyperbola( hyperbola, targetPath, hyperbolaAnalyticalFilename );

            System.out.printf("\nThe directory for source file №%d has been generated.\n", fileNum);
        }
    }

    private static File[] selectSourceFiles() {
        Frame frame = new Frame("Source files issue");
        FileDialog fileDialog = new FileDialog( frame, "Select source files", FileDialog.LOAD );
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);
        File[] filesToOpen = fileDialog.getFiles();
        frame.dispose();
        return filesToOpen;
    }

    private static void selectTargetDirectory() throws Exception {
        setSystemLookAndFeel();

        Frame frame = new Frame("Source files issue");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            TARGET_DIRECTORY = fileChooser.getSelectedFile().getAbsolutePath();
        }
        frame.dispose();
    }

    public static void getAnalyticalHyperbola( Hyperbola hyperbola, String targetPath, String fileName )
            throws IOException {
        HyperbolaExpressionBuilder builder = new HyperbolaExpressionBuilder();
        HyperbolaExpressionPrinter printer = new HyperbolaExpressionPrinter();

        List<String> stringsToPrint = builder.perform( hyperbola, Map.of() );
        File analyticalHyperbolaFile = printer.perform( stringsToPrint, Map.of() );
        Persistence.persist(analyticalHyperbolaFile, targetPath, fileName);
    }

    public static void getDiscreteHyperbola( Hyperbola hyperbola, String targetPath, String fileName )
            throws IOException {
        HyperbolaSampler sampler = new HyperbolaSampler();
        HyperbolaSamplePrinter printer = new HyperbolaSamplePrinter();

        Map<Double, Double> samples = sampler
                .perform( hyperbola, Map.of("start", 1429.155, "end", 4000.092, "period", 1.929)  );
        File discreteHyperbolaFile = printer.perform( samples, Map.of() );
        Persistence.persist(discreteHyperbolaFile, targetPath, fileName);
    }

    public static Hyperbola getHyperbola( Interferogram interferogram ) {
        return new Hyperbola( interferogram );
    }

    public static void getAnalyticalInterferogram( Interferogram interferogram, String targetPath, String fileName )
            throws IOException {
        InterferogramExpressionBuilder builder = new UnsignedInterferogramExpressionBuilder();
        InterferogramExpressionPrinter printer = new InterferogramExpressionPrinter();

        List<String> stringsToPrint = builder.perform( interferogram, Map.of() );
        File analyticalInterferogramFile = printer.perform( stringsToPrint, Map.of() );
        Persistence.persist(analyticalInterferogramFile, targetPath, fileName);
    }

    public static void getDiscreteInterferogram( Interferogram interferogram, String targetPath, String fileName )
            throws IOException {
        InterferogramSampler sampler = new InterferogramSampler();
        InterferogramSamplePrinter printer = new InterferogramSamplePrinter();

        Map<Double, Double> samples = sampler
                .perform( interferogram, Map.of("start", 1429.155, "end", 4000.092, "period", 1.929) );
        File discreteInterferogramFile = printer.perform( samples, Map.of() );
        Persistence.persist(discreteInterferogramFile, targetPath, fileName);
    }

    public static Interferogram getInterferogram( Spline sbf ) {
        return new Interferogram( sbf );
    }

    public static void getDiscreteSpectrum( Spline sbf, String targetPath, String fileName )
            throws IOException {
        SplineSampler sampler = new SplineSampler();
        SplineSamplePrinter printer = new SplineSamplePrinter();

        Map<Double, Double> samples = sampler
                .perform( sbf, Map.of( "period", 1.929 ) );
        File discreteSpectrumFile = printer.perform( samples, Map.of() );
        Persistence.persist(discreteSpectrumFile, targetPath, fileName);
    }

    public static Spline getSplineBasedFunction( File file, int period ) {
        return SplineEquationResolver.resolve( file, period );
    }

}