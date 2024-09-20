import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TOPSISAlgorithm {
    private List<String> criteria;
    private List<Double> weights;
    private List<Boolean> isNegative;
    private List<List<Double>> decisionMatrix;
    private int numOptions;
    private List<String> optionNames; // Nouvelle liste pour stocker les noms des options

    public TOPSISAlgorithm(String csvFilePath) {
        criteria = new ArrayList<>();
        weights = new ArrayList<>();
        isNegative = new ArrayList<>();
        decisionMatrix = new ArrayList<>();
        optionNames = new ArrayList<>(); // Initialisation de la liste des noms d'options
        loadDataFromCSV(csvFilePath);
    }

    private void loadDataFromCSV(String csvFilePath) {
        String absolutePath = Paths.get(csvFilePath).toAbsolutePath().toString();
        System.out.println("Tentative de lecture du fichier : " + absolutePath);
        
        try (BufferedReader br = new BufferedReader(new FileReader(absolutePath))) {
            String line = br.readLine(); // Read header
            if (line == null) {
                throw new IOException("Le fichier CSV est vide");
            }
            String[] header = line.split(",");
            
            numOptions = header.length - 3;
            System.out.println("Nombre d'options: " + numOptions);

            // Stockage des noms des options
            for (int i = 3; i < header.length; i++) {
                optionNames.add(header[i]);
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                criteria.add(values[0]);
                weights.add(Double.parseDouble(values[1]));
                isNegative.add(values[2].equals("1"));
                
                List<Double> optionValues = new ArrayList<>();
                for (int i = 3; i < values.length; i++) {
                    optionValues.add(Double.parseDouble(values[i]));
                }
                decisionMatrix.add(optionValues);
            }
            
            System.out.println("Nombre de critères: " + criteria.size());
            System.out.println("Matrice de décision:");
            for (int i = 0; i < criteria.size(); i++) {
                System.out.println(criteria.get(i) + ": " + decisionMatrix.get(i));
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public List<Double> calculateTOPSIS() {
        System.out.println("Début du calcul TOPSIS");
        
        List<List<Double>> normalizedMatrix = normalizeMatrix();
        printMatrix("Matrice normalisée", normalizedMatrix);

        List<List<Double>> weightedNormalizedMatrix = calculateWeightedNormalizedMatrix(normalizedMatrix);
        printMatrix("Matrice normalisée pondérée", weightedNormalizedMatrix);

        List<Double> idealSolution = new ArrayList<>();
        List<Double> negativeIdealSolution = new ArrayList<>();

        for (int j = 0; j < criteria.size(); j++) {
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            for (int i = 0; i < numOptions; i++) {
                double value = weightedNormalizedMatrix.get(j).get(i);
                if (value > max) max = value;
                if (value < min) min = value;
            }

            idealSolution.add(isNegative.get(j) ? min : max);
            negativeIdealSolution.add(isNegative.get(j) ? max : min);
        }

        System.out.println("Solution idéale: " + idealSolution);
        System.out.println("Solution anti-idéale: " + negativeIdealSolution);

        List<Double> separationFromIdeal = new ArrayList<>();
        List<Double> separationFromNegativeIdeal = new ArrayList<>();

        for (int i = 0; i < numOptions; i++) {
            double sumIdeal = 0;
            double sumNegativeIdeal = 0;

            for (int j = 0; j < criteria.size(); j++) {
                double value = weightedNormalizedMatrix.get(j).get(i);
                sumIdeal += Math.pow(value - idealSolution.get(j), 2);
                sumNegativeIdeal += Math.pow(value - negativeIdealSolution.get(j), 2);
            }

            separationFromIdeal.add(Math.sqrt(sumIdeal));
            separationFromNegativeIdeal.add(Math.sqrt(sumNegativeIdeal));
        }

        System.out.println("Séparation de l'idéal: " + separationFromIdeal);
        System.out.println("Séparation de l'anti-idéal: " + separationFromNegativeIdeal);

        List<Double> relativeCloseness = new ArrayList<>();

        for (int i = 0; i < numOptions; i++) {
            double denominator = separationFromIdeal.get(i) + separationFromNegativeIdeal.get(i);
            if (denominator == 0) {
                System.out.println("Attention: Division par zéro évitée pour l'option " + (i + 1));
                relativeCloseness.add(0.0);
            } else {
                double value = separationFromNegativeIdeal.get(i) / denominator;
                relativeCloseness.add(value);
            }
        }

        System.out.println("Proximité relative: " + relativeCloseness);

        return relativeCloseness;
    }

    private List<List<Double>> normalizeMatrix() {
        List<List<Double>> normalizedMatrix = new ArrayList<>();

        for (int j = 0; j < criteria.size(); j++) {
            double sumOfSquares = 0;
            for (int i = 0; i < numOptions; i++) {
                sumOfSquares += Math.pow(decisionMatrix.get(j).get(i), 2);
            }
            double denominator = Math.sqrt(sumOfSquares);

            List<Double> normalizedColumn = new ArrayList<>();
            for (int i = 0; i < numOptions; i++) {
                if (denominator == 0) {
                    System.out.println("Attention: Division par zéro évitée lors de la normalisation");
                    normalizedColumn.add(0.0);
                } else {
                    normalizedColumn.add(decisionMatrix.get(j).get(i) / denominator);
                }
            }
            normalizedMatrix.add(normalizedColumn);
        }

        return normalizedMatrix;
    }

    private List<List<Double>> calculateWeightedNormalizedMatrix(List<List<Double>> normalizedMatrix) {
        List<List<Double>> weightedNormalizedMatrix = new ArrayList<>();

        for (int j = 0; j < criteria.size(); j++) {
            List<Double> weightedColumn = new ArrayList<>();
            for (int i = 0; i < numOptions; i++) {
                weightedColumn.add(normalizedMatrix.get(j).get(i) * weights.get(j));
            }
            weightedNormalizedMatrix.add(weightedColumn);
        }

        return weightedNormalizedMatrix;
    }

    private void printMatrix(String name, List<List<Double>> matrix) {
        System.out.println(name + ":");
        for (int i = 0; i < criteria.size(); i++) {
            System.out.println(criteria.get(i) + ": " + matrix.get(i));
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String csvFilePath = "options.csv";
        if (args.length > 0) {
            csvFilePath = args[0];
        }
        TOPSISAlgorithm topsis = new TOPSISAlgorithm(csvFilePath);
        List<Double> results = topsis.calculateTOPSIS();

        System.out.println("\nRésultats TOPSIS :");
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("%s : %.4f%n", topsis.optionNames.get(i), results.get(i));
        }

        int bestOptionIndex = results.indexOf(results.stream().max(Double::compare).get());
        System.out.printf("\nLa meilleure option est %s avec un score de %.4f%n", 
                          topsis.optionNames.get(bestOptionIndex), 
                          results.get(bestOptionIndex));
    }
}