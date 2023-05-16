package part1;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.stream.*;

public class Program {

    public static Map<String, ArrayList<String>> features = new HashMap<>() {
        {
            put("age", new ArrayList<>(
                    Arrays.asList("10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-99")));
            put("menopause", new ArrayList<>(Arrays.asList("lt40", "ge40", "premeno")));
            put("tumor-size", new ArrayList<>(Arrays.asList("0-4", "5-9", "10-14", "15-19", "20-24", "25-29", "30-34",
                    "35-39", "40-44", "45-49", "50-54", "55-59")));
            put("inv-nodes", new ArrayList<>(Arrays.asList("0-2", "3-5", "6-8", "9-11", "12-14", "15-17", "18-20",
                    "21-23", "24-26", "27-29", "30-32", "33-35", "36-39")));
            put("node-caps", new ArrayList<>(Arrays.asList("yes", "no")));
            put("deg-malig", new ArrayList<>(Arrays.asList("1", "2", "3")));
            put("breast", new ArrayList<>(Arrays.asList("left", "right")));
            put("breast-quad",
                    new ArrayList<>(Arrays.asList("left_up", "left_low", "right_up", "right_low", "central")));
            put("irradiat", new ArrayList<>(Arrays.asList("yes", "no")));
        }
    };

    public static ArrayList<String> classes = new ArrayList<String>(
            Arrays.asList("recurrence-events", "no-recurrence-events"));

    public static ArrayList<Instance> readFile(File file) {
        ArrayList<Instance> instances = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);
            String[] title = sc.nextLine().split(",");
            while (sc.hasNextLine()) {
                String[] currentLine = sc.nextLine().split(",");
                Map<String, String> data = new HashMap<>();
                String classString = currentLine[1];
                for (int i = 2; i < currentLine.length; i++) {
                    data.put(title[i].toLowerCase(), currentLine[i]);
                }
                instances.add(new Instance(data, classString));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return instances;
    }

    // CLASSIFY
    public static List<Instance> classify(List<Instance> trainingInstances, List<Instance> testInstances) {
        ArrayList<Instance> newList = new ArrayList<>();
        return newList;
    }

    public static Map<String, TreeNode> training(List<Instance> trainingInstances) {
        Map<String, TreeNode> test = new HashMap<String, TreeNode>();
        for (String classString : classes) {
            test.put(classString, new TreeNode(classString));
            for (String feature : features.keySet()) {
                test.get(classString).data.put(feature, new TreeNode(feature));
                for (String value : features.get(feature)) {
                    test.get(classString).data.get(feature).data.put(value, new TreeNode(value));
                }
            }
        }

        for (Instance instance : trainingInstances) {
            test.get(instance.classString).count++;
            for (String feature : instance.data.keySet()) {
                String currentKey = instance.data.get(feature);
                test.get(instance.classString).data.get(feature).data.get(currentKey).count++;
            }
        }

        int classTotal = 0;
        for (String classString : classes) {
            classTotal += test.get(classString).count;
            for (String feature : features.keySet()) {
                int total = 0;
                for (String key : test.get(classString).data.get(feature).data.keySet()) {
                    total += test.get(classString).data.get(feature).data.get(key).count;
                }
                test.get(classString).data.get(feature).setTotal(total);
            }
        }

        for (String classString : classes) {
            TreeNode classNode = test.get(classString);
            double probability = (double) classNode.count / classTotal;
            test.get(classString).setProbability(probability);

            for (String feature : features.keySet()) {
                for (String key : test.get(classString).data.get(feature).data.keySet()) {
                    TreeNode featureNode = test.get(classString).data.get(feature);
                    double featureProbability = (double) featureNode.data.get(key).count / (double) featureNode.total;
                    test.get(classString).data.get(feature).data.get(key).setProbability(featureProbability);
                }
            }
        }
        return test;
    }

    public static void predict(Map<String, TreeNode> test, List<Instance> testInstances) {

        double count = 0;


        System.out.println("No-recurrence-probability,     Recurrence-probability,         Predicted,    Actual");

        String output = "";
        for (Instance instance : testInstances) {
            double scoreX = score(test, instance, "no-recurrence-events");
            double scoreY = score(test, instance, "recurrence-events");

            if (scoreX > scoreY && instance.classString.equals("no-recurrence-events")) {
                count++;
            } else if (scoreX < scoreY && instance.classString.equals("recurrence-events")) {
                count++;
            }

            String s = scoreX > scoreY ? "no-recurrence" : "recurrence";
            System.out.println(scoreX + ",         " + scoreY + ",     " + s + ",   " + instance.classString);
        }

        double accuracy = 100 * (double) count / (double) testInstances.size();
        System.out.println("Total Accuracy:" + accuracy + "%");
    }

    public static double score(Map<String, TreeNode> test, Instance instance, String classString) {
        double score = test.get(classString).prob;
        for (String feature : instance.data.keySet()) {
            score = score * test.get(classString).data.get(feature).data.get(instance.data.get(feature)).prob;
        }
        return score;
    }

    public static void main(String[] args) {
        String path = "part2data/";
        ArrayList<Instance> trainingInstances = readFile(new File(path + args[0]));
        ArrayList<Instance> testInstances = readFile(new File(path + args[1]));
        Map<String, TreeNode> test = training(trainingInstances);

        String classOutputs = "";
        System.out.println("1.\n");
        for(String label : test.keySet()){
            classOutputs += "P(" + label + ") = " + test.get(label).toString() + "\n";
            for(String feature : test.get(label).data.keySet()){
                for(String value : test.get(label).data.get(feature).data.keySet()){
                    System.out.println("P("+feature+" = "+value+" | " + label +") = " +test.get(label).data.get(feature).data.get(value).toString());
                }
            }
        }
        System.out.println("\n2.\n\n"+ classOutputs);
        
        System.out.println("3.\n");
        predict(test, testInstances);
    }
}

class TreeNode {

    Map<String, TreeNode> data = new HashMap<>();
    int count = 1;
    int total;
    double prob = 0;
    String name;

    public TreeNode(String name) {
        this.name = name;
    }

    public int count() {
        return count;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setProbability(double prob) {
        this.prob = prob;
    }

    public String toString(){
        return String.valueOf(prob);
    }
}

class Instance {
    Map<String, String> data = new HashMap<>();
    String classString;

    public Instance(Map<String, String> data, String classString) {
        this.classString = classString;
        this.data = data;
    }
}